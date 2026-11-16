package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FrostedGlassCard
import com.example.StrategyParams
import com.example.TradeRecord
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Institutional Portfolio & Statement View matching IBKR Screenshots 2, 3, and 4.
 * Features Net Liquidation Value, 8-metric institutional grid, Positions, Balances, and Trade History.
 */
@Composable
fun HistoryView(
    trades: List<TradeRecord>,
    params: StrategyParams,
    isFa: Boolean,
    onCloseTrade: (String, Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Positions") } // Positions, Transactions, Balances

    val openTrades = remember(trades) { trades.filter { it.status.startsWith("OPEN") } }
    val closedTrades = remember(trades) { trades.filter { !it.status.startsWith("OPEN") } }
    val totalRealizedPnl = closedTrades.sumOf { it.pnl }
    val totalUnrealizedPnl = openTrades.size * 32.50 // simulated live position drift

    val netLiquidationValue = params.accountBalance + totalRealizedPnl + totalUnrealizedPnl
    val buyingPower = netLiquidationValue * 3.8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // 1. IBKR Portfolio Header Card (Screenshot 4)
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = IbkrBid
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top: Net Liquidation Value & Daily P&L
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = if (isFa) "ارزش خالص نقدینگی (Net Liq)" else "Net Liquidation Value",
                            color = IbkrTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", netLiquidationValue),
                            color = IbkrTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isFa) "سود/زیان روزانه (Daily P&L)" else "Daily P&L",
                            color = IbkrTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val isPos = totalRealizedPnl >= 0
                        Text(
                            text = "${if (isPos) "+" else ""}${String.format(Locale.US, "$%,.2f", totalRealizedPnl)} (${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", if (params.accountBalance > 0) totalRealizedPnl / params.accountBalance * 100 else 0.0)}%)",
                            color = if (isPos) IbkrGreen else IbkrRed,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Divider(color = IbkrBorder, thickness = 0.8.dp)

                // 8-Metric Institutional Grid (Screenshot 4)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Row 1
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IbkrMetricCell(
                            label = "UNREALIZED P&L",
                            value = "${if (totalUnrealizedPnl >= 0) "+" else ""}${String.format(Locale.US, "$%,.2f", totalUnrealizedPnl)}",
                            color = if (totalUnrealizedPnl >= 0) IbkrGreen else IbkrRed,
                            modifier = Modifier.weight(1f)
                        )
                        IbkrMetricCell(
                            label = "MKT VAL",
                            value = String.format(Locale.US, "$%,.0f", netLiquidationValue * 0.25),
                            color = IbkrTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IbkrMetricCell(
                            label = "BUYING POWER",
                            value = String.format(Locale.US, "$%,.0f", buyingPower),
                            color = IbkrBid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IbkrMetricCell(
                            label = "REALIZED P&L",
                            value = "${if (totalRealizedPnl >= 0) "+" else ""}${String.format(Locale.US, "$%,.2f", totalRealizedPnl)}",
                            color = if (totalRealizedPnl >= 0) IbkrGreen else IbkrRed,
                            modifier = Modifier.weight(1f)
                        )
                        IbkrMetricCell(
                            label = "MAINT MARGIN",
                            value = String.format(Locale.US, "$%,.0f", params.accountBalance * 0.08),
                            color = IbkrTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IbkrMetricCell(
                            label = "CASH BALANCE",
                            value = String.format(Locale.US, "$%,.2f", params.accountBalance),
                            color = IbkrTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. IBKR Sub-Tabs (Positions, Transactions, Balances)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(IbkrSurface)
                .border(1.dp, IbkrBorder, RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Positions", "Transactions", "Balances").forEach { tab ->
                val isSel = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) IbkrBid.copy(alpha = 0.22f) else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            "Positions" -> if (isFa) "موقعیت‌های باز" else "Positions (${openTrades.size})"
                            "Transactions" -> if (isFa) "تراکنش‌ها" else "Trades (${closedTrades.size})"
                            else -> if (isFa) "تراز حساب" else "Balances"
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) IbkrTextPrimary else IbkrTextSecondary,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }

        // 3. Tab Contents
        when (selectedTab) {
            "Positions" -> {
                if (openTrades.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IbkrSurface)
                            .border(1.dp, IbkrBorder, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Inbox, contentDescription = null, tint = IbkrTextSecondary, modifier = Modifier.size(32.dp))
                            Text(
                                if (isFa) "هیچ موقعیت معاملاتی بازی وجود ندارد" else "No open positions",
                                color = IbkrTextSecondary,
                                fontSize = 11.5.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        openTrades.forEach { trade ->
                            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            val isLong = trade.direction == "LONG"
                                            val dirColor = if (isLong) IbkrGreen else IbkrRed
                                            Box(
                                                modifier = Modifier
                                                    .background(dirColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(trade.direction, color = dirColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Text(trade.symbol, color = IbkrTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }

                                        Text(
                                            text = "Position: ${trade.positionSize}",
                                            color = IbkrTextSecondary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Entry: ${trade.entryPrice}", color = IbkrTextSecondary, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                                        Text("TP: ${trade.targetPrice}", color = IbkrGreen, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                                        Text("SL: ${trade.stopLoss}", color = IbkrRed, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    // Close Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { onCloseTrade(trade.id, true) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IbkrGreen),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text(if (isFa) "بستن با سود (WIN)" else "Close (WIN)", fontSize = 10.5.sp, color = Color.White, fontFamily = VazirmatnFontFamily)
                                        }

                                        Button(
                                            onClick = { onCloseTrade(trade.id, false) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IbkrRed),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text(if (isFa) "بستن با ضرر (LOSS)" else "Close (LOSS)", fontSize = 10.5.sp, color = Color.White, fontFamily = VazirmatnFontFamily)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Transactions" -> {
                // Statements & Transactions Table (Screenshot 2)
                if (closedTrades.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IbkrSurface)
                            .border(1.dp, IbkrBorder, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isFa) "هیچ معامله بسته‌شده‌ای ثبت نشده است" else "No completed trades yet", color = IbkrTextSecondary, fontSize = 11.5.sp, fontFamily = VazirmatnFontFamily)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        closedTrades.forEach { trade ->
                            val isWin = trade.status == "WIN"
                            val pnlColor = if (isWin) IbkrGreen else IbkrRed
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IbkrSurface)
                                    .border(1.dp, IbkrBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(trade.symbol, color = IbkrTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(trade.direction, color = if (trade.direction == "LONG") IbkrGreen else IbkrRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Text(trade.timestamp, color = IbkrTextSecondary, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${if (isWin) "+" else ""}${String.format(Locale.US, "$%,.2f", trade.pnl)}",
                                            color = pnlColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(trade.status, color = pnlColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onClearHistory,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IbkrRed),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IbkrRed.copy(alpha = 0.5f)))
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = IbkrRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "پاکسازی تاریخچه معاملات" else "Clear Trade History", fontSize = 11.sp, fontFamily = VazirmatnFontFamily)
                        }
                    }
                }
            }

            "Balances" -> {
                // Balances Summary with Deposit / Withdraw Buttons (Screenshot 3)
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isFa) "تراز کل حساب (USD)" else "Total (USD)", color = IbkrTextSecondary, fontSize = 11.5.sp, fontFamily = VazirmatnFontFamily)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IbkrBidBg)
                                        .border(1.dp, IbkrBid, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isFa) "واریز" else "Deposit", color = IbkrBid, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IbkrSurface)
                                        .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isFa) "برداشت" else "Withdraw", color = IbkrTextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                                }
                            }
                        }

                        Divider(color = IbkrBorder, thickness = 0.8.dp)

                        IbkrBalanceRow(label = "Net Liquidation Value", value = String.format(Locale.US, "$%,.2f", netLiquidationValue))
                        IbkrBalanceRow(label = "Cash Balance", value = String.format(Locale.US, "$%,.2f", params.accountBalance))
                        IbkrBalanceRow(label = "Reg T Equity with Loan Value", value = String.format(Locale.US, "$%,.2f", netLiquidationValue))
                        IbkrBalanceRow(label = "Net Liquidation Uncertainty", value = "$0.00")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun IbkrMetricCell(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = IbkrTextSecondary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun IbkrBalanceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = IbkrTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = IbkrTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}
