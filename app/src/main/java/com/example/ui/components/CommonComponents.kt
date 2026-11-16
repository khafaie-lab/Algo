package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.ui.screens.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Reusable Frosted Glass Container with dual-gradient refraction border
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    accentGlow: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = ThemeState.current.isDark
    val backgroundBrush = if (accentGlow != null) {
        Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    accentGlow.copy(alpha = 0.14f),
                    IbkrSurface,
                    IbkrSurfaceVariant.copy(alpha = 0.9f)
                )
            } else {
                listOf(
                    accentGlow.copy(alpha = 0.08f),
                    IbkrSurface,
                    IbkrSurface
                )
            },
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.verticalGradient(
            colors = if (isDark) {
                listOf(IbkrSurfaceVariant.copy(alpha = 0.85f), IbkrSurface)
            } else {
                listOf(IbkrSurface, IbkrSurface)
            }
        )
    }

    val rimBrush = Brush.verticalGradient(
        colors = listOf(
            FrostedBorderTop,
            FrostedBorderBottom
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(borderWidth, rimBrush, shape)
            .padding(18.dp),
        content = content
    )
}

@Composable
fun AmbientBackdropGlow() {
    val isDark = ThemeState.current.isDark
    val accent = ThemeState.current.accent
    val bullish = ThemeState.current.bullish
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val alphaTop = if (isDark) 0.12f else 0.05f
        val alphaMid = if (isDark) 0.13f else 0.04f
        val alphaBottom = if (isDark) 0.08f else 0.03f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = alphaTop), Color.Transparent),
                center = Offset(width * 0.9f, height * 0.12f),
                radius = width * 0.7f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Indigo600.copy(alpha = alphaMid), Color.Transparent),
                center = Offset(width * 0.05f, height * 0.45f),
                radius = width * 0.65f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bullish.copy(alpha = alphaBottom), Color.Transparent),
                center = Offset(width * 0.85f, height * 0.8f),
                radius = width * 0.6f
            )
        )
    }
}

// -------------------------------------------------------------
// POSITION SIZE & RISK CALCULATOR DIALOG
// -------------------------------------------------------------
@Composable
fun PositionRiskCalculatorDialog(
    opportunity: ScannerOpportunity?,
    currentAsset: String,
    currentPriceStr: String,
    viewModel: MarketDataViewModel,
    isFa: Boolean,
    onDismiss: () -> Unit
) {
    fun parsePrice(str: String): Double {
        return str.replace("$", "").replace("تومان", "").replace(",", "").trim().toDoubleOrNull() ?: 1.0
    }

    val assetSymbol = opportunity?.symbol ?: currentAsset
    val isShort = opportunity?.direction?.equals("SHORT", ignoreCase = true) == true
    val defaultEntry = opportunity?.let { parsePrice(it.entryPrice) } ?: parsePrice(currentPriceStr)
    val defaultSl = opportunity?.let { parsePrice(it.stopLoss) } ?: (if (isShort) defaultEntry * 1.015 else defaultEntry * 0.985)
    val defaultTp1 = opportunity?.let { parsePrice(it.tp1) } ?: (if (isShort) defaultEntry * 0.97 else defaultEntry * 1.03)
    val defaultTp2 = opportunity?.let { parsePrice(it.tp2) } ?: (if (isShort) defaultEntry * 0.95 else defaultEntry * 1.05)

    var equityText by remember { mutableStateOf("10000") }
    var selectedRiskPct by remember { mutableFloatStateOf(1.0f) }
    var entryText by remember { mutableStateOf(String.format(Locale.US, if (defaultEntry < 1.0) "%.4f" else "%.2f", defaultEntry)) }
    var slText by remember { mutableStateOf(String.format(Locale.US, if (defaultSl < 1.0) "%.4f" else "%.2f", defaultSl)) }
    var tp1Text by remember { mutableStateOf(String.format(Locale.US, if (defaultTp1 < 1.0) "%.4f" else "%.2f", defaultTp1)) }
    var tp2Text by remember { mutableStateOf(String.format(Locale.US, if (defaultTp2 < 1.0) "%.4f" else "%.2f", defaultTp2)) }

    val equity = equityText.toDoubleOrNull() ?: 10000.0
    val entry = entryText.toDoubleOrNull() ?: defaultEntry
    val sl = slText.toDoubleOrNull() ?: defaultSl
    val tp1 = tp1Text.toDoubleOrNull() ?: defaultTp1
    val tp2 = tp2Text.toDoubleOrNull() ?: defaultTp2

    val calcResult = remember(equity, selectedRiskPct, entry, sl, tp1, tp2, isShort) {
        viewModel.calculateRiskParameters(
            equity = equity,
            riskPct = selectedRiskPct,
            entry = entry,
            sl = sl,
            tp1 = tp1,
            tp2 = tp2,
            isShort = isShort
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD906090F))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .clickable(enabled = false) {},
                accentGlow = if (isShort) Rose500 else Cyan500
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🧮", fontSize = 16.sp)
                            Column {
                                Text(
                                    if (isFa) "ماشین‌حساب هوشمند مدیریت سرمایه و حجم" else "Smart Position Size & Risk Calculator",
                                    color = Slate50,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFontFamily
                                )
                                Text(
                                    "${assetSymbol}/USDT • ${if (isShort) "🔴 پوزیشن فروش (SHORT)" else "🟢 پوزیشن خرید (LONG)"}",
                                    color = if (isShort) Rose400 else Emerald400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Text("✕", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Equity Input & Risk % selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isFa) "کل موجودی حساب ($):" else "Account Equity ($):", color = Slate300, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                            BasicTextField(
                                value = equityText,
                                onValueChange = { equityText = it },
                                textStyle = TextStyle(color = Slate50, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                singleLine = true
                            )
                        }

                        // Risk % Chips
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(if (isFa) "درصد ریسک در معامله:" else "Risk % per Trade:", color = Slate300, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0.5f, 1.0f, 2.0f, 3.0f).forEach { r ->
                                    val isSel = selectedRiskPct == r
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Blue600.copy(alpha = 0.6f) else Color(0x18FFFFFF))
                                            .border(1.dp, if (isSel) Cyan400 else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { selectedRiskPct = r }
                                            .padding(horizontal = 5.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "$r%",
                                            color = if (isSel) Cyan300 else Slate400,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Price Level Inputs (Entry, SL, TP1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isFa) "نقطه ورود:" else "Entry:", color = Slate300, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                            BasicTextField(
                                value = entryText,
                                onValueChange = { entryText = it },
                                textStyle = TextStyle(color = Slate100, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isFa) "حد ضرر (SL):" else "Stop Loss:", color = Rose400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                            BasicTextField(
                                value = slText,
                                onValueChange = { slText = it },
                                textStyle = TextStyle(color = Rose400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Rose400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isFa) "حد سود ۱ (TP1):" else "TP1:", color = Emerald400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                            BasicTextField(
                                value = tp1Text,
                                onValueChange = { tp1Text = it },
                                textStyle = TextStyle(color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                                singleLine = true
                            )
                        }
                    }

                    // Calculation Results Grid
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0B111E))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isFa) "حداکثر ضرر مجاز:" else "Max Risk Amount:", color = Slate400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    Text(
                                        "$${String.format(Locale.US, "%,.2f", calcResult.totalRiskAmount)} (${selectedRiskPct}%)",
                                        color = Rose400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isFa) "فاصله استاپ:" else "Stop Distance:", color = Slate400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    Text(
                                        String.format(Locale.US, "%.2f%%", calcResult.stopDistancePercent),
                                        color = Amber400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Divider(color = Color(0x18FFFFFF))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isFa) "حجم مجاز معامله:" else "Position Size (Units):", color = Slate400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    Text(
                                        "${String.format(Locale.US, if (calcResult.positionSizeUnits < 1.0) "%.4f" else "%.2f", calcResult.positionSizeUnits)} $assetSymbol",
                                        color = Cyan300,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isFa) "ارزش کل موقعیت:" else "Total Position Value:", color = Slate400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    Text(
                                        "$${String.format(Locale.US, "%,.2f", calcResult.positionSizeTotalValue)}",
                                        color = Slate100,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Divider(color = Color(0x18FFFFFF))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isFa) "سود در TP1 (هدف اول):" else "Profit at TP1:", color = Slate400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    Text(
                                        "+$${String.format(Locale.US, "%,.2f", calcResult.tp1ProfitAmount)}",
                                        color = Emerald400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isFa) "نسبت ریسک به ریوارد:" else "Risk / Reward Ratio:", color = Slate400, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    Text(
                                        "R:R ${calcResult.riskRewardRatio}",
                                        color = Amber400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Breakeven Automation Rule Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Indigo600.copy(alpha = 0.2f))
                            .border(1.dp, Indigo400.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🛡️", fontSize = 12.sp)
                            Text(
                                calcResult.breakevenRule,
                                color = Indigo200,
                                fontSize = 9.5.sp,
                                fontFamily = VazirmatnFontFamily,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Action Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isFa) "تایید و بازگشت به معامله" else "Apply & Close",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }
            }
        }
    }
}


// -------------------------------------------------------------
// TOP BAR & NAVIGATION
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedTopBar(
    currentTab: String,
    selectedAsset: String,
    price: String,
    change: String,
    isLoading: Boolean,
    isFa: Boolean,
    nobitexOrderBook: NobitexOrderBook?,
    nobitexMarkets: List<NobitexMarketItem> = emptyList(),
    onOpenMenu: () -> Unit = {},
    onOpenThemeSwitcher: () -> Unit = {},
    onOpenNobitexDepth: () -> Unit,
    onToggleLanguage: () -> Unit,
    onRefresh: () -> Unit
) {
    val titleText = when (currentTab) {
        "MARKET" -> if (isFa) "دیده‌بان بازارها" else "Watchlists"
        "CHART" -> if (selectedAsset.contains("IRT")) "USDT/IRT" else "$selectedAsset/USDT"
        "SCANNER" -> if (isFa) "اسکنر هوشمند همگرایی" else "Smart Scanner"
        "JOURNAL" -> if (isFa) "پورتفوی و ژورنال" else "Portfolio"
        "MORE" -> if (isFa) "تنظیمات و ریسک" else "Settings"
        else -> "QuantPro"
    }

    val subtitleBadge = when (currentTab) {
        "MARKET" -> if (isFa) "لایو نوبیتکس" else "NOBITEX & BINANCE"
        "CHART" -> if (isFa) "تحلیل تکنیکال دوگانه" else "DUAL CHART ENGINE"
        "SCANNER" -> if (isFa) "اسمارت مانی" else "SMART MONEY"
        "JOURNAL" -> if (isFa) "تراز نهادی" else "INSTITUTIONAL"
        "MORE" -> if (isFa) "تنظیمات سیستم" else "SYSTEM RULES"
        else -> "IBKR STYLE"
    }

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = if (isFa) "منوی اصلی" else "Menu",
                    tint = IbkrTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titleText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = IbkrTextPrimary,
                        fontFamily = VazirmatnFontFamily
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(IbkrSurfaceVariant, RoundedCornerShape(4.dp))
                            .border(0.8.dp, IbkrBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = subtitleBadge,
                            fontSize = 8.5.sp,
                            color = IbkrBid,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        },
        actions = {
            // Live Nobitex Tether Toman Badge
            val liveUsdtToman = nobitexMarkets.find { it.symbol == "USDT/IRT" }?.priceToman
                ?: (if (nobitexOrderBook != null && nobitexOrderBook.symbol == "USDT/IRT") nobitexOrderBook.lastTradePrice else 268755.0)
            val tomanPriceFormatted = String.format(Locale.US, "%,.0f", liveUsdtToman)
            val usdtChange = nobitexMarkets.find { it.symbol == "USDT/IRT" }?.dayChange ?: -0.14
            val isPositive = usdtChange >= 0

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(IbkrSurface)
                    .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                    .clickable { onOpenNobitexDepth() }
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(if (isPositive) IbkrGreen else IbkrRed, CircleShape)
                    )
                    Text(
                        text = "USDT",
                        color = IbkrTextSecondary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = tomanPriceFormatted,
                        color = IbkrTextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Notification Bell with Red Dot (Matching Screenshot 4 and 5)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = IbkrTextPrimary,
                    modifier = Modifier.size(19.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(IbkrRed)
                        .align(Alignment.TopEnd)
                )
            }

            // Live Theme Switcher Quick Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_bar_theme_button")
                    .clip(RoundedCornerShape(6.dp))
                    .background(IbkrSurface)
                    .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                    .clickable { onOpenThemeSwitcher() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = if (isFa) "سوییچر قالب" else "Switch Theme",
                    tint = IbkrBid,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Language Toggle Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(IbkrSurface)
                    .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "FA" else "EN",
                    color = IbkrBid,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = IbkrCanvas,
            titleContentColor = IbkrTextPrimary
        )
    )
}


@Composable
fun FrostedOrderBookDialog(
    nobitexOrderBook: NobitexOrderBook?,
    isLoading: Boolean,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onExecuteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x30FFFFFF))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Slate200,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                FrostedOrderBookCard(
                    nobitexOrderBook = nobitexOrderBook,
                    isLoading = isLoading,
                    isFa = isFa,
                    onExecuteClick = {
                        onDismiss()
                        onExecuteClick()
                    }
                )
            }
        }
    }
}

@Composable
fun FrostedOrderExecutionDialog(
    asset: String,
    currentPrice: String,
    accountBalance: Double,
    riskPercent: Float,
    advisorSetup: AdvisorSetup?,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirmOrder: () -> Unit
) {
    val isToman = asset.contains("IRT") || currentPrice.contains("تومان")
    val riskAmount = accountBalance * (riskPercent / 100.0)
    val isLong = !advisorSetup?.type.equals("SHORT", ignoreCase = true)

    fun parseNumericPrice(raw: String?): Double? =
        raw?.replace("$", "")?.replace("تومان", "")?.replace("ت", "")?.replace(",", "")?.trim()?.toDoubleOrNull()

    val parsedEntry = parseNumericPrice(advisorSetup?.entryPrice) ?: parseNumericPrice(currentPrice)
    val parsedSl = parseNumericPrice(advisorSetup?.stopLoss)
    val estPositionSizeText = if (parsedEntry != null && parsedSl != null && parsedEntry > 0.0) {
        val stopDistPct = (kotlin.math.abs(parsedEntry - parsedSl) / parsedEntry).coerceAtLeast(0.0015)
        if (isToman) String.format(Locale.US, "%,.0f تومان", riskAmount / stopDistPct)
        else "$${String.format(Locale.US, "%,.2f", riskAmount / stopDistPct)}"
    } else {
        if (isFa) "در حال محاسبه ATR..." else "Calculating ATR..."
    }

    fun fmtMoney(v: Double): String = if (isToman) String.format(Locale.US, "%,.0f تومان", v) else "$${String.format(Locale.US, "%,.2f", v)}"

    Dialog(onDismissRequest = onDismiss) {
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            accentGlow = if (isToman) Amber400 else if (isLong) Emerald500 else Rose500
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isFa) "تاییدیه ثبت آزمایشی معامله (شبیه‌سازی)" else "PAPER ORDER CONFIRMATION",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                        Text(
                            text = when {
                                isToman -> "USDT/IRT ${if (isFa) "معامله تومانی" else "TOMAN ORDER"}"
                                isLong -> "$asset/USD ${if (isFa) "خرید کوانت (LONG)" else "QUANT LONG"}"
                                else -> "$asset/USD ${if (isFa) "فروش کوانت (SHORT)" else "QUANT SHORT"}"
                            },
                            color = Slate100,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                (if (isToman) Amber400 else if (isLong) Emerald500 else Rose500).copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                (if (isToman) Amber400 else if (isLong) Emerald500 else Rose500).copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = advisorSetup?.grade ?: "LOADING",
                            color = if (isToman) Amber400 else if (isLong) Emerald400 else Rose400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = Color(0x18FFFFFF))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrostedOrderParamRow(if (isFa) "قیمت ورود" else "Entry Spot", currentPrice, isFa)
                    advisorSetup?.let { setup ->
                        FrostedOrderParamRow(if (isFa) "تارگت ۱ (۵۰٪ + ریسک‌فری)" else "TP 1 (50% + Breakeven)", setup.tp1, isFa)
                        FrostedOrderParamRow(if (isFa) "تارگت ۲ (۳۰٪)" else "TP 2 (30%)", setup.tp2, isFa)
                        FrostedOrderParamRow(if (isFa) "تارگت ۳ (۲۰٪ رانر)" else "TP 3 (20% Runner)", setup.tp3, isFa)
                        FrostedOrderParamRow(if (isFa) "حد ضرر (ATR ساختار)" else "Stop Loss (ATR)", setup.stopLoss, isFa)
                    }
                    FrostedOrderParamRow(
                        title = if (isFa) "مقدار ریسک (${riskPercent}٪)" else "Risk Amount (${riskPercent}%)",
                        value = fmtMoney(riskAmount),
                        isFa = isFa
                    )
                    FrostedOrderParamRow(
                        title = if (isFa) "حجم پوزیشن بر اساس SL" else "Est. Position Size (SL)",
                        value = estPositionSizeText,
                        isFa = isFa
                    )
                }

                HorizontalDivider(color = Color(0x18FFFFFF))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                    ) {
                        Text(if (isFa) "انصراف" else "Cancel", fontFamily = VazirmatnFontFamily)
                    }

                    Button(
                        onClick = onConfirmOrder,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_order_submit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isToman) Amber400 else Emerald600,
                            contentColor = if (isToman) DarkCanvasBackground else Color.White
                        )
                    ) {
                        Text(if (isFa) "تایید و ثبت آزمایشی (شبیه‌سازی)" else "Confirm Paper Order", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    }
                }
            }
        }
    }
}

/**
 * Dispatches trade order parameters to MetaTrader 5 or MetaTrader 4 app via deep-link intent,
 * copies trade parameters to clipboard as a seamless fallback, and redirects to Google Play
 * if MetaTrader is not installed.
 */
fun launchMetaTraderOrder(
    context: Context,
    request: OrderExecutionRequest,
    lotSize: Double,
    preferMt5: Boolean
) {
    val cleanSym = request.symbol.replace("/", "").replace(" ", "").uppercase()
    val mtSymbol = when {
        cleanSym.startsWith("BTC") -> "BTCUSD"
        cleanSym.startsWith("ETH") -> "ETHUSD"
        cleanSym.startsWith("SOL") -> "SOLUSD"
        cleanSym.startsWith("TON") -> "TONUSD"
        cleanSym.startsWith("DOGE") -> "DOGEUSD"
        cleanSym.contains("IRT") -> "USDTIRT"
        else -> "${cleanSym}USD"
    }

    val action = if (request.direction.equals("SHORT", ignoreCase = true)) "SELL" else "BUY"
    val cleanPrice = request.entryPrice.replace("$", "").replace("تومان", "").replace(",", "").trim()
    val cleanSl = request.stopLoss.replace("$", "").replace("تومان", "").replace(",", "").trim()
    val cleanTp = request.tp1.replace("$", "").replace("تومان", "").replace(",", "").trim()
    val lotStr = String.format(Locale.US, "%.2f", lotSize)

    // 1. Copy exact trading instructions to clipboard for zero friction
    val summaryText = "$action $mtSymbol | Entry: $cleanPrice | SL: $cleanSl | TP: $cleanTp | Lot: $lotStr"
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard != null) {
            val clip = ClipData.newPlainText("MetaTrader Trade Order", summaryText)
            clipboard.setPrimaryClip(clip)
        }
    } catch (_: Exception) {}

    // 2. Launch MetaTrader via deep link scheme
    val scheme = if (preferMt5) "metatrader5" else "metatrader4"
    val packageName = if (preferMt5) "net.metaquotes.metatrader5" else "net.metaquotes.metatrader4"
    val deepLinkUri = Uri.parse(
        "$scheme://trade?symbol=$mtSymbol&action=$action&volume=$lotStr&price=$cleanPrice&sl=$cleanSl&tp=$cleanTp"
    )

    var launched = false
    try {
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage(packageName)
        }
        context.startActivity(deepLinkIntent)
        launched = true
    } catch (_: Exception) {}

    if (!launched) {
        try {
            val genericIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericIntent)
            launched = true
        } catch (_: Exception) {}
    }

    if (!launched) {
        try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                launched = true
            }
        } catch (_: Exception) {}
    }

    if (!launched) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}

/**
 * Dedicated Institutional MetaTrader Order Confirmation Dialog.
 * Allows the user to inspect the full signal setup (Symbol, Direction, Grade, Entry, SL, TP1-3, R:R),
 * choose the destination MetaTrader platform (MT5 / MT4), adjust lot volume,
 * and dispatch the order directly to MetaTrader with 1 tap.
 */
@Composable
fun MetaTraderOrderConfirmationDialog(
    request: OrderExecutionRequest,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirmAndSend: (lotSize: Double, preferMt5: Boolean) -> Unit
) {
    var lotSize by remember { mutableDoubleStateOf(request.defaultLot) }
    var preferMt5 by remember { mutableStateOf(true) }
    val isShort = request.direction.equals("SHORT", ignoreCase = true)
    val actionColor = if (isShort) Rose500 else Emerald500
    val actionText = if (isFa) (if (isShort) "فروش (SELL / SHORT)" else "خرید (BUY / LONG)") else (if (isShort) "SELL" else "BUY")

    Dialog(onDismissRequest = onDismiss) {
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(26.dp),
            accentGlow = actionColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(actionColor.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, actionColor.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isShort) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = actionColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (isFa) "تاییدیه ثبت آزمایشی معامله (شبیه‌سازی)" else "PAPER TRADE SIMULATION CONFIRMATION",
                                color = Slate300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${request.symbol} • $actionText",
                            color = Slate100,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    // Grade Badge
                    Box(
                        modifier = Modifier
                            .background(actionColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, actionColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = request.grade,
                            color = actionColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Source indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isFa) "منبع سیگنال:" else "Source:", color = Slate400, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                    Text(request.source, color = Cyan300, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                }

                HorizontalDivider(color = Color(0x18FFFFFF))

                // Price levels grid
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    FrostedOrderParamRow(if (isFa) "قیمت ورود (Entry)" else "Entry Price", request.entryPrice, isFa)
                    FrostedOrderParamRow(if (isFa) "حد ضرر (Stop Loss)" else "Stop Loss (SL)", request.stopLoss, isFa)
                    FrostedOrderParamRow(if (isFa) "تارگت ۱ (TP 1 - ۵۰٪ + ریسک‌فری)" else "Target 1 (TP1 50%)", request.tp1, isFa)
                    if (request.tp2.isNotEmpty()) {
                        FrostedOrderParamRow(if (isFa) "تارگت ۲ (TP 2 - ۳۰٪)" else "Target 2 (TP2 30%)", request.tp2, isFa)
                    }
                    if (request.tp3.isNotEmpty()) {
                        FrostedOrderParamRow(if (isFa) "تارگت ۳ (TP 3 - ۲۰٪ رانر)" else "Target 3 (TP3 20%)", request.tp3, isFa)
                    }
                    FrostedOrderParamRow(if (isFa) "نسبت ریسک به ریوارد (R:R)" else "Risk:Reward", request.riskReward, isFa)
                }

                HorizontalDivider(color = Color(0x18FFFFFF))

                // Platform Toggle (MetaTrader 5 vs MetaTrader 4)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (isFa) "انتخاب پلتفرم متاتریدر مقصد:" else "Destination MetaTrader Platform:",
                        color = Slate300,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = VazirmatnFontFamily
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (preferMt5) Blue600.copy(alpha = 0.35f) else Color(0x15FFFFFF))
                                .border(1.dp, if (preferMt5) Blue400 else Color(0x25FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { preferMt5 = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "MetaTrader 5 (MT5)",
                                color = if (preferMt5) Slate50 else Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!preferMt5) Blue600.copy(alpha = 0.35f) else Color(0x15FFFFFF))
                                .border(1.dp, if (!preferMt5) Blue400 else Color(0x25FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { preferMt5 = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "MetaTrader 4 (MT4)",
                                color = if (!preferMt5) Slate50 else Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Volume / Lot Size Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isFa) "حجم معامله (Lot Size):" else "Trade Volume (Lot):",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = VazirmatnFontFamily
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Minus button
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x25FFFFFF))
                                    .clickable {
                                        if (lotSize > 0.015) lotSize = ((lotSize - 0.01) * 100).roundToInt() / 100.0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            // Lot display
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x18000000))
                                    .border(1.dp, Blue400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    String.format(Locale.US, "%.2f Lot", lotSize),
                                    color = Amber300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            // Plus button
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x25FFFFFF))
                                    .clickable {
                                        if (lotSize < 5.0) lotSize = ((lotSize + 0.01) * 100).roundToInt() / 100.0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Quick Lot chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.01, 0.02, 0.05, 0.10, 0.25, 0.50, 1.00).forEach { preset ->
                            val isSel = kotlin.math.abs(lotSize - preset) < 0.005
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Amber400.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                                    .border(1.dp, if (isSel) Amber400 else Color(0x1FFFFFFF), RoundedCornerShape(6.dp))
                                    .clickable { lotSize = preset }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    String.format(Locale.US, "%.2f", preset),
                                    color = if (isSel) Amber300 else Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Help/Info note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x183B82F6))
                        .border(0.8.dp, Blue400.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("💡", fontSize = 12.sp)
                        Text(
                            if (isFa) "با لمس دکمه زیر، اپلیکیشن متاتریدر مستقیماً باز شده و مقادیر قیمت، حد ضرر و تارگت در کلیپ‌بورد کپی می‌شوند."
                            else "MetaTrader app will open directly with your parameters populated and copied to clipboard.",
                            color = Blue300,
                            fontSize = 9.5.sp,
                            fontFamily = VazirmatnFontFamily,
                            lineHeight = 14.sp
                        )
                    }
                }

                HorizontalDivider(color = Color(0x18FFFFFF))

                // Bottom Buttons: Cancel & Confirm+Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                    ) {
                        Text(if (isFa) "انصراف" else "Cancel", fontFamily = VazirmatnFontFamily)
                    }

                    Button(
                        onClick = { onConfirmAndSend(lotSize, preferMt5) },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("confirm_and_send_metatrader_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (preferMt5) Blue600 else Emerald600,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📝", fontSize = 13.sp)
                            Text(
                                if (isFa) "تایید و ثبت آزمایشی (شبیه‌سازی)" else "Confirm Paper Trade",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedOrderParamRow(title: String, value: String, isFa: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Slate400, fontSize = 12.sp, fontFamily = VazirmatnFontFamily)
        Text(value, color = Slate200, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FrostedFloatingBottomNav(
    currentTab: String,
    isFa: Boolean,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IbkrSurface)
                .border(1.dp, IbkrBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Markets
                FrostedBottomNavItem(
                    icon = Icons.Filled.List,
                    label = if (isFa) "بازارها" else "Markets",
                    isSelected = currentTab == "MARKET",
                    onClick = { onTabSelected("MARKET") }
                )
                // 2. Scanner
                FrostedBottomNavItem(
                    icon = Icons.Filled.Radar,
                    label = if (isFa) "اسکنر" else "Scanner",
                    isSelected = currentTab == "SCANNER",
                    onClick = { onTabSelected("SCANNER") }
                )
                // 3. Chart
                FrostedBottomNavItem(
                    icon = Icons.Filled.CandlestickChart,
                    label = if (isFa) "نمودار" else "Chart",
                    isSelected = currentTab == "CHART",
                    onClick = { onTabSelected("CHART") }
                )
                // 4. Journal / Portfolio
                FrostedBottomNavItem(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = if (isFa) "ژورنال" else "Journal",
                    isSelected = currentTab == "JOURNAL",
                    onClick = { onTabSelected("JOURNAL") }
                )
                // 5. Settings / More
                FrostedBottomNavItem(
                    icon = Icons.Filled.Settings,
                    label = if (isFa) "تنظیمات" else "Settings",
                    isSelected = currentTab == "MORE",
                    onClick = { onTabSelected("MORE") }
                )
            }
        }
    }
}

@Composable
fun FrostedBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = IbkrBid
    val inactiveColor = IbkrTextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .border(
                    1.dp,
                    if (isSelected) activeColor.copy(alpha = 0.5f) else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(19.dp)
            )
        }
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) IbkrTextPrimary else inactiveColor,
            fontFamily = VazirmatnFontFamily
        )
    }
}
