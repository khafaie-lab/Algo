package com.example.quant

import com.example.Candle
import com.example.ScannerOpportunity
import com.example.StrategyParams
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Pure Kotlin Quantitative Multi-Timeframe + Smart Money Concepts (SMC) + Historical Backtesting Engine.
 *
 * Architectural Blueprint from verified open-source GitHub quant frameworks:
 * 1. `ta4j/ta4j`: Wilder RSI(14), Wilder ATR(14), Wilder ADX(14), EMA(20/50/200), non-lookahead Backtesting.
 * 2. `joshyattridge/smart-money-concepts`: Confirmed Pivot Swings, BOS / CHoCH, unmitigated FVG & Order Blocks, Liquidity Sweeps.
 * 3. `freqtrade/freqtrade-strategies` (@informative MTF): Higher-Timeframe (HTF) macro trend alignment paired with Lower-Timeframe (LTF) triggers.
 */
object QuantScannerEngine {

    data class AdxResult(
        val adx: List<Double>,
        val plusDi: List<Double>,
        val minusDi: List<Double>
    )

    data class SwingPoint(
        val index: Int,
        val confirmedAtIndex: Int,
        val price: Double,
        val isHigh: Boolean
    )

    data class HtfContext(
        val timeframe: String,
        val bias: String, // "BULLISH", "BEARISH", "NEUTRAL"
        val ema50: Double,
        val ema200: Double,
        val adx: Double,
        val lastClose: Double
    )

    data class BacktestStats(
        val totalTrades: Int,
        val wins: Int,
        val losses: Int,
        val winRatePct: Double,
        val profitFactor: Double,
        val expectancyR: Double
    )

    data class BacktestTradeRecord(
        val tradeNumber: Int,
        val direction: String,
        val entryTimestamp: Long,
        val exitTimestamp: Long,
        val entryPrice: Double,
        val stopLoss: Double,
        val takeProfit: Double,
        val exitPrice: Double,
        val confluenceScore: Int,
        val pnlR: Double,
        val pnlUsd: Double,
        val outcome: String, // "WIN", "LOSS", "TIMEOUT"
        val durationBars: Int,
        val reasons: List<String>
    )

    data class DetailedBacktestReport(
        val symbol: String,
        val timeframe: String,
        val htfTimeframe: String,
        val dataSource: String,
        val candleCount: Int,
        val startTimestamp: Long,
        val endTimestamp: Long,
        val minScoreThreshold: Int,
        val atrMultiplier: Float,
        val targetRr: Double,
        val directionFilter: String,
        val totalTrades: Int,
        val wins: Int,
        val losses: Int,
        val winRatePct: Double,
        val profitFactor: Double,
        val expectancyR: Double,
        val initialEquity: Double,
        val finalEquity: Double,
        val netReturnPct: Double,
        val maxDrawdownPct: Double,
        val equityCurve: List<Double>,
        val trades: List<BacktestTradeRecord>
    )

    data class BarSignalEvaluation(
        val direction: String, // "LONG" or "SHORT"
        val score: Int,
        val rsi: Double,
        val adx: Double,
        val atr: Double,
        val atrPct: Double,
        val volumeRatio: Double,
        val hasHtfAlignment: Boolean,
        val hasEmaCross: Boolean,
        val hasSmcZone: Boolean,
        val hasStructureBreak: Boolean,
        val structureLabel: String,
        val hasRsiDivergence: Boolean,
        val divergenceLabel: String,
        val hasLiquiditySweep: Boolean,
        val hasVolumeSpike: Boolean,
        val stopLossPrice: Double,
        val tp1Price: Double,
        val tp2Price: Double,
        val tp3Price: Double,
        val riskRewardRatio: Double,
        val passedConditions: List<String>
    )

    fun htfFor(timeframe: String): String = when (timeframe.lowercase()) {
        "1m", "3m", "5m" -> "1h"
        "15m", "1h" -> "4h"
        "4h", "1d" -> "1d"
        else -> "4h"
    }

    // -----------------------------------------------------------------
    // 1. TA4J-GRADE TECHNICAL INDICATORS
    // -----------------------------------------------------------------

    fun emaSeries(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val out = MutableList(values.size) { values[0] }
        if (period <= 1) return values.toList()
        val k = 2.0 / (period + 1.0)
        val seedLen = min(period, values.size)
        var ema = values.subList(0, seedLen).average()
        for (i in 0 until seedLen) {
            out[i] = values.subList(0, i + 1).average()
        }
        if (seedLen < values.size) {
            out[seedLen - 1] = ema
            for (i in seedLen until values.size) {
                ema = (values[i] - ema) * k + ema
                out[i] = ema
            }
        }
        return out
    }

    fun wilderRsiSeries(closes: List<Double>, period: Int = 14): List<Double> {
        val n = closes.size
        if (n == 0) return emptyList()
        val out = MutableList(n) { 50.0 }
        if (n <= period) return out

        var gainSum = 0.0
        var lossSum = 0.0
        for (i in 1..period) {
            val diff = closes[i] - closes[i - 1]
            if (diff >= 0) gainSum += diff else lossSum += -diff
        }
        var avgGain = gainSum / period
        var avgLoss = lossSum / period
        out[period] = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))

        for (i in (period + 1) until n) {
            val diff = closes[i] - closes[i - 1]
            val gain = if (diff > 0) diff else 0.0
            val loss = if (diff < 0) -diff else 0.0
            avgGain = ((avgGain * (period - 1)) + gain) / period
            avgLoss = ((avgLoss * (period - 1)) + loss) / period
            out[i] = if (avgLoss == 0.0) 100.0 else if (avgGain == 0.0) 0.0 else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
        }
        return out
    }

    fun wilderAtrSeries(candles: List<Candle>, period: Int = 14): List<Double> {
        val n = candles.size
        if (n == 0) return emptyList()
        val tr = MutableList(n) { 0.0 }
        tr[0] = (candles[0].high - candles[0].low).toDouble().coerceAtLeast(1e-9)
        for (i in 1 until n) {
            val h = candles[i].high.toDouble()
            val l = candles[i].low.toDouble()
            val prevC = candles[i - 1].close.toDouble()
            tr[i] = max(h - l, max(abs(h - prevC), abs(l - prevC))).coerceAtLeast(1e-9)
        }
        val atr = MutableList(n) { tr[0] }
        val seedLen = min(period, n)
        var running = tr.subList(0, seedLen).average()
        for (i in 0 until seedLen) atr[i] = tr.subList(0, i + 1).average()
        for (i in seedLen until n) {
            running = ((running * (period - 1)) + tr[i]) / period
            atr[i] = running
        }
        return atr
    }

    fun wilderAdxSeries(candles: List<Candle>, period: Int = 14): AdxResult {
        val n = candles.size
        if (n == 0) return AdxResult(emptyList(), emptyList(), emptyList())
        val adx = MutableList(n) { 20.0 }
        val plusDi = MutableList(n) { 20.0 }
        val minusDi = MutableList(n) { 20.0 }
        if (n <= period * 2) return AdxResult(adx, plusDi, minusDi)

        val tr = MutableList(n) { 0.0 }
        val plusDm = MutableList(n) { 0.0 }
        val minusDm = MutableList(n) { 0.0 }
        for (i in 1 until n) {
            val h = candles[i].high.toDouble()
            val l = candles[i].low.toDouble()
            val prevH = candles[i - 1].high.toDouble()
            val prevL = candles[i - 1].low.toDouble()
            val prevC = candles[i - 1].close.toDouble()

            tr[i] = max(h - l, max(abs(h - prevC), abs(l - prevC)))
            val upMove = h - prevH
            val downMove = prevL - l
            plusDm[i] = if (upMove > downMove && upMove > 0) upMove else 0.0
            minusDm[i] = if (downMove > upMove && downMove > 0) downMove else 0.0
        }

        var smTr = tr.subList(1, period + 1).sum()
        var smPlus = plusDm.subList(1, period + 1).sum()
        var smMinus = minusDm.subList(1, period + 1).sum()
        val dxList = MutableList(n) { 0.0 }

        for (i in period until n) {
            if (i > period) {
                smTr = smTr - (smTr / period) + tr[i]
                smPlus = smPlus - (smPlus / period) + plusDm[i]
                smMinus = smMinus - (smMinus / period) + minusDm[i]
            }
            val pDi = if (smTr > 0) 100.0 * (smPlus / smTr) else 0.0
            val mDi = if (smTr > 0) 100.0 * (smMinus / smTr) else 0.0
            plusDi[i] = pDi
            minusDi[i] = mDi
            val diSum = pDi + mDi
            dxList[i] = if (diSum > 0) 100.0 * (abs(pDi - mDi) / diSum) else 0.0
        }

        val adxStart = period * 2 - 1
        if (adxStart < n) {
            var smAdx = dxList.subList(period, adxStart + 1).average()
            adx[adxStart] = smAdx
            for (i in (adxStart + 1) until n) {
                smAdx = ((smAdx * (period - 1)) + dxList[i]) / period
                adx[i] = smAdx
            }
        }
        return AdxResult(adx, plusDi, minusDi)
    }

    fun volumeRatioSeries(candles: List<Candle>, period: Int = 20): List<Double> {
        val n = candles.size
        if (n == 0) return emptyList()
        val out = MutableList(n) { 1.0 }
        for (i in 0 until n) {
            val start = max(0, i - period)
            val window = candles.subList(start, i + 1)
            val avg = window.map { it.volume.toDouble() }.average().coerceAtLeast(1e-9)
            out[i] = candles[i].volume.toDouble() / avg
        }
        return out
    }

    // -----------------------------------------------------------------
    // 2. SMART MONEY CONCEPTS (ZERO-LOOKAHEAD CONFIRMED SWINGS)
    // -----------------------------------------------------------------

    /**
     * A pivot at bar `p` is strictly confirmed only at `p + rightBars`.
     * Any evaluation at bar `i` MUST filter swings by `confirmedAtIndex <= i` to prevent lookahead bias.
     */
    fun findConfirmedSwings(
        candles: List<Candle>,
        leftBars: Int = 3,
        rightBars: Int = 3
    ): List<SwingPoint> {
        val swings = mutableListOf<SwingPoint>()
        if (candles.size < leftBars + rightBars + 1) return swings
        for (p in leftBars until (candles.size - rightBars)) {
            val pHigh = candles[p].high.toDouble()
            val pLow = candles[p].low.toDouble()
            var isSwingHigh = true
            var isSwingLow = true
            for (k in (p - leftBars)..(p + rightBars)) {
                if (k == p) continue
                if (candles[k].high.toDouble() >= pHigh) isSwingHigh = false
                if (candles[k].low.toDouble() <= pLow) isSwingLow = false
            }
            if (isSwingHigh) swings.add(SwingPoint(p, p + rightBars, pHigh, isHigh = true))
            if (isSwingLow) swings.add(SwingPoint(p, p + rightBars, pLow, isHigh = false))
        }
        return swings
    }

    fun evaluateHtfContext(htfCandles: List<Candle>, htfTimeframe: String): HtfContext {
        if (htfCandles.size < 25) {
            val last = htfCandles.lastOrNull()?.close?.toDouble() ?: 0.0
            return HtfContext(htfTimeframe, "NEUTRAL", last, last, 20.0, last)
        }
        val closes = htfCandles.map { it.close.toDouble() }
        val fastPeriod = if (closes.size >= 55) 50 else 20
        val slowPeriod = if (closes.size >= 200) 200 else 50
        val emaFast = emaSeries(closes, fastPeriod).last()
        val emaSlow = emaSeries(closes, slowPeriod).last()
        val adx = wilderAdxSeries(htfCandles, 14).adx.last()
        val lastClose = closes.last()

        val bias = when {
            lastClose > emaFast && emaFast >= emaSlow -> "BULLISH"
            lastClose < emaFast && emaFast <= emaSlow -> "BEARISH"
            lastClose > emaFast -> "BULLISH"
            lastClose < emaFast -> "BEARISH"
            else -> "NEUTRAL"
        }
        return HtfContext(
            timeframe = htfTimeframe,
            bias = bias,
            ema50 = emaFast,
            ema200 = emaSlow,
            adx = adx,
            lastClose = lastClose
        )
    }

    private fun hasUnmitigatedBullishZone(
        candles: List<Candle>,
        index: Int,
        atr: Double
    ): Pair<Boolean, String> {
        val currentClose = candles[index].close.toDouble()
        val currentLow = candles[index].low.toDouble()
        val lookbackStart = max(2, index - 35)

        // Check unmitigated Bullish FVG: candles[k].low > candles[k-2].high
        for (k in (index - 1) downTo lookbackStart) {
            val gapLow = candles[k - 2].high.toDouble()
            val gapHigh = candles[k].low.toDouble()
            if (gapHigh > gapLow) {
                var mitigated = false
                for (m in (k + 1) until index) {
                    if (candles[m].close.toDouble() < gapLow) {
                        mitigated = true
                        break
                    }
                }
                if (!mitigated && currentLow <= gapHigh + 0.5 * atr && currentClose >= gapLow - 0.5 * atr) {
                    return true to "واکنش به شکاف ارزش منصفانه صعودی (Bullish FVG)"
                }
            }
        }

        // Check unmitigated Bullish Order Block before displacement candle
        for (k in (index - 2) downTo lookbackStart) {
            val c = candles[k]
            val next = candles[k + 1]
            val isBearishCandle = c.close < c.open
            val nextImpulse = (next.close - next.open).toDouble() > 1.05 * atr
            if (isBearishCandle && nextImpulse) {
                val obHigh = c.high.toDouble()
                val obLow = c.low.toDouble()
                var invalidated = false
                for (m in (k + 2) until index) {
                    if (candles[m].close.toDouble() < obLow) {
                        invalidated = true
                        break
                    }
                }
                if (!invalidated && currentLow <= obHigh + 0.4 * atr && currentClose >= obLow - 0.3 * atr) {
                    return true to "تست اردر بلاک تقاضا (Bullish Order Block)"
                }
            }
        }
        return false to ""
    }

    private fun hasUnmitigatedBearishZone(
        candles: List<Candle>,
        index: Int,
        atr: Double
    ): Pair<Boolean, String> {
        val currentClose = candles[index].close.toDouble()
        val currentHigh = candles[index].high.toDouble()
        val lookbackStart = max(2, index - 35)

        // Bearish FVG: candles[k].high < candles[k-2].low
        for (k in (index - 1) downTo lookbackStart) {
            val gapHigh = candles[k - 2].low.toDouble()
            val gapLow = candles[k].high.toDouble()
            if (gapHigh > gapLow) {
                var mitigated = false
                for (m in (k + 1) until index) {
                    if (candles[m].close.toDouble() > gapHigh) {
                        mitigated = true
                        break
                    }
                }
                if (!mitigated && currentHigh >= gapLow - 0.5 * atr && currentClose <= gapHigh + 0.5 * atr) {
                    return true to "برخورد به شکاف ارزش منصفانه نزولی (Bearish FVG)"
                }
            }
        }

        // Bearish Order Block before bearish displacement
        for (k in (index - 2) downTo lookbackStart) {
            val c = candles[k]
            val next = candles[k + 1]
            val isBullishCandle = c.close > c.open
            val nextImpulse = (next.open - next.close).toDouble() > 1.05 * atr
            if (isBullishCandle && nextImpulse) {
                val obLow = c.low.toDouble()
                val obHigh = c.high.toDouble()
                var invalidated = false
                for (m in (k + 2) until index) {
                    if (candles[m].close.toDouble() > obHigh) {
                        invalidated = true
                        break
                    }
                }
                if (!invalidated && currentHigh >= obLow - 0.4 * atr && currentClose <= obHigh + 0.3 * atr) {
                    return true to "تست اردر بلاک عرضه (Bearish Order Block)"
                }
            }
        }
        return false to ""
    }

    // -----------------------------------------------------------------
    // 3. SINGLE-BAR CONFLUENCE EVALUATION (STRICTLY ZERO-LOOKAHEAD)
    // -----------------------------------------------------------------

    fun evaluateBarConfluence(
        candles: List<Candle>,
        index: Int,
        htfContext: HtfContext,
        ema20: List<Double>,
        ema50: List<Double>,
        ema200: List<Double>,
        rsi: List<Double>,
        atr: List<Double>,
        adx: AdxResult,
        volRatio: List<Double>,
        allSwings: List<SwingPoint>,
        params: StrategyParams
    ): BarSignalEvaluation {
        val c = candles[index]
        val close = c.close.toDouble()
        val curRsi = rsi[index]
        val curAtr = atr[index].coerceAtLeast(close * 0.002)
        val atrPct = if (close > 0) (curAtr / close) * 100.0 else 0.0
        val curAdx = adx.adx[index]
        val curPlusDi = adx.plusDi[index]
        val curMinusDi = adx.minusDi[index]
        val curVol = volRatio[index]

        val visibleSwings = allSwings.filter { it.confirmedAtIndex <= index }
        val recentHighs = visibleSwings.filter { it.isHigh }.takeLast(3)
        val recentLows = visibleSwings.filter { !it.isHigh }.takeLast(3)

        // Structure & Liquidity detection within recent 12 bars
        var bullStructure = false
        var bearStructure = false
        var bullStructureLabel = ""
        var bearStructureLabel = ""
        val lastSwingHigh = recentHighs.lastOrNull()
        val lastSwingLow = recentLows.lastOrNull()

        if (lastSwingHigh != null) {
            val brokeHigh = (max(index - 10, lastSwingHigh.confirmedAtIndex)..index).any { k ->
                candles[k].close.toDouble() > lastSwingHigh.price
            }
            if (brokeHigh) {
                bullStructure = true
                bullStructureLabel = if (htfContext.bias == "BEARISH") "تغییر ساختار صعودی (Bullish CHoCH)" else "شکست ساختار صعودی (Bullish BOS)"
            }
        }
        if (lastSwingLow != null) {
            val brokeLow = (max(index - 10, lastSwingLow.confirmedAtIndex)..index).any { k ->
                candles[k].close.toDouble() < lastSwingLow.price
            }
            if (brokeLow) {
                bearStructure = true
                bearStructureLabel = if (htfContext.bias == "BULLISH") "تغییر ساختار نزولی (Bearish CHoCH)" else "شکست ساختار نزولی (Bearish BOS)"
            }
        }

        val bullSweep = lastSwingLow != null && (max(index - 3, lastSwingLow.confirmedAtIndex)..index).any { k ->
            candles[k].low.toDouble() < lastSwingLow.price && candles[k].close.toDouble() > lastSwingLow.price
        }
        val bearSweep = lastSwingHigh != null && (max(index - 3, lastSwingHigh.confirmedAtIndex)..index).any { k ->
            candles[k].high.toDouble() > lastSwingHigh.price && candles[k].close.toDouble() < lastSwingHigh.price
        }

        // Pivot RSI Regular Divergence (Zero lookahead)
        var bullDiv = false
        var bullDivLabel = ""
        if (recentLows.size >= 2) {
            val p1 = recentLows[recentLows.size - 2]
            val p2 = recentLows[recentLows.size - 1]
            if (index - p2.confirmedAtIndex <= 25 && p2.price < p1.price && rsi[p2.index] > rsi[p1.index] + 1.8 && rsi[p2.index] <= 48.0) {
                bullDiv = true
                bullDivLabel = "واگرایی مثبت RSI بین دو کف متوالی (${String.format(Locale.US, "%.1f", rsi[p1.index])} -> ${String.format(Locale.US, "%.1f", rsi[p2.index])})"
            }
        }
        var bearDiv = false
        var bearDivLabel = ""
        if (recentHighs.size >= 2) {
            val p1 = recentHighs[recentHighs.size - 2]
            val p2 = recentHighs[recentHighs.size - 1]
            if (index - p2.confirmedAtIndex <= 25 && p2.price > p1.price && rsi[p2.index] < rsi[p1.index] - 1.8 && rsi[p2.index] >= 52.0) {
                bearDiv = true
                bearDivLabel = "واگرایی منفی RSI بین دو سقف متوالی (${String.format(Locale.US, "%.1f", rsi[p1.index])} -> ${String.format(Locale.US, "%.1f", rsi[p2.index])})"
            }
        }

        val (bullZone, bullZoneLabel) = hasUnmitigatedBullishZone(candles, index, curAtr)
        val (bearZone, bearZoneLabel) = hasUnmitigatedBearishZone(candles, index, curAtr)

        // Score LONG
        var longScore = 0
        val longConds = mutableListOf<String>()
        val ltfBullEma = ema20[index] >= ema50[index] && close >= ema50[index]
        val ltfGoldenEma = ema50[index] >= ema200[index]
        val htfBull = htfContext.bias == "BULLISH"
        if (htfBull) {
            longScore += 22
            longConds.add("هم‌جهتی روند تایم‌فریم بالا (${htfContext.timeframe.uppercase()} Bullish)")
        }
        if (params.enableEmaCross && (ltfBullEma || ltfGoldenEma)) {
            longScore += if (ltfBullEma && ltfGoldenEma) 16 else 10
            longConds.add("ساختار صعودی EMA (20/50/200)")
        }
        if (curAdx >= 18.0 && curPlusDi >= curMinusDi) {
            longScore += if (curAdx >= 24.0) 15 else 10
            longConds.add("قدرت روند تاییدشده ADX=${String.format(Locale.US, "%.1f", curAdx)} (DI+ غالب)")
        }
        if (params.enableOrderBlock && (bullZone || bullSweep)) {
            longScore += if (bullZone && bullSweep) 24 else 18
            if (bullZone) longConds.add(bullZoneLabel)
            if (bullSweep) longConds.add("شکار نقدینگی زیر کف قبلی (Sell-Side Liquidity Sweep)")
        }
        if (params.enableChoch && bullStructure) {
            longScore += 14
            longConds.add(bullStructureLabel)
        }
        if (params.enableRsiDiv && bullDiv) {
            longScore += 14
            longConds.add(bullDivLabel)
        } else if (curRsi in 28.0..54.0) {
            longScore += 7
            longConds.add("ناحیه بهینه مومنتوم خرید (RSI=${String.format(Locale.US, "%.1f", curRsi)})")
        }
        if (params.enableVolumeFilter && curVol >= 1.15) {
            longScore += 10
            longConds.add("تایید جهش حجم معاملات (${String.format(Locale.US, "%.2fx", curVol)} میانگین ۲۰ کندل)")
        }

        // Score SHORT
        var shortScore = 0
        val shortConds = mutableListOf<String>()
        val ltfBearEma = ema20[index] <= ema50[index] && close <= ema50[index]
        val ltfDeathEma = ema50[index] <= ema200[index]
        val htfBear = htfContext.bias == "BEARISH"
        if (htfBear) {
            shortScore += 22
            shortConds.add("هم‌جهتی روند تایم‌فریم بالا (${htfContext.timeframe.uppercase()} Bearish)")
        }
        if (params.enableEmaCross && (ltfBearEma || ltfDeathEma)) {
            shortScore += if (ltfBearEma && ltfDeathEma) 16 else 10
            shortConds.add("ساختار نزولی EMA (20/50/200)")
        }
        if (curAdx >= 18.0 && curMinusDi >= curPlusDi) {
            shortScore += if (curAdx >= 24.0) 15 else 10
            shortConds.add("قدرت روند نزولی تاییدشده ADX=${String.format(Locale.US, "%.1f", curAdx)} (DI- غالب)")
        }
        if (params.enableOrderBlock && (bearZone || bearSweep)) {
            shortScore += if (bearZone && bearSweep) 24 else 18
            if (bearZone) shortConds.add(bearZoneLabel)
            if (bearSweep) shortConds.add("شکار نقدینگی بالای سقف قبلی (Buy-Side Liquidity Sweep)")
        }
        if (params.enableChoch && bearStructure) {
            shortScore += 14
            shortConds.add(bearStructureLabel)
        }
        if (params.enableRsiDiv && bearDiv) {
            shortScore += 14
            shortConds.add(bearDivLabel)
        } else if (curRsi in 46.0..74.0) {
            shortScore += 7
            shortConds.add("ناحیه بهینه مومنتوم فروش (RSI=${String.format(Locale.US, "%.1f", curRsi)})")
        }
        if (params.enableVolumeFilter && curVol >= 1.15) {
            shortScore += 10
            shortConds.add("تایید جهش حجم معاملات (${String.format(Locale.US, "%.2fx", curVol)} میانگین ۲۰ کندل)")
        }

        // Choose dominant direction (prefer HTF bias on close ties)
        val chooseLong = when {
            longScore > shortScore -> true
            shortScore > longScore -> false
            htfBull -> true
            htfBear -> false
            else -> curPlusDi >= curMinusDi
        }

        val dir = if (chooseLong) "LONG" else "SHORT"
        val rawScore = (if (chooseLong) longScore else shortScore).coerceIn(15, 98)
        val atrMult = params.atrMultiplier.toDouble().coerceIn(1.0, 3.0)

        // Dynamic ATR + Structural Pivot Stop-Loss & Multi-Target TP
        val (slPrice, riskDist) = if (chooseLong) {
            val atrStop = close - (atrMult * curAtr)
            val structuralLow = recentLows.lastOrNull()?.price
            val candidate = if (structuralLow != null && structuralLow < close) {
                min(atrStop, structuralLow - (0.3 * curAtr))
            } else atrStop
            val clampedDist = (close - candidate).coerceIn(0.8 * curAtr, 2.8 * curAtr)
            (close - clampedDist) to clampedDist
        } else {
            val atrStop = close + (atrMult * curAtr)
            val structuralHigh = recentHighs.lastOrNull()?.price
            val candidate = if (structuralHigh != null && structuralHigh > close) {
                max(atrStop, structuralHigh + (0.3 * curAtr))
            } else atrStop
            val clampedDist = (candidate - close).coerceIn(0.8 * curAtr, 2.8 * curAtr)
            (close + clampedDist) to clampedDist
        }

        val rr = if (rawScore >= 78) 2.4 else 2.0
        val tp1 = if (chooseLong) close + (riskDist * rr) else close - (riskDist * rr)
        val tp2 = if (chooseLong) close + (riskDist * (rr + 1.0)) else close - (riskDist * (rr + 1.0))
        val tp3 = if (chooseLong) close + (riskDist * (rr + 2.2)) else close - (riskDist * (rr + 2.2))

        val condList = if (chooseLong) longConds else shortConds
        if (condList.isEmpty()) {
            condList.add("تحلیل ساختار کندلی OHLCV و نوسان ATR=${String.format(Locale.US, "%.2f%%", atrPct)}")
        }

        val finalDivLabel = if (chooseLong) {
            if (bullDiv) bullDivLabel else "RSI(14)=${String.format(Locale.US, "%.1f", curRsi)} در تایم‌فریم اجرا"
        } else {
            if (bearDiv) bearDivLabel else "RSI(14)=${String.format(Locale.US, "%.1f", curRsi)} در تایم‌فریم اجرا"
        }
        val finalStructLabel = if (chooseLong) {
            if (bullStructure) bullStructureLabel else "روند HTF: ${htfContext.bias} (${htfContext.timeframe.uppercase()})"
        } else {
            if (bearStructure) bearStructureLabel else "روند HTF: ${htfContext.bias} (${htfContext.timeframe.uppercase()})"
        }

        return BarSignalEvaluation(
            direction = dir,
            score = rawScore,
            rsi = curRsi,
            adx = curAdx,
            atr = curAtr,
            atrPct = atrPct,
            volumeRatio = curVol,
            hasHtfAlignment = if (chooseLong) htfBull else htfBear,
            hasEmaCross = if (chooseLong) (ltfBullEma || ltfGoldenEma) else (ltfBearEma || ltfDeathEma),
            hasSmcZone = if (chooseLong) (bullZone || bullSweep) else (bearZone || bearSweep),
            hasStructureBreak = if (chooseLong) bullStructure else bearStructure,
            structureLabel = finalStructLabel,
            hasRsiDivergence = if (chooseLong) bullDiv else bearDiv,
            divergenceLabel = finalDivLabel,
            hasLiquiditySweep = if (chooseLong) bullSweep else bearSweep,
            hasVolumeSpike = curVol >= 1.15,
            stopLossPrice = slPrice,
            tp1Price = tp1,
            tp2Price = tp2,
            tp3Price = tp3,
            riskRewardRatio = rr,
            passedConditions = condList
        )
    }

    // -----------------------------------------------------------------
    // 4. NON-LOOKAHEAD HISTORICAL BACKTESTER (TA4J / FREQTRADE STYLE)
    // -----------------------------------------------------------------

    /**
     * Walks historical bars strictly sequentially (`i` only uses data <= i, and exits strictly check bars > i).
     * If a single bar touches both SL and TP, conservatively records a LOSS (pessimistic fill).
     */
    fun runHistoricalBacktest(
        candles: List<Candle>,
        htfContext: HtfContext,
        ema20: List<Double>,
        ema50: List<Double>,
        ema200: List<Double>,
        rsi: List<Double>,
        atr: List<Double>,
        adx: AdxResult,
        volRatio: List<Double>,
        allSwings: List<SwingPoint>,
        params: StrategyParams,
        minScoreThreshold: Int = 45
    ): BacktestStats {
        if (candles.size < 80) return BacktestStats(0, 0, 0, 0.0, 0.0, 0.0)

        var wins = 0
        var losses = 0
        var grossProfitR = 0.0
        var grossLossR = 0.0
        var nextAllowedEntryIndex = 50
        val lastSimIndex = candles.size - 8

        var i = 50
        while (i <= lastSimIndex) {
            if (i < nextAllowedEntryIndex) {
                i++
                continue
            }
            val eval = evaluateBarConfluence(
                candles = candles,
                index = i,
                htfContext = htfContext,
                ema20 = ema20,
                ema50 = ema50,
                ema200 = ema200,
                rsi = rsi,
                atr = atr,
                adx = adx,
                volRatio = volRatio,
                allSwings = allSwings,
                params = params
            )
            if (eval.score < minScoreThreshold) {
                i++
                continue
            }

            val isLong = eval.direction == "LONG"
            val entry = candles[i].close.toDouble()
            val sl = eval.stopLossPrice
            val tp = eval.tp1Price
            val risk = abs(entry - sl).coerceAtLeast(1e-9)

            var exitBar = min(candles.size - 1, i + 40)
            var tradeWon: Boolean? = null

            for (j in (i + 1)..min(candles.size - 1, i + 40)) {
                val barHigh = candles[j].high.toDouble()
                val barLow = candles[j].low.toDouble()
                val hitSl = if (isLong) barLow <= sl else barHigh >= sl
                val hitTp = if (isLong) barHigh >= tp else barLow <= tp

                if (hitSl && hitTp) {
                    // Conservative worst-case tie-break: stop hit first
                    tradeWon = false
                    exitBar = j
                    break
                } else if (hitSl) {
                    tradeWon = false
                    exitBar = j
                    break
                } else if (hitTp) {
                    tradeWon = true
                    exitBar = j
                    break
                }
            }

            if (tradeWon == true) {
                wins++
                grossProfitR += eval.riskRewardRatio
            } else if (tradeWon == false) {
                losses++
                grossLossR += 1.0
            } else {
                // Closed at timeout bar
                val closeExit = candles[exitBar].close.toDouble()
                val pnlR = if (isLong) (closeExit - entry) / risk else (entry - closeExit) / risk
                if (pnlR > 0.0) {
                    wins++
                    grossProfitR += pnlR
                } else {
                    losses++
                    grossLossR += abs(pnlR)
                }
            }

            nextAllowedEntryIndex = exitBar + 2
            i = nextAllowedEntryIndex
        }

        val total = wins + losses
        val winRate = if (total > 0) (wins.toDouble() / total.toDouble()) * 100.0 else 0.0
        val pf = when {
            grossLossR > 0.0 -> grossProfitR / grossLossR
            grossProfitR > 0.0 -> 9.9
            else -> 0.0
        }
        val expectancy = if (total > 0) (grossProfitR - grossLossR) / total.toDouble() else 0.0
        return BacktestStats(
            totalTrades = total,
            wins = wins,
            losses = losses,
            winRatePct = winRate,
            profitFactor = pf,
            expectancyR = expectancy
        )
    }

    // -----------------------------------------------------------------
    // 5. FULL MULTI-TIMEFRAME SCANNER ENTRY POINT
    // -----------------------------------------------------------------

    fun analyzeAssetTimeframe(
        symbol: String,
        timeframe: String,
        ltfCandles: List<Candle>,
        htfCandles: List<Candle>,
        dataSource: String,
        params: StrategyParams,
        scannedAtLabel: String = "کندل واقعی"
    ): ScannerOpportunity? {
        if (ltfCandles.size < 40) return null

        val sortedLtf = ltfCandles.sortedBy { it.timestamp }.distinctBy { it.timestamp }
        val sortedHtf = htfCandles.sortedBy { it.timestamp }.distinctBy { it.timestamp }
        val htfTf = htfFor(timeframe)
        val htfCtx = evaluateHtfContext(if (sortedHtf.size >= 25) sortedHtf else sortedLtf, htfTf)

        val closes = sortedLtf.map { it.close.toDouble() }
        val ema20 = emaSeries(closes, 20)
        val ema50 = emaSeries(closes, min(50, closes.size))
        val ema200 = emaSeries(closes, min(200, closes.size))
        val rsi = wilderRsiSeries(closes, 14)
        val atr = wilderAtrSeries(sortedLtf, 14)
        val adx = wilderAdxSeries(sortedLtf, 14)
        val volRatio = volumeRatioSeries(sortedLtf, 20)
        val allSwings = findConfirmedSwings(sortedLtf, leftBars = 3, rightBars = 3)

        val evalIdx = sortedLtf.lastIndex
        val latest = evaluateBarConfluence(
            candles = sortedLtf,
            index = evalIdx,
            htfContext = htfCtx,
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            rsi = rsi,
            atr = atr,
            adx = adx,
            volRatio = volRatio,
            allSwings = allSwings,
            params = params
        )

        val bt = runHistoricalBacktest(
            candles = sortedLtf,
            htfContext = htfCtx,
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            rsi = rsi,
            atr = atr,
            adx = adx,
            volRatio = volRatio,
            allSwings = allSwings,
            params = params
        )

        val isIrt = symbol.contains("IRT")
        fun fmt(v: Double): String = if (isIrt) {
            "${NumberFormat.getNumberInstance(Locale.US).format(v.toLong())} ت"
        } else {
            when {
                v < 0.01 -> String.format(Locale.US, "$%.6f", v)
                v < 1.0 -> String.format(Locale.US, "$%.4f", v)
                else -> String.format(Locale.US, "$%,.2f", v)
            }
        }

        val entry = sortedLtf.last().close.toDouble()
        val grade = when {
            latest.score >= 76 && (bt.totalTrades == 0 || bt.winRatePct >= 50.0) -> "A+ GRADE"
            latest.score >= 56 -> "B GRADE"
            else -> "FILTERED"
        }

        val dualConfirmed = (latest.hasHtfAlignment && latest.hasSmcZone) ||
            (latest.hasRsiDivergence && latest.hasStructureBreak)

        val bePrice = if (latest.direction == "LONG") {
            entry + abs(entry - latest.stopLossPrice) * 0.8
        } else {
            entry - abs(entry - latest.stopLossPrice) * 0.8
        }

        return ScannerOpportunity(
            id = "MTF-${symbol}-${timeframe}",
            symbol = symbol,
            timeframe = timeframe,
            direction = latest.direction,
            entryPrice = fmt(entry),
            stopLoss = fmt(latest.stopLossPrice),
            tp1 = fmt(latest.tp1Price),
            tp2 = fmt(latest.tp2Price),
            tp3 = fmt(latest.tp3Price),
            riskReward = "1:${String.format(Locale.US, "%.1f", latest.riskRewardRatio)}",
            confluenceScore = latest.score,
            grade = grade,
            passedConditions = latest.passedConditions,
            scannedAt = "$scannedAtLabel • ${sortedLtf.size} کندل",
            isDualConfirmed = dualConfirmed,
            divergenceType = latest.divergenceLabel,
            structureBreakType = latest.structureLabel,
            breakevenPrice = fmt(bePrice),
            dataSource = dataSource,
            candleCount = sortedLtf.size,
            rsiValue = latest.rsi,
            adxValue = latest.adx,
            atrPercent = latest.atrPct,
            volumeRatio = latest.volumeRatio,
            htfBias = "${htfCtx.timeframe.uppercase()} ${htfCtx.bias}",
            backtestWinRate = if (bt.totalTrades > 0) bt.winRatePct else null,
            backtestTrades = bt.totalTrades,
            backtestProfitFactor = if (bt.totalTrades > 0) bt.profitFactor else null
        )
    }

    /**
     * Interactive Backtest Lab Simulator with strict non-lookahead HTF & LTF alignment.
     * Tracks trade-by-trade history, equity curve, max drawdown %, profit factor, and expectancy R.
     */
    fun runDetailedBacktest(
        symbol: String,
        timeframe: String,
        ltfCandles: List<Candle>,
        htfCandles: List<Candle>,
        dataSource: String,
        baseParams: StrategyParams,
        minScoreThreshold: Int = 55,
        atrMultiplier: Float = 1.5f,
        targetRr: Double = 2.0,
        directionFilter: String = "ALL",
        initialEquity: Double = 10000.0,
        riskPerTradePercent: Float = 1.0f
    ): DetailedBacktestReport? {
        val sortedLtf = ltfCandles.sortedBy { it.timestamp }.distinctBy { it.timestamp }
        if (sortedLtf.size < 80) return null

        val sortedHtf = htfCandles.sortedBy { it.timestamp }.distinctBy { it.timestamp }
        val htfTf = htfFor(timeframe)
        val useHtf = if (sortedHtf.size >= 25) sortedHtf else sortedLtf

        val htfCloses = useHtf.map { it.close.toDouble() }
        val htfFastP = if (htfCloses.size >= 55) 50 else 20
        val htfSlowP = if (htfCloses.size >= 200) 200 else 50
        val htfEmaFast = emaSeries(htfCloses, htfFastP)
        val htfEmaSlow = emaSeries(htfCloses, htfSlowP)
        val htfAdx = wilderAdxSeries(useHtf, 14).adx

        val closes = sortedLtf.map { it.close.toDouble() }
        val ema20 = emaSeries(closes, 20)
        val ema50 = emaSeries(closes, min(50, closes.size))
        val ema200 = emaSeries(closes, min(200, closes.size))
        val rsi = wilderRsiSeries(closes, 14)
        val atr = wilderAtrSeries(sortedLtf, 14)
        val adx = wilderAdxSeries(sortedLtf, 14)
        val volRatio = volumeRatioSeries(sortedLtf, 20)
        val allSwings = findConfirmedSwings(sortedLtf, leftBars = 3, rightBars = 3)

        val effectiveParams = baseParams.copy(atrMultiplier = atrMultiplier)
        val trades = mutableListOf<BacktestTradeRecord>()
        val equityCurve = mutableListOf(initialEquity)

        var equity = initialEquity
        var peakEquity = initialEquity
        var maxDrawdownPct = 0.0
        var wins = 0
        var losses = 0
        var grossProfitR = 0.0
        var grossLossR = 0.0

        var htfPtr = 0
        var nextAllowedEntryIndex = 50
        val lastSimIndex = sortedLtf.size - 8
        var i = 50

        while (i <= lastSimIndex) {
            val ltfTs = sortedLtf[i].timestamp
            while (htfPtr + 1 < useHtf.size && useHtf[htfPtr + 1].timestamp <= ltfTs) {
                htfPtr++
            }
            if (i < nextAllowedEntryIndex) {
                i++
                continue
            }

            val hClose = htfCloses[htfPtr]
            val hFast = htfEmaFast[htfPtr]
            val hSlow = htfEmaSlow[htfPtr]
            val hBias = when {
                hClose > hFast && hFast >= hSlow -> "BULLISH"
                hClose < hFast && hFast <= hSlow -> "BEARISH"
                hClose > hFast -> "BULLISH"
                hClose < hFast -> "BEARISH"
                else -> "NEUTRAL"
            }
            val pointHtfCtx = HtfContext(htfTf, hBias, hFast, hSlow, htfAdx[htfPtr], hClose)

            val eval = evaluateBarConfluence(
                candles = sortedLtf,
                index = i,
                htfContext = pointHtfCtx,
                ema20 = ema20,
                ema50 = ema50,
                ema200 = ema200,
                rsi = rsi,
                atr = atr,
                adx = adx,
                volRatio = volRatio,
                allSwings = allSwings,
                params = effectiveParams
            )

            val dirAllowed = directionFilter == "ALL" || eval.direction.equals(directionFilter, ignoreCase = true)
            if (!dirAllowed || eval.score < minScoreThreshold) {
                i++
                continue
            }

            val isLong = eval.direction == "LONG"
            val entry = sortedLtf[i].close.toDouble()
            val sl = eval.stopLossPrice
            val risk = abs(entry - sl).coerceAtLeast(1e-9)
            val tp = if (isLong) entry + (risk * targetRr) else entry - (risk * targetRr)

            var exitBar = min(sortedLtf.size - 1, i + 45)
            var exitPrice = sortedLtf[exitBar].close.toDouble()
            var outcome = "TIMEOUT"

            for (j in (i + 1)..min(sortedLtf.size - 1, i + 45)) {
                val barHigh = sortedLtf[j].high.toDouble()
                val barLow = sortedLtf[j].low.toDouble()
                val hitSl = if (isLong) barLow <= sl else barHigh >= sl
                val hitTp = if (isLong) barHigh >= tp else barLow <= tp

                if (hitSl && hitTp) {
                    outcome = "LOSS"
                    exitPrice = sl
                    exitBar = j
                    break
                } else if (hitSl) {
                    outcome = "LOSS"
                    exitPrice = sl
                    exitBar = j
                    break
                } else if (hitTp) {
                    outcome = "WIN"
                    exitPrice = tp
                    exitBar = j
                    break
                }
            }

            val pnlR = when (outcome) {
                "WIN" -> targetRr
                "LOSS" -> -1.0
                else -> if (isLong) (exitPrice - entry) / risk else (entry - exitPrice) / risk
            }

            val riskDollar = equity * (riskPerTradePercent.toDouble() / 100.0)
            val pnlUsd = riskDollar * pnlR
            equity = (equity + pnlUsd).coerceAtLeast(1.0)
            equityCurve.add(equity)

            if (equity > peakEquity) peakEquity = equity
            val dd = if (peakEquity > 0.0) ((peakEquity - equity) / peakEquity) * 100.0 else 0.0
            if (dd > maxDrawdownPct) maxDrawdownPct = dd

            if (pnlR > 0.0) {
                wins++
                grossProfitR += pnlR
            } else {
                losses++
                grossLossR += abs(pnlR)
            }

            trades.add(
                BacktestTradeRecord(
                    tradeNumber = trades.size + 1,
                    direction = eval.direction,
                    entryTimestamp = sortedLtf[i].timestamp,
                    exitTimestamp = sortedLtf[exitBar].timestamp,
                    entryPrice = entry,
                    stopLoss = sl,
                    takeProfit = tp,
                    exitPrice = exitPrice,
                    confluenceScore = eval.score,
                    pnlR = pnlR,
                    pnlUsd = pnlUsd,
                    outcome = outcome,
                    durationBars = exitBar - i,
                    reasons = eval.passedConditions.take(3)
                )
            )

            nextAllowedEntryIndex = exitBar + 2
            i = nextAllowedEntryIndex
        }

        val total = wins + losses
        val winRate = if (total > 0) (wins.toDouble() / total.toDouble()) * 100.0 else 0.0
        val pf = when {
            grossLossR > 0.0 -> grossProfitR / grossLossR
            grossProfitR > 0.0 -> 9.9
            else -> 0.0
        }
        val expectancy = if (total > 0) (grossProfitR - grossLossR) / total.toDouble() else 0.0
        val netReturnPct = ((equity - initialEquity) / initialEquity) * 100.0

        return DetailedBacktestReport(
            symbol = symbol,
            timeframe = timeframe,
            htfTimeframe = htfTf,
            dataSource = dataSource,
            candleCount = sortedLtf.size,
            startTimestamp = sortedLtf.first().timestamp,
            endTimestamp = sortedLtf.last().timestamp,
            minScoreThreshold = minScoreThreshold,
            atrMultiplier = atrMultiplier,
            targetRr = targetRr,
            directionFilter = directionFilter,
            totalTrades = total,
            wins = wins,
            losses = losses,
            winRatePct = winRate,
            profitFactor = pf,
            expectancyR = expectancy,
            initialEquity = initialEquity,
            finalEquity = equity,
            netReturnPct = netReturnPct,
            maxDrawdownPct = maxDrawdownPct,
            equityCurve = equityCurve,
            trades = trades.reversed()
        )
    }
}
