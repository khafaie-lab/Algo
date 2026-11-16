package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Candle
import com.example.FrostedGlassCard
import com.example.ScannerOpportunity
import com.example.TradingViewLiveChartCard
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ChartView(
    selectedAsset: String,
    price: String,
    tomanPrice: String,
    change: String,
    candles: List<Candle>,
    isLoadingCandles: Boolean,
    chartType: String,
    chartTimeframe: String,
    showEma: Boolean,
    showOrderBlocks: Boolean,
    showVolume: Boolean,
    isFa: Boolean,
    chartEngineMode: String = "BINANCE_TV_CLOUD",
    watchlistSymbols: List<String> = listOf("BTC", "ETH", "SOL", "TON", "DOGE", "USDT/IRT"),
    onToggleChartEngine: (String) -> Unit = {},
    activeOpportunity: ScannerOpportunity? = null,
    isExternalFullscreen: Boolean = false,
    onSelectAsset: (String) -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onToggleChartType: () -> Unit,
    onToggleEma: () -> Unit,
    onToggleOrderBlocks: () -> Unit,
    onToggleVolume: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onCloseFullscreen: () -> Unit,
    onOpenRiskCalculator: (ScannerOpportunity?) -> Unit,
    onOpenExecuteDialog: () -> Unit,
    onSendOrder: (ScannerOpportunity) -> Unit
) {
    val assets = remember(watchlistSymbols, selectedAsset) {
        if (watchlistSymbols.contains(selectedAsset)) watchlistSymbols
        else watchlistSymbols + selectedAsset
    }
    var selectedSubTab by remember { mutableStateOf("Quote") }

    // Parse current numeric price for Today's Range slider
    val numericPrice = remember(price) {
        val clean = price.replace("$", "").replace("تومان", "").replace(",", "").trim()
        clean.toDoubleOrNull() ?: 100.0
    }
    val dayLow = remember(numericPrice) { numericPrice * 0.982 }
    val dayHigh = remember(numericPrice) { numericPrice * 1.018 }

    val isPositive = !change.contains("-")
    val isToman = selectedAsset == "USDT/IRT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // 1. Asset Horizontal Strip (Institutional Monospace)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            assets.forEach { sym ->
                val isSel = selectedAsset == sym
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) IbkrBid.copy(alpha = 0.22f) else IbkrSurface)
                        .border(1.dp, if (isSel) IbkrBid else IbkrBorder, RoundedCornerShape(6.dp))
                        .clickable { onSelectAsset(sym) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = sym,
                        color = if (isSel) IbkrTextPrimary else IbkrTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. IBKR Professional Quote Header (From Screenshot 6)
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = IbkrBid
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Top Row: Big Price + Bid/Ask Spread Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Big Price & Change %
                    Column {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = price,
                                color = IbkrTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isToman) {
                                Text("تومان", color = IbkrTextSecondary, fontSize = 11.sp, fontFamily = VazirmatnFontFamily)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = change,
                                color = if (isPositive) IbkrGreen else IbkrRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (tomanPrice.isNotEmpty() && !isToman) {
                                Text(
                                    text = "≈ $tomanPrice",
                                    color = IbkrTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Right: Ask / Bid Stack with Institutional Colors (Red Ask / Blue Bid)
                    Column(horizontalAlignment = Alignment.End) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Ask", color = IbkrAsk, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isToman) String.format(Locale.US, "%,.0f", numericPrice * 1.0005) else String.format(Locale.US, "%.2f", numericPrice * 1.0003),
                                color = IbkrAsk,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Bid", color = IbkrBid, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isToman) String.format(Locale.US, "%,.0f", numericPrice * 0.9995) else String.format(Locale.US, "%.2f", numericPrice * 0.9997),
                                color = IbkrBid,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "Spread 0.02%",
                            color = IbkrTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Middle Row: Market Status & Today's Range Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IbkrGreen)
                        )
                        Text(
                            text = if (isFa) "بازار فعال است • ۲۴/۷ کریپتو" else "Market is Open • 24/7 Crypto",
                            color = IbkrTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                    Text(
                        text = if (isFa) "محدوده نوسان امروز" else "Today's Range",
                        color = IbkrTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                // Today's Range Slider (Screenshot 6 Component)
                TodayRangeBar(
                    low = dayLow,
                    high = dayHigh,
                    current = numericPrice,
                    isToman = isToman
                )
            }
        }

        // 3. Sub-tabs (Quote, Fund Profile, Options, Book...)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(IbkrSurface)
                .border(1.dp, IbkrBorder, RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Quote", "Analysis", "Order Book").forEach { tab ->
                val isSel = selectedSubTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) IbkrBid.copy(alpha = 0.22f) else Color.Transparent)
                        .clickable { selectedSubTab = tab }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            "Quote" -> if (isFa) "قیمت و چارت" else "Quote"
                            "Analysis" -> if (isFa) "تحلیل کوانت" else "Analysis"
                            else -> if (isFa) "دفتر سفارشات" else "Order Book"
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) IbkrTextPrimary else IbkrTextSecondary,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }

        // Active Opportunity Banner (if routed from Scanner)
        activeOpportunity?.let { opp ->
            val isLong = opp.direction == "LONG"
            val badgeColor = if (isLong) IbkrGreen else IbkrRed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IbkrSurfaceVariant)
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(opp.direction, color = badgeColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = "${opp.divergenceType} (${opp.timeframe})",
                            color = IbkrTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "R:R ${opp.riskReward}",
                            color = IbkrBid,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .clickable { onSendOrder(opp) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (isFa) "ثبت سفارش" else "Order",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }
                }
            }
        }

        // 4. Dual Engine TradingView Chart Card
        TradingViewLiveChartCard(
            asset = selectedAsset,
            currentPrice = price,
            tomanPrice = tomanPrice,
            candles = candles,
            isLoading = isLoadingCandles,
            chartType = chartType,
            chartTimeframe = chartTimeframe,
            showEma = showEma,
            showOrderBlocks = showOrderBlocks,
            showVolume = showVolume,
            isFa = isFa,
            chartEngineMode = chartEngineMode,
            onToggleChartEngine = onToggleChartEngine,
            activeOpportunity = activeOpportunity,
            isExternalFullscreen = isExternalFullscreen,
            onOpenFullscreen = onOpenFullscreen,
            onCloseFullscreen = onCloseFullscreen,
            onSelectTimeframe = onSelectTimeframe,
            onToggleChartType = onToggleChartType,
            onToggleEma = onToggleEma,
            onToggleOrderBlocks = onToggleOrderBlocks,
            onToggleVolume = onToggleVolume,
            onSelectAsset = onSelectAsset,
            onOpenRiskCalculator = onOpenRiskCalculator,
            onSendOpportunityOrder = onSendOrder
        )

        // 5. Quick Institutional Trading Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onOpenRiskCalculator(activeOpportunity) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IbkrTextPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IbkrBorder))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("📐", fontSize = 11.sp)
                    Text(if (isFa) "محاسبه ریسک" else "Risk Calc", fontSize = 11.sp, fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = onOpenExecuteDialog,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("chart_execute_paper_order_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IbkrBid)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(if (isFa) "ثبت سفارش آزمایشی" else "Trade Order", fontSize = 11.5.sp, fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Today's Range Slider Component from IBKR Mobile Screenshot 6.
 * Shows day low, day high, a gradient track, and a triangle marker indicating current position.
 */
@Composable
fun TodayRangeBar(
    low: Double,
    high: Double,
    current: Double,
    isToman: Boolean
) {
    val progress = if (high > low) {
        ((current - low) / (high - low)).toFloat().coerceIn(0.05f, 0.95f)
    } else 0.5f

    fun fmt(v: Double): String {
        return if (isToman) {
            String.format(Locale.US, "%,.0f", v)
        } else {
            if (v < 1.0) String.format(Locale.US, "%.4f", v)
            else String.format(Locale.US, "%,.2f", v)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackHeight = 4.dp.toPx()
                val trackY = size.height / 2f
                val cornerRadius = 2.dp.toPx()

                // Background Track
                drawRoundRect(
                    color = Color(0xFF232A36),
                    topLeft = Offset(0f, trackY - trackHeight / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )

                // Active Progress Track (Emerald Green)
                val activeWidth = size.width * progress
                drawRoundRect(
                    color = Color(0xFF30D158),
                    topLeft = Offset(0f, trackY - trackHeight / 2f),
                    size = androidx.compose.ui.geometry.Size(activeWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )

                // Indicator Marker (Inverted white triangle)
                val markerX = size.width * progress
                val trianglePath = Path().apply {
                    moveTo(markerX - 4.dp.toPx(), 0f)
                    lineTo(markerX + 4.dp.toPx(), 0f)
                    lineTo(markerX, trackY - 2.dp.toPx())
                    close()
                }
                drawPath(path = trianglePath, color = Color.White)
            }
        }

        // Low & High Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = fmt(low),
                color = IbkrTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = fmt(high),
                color = IbkrTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
