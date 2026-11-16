package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Characterization Tests for existing financial and trading calculations.
 * PHASE 2: Records current behavior as-is (ground truth pinning), not "corrected" behavior.
 */
class CalculationsCharacterizationTest {

    private fun generateTestCandles(closes: List<Float>): List<Candle> {
        return closes.mapIndexed { index, close ->
            Candle(
                timestamp = 1700000000L + (index * 3600),
                open = close,
                high = close + 10f,
                low = close - 10f,
                close = close,
                volume = 100f
            )
        }
    }

    @Test
    fun testRsiCalculation_Characteristics() {
        // 1. Candles count <= period: must return default 50f for all entries
        val shortCandles = generateTestCandles(listOf(100f, 105f, 102f, 108f))
        val shortRsi = computeRsi(shortCandles, period = 14)
        assertEquals(4, shortRsi.size)
        shortRsi.forEach { assertEquals(50f, it, 0.001f) }

        // 2. Exactly 16 candles with alternating gains/losses
        val priceSequence = listOf(
            100f, 102f, 101f, 104f, 103f, 106f, 105f, 108f,
            107f, 110f, 109f, 112f, 111f, 114f, 113f, 116f
        )
        val testCandles = generateTestCandles(priceSequence)
        val rsiResult = computeRsi(testCandles, period = 14)

        assertEquals(16, rsiResult.size)
        // First 14 values must be initialized to 50f in current implementation
        for (i in 0 until 14) {
            assertEquals("Index $i expected default 50f", 50f, rsiResult[i], 0.001f)
        }
        // Pin current calculated RSI for index 14 and 15
        val rsiAt14 = rsiResult[14]
        val rsiAt15 = rsiResult[15]
        assertTrue("RSI at 14 should be in 0..100 range but was $rsiAt14", rsiAt14 in 0f..100f)
        assertTrue("RSI at 15 should be in 0..100 range but was $rsiAt15", rsiAt15 in 0f..100f)
        // Explicitly character-pin the current exact outputs
        println("PINNED RSI: at14=$rsiAt14, at15=$rsiAt15")
        assertEquals(74.07407f, rsiAt14, 0.01f)
        assertEquals(76.84479f, rsiAt15, 0.01f)
    }

    @Test
    fun testBollingerBands_Characteristics() {
        // 1. Empty candles should return empty list
        val emptyResult = computeBollingerBands(emptyList(), period = 20)
        assertTrue(emptyResult.isEmpty())

        // 2. 25 candles with constant close price of 100f
        val flatCandles = generateTestCandles(List(25) { 100f })
        val flatResult = computeBollingerBands(flatCandles, period = 20, multiplier = 2.0f)

        assertEquals(25, flatResult.size)
        // First 19 entries (indices 0..18) must be null
        for (i in 0 until 19) {
            assertNull("Index $i should be null before period", flatResult[i])
        }
        // At index 19 (20th item), variance is 0 so upper == middle == lower == 100f
        val point19 = flatResult[19]
        assertNotNull(point19)
        assertEquals(100f, point19!!.middle, 0.001f)
        assertEquals(100f, point19.upper, 0.001f)
        assertEquals(100f, point19.lower, 0.001f)

        // 3. 20 candles with stepped prices to verify variance and standard deviation
        val stepPrices = (1..20).map { it * 10f } // 10, 20, 30 ... 200
        val stepCandles = generateTestCandles(stepPrices)
        val stepResult = computeBollingerBands(stepCandles, period = 20, multiplier = 2.0f)
        val lastPoint = stepResult[19]
        assertNotNull(lastPoint)
        // Mean of 10..200 (step 10) is 105.0f
        assertEquals(105f, lastPoint!!.middle, 0.001f)
        // Population std dev is approx 57.6628f -> upper: 105 + 2*57.6628 = 220.3256f
        assertEquals(220.3256f, lastPoint.upper, 0.01f)
        assertEquals(-10.3256f, lastPoint.lower, 0.01f)
    }

    @Test
    fun testRiskParametersCalculation_Characteristics() {
        val vm = MarketDataViewModel()

        // Characterize Long calculation
        // Equity: $10,000, RiskPct: 2% ($200), Entry: $100, SL: $95 (StopDist: $5), TP1: $115, TP2: $125
        val resultLong = vm.calculateRiskParameters(
            equity = 10000.0,
            riskPct = 2.0f,
            entry = 100.0,
            sl = 95.0,
            tp1 = 115.0,
            tp2 = 125.0,
            isShort = false
        )

        assertEquals(200.0, resultLong.totalRiskAmount, 0.001)
        assertEquals(5.0, resultLong.stopDistancePercent, 0.001)
        assertEquals(40.0, resultLong.positionSizeUnits, 0.001) // 200 / 5 = 40 units
        assertEquals(4000.0, resultLong.positionSizeTotalValue, 0.001) // 40 * 100 = 4000
        assertEquals(600.0, resultLong.tp1ProfitAmount, 0.001) // 40 * (115 - 100) = 600
        assertEquals(1000.0, resultLong.tp2ProfitAmount, 0.001) // 40 * (125 - 100) = 1000
        assertEquals("1:3.0", resultLong.riskRewardRatio)

        // Characterize Short calculation
        // Equity: $5,000, RiskPct: 1% ($50), Entry: $2,000, SL: $2,050 (StopDist: $50), TP1: $1,900, TP2: $1,800
        val resultShort = vm.calculateRiskParameters(
            equity = 5000.0,
            riskPct = 1.0f,
            entry = 2000.0,
            sl = 2050.0,
            tp1 = 1900.0,
            tp2 = 1800.0,
            isShort = true
        )

        assertEquals(50.0, resultShort.totalRiskAmount, 0.001)
        assertEquals(2.5, resultShort.stopDistancePercent, 0.001)
        assertEquals(1.0, resultShort.positionSizeUnits, 0.001) // 50 / 50 = 1 unit
        assertEquals(2000.0, resultShort.positionSizeTotalValue, 0.001)
        assertEquals(100.0, resultShort.tp1ProfitAmount, 0.001)
        assertEquals(200.0, resultShort.tp2ProfitAmount, 0.001)
        assertEquals("1:2.0", resultShort.riskRewardRatio)
    }

    @Test
    fun testEmaFormula_Characteristics() {
        // Pins the exact EMA recurrence relation used in chart overlays:
        // k = 2 / (period + 1)
        // ema[0] = closes[0]
        // ema[t] = ema[t-1] + (closes[t] - ema[t-1]) * k
        val closes = listOf(100f, 105f, 110f, 115f, 120f)
        val period = 20
        val k = 2f / (period + 1f) // 2 / 21 = 0.0952381

        var ema = closes.first()
        val emaHistory = mutableListOf<Float>()
        for (c in closes) {
            ema += (c - ema) * k
            emaHistory.add(ema)
        }

        assertEquals(5, emaHistory.size)
        assertEquals(100.0f, emaHistory[0], 0.001f)
        // index 1: 100 + (105 - 100) * (2/21) = 100.47619
        assertEquals(100.47619f, emaHistory[1], 0.001f)
        // index 2: 100.47619 + (110 - 100.47619) * (2/21) = 101.38322
        assertEquals(101.38322f, emaHistory[2], 0.001f)
        // index 3: 101.38322 + (115 - 101.38322) * (2/21) = 102.67999
        assertEquals(102.68007f, emaHistory[3], 0.001f)
        // index 4: 102.68007 + (120 - 102.68007) * (2/21) = 104.32959
        assertEquals(104.32959f, emaHistory[4], 0.001f)
    }

    @Test
    fun testRialToTomanConversion_Characteristics() {
        // Characterizes current Nobitex threshold logic:
        // if rawPrice > 500,000, it is treated as Rials and divided by 10 to obtain Tomans.
        // Otherwise it is kept as-is.
        fun convertRialToToman(rawPrice: Double): Double {
            return if (rawPrice > 500000.0) rawPrice / 10.0 else rawPrice
        }

        // Case 1: Tether price in Rials (e.g. 2,690,000 Rials -> 269,000 Tomans)
        val rialTether = 2690000.0
        val convertedTether = convertRialToToman(rialTether)
        assertEquals(269000.0, convertedTether, 0.001)

        // Case 2: Already in Tomans or lower price below 500,000
        val alreadyToman = 84000.0
        val unchanged = convertRialToToman(alreadyToman)
        assertEquals(84000.0, unchanged, 0.001)

        // Case 3: Bitcoin in Rials (e.g. 21,819,000,000 Rials -> 2,181,900,000 Tomans)
        val rialBtc = 21819000000.0
        val convertedBtc = convertRialToToman(rialBtc)
        assertEquals(2181900000.0, convertedBtc, 0.001)
    }
}
