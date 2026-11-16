package com.example

import com.example.quant.QuantScannerEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class QuantScannerEngineTest {

    private fun buildTrendCandles(
        count: Int,
        startPrice: Double,
        stepPerBar: Double,
        spreadRatio: Double = 0.01,
        pullbackEnd: Boolean = false
    ): List<Candle> {
        val list = mutableListOf<Candle>()
        var price = startPrice
        val baseTs = 1_700_000_000_000L
        for (i in 0 until count) {
            val wave = when {
                pullbackEnd && i >= count - 4 -> -stepPerBar * 0.6
                i % 7 == 0 -> -stepPerBar * 0.5
                else -> stepPerBar
            }
            val open = price.toFloat()
            val close = (price + wave).coerceAtLeast(1.0).toFloat()
            val bodyHigh = maxOf(open, close)
            val bodyLow = minOf(open, close)
            val wick = (bodyHigh * spreadRatio).toFloat()
            val high = bodyHigh + wick
            val low = (bodyLow - wick).coerceAtLeast(0.5f)
            val vol = if (i == count - 1) 4500f else 1800f + (i % 5) * 150f
            list.add(
                Candle(
                    timestamp = baseTs + i * 3_600_000L,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = vol
                )
            )
            price = close.toDouble()
        }
        return list
    }

    @Test
    fun `wilder RSI rises on uptrend and drops on downtrend`() {
        val bullCloses = (1..40).map { 100.0 + it * 2.0 }
        val bearCloses = (1..40).map { 200.0 - it * 2.0 }

        val bullRsi = QuantScannerEngine.wilderRsiSeries(bullCloses, 14).last()
        val bearRsi = QuantScannerEngine.wilderRsiSeries(bearCloses, 14).last()

        assertTrue("Bullish RSI ($bullRsi) should be > 70", bullRsi > 70.0)
        assertTrue("Bearish RSI ($bearRsi) should be < 30", bearRsi < 30.0)
    }

    @Test
    fun `confirmed swing pivots strictly enforce zero lookahead confirmation offset`() {
        val baseTs = 1_700_000_000_000L
        val highs = listOf(10f, 11f, 12f, 15f, 13f, 12f, 11f, 10f, 9f, 10f)
        val candles = highs.mapIndexed { idx, h ->
            Candle(baseTs + idx * 60_000L, h - 1f, h, h - 2f, h - 0.5f, 100f)
        }
        val swings = QuantScannerEngine.findConfirmedSwings(candles, leftBars = 3, rightBars = 3)
        val peak = swings.firstOrNull { it.isHigh && it.index == 3 }
        assertNotNull("Index 3 with high 15 should be detected as swing high", peak)
        assertEquals("Confirmation must happen strictly at index + rightBars (6)", 6, peak!!.confirmedAtIndex)
    }

    @Test
    fun `analyzeAssetTimeframe classifies bullish OHLCV as LONG and bearish OHLCV as SHORT`() {
        val params = StrategyParams()
        val bullLtf = buildTrendCandles(160, startPrice = 50000.0, stepPerBar = 180.0, spreadRatio = 0.008)
        val bullHtf = buildTrendCandles(120, startPrice = 42000.0, stepPerBar = 250.0, spreadRatio = 0.012)

        val bullOpp = QuantScannerEngine.analyzeAssetTimeframe(
            symbol = "BTC",
            timeframe = "1h",
            ltfCandles = bullLtf,
            htfCandles = bullHtf,
            dataSource = "UnitTest OHLCV",
            params = params
        )
        assertNotNull(bullOpp)
        assertEquals("LONG", bullOpp!!.direction)
        assertEquals(160, bullOpp.candleCount)
        assertTrue("Expected 4H BULLISH HTF bias, got ${bullOpp.htfBias}", bullOpp.htfBias.contains("BULLISH"))

        val bearLtf = buildTrendCandles(160, startPrice = 90000.0, stepPerBar = -180.0, spreadRatio = 0.008)
        val bearHtf = buildTrendCandles(120, startPrice = 98000.0, stepPerBar = -250.0, spreadRatio = 0.012)

        val bearOpp = QuantScannerEngine.analyzeAssetTimeframe(
            symbol = "BTC",
            timeframe = "1h",
            ltfCandles = bearLtf,
            htfCandles = bearHtf,
            dataSource = "UnitTest OHLCV",
            params = params
        )
        assertNotNull(bearOpp)
        assertEquals("SHORT", bearOpp!!.direction)
        assertTrue("Expected 4H BEARISH HTF bias, got ${bearOpp.htfBias}", bearOpp.htfBias.contains("BEARISH"))
    }

    @Test
    fun `stop loss distance scales dynamically with ATR rather than hardcoded percentage`() {
        val params = StrategyParams(atrMultiplier = 1.5f)
        val lowVolLtf = buildTrendCandles(150, startPrice = 100.0, stepPerBar = 0.25, spreadRatio = 0.003)
        val highVolLtf = buildTrendCandles(150, startPrice = 100.0, stepPerBar = 0.25, spreadRatio = 0.045)
        val htf = buildTrendCandles(100, startPrice = 90.0, stepPerBar = 0.5, spreadRatio = 0.01)

        val lowOpp = QuantScannerEngine.analyzeAssetTimeframe("SOL", "1h", lowVolLtf, htf, "Test", params)!!
        val highOpp = QuantScannerEngine.analyzeAssetTimeframe("SOL", "1h", highVolLtf, htf, "Test", params)!!

        assertTrue("High-volatility ATR% (${highOpp.atrPercent}) must exceed low-volatility ATR% (${lowOpp.atrPercent})", highOpp.atrPercent > lowOpp.atrPercent * 3.0)

        fun parseUsd(s: String) = s.replace("$", "").replace(",", "").toDouble()
        val lowRiskPct = abs(parseUsd(lowOpp.entryPrice) - parseUsd(lowOpp.stopLoss)) / parseUsd(lowOpp.entryPrice)
        val highRiskPct = abs(parseUsd(highOpp.entryPrice) - parseUsd(highOpp.stopLoss)) / parseUsd(highOpp.entryPrice)
        assertTrue("High-volatility SL distance ($highRiskPct) must be substantially wider than low-volatility ($lowRiskPct)", highRiskPct > lowRiskPct * 2.0)
    }

    @Test
    fun `historical backtester runs strictly non-lookahead and produces bounded metrics`() {
        val params = StrategyParams()
        val ltf = buildTrendCandles(240, startPrice = 2000.0, stepPerBar = 15.0, spreadRatio = 0.012)
        val htf = buildTrendCandles(100, startPrice = 1800.0, stepPerBar = 25.0, spreadRatio = 0.012)
        val opp = QuantScannerEngine.analyzeAssetTimeframe("ETH", "1h", ltf, htf, "Test", params)!!

        assertTrue("Backtest should execute trades on 240 bars", opp.backtestTrades > 0)
        assertNotNull(opp.backtestWinRate)
        assertTrue(opp.backtestWinRate!! in 0.0..100.0)
        assertNotNull(opp.backtestProfitFactor)
        assertTrue(opp.backtestProfitFactor!! >= 0.0)
    }

    @Test
    fun `detailed backtest returns valid equity curve, drawdown bounds, and respects direction filter`() {
        val params = StrategyParams()
        val ltf = buildTrendCandles(280, startPrice = 30000.0, stepPerBar = 140.0, spreadRatio = 0.012)
        val htf = buildTrendCandles(120, startPrice = 25000.0, stepPerBar = 220.0, spreadRatio = 0.012)

        val reportAll = QuantScannerEngine.runDetailedBacktest(
            symbol = "BTC",
            timeframe = "1h",
            ltfCandles = ltf,
            htfCandles = htf,
            dataSource = "UnitTest",
            baseParams = params,
            minScoreThreshold = 45,
            atrMultiplier = 1.5f,
            targetRr = 2.0,
            directionFilter = "ALL",
            initialEquity = 10000.0,
            riskPerTradePercent = 1.0f
        )
        assertNotNull(reportAll)
        assertTrue("Expected at least 1 simulated trade", reportAll!!.totalTrades > 0)
        assertEquals(reportAll.totalTrades + 1, reportAll.equityCurve.size)
        assertTrue(reportAll.maxDrawdownPct >= 0.0)
        assertTrue(reportAll.finalEquity > 0.0)
        assertEquals(reportAll.totalTrades, reportAll.trades.size)

        val reportLongOnly = QuantScannerEngine.runDetailedBacktest(
            symbol = "BTC",
            timeframe = "1h",
            ltfCandles = ltf,
            htfCandles = htf,
            dataSource = "UnitTest",
            baseParams = params,
            minScoreThreshold = 45,
            atrMultiplier = 1.5f,
            targetRr = 2.0,
            directionFilter = "LONG"
        )!!
        assertTrue(reportLongOnly.trades.all { it.direction == "LONG" })

        val reportStrict = QuantScannerEngine.runDetailedBacktest(
            symbol = "BTC",
            timeframe = "1h",
            ltfCandles = ltf,
            htfCandles = htf,
            dataSource = "UnitTest",
            baseParams = params,
            minScoreThreshold = 78,
            atrMultiplier = 1.5f,
            targetRr = 2.0,
            directionFilter = "ALL"
        )!!
        assertTrue("Higher threshold (78) must not produce more trades than lower threshold (45)", reportStrict.totalTrades <= reportAll.totalTrades)
    }

    @Test
    fun `author profile defaults to موسی خفائی and updates cleanly without blanking name`() {
        val vm = MarketDataViewModel()
        assertEquals("موسی خفائی", vm.authorProfile.value.name)

        vm.updateAuthorProfile(
            AuthorProfile(
                name = "موسی خفائی",
                title = "توسعه‌دهنده کوانتوم",
                contact = "mousa@example.com",
                notes = "نسخه تخصصی"
            )
        )
        assertEquals("موسی خفائی", vm.authorProfile.value.name)
        assertEquals("mousa@example.com", vm.authorProfile.value.contact)

        vm.updateAuthorProfile(AuthorProfile(name = "   "))
        assertEquals("موسی خفائی", vm.authorProfile.value.name)
    }
}
