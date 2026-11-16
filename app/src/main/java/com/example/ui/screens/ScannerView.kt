package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FrostedGlassCard
import com.example.ScannerOpportunity
import com.example.ui.theme.*

@Composable
fun ScannerView(
    opportunities: List<ScannerOpportunity>,
    isScanning: Boolean,
    selectedTimeframe: String,
    selectedDirection: String = "ALL",
    auditStatus: String = "",
    isFa: Boolean,
    onSelectTimeframe: (String) -> Unit,
    onSelectDirection: (String) -> Unit = {},
    onRescan: () -> Unit,
    onOpenRiskCalculator: (ScannerOpportunity) -> Unit = {},
    onSendOrder: (ScannerOpportunity) -> Unit = {},
    onOpenBacktestLab: (symbol: String, timeframe: String) -> Unit = { _, _ -> },
    onSelectOpportunity: (ScannerOpportunity) -> Unit
) {
    val filteredList = remember(opportunities, selectedTimeframe, selectedDirection) {
        opportunities.filter { opp ->
            val tfMatch = (selectedTimeframe == "ALL" || opp.timeframe.equals(selectedTimeframe, ignoreCase = true))
            val dirMatch = (selectedDirection == "ALL" || opp.direction.equals(selectedDirection, ignoreCase = true))
            tfMatch && dirMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Scanner Hero Banner with Live OHLCV + Backtest Status
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = AppAccent
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(AppAccent.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, AppAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Radar, contentDescription = null, tint = AppAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                if (isFa) "اسکنر کوانتوم چندتایم‌فریمی (MTF + SMC)" else "Quant MTF + SMC Scanner",
                                color = AppText,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MinimalPersianFontFamily
                            )
                            Text(
                                if (isFa) "الگوبرداری از ta4j، SmartMoneyConcepts و Freqtrade" else "Powered by OHLCV Indicators, SMC & Non-Lookahead Backtest",
                                color = AppTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = MinimalPersianFontFamily
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val firstOpp = filteredList.firstOrNull() ?: opportunities.firstOrNull()
                                onOpenBacktestLab(firstOpp?.symbol ?: "BTC", firstOpp?.timeframe ?: "1h")
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Amber400.copy(alpha = 0.65f)),
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_backtest_lab_button")
                        ) {
                            Icon(Icons.Filled.Science, contentDescription = null, tint = Amber400, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isFa) "آزمایشگاه بک‌تست" else "Backtest Lab",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber400,
                                fontFamily = MinimalPersianFontFamily
                            )
                        }

                        Button(
                            onClick = onRescan,
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("rescan_scanner_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFa) "اسکن..." else "Scanning...", fontSize = 10.sp, color = Color.White, fontFamily = MinimalPersianFontFamily)
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFa) "اسکن مجدد" else "Rescan", fontSize = 10.sp, color = Color.White, fontFamily = MinimalPersianFontFamily)
                            }
                        }
                    }
                }

                if (auditStatus.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppSurfaceVariant)
                            .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (opportunities.isNotEmpty()) AppBullish else Amber400)
                        )
                        Text(
                            text = auditStatus,
                            color = AppTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = MinimalPersianFontFamily
                        )
                    }
                }
            }
        }

        // 2. GitHub Pattern Confluence Weight Breakdown Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Indigo400
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (isFa) "وزن‌دهی الگوریتم همگرایی و بک‌تست واقعی (Zero-Lookahead)" else "Quant Confluence & Backtest Weights",
                    color = AppText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MinimalPersianFontFamily
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        (if (isFa) "روند تایم‌فریم بالا (HTF)" else "HTF Trend") to "22%",
                        (if (isFa) "اردر بلاک / FVG / نقدینگی" else "OB / FVG / Sweep") to "24%",
                        "EMA 20/50/200" to "16%",
                        "ADX + DI" to "15%",
                        "BOS / CHoCH" to "14%",
                        (if (isFa) "واگرایی پیوت RSI" else "RSI Pivot Div") to "14%",
                        (if (isFa) "جهش حجم" else "Vol Spike") to "10%"
                    ).forEach { (name, weight) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppSurfaceVariant)
                                .border(1.dp, AppBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(name, color = AppText, fontSize = 9.sp, fontFamily = MinimalPersianFontFamily)
                                Text(weight, color = Amber400, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // 3. Direction Filter Selector (ALL, LONG, SHORT)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isFa) "جهت معامله:" else "Direction:",
                color = AppTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MinimalPersianFontFamily
            )

            listOf(
                "ALL" to (if (isFa) "همه (All)" else "All"),
                "LONG" to (if (isFa) "خرید (Long)" else "Long"),
                "SHORT" to (if (isFa) "فروش (Short)" else "Short")
            ).forEach { (dir, label) ->
                val isSel = dir == selectedDirection
                val chipBg = when {
                    !isSel -> AppSurfaceVariant
                    dir == "LONG" -> AppBullish.copy(alpha = 0.22f)
                    dir == "SHORT" -> AppBearish.copy(alpha = 0.22f)
                    else -> AppAccent.copy(alpha = 0.22f)
                }
                val chipBorder = when {
                    !isSel -> AppBorder
                    dir == "LONG" -> AppBullish
                    dir == "SHORT" -> AppBearish
                    else -> AppAccent
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectDirection(dir) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        label,
                        color = if (isSel) AppText else AppTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = MinimalPersianFontFamily
                    )
                }
            }
        }

        // 4. Timeframe Filter Selector (ALL, 1m, 3m, 5m, 15m, 1h, 4h)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to if (isFa) "همه تایم‌فریم‌ها" else "ALL",
                "1m" to if (isFa) "۱ دقیقه" else "1m",
                "3m" to if (isFa) "۳ دقیقه" else "3m",
                "5m" to if (isFa) "۵ دقیقه" else "5m",
                "15m" to if (isFa) "۱۵ دقیقه" else "15m",
                "1h" to if (isFa) "۱ ساعته" else "1h",
                "4h" to if (isFa) "۴ ساعته" else "4h"
            ).forEach { (tf, label) ->
                val isSel = tf == selectedTimeframe
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) AppAccent.copy(alpha = 0.22f) else AppSurfaceVariant)
                        .border(1.dp, if (isSel) AppAccent else AppBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectTimeframe(tf) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        color = if (isSel) AppText else AppTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = MinimalPersianFontFamily
                    )
                }
            }
        }

        // 5. Opportunities List
        if (isScanning && filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(color = AppAccent, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    Text(
                        if (isFa) "در حال واکشی کندل‌های واقعی و محاسبه Win Rate..." else "Fetching real OHLCV bars & running historical backtest...",
                        color = AppTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = MinimalPersianFontFamily
                    )
                }
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isFa) "در این فیلتر سیگنال تاییدشده‌ای از کندل‌های زنده یافت نشد." else "No verified OHLCV setups found for this filter.",
                    color = AppTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = MinimalPersianFontFamily
                )
            }
        } else {
            filteredList.forEachIndexed { index, opp ->
                val isA = opp.confluenceScore >= 76
                val accentColor = if (isA) AppBullish else Amber400
                val isLong = opp.direction.equals("LONG", ignoreCase = true)
                val dirBg = if (isLong) AppBullish.copy(alpha = 0.16f) else AppBearish.copy(alpha = 0.16f)
                val dirBorder = if (isLong) AppBullish.copy(alpha = 0.55f) else AppBearish.copy(alpha = 0.55f)
                val dirColor = if (isLong) AppBullish else AppBearish
                val dirText = if (isFa) (if (isLong) "خرید (LONG)" else "فروش (SHORT)") else opp.direction
                val pairTitle = if (opp.symbol.contains("IRT")) "USDT/IRT" else "${opp.symbol}/USDT"

                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = accentColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(accentColor.copy(alpha = 0.18f), CircleShape)
                                        .border(1.dp, accentColor.copy(alpha = 0.55f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "#${index + 1}",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    pairTitle,
                                    color = AppText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AppAccent.copy(alpha = 0.16f))
                                        .border(1.dp, AppAccent.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        opp.timeframe.uppercase(),
                                        color = AppAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(dirBg)
                                        .border(1.dp, dirBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        dirText,
                                        color = dirColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    opp.grade,
                                    color = accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    "${opp.confluenceScore}/100",
                                    color = AppTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Real Backtest Win Rate + OHLCV Indicator Telemetry Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppSurfaceVariant)
                                .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val winRateText = if (opp.backtestWinRate != null) {
                                "${String.format(java.util.Locale.US, "%.1f%%", opp.backtestWinRate)} (${opp.backtestTrades})"
                            } else {
                                if (isFa) "داده کم" else "N/A"
                            }
                            val pfText = opp.backtestProfitFactor?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "--"

                            AuditStatChip(
                                label = if (isFa) "Win Rate بک‌تست" else "Backtest WR",
                                value = winRateText,
                                valueColor = if ((opp.backtestWinRate ?: 0.0) >= 50.0) AppBullish else Amber400
                            )
                            AuditStatChip(
                                label = "PF",
                                value = pfText,
                                valueColor = if ((opp.backtestProfitFactor ?: 0.0) >= 1.2) AppBullish else AppText
                            )
                            AuditStatChip(
                                label = "RSI(14)",
                                value = String.format(java.util.Locale.US, "%.1f", opp.rsiValue),
                                valueColor = AppText
                            )
                            AuditStatChip(
                                label = "ADX(14)",
                                value = String.format(java.util.Locale.US, "%.1f", opp.adxValue),
                                valueColor = if (opp.adxValue >= 20.0) AppBullish else AppTextSecondary
                            )
                            AuditStatChip(
                                label = "ATR%",
                                value = String.format(java.util.Locale.US, "%.2f%%", opp.atrPercent),
                                valueColor = AppText
                            )
                            AuditStatChip(
                                label = "Vol",
                                value = "${String.format(java.util.Locale.US, "%.2f", opp.volumeRatio)}x",
                                valueColor = if (opp.volumeRatio >= 1.15) AppBullish else AppTextSecondary
                            )
                            AuditStatChip(
                                label = "HTF",
                                value = opp.htfBias,
                                valueColor = AppAccent
                            )
                        }

                        // Dual Confirmation Banner
                        if (opp.isDualConfirmed) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppBullish.copy(alpha = 0.12f))
                                    .border(0.8.dp, AppBullish.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    if (isFa) "همگرایی چندتایم‌فریمی: ${opp.divergenceType} • ${opp.structureBreakType}"
                                    else "MTF Confirmed: ${opp.divergenceType} • ${opp.structureBreakType}",
                                    color = AppBullish,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MinimalPersianFontFamily
                                )
                            }
                        }

                        // Dynamic ATR + Structure Targets Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppSurfaceVariant)
                                .border(1.dp, AppBorder, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isFa) "ورود" else "Entry", color = AppTextSecondary, fontSize = 9.sp, fontFamily = MinimalPersianFontFamily)
                                Text(opp.entryPrice, color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isFa) "SL (ATR)" else "Stop (ATR)", color = AppBearish, fontSize = 9.sp, fontFamily = MinimalPersianFontFamily)
                                Text(opp.stopLoss, color = AppBearish, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TP1 (50%)", color = AppBullish, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(opp.tp1, color = AppBullish, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TP2 (30%)", color = AppBullish, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(opp.tp2, color = AppBullish, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("R:R", color = Amber400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(opp.riskReward, color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Passed real conditions checklist
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            opp.passedConditions.forEach { cond ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppBullish, modifier = Modifier.size(12.dp))
                                    Text(cond, color = AppText, fontSize = 10.sp, fontFamily = MinimalPersianFontFamily)
                                }
                            }
                        }

                        // Footer: Real OHLCV Source + Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${opp.dataSource} • ${opp.scannedAt}",
                                color = AppTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppAccent.copy(alpha = 0.14f))
                                        .border(1.dp, AppAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onOpenBacktestLab(opp.symbol, opp.timeframe) }
                                        .padding(horizontal = 7.dp, vertical = 7.dp)
                                        .testTag("scanner_backtest_${opp.id}")
                                ) {
                                    Text(
                                        if (isFa) "بک‌تست" else "Backtest",
                                        color = AppAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Amber500.copy(alpha = 0.16f))
                                        .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onOpenRiskCalculator(opp) }
                                        .padding(horizontal = 7.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        if (isFa) "ریسک" else "Risk",
                                        color = Amber400,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }

                                Button(
                                    onClick = { onSelectOpportunity(opp) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("scanner_open_chart_${opp.id}")
                                ) {
                                    Icon(Icons.Filled.CandlestickChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (isFa) "چارت" else "Chart",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }

                                Button(
                                    onClick = { onSendOrder(opp) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLong) AppBullish else AppBearish
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("scanner_send_order_${opp.id}")
                                ) {
                                    Text(
                                        if (isFa) "ثبت آزمایشی" else "Paper Order",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun AuditStatChip(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = label,
            color = AppTextSecondary,
            fontSize = 8.5.sp,
            fontFamily = MinimalPersianFontFamily
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
