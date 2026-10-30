package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testOrderExecutionRequest_fromScannerOpportunity() {
    val opp = ScannerOpportunity(
      id = "SCAN-01",
      symbol = "BTC",
      timeframe = "5m",
      direction = "SHORT",
      entryPrice = "$81,438.50",
      stopLoss = "$82,450.00",
      tp1 = "$80,200.00",
      tp2 = "$79,150.00",
      tp3 = "$77,800.00",
      riskReward = "1:2.9",
      confluenceScore = 94,
      grade = "A+ GRADE",
      isDualConfirmed = true,
      divergenceType = "واگرایی منفی RSI",
      structureBreakType = "ChoCH در ۵ دقیقه",
      breakevenPrice = "$81,438.50",
      passedConditions = listOf("Cond 1", "Cond 2"),
      scannedAt = "هم‌اکنون"
    )

    val req = OrderExecutionRequest(
      symbol = opp.symbol,
      direction = opp.direction,
      entryPrice = opp.entryPrice,
      stopLoss = opp.stopLoss,
      tp1 = opp.tp1,
      tp2 = opp.tp2,
      tp3 = opp.tp3,
      riskReward = opp.riskReward,
      grade = opp.grade,
      defaultLot = 0.10,
      source = "اسکنر کانفلوئنس هوشمند"
    )

    assertEquals("BTC", req.symbol)
    assertEquals("SHORT", req.direction)
    assertEquals("$81,438.50", req.entryPrice)
    assertEquals("$82,450.00", req.stopLoss)
    assertEquals("$80,200.00", req.tp1)
    assertEquals("1:2.9", req.riskReward)
    assertEquals("A+ GRADE", req.grade)
    assertEquals(0.10, req.defaultLot, 0.001)
  }

  @Test
  fun testMetaTraderSymbolTranslation() {
    fun getMtSymbol(raw: String): String {
      val cleanSym = raw.replace("/", "").replace(" ", "").uppercase()
      return when {
        cleanSym.startsWith("BTC") -> "BTCUSD"
        cleanSym.startsWith("ETH") -> "ETHUSD"
        cleanSym.startsWith("SOL") -> "SOLUSD"
        cleanSym.startsWith("TON") -> "TONUSD"
        cleanSym.startsWith("DOGE") -> "DOGEUSD"
        cleanSym.contains("IRT") -> "USDTIRT"
        else -> "${cleanSym}USD"
      }
    }

    assertEquals("BTCUSD", getMtSymbol("BTC"))
    assertEquals("BTCUSD", getMtSymbol("BTC/USD"))
    assertEquals("ETHUSD", getMtSymbol("ETH"))
    assertEquals("SOLUSD", getMtSymbol("SOL"))
    assertEquals("TONUSD", getMtSymbol("TON"))
    assertEquals("DOGEUSD", getMtSymbol("DOGE"))
    assertEquals("USDTIRT", getMtSymbol("USDT/IRT"))
  }

  @Test
  fun testViewModel_OrderConfirmationFlow() {
    val vm = MarketDataViewModel()
    assertNull(vm.pendingOrderRequest.value)

    val sampleReq = OrderExecutionRequest(
      symbol = "ETH",
      direction = "LONG",
      entryPrice = "$2,398.80",
      stopLoss = "$2,350.00",
      tp1 = "$2,480.00",
      tp2 = "$2,540.00",
      tp3 = "$2,620.00",
      riskReward = "1:2.7",
      grade = "A+ GRADE",
      defaultLot = 0.05,
      source = "مشاور پرایس‌اکشن"
    )

    vm.openOrderConfirmation(sampleReq)
    assertEquals(sampleReq, vm.pendingOrderRequest.value)

    // Execute trade with MetaTrader 5 and 0.05 lot
    val trade = vm.executeTrade(
      symbol = sampleReq.symbol,
      direction = sampleReq.direction,
      entryPrice = sampleReq.entryPrice,
      customSl = sampleReq.stopLoss,
      customTp = sampleReq.tp1,
      customRiskReward = sampleReq.riskReward,
      lotSize = 0.05,
      platform = "MetaTrader 5"
    )

    assertNotNull(trade)
    assertEquals("ETH/USD", trade.symbol)
    assertEquals("LONG", trade.direction)
    assertEquals("$2,398.80", trade.entryPrice)
    assertEquals("$2,350.00", trade.stopLoss)
    assertEquals("$2,480.00", trade.targetPrice)
    assertEquals("1:2.7", trade.riskReward)
    assertTrue(trade.positionSize.contains("0.05 Lot (MetaTrader 5)"))

    vm.closeOrderConfirmation()
    assertNull(vm.pendingOrderRequest.value)
  }
}

