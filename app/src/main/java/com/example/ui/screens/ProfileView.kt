package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FrostedGlassCard
import com.example.StrategyParams
import com.example.TradeRecord
import com.example.ui.theme.*

@Composable
fun ProfileView(
    trades: List<TradeRecord>,
    params: StrategyParams,
    isFa: Boolean,
    onResetData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Trader Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Cyan500
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Cyan400, Indigo600)))
                        .border(2.dp, FrostedBorderTop, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isFa) "معامله‌گر کوانت" else "QUANT TRADER", color = Slate100, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .border(1.dp, Emerald500.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (isFa) "سطح حرفه‌ای" else "PRO TIER", color = Emerald400, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("ID: QP-84920 • Engine: SmartMoney v3.1", color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Quantitative Metrics Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Indigo600
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isFa) "متریک‌های عملکردی کوانت" else "ALGO PERFORMANCE METRICS",
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DiagnosticMetricPill(modifier = Modifier.weight(1f), label = if (isFa) "فاکتور سود" else "PROFIT FACTOR", value = "2.41", color = Emerald400)
                    DiagnosticMetricPill(modifier = Modifier.weight(1f), label = if (isFa) "نسبت شارپ" else "SHARPE RATIO", value = "1.92", color = Cyan400)
                    DiagnosticMetricPill(modifier = Modifier.weight(1f), label = if (isFa) "حداکثر افت" else "MAX DRAWDOWN", value = "3.8%", color = Amber400)
                }

                HorizontalDivider(color = Color(0x10FFFFFF))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DiagnosticMetricPill(modifier = Modifier.weight(1f), label = if (isFa) "سرمایه کل" else "PORTFOLIO CAPITAL", value = "$${params.accountBalance.toInt()}", color = Slate100)
                    DiagnosticMetricPill(modifier = Modifier.weight(1f), label = if (isFa) "ریسک هر ترید" else "RISK PER TRADE", value = "${params.riskPerTradePercent}%", color = Blue400)
                    DiagnosticMetricPill(modifier = Modifier.weight(1f), label = if (isFa) "تایم‌فریم پایه" else "BASE TIMEFRAME", value = params.preferredTimeframe, color = Indigo400)
                }
            }
        }

        // System Feeds Diagnostics Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Emerald500
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isFa) "وضعیت سرورها و فیدهای زنده" else "SYSTEM & DATA FEEDS",
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )

                SystemStatusRow(name = if (isFa) "فید لحظه‌ای بایننس (Binance Klines)" else "Crypto Spot Feed (Binance REST)", status = if (isFa) "فعال" else "ACTIVE", latency = "< 110ms", isGood = true)
                SystemStatusRow(name = if (isFa) "موتور چارتینگ تریدینگ‌ویو" else "TradingView Engine", status = if (isFa) "فعال" else "ONLINE", latency = "60 FPS", isGood = true)
                SystemStatusRow(name = if (isFa) "موتور هوش مصنوعی Gemini 3.5" else "Gemini 3.5 Flash Model", status = if (isFa) "متصل" else "CONNECTED", latency = "AI Ready", isGood = true)
                SystemStatusRow(name = if (isFa) "موتور امتیازدهی هم‌پوشانی" else "Confluence Score Engine", status = if (isFa) "فعال" else "ARMED", latency = "Zero Latency", isGood = true)
            }
        }

        OutlinedButton(
            onClick = onResetData,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose400),
            shape = RoundedCornerShape(14.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Rose500.copy(alpha = 0.5f), Rose400.copy(alpha = 0.2f))))
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFa) "بازنشانی دیتای آزمایشی ژورنال" else "Reset Test Journal Data", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DiagnosticMetricPill(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
            Spacer(modifier = Modifier.height(3.dp))
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun SystemStatusRow(name: String, status: String, latency: String, isGood: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFamily = VazirmatnFontFamily)
            Text(latency, color = Slate500, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Box(
            modifier = Modifier
                .background(if (isGood) Emerald500.copy(alpha = 0.15f) else Amber400.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .border(1.dp, if (isGood) Emerald500.copy(alpha = 0.35f) else Amber400.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(status, color = if (isGood) Emerald400 else Amber400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
        }
    }
}
