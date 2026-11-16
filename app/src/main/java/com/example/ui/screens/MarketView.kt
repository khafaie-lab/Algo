package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NobitexMarketItem
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Watchlist View matching IBKR Mobile Screenshot 5.
 * Features institutional tabular headers, expandable items with Bid (blue) and Ask (red),
 * mini sparkline chart, and Today H/L metrics.
 */
@Composable
fun MarketView(
    markets: List<NobitexMarketItem>,
    selectedAsset: String,
    serverPriority: String,
    isFa: Boolean,
    onOpenAddCrypto: () -> Unit = {},
    onSelectAsset: (String) -> Unit,
    onNavigateToChart: (String) -> Unit,
    onOpenOrderBook: (String) -> Unit,
    onQuickOrder: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedListTab by remember { mutableStateOf("Favorites") } // Favorites, Crypto, Toman
    var expandedSymbol by remember { mutableStateOf<String?>("BTC") } // Default first item expanded like screenshot

    val filteredMarkets = remember(markets, searchQuery, selectedListTab) {
        markets.filter { item ->
            val matchQuery = searchQuery.isEmpty() ||
                item.symbol.contains(searchQuery, ignoreCase = true) ||
                item.nameFa.contains(searchQuery, ignoreCase = true) ||
                item.nameEn.contains(searchQuery, ignoreCase = true)
            val matchTab = when (selectedListTab) {
                "Crypto" -> !item.symbol.contains("IRT")
                "Toman" -> item.symbol.contains("IRT") || item.priceToman > 0.0
                else -> true
            }
            matchQuery && matchTab
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // 1. IBKR Watchlist Sub-Tabs (+, Favorites, Crypto, Toman)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("add_watchlist_symbol_button")
                    .clip(RoundedCornerShape(8.dp))
                    .background(IbkrBidBg)
                    .border(1.dp, IbkrBid.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable { onOpenAddCrypto() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = if (isFa) "افزودن ارز دیجیتال" else "Add Symbol",
                    tint = IbkrBid,
                    modifier = Modifier.size(18.dp)
                )
            }

            listOf(
                "Favorites" to (if (isFa) "منتخب‌ها" else "Favorites"),
                "Crypto" to (if (isFa) "ارزهای دیجیتال" else "Crypto (USDT)"),
                "Toman" to (if (isFa) "بازار تومانی" else "Toman (IRT)")
            ).forEach { (tabKey, tabLabel) ->
                val isSel = selectedListTab == tabKey
                Column(
                    modifier = Modifier
                        .clickable { selectedListTab = tabKey }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tabLabel,
                        color = if (isSel) IbkrTextPrimary else IbkrTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = VazirmatnFontFamily
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .background(if (isSel) IbkrBid else Color.Transparent)
                    )
                }
            }
        }

        // 2. Table Column Header Row (Screenshot 5: Instrument, Last, Change, Change %, Volume)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IbkrSurface)
                .border(1.dp, IbkrBorder, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFa) "نماد (Instrument)" else "Instrument",
                color = IbkrTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(2.0f)
            )
            Text(
                text = if (isFa) "آخرین" else "Last",
                color = IbkrTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            Text(
                text = if (isFa) "تغییر" else "Change",
                color = IbkrTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.2f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            Text(
                text = if (isFa) "تغییر %" else "Change %",
                color = IbkrTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.3f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        // 3. Table Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .border(1.dp, IbkrBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            filteredMarkets.forEach { item ->
                val isExpanded = expandedSymbol == item.symbol
                val isSelected = selectedAsset == item.symbol
                val isPos = item.dayChange >= 0
                val color = if (isPos) IbkrGreen else IbkrRed
                val isToman = item.symbol.contains("IRT")

                val lastFormatted = if (isToman) {
                    "${NumberFormat.getNumberInstance(Locale.US).format(item.priceToman.toLong())}"
                } else {
                    if (item.priceUsdt < 1.0) String.format(Locale.US, "%.4f", item.priceUsdt)
                    else String.format(Locale.US, "%,.2f", item.priceUsdt)
                }

                val changeFormatted = String.format(Locale.US, "%+.2f", item.dayChange * (if (isToman) item.priceToman else item.priceUsdt) * 0.01)
                val pctFormatted = String.format(Locale.US, "%+.2f%%", item.dayChange)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) IbkrBid.copy(alpha = 0.08f) else IbkrSurface)
                        .clickable {
                            onSelectAsset(item.symbol)
                            expandedSymbol = if (isExpanded) null else item.symbol
                        }
                ) {
                    // Main Compact Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Instrument + Exchange tag
                        Column(modifier = Modifier.weight(2.0f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = item.symbol,
                                    color = IbkrTextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) IbkrBid else Color.Transparent)
                                )
                            }
                            Text(
                                text = if (isToman) "NOBITEX • IRT" else "BINANCE • USDT",
                                color = IbkrTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Last Price
                        Text(
                            text = lastFormatted,
                            color = IbkrTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )

                        // Change Value
                        Text(
                            text = changeFormatted,
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1.2f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )

                        // Change Percent
                        Text(
                            text = pctFormatted,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1.3f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }

                    // Expanded Details (Screenshot 5: Bid/Ask, Mini Sparkline, Today H/L, 52Wk H/L)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IbkrSurfaceVariant)
                                .border(1.dp, IbkrBorder.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row A: IBKR Bid & Ask Boxes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Blue BID Box
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IbkrBidBg)
                                        .border(1.dp, IbkrBid.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "BID 1.25 x $lastFormatted",
                                        color = IbkrBid,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Red ASK Box
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IbkrAskBg)
                                        .border(1.dp, IbkrAsk.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$lastFormatted x 0.85 ASK",
                                        color = IbkrAsk,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Row B: Mini Sparkline & Today / 52Wk High-Low
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Mini Sparkline Chart
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IbkrCanvas)
                                        .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                                        .padding(4.dp)
                                ) {
                                    IbkrMiniSparkline(
                                        isBullish = isPos,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Metrics Columns (Today H/L, 24h Vol)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(text = "Today H/L", color = IbkrTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", (if (isToman) item.priceToman else item.priceUsdt) * 1.015)} / ${String.format(Locale.US, "%.1f", (if (isToman) item.priceToman else item.priceUsdt) * 0.985)}",
                                        color = IbkrTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    val volStr = when {
                                        item.volume24h >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", item.volume24h / 1_000_000_000.0)
                                        item.volume24h >= 1_000_000 -> String.format(Locale.US, "%.1fM", item.volume24h / 1_000_000.0)
                                        item.volume24h >= 1_000 -> String.format(Locale.US, "%.1fK", item.volume24h / 1_000.0)
                                        else -> String.format(Locale.US, "%.0f", item.volume24h)
                                    }
                                    Text(text = "24h Volume", color = IbkrTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(
                                        text = volStr,
                                        color = IbkrTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Row C: Direct Action Buttons (View Chart, Trade, Order Book)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToChart(item.symbol) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IbkrBid),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Filled.ShowChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        Text(if (isFa) "نمودار زنده" else "View Chart", fontSize = 10.5.sp, fontFamily = VazirmatnFontFamily, color = Color.White)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onQuickOrder(item.symbol) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IbkrTextPrimary),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IbkrBorder)),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(if (isFa) "سفارش سریع" else "Trade", fontSize = 10.5.sp, fontFamily = VazirmatnFontFamily)
                                }

                                OutlinedButton(
                                    onClick = { onOpenOrderBook(item.symbol) },
                                    modifier = Modifier.weight(0.9f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IbkrTextSecondary),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IbkrBorder)),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(if (isFa) "عمق بازار" else "Book", fontSize = 10.5.sp, fontFamily = VazirmatnFontFamily)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Mini Canvas Sparkline for Watchlist Rows (Screenshot 5).
 * Renders smooth price action curve with institutional gradient fill.
 */
@Composable
fun IbkrMiniSparkline(
    isBullish: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = if (isBullish) IbkrGreen else IbkrRed
    val fillColor = strokeColor.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Simulated normalized points for intraday trend
        val points = if (isBullish) {
            listOf(0.8f, 0.75f, 0.65f, 0.7f, 0.55f, 0.6f, 0.4f, 0.35f, 0.2f)
        } else {
            listOf(0.2f, 0.3f, 0.35f, 0.45f, 0.4f, 0.6f, 0.65f, 0.75f, 0.85f)
        }

        val stepX = w / (points.size - 1)
        val linePath = Path()
        val fillPath = Path()

        fillPath.moveTo(0f, h)
        linePath.moveTo(0f, points[0] * h)
        fillPath.lineTo(0f, points[0] * h)

        for (i in 1 until points.size) {
            val prevX = (i - 1) * stepX
            val prevY = points[i - 1] * h
            val curX = i * stepX
            val curY = points[i] * h

            val midX = (prevX + curX) / 2f
            linePath.cubicTo(midX, prevY, midX, curY, curX, curY)
            fillPath.cubicTo(midX, prevY, midX, curY, curX, curY)
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        drawPath(
            path = linePath,
            color = strokeColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
    }
}
