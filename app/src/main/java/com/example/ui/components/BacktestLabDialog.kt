package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.quant.QuantScannerEngine
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BacktestLabDialog(
    initialSymbol: String,
    initialTimeframe: String,
    watchlistSymbols: List<String>,
    currentParams: StrategyParams,
    backtestState: BacktestLabState,
    isFa: Boolean,
    onRunBacktest: (
        symbol: String,
        timeframe: String,
        candleLimit: Int,
        minScore: Int,
        atrMult: Float,
        targetRr: Double,
        directionFilter: String
    ) -> Unit,
    onApplyParams: (atrMult: Float, timeframe: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(initialSymbol) }
    var selectedTimeframe by remember {
        mutableStateOf(if (initialTimeframe == "ALL" || initialTimeframe.isBlank()) "1h" else initialTimeframe)
    }
    var candleLimit by remember { mutableIntStateOf(500) }
    var minScore by remember { mutableIntStateOf(58) }
    var atrMult by remember { mutableFloatStateOf(currentParams.atrMultiplier) }
    var targetRr by remember { mutableDoubleStateOf(2.0) }
    var directionFilter by remember { mutableStateOf("ALL") }

    // Auto-run once when dialog opens so the user sees immediate results
    LaunchedEffect(Unit) {
        onRunBacktest(
            selectedSymbol,
            selectedTimeframe,
            candleLimit,
            minScore,
            atrMult,
            targetRr,
            directionFilter
        )
    }

    val isLoading = backtestState is BacktestLabState.Loading

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(18.dp))
                .background(AppSurface)
                .border(1.dp, AppBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
                .testTag("backtest_lab_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(AppAccent.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, AppAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Science, contentDescription = null, tint = AppAccent, modifier = Modifier.size(19.dp))
                        }
                        Column {
                            Text(
                                text = if (isFa) "آزمایشگاه بک‌تست و بهینه‌سازی Win Rate" else "Interactive Quant Backtest Lab",
                                color = AppText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MinimalPersianFontFamily
                            )
                            Text(
                                text = if (isFa) "شبیه‌سازی کندل‌به‌کندل بدون نگاه به آینده (Zero-Lookahead)" else "Bar-by-bar Zero-Lookahead MTF Simulation",
                                color = AppTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = MinimalPersianFontFamily
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = AppTextSecondary)
                    }
                }

                // Controls + Scrollable Report Area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Parameter Tuning Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceVariant)
                                .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Asset Selector
                            SelectorStrip(
                                label = if (isFa) "نماد:" else "Asset:",
                                options = watchlistSymbols.distinct().map { it to it },
                                selectedKey = selectedSymbol,
                                onSelect = { selectedSymbol = it }
                            )

                            // Timeframe Selector
                            SelectorStrip(
                                label = if (isFa) "تایم‌فریم:" else "TF:",
                                options = listOf("5m", "15m", "1h", "4h", "1d").map { it to it.uppercase() },
                                selectedKey = selectedTimeframe,
                                onSelect = { selectedTimeframe = it }
                            )

                            // Candle Depth Selector
                            SelectorStrip(
                                label = if (isFa) "عمق تاریخچه:" else "History:",
                                options = listOf(
                                    260 to (if (isFa) "۲۶۰ کندل" else "260 bars"),
                                    500 to (if (isFa) "۵۰۰ کندل" else "500 bars"),
                                    1000 to (if (isFa) "۱۰۰۰ کندل" else "1000 bars")
                                ),
                                selectedKey = candleLimit,
                                onSelect = { candleLimit = it }
                            )

                            // Min Confluence Score Filter
                            SelectorStrip(
                                label = if (isFa) "حداقل امتیاز ورود:" else "Min Score:",
                                options = listOf(45, 58, 70, 78).map { it to "$it+" },
                                selectedKey = minScore,
                                onSelect = { minScore = it }
                            )

                            // ATR Multiplier
                            SelectorStrip(
                                label = if (isFa) "ضریب SL (ATR):" else "SL ATR:",
                                options = listOf(1.0f, 1.5f, 2.0f, 2.5f).map { it to "${it}x" },
                                selectedKey = atrMult,
                                onSelect = { atrMult = it }
                            )

                            // Target R:R
                            SelectorStrip(
                                label = if (isFa) "نسبت R:R هدف:" else "Target R:R:",
                                options = listOf(1.5, 2.0, 2.5, 3.0).map { it to "1:$it" },
                                selectedKey = targetRr,
                                onSelect = { targetRr = it }
                            )

                            // Direction Mode
                            SelectorStrip(
                                label = if (isFa) "جهت:" else "Side:",
                                options = listOf(
                                    "ALL" to (if (isFa) "خرید و فروش" else "Both"),
                                    "LONG" to (if (isFa) "فقط Long" else "Long Only"),
                                    "SHORT" to (if (isFa) "فقط Short" else "Short Only")
                                ),
                                selectedKey = directionFilter,
                                onSelect = { directionFilter = it }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onRunBacktest(
                                            selectedSymbol,
                                            selectedTimeframe,
                                            candleLimit,
                                            minScore,
                                            atrMult,
                                            targetRr,
                                            directionFilter
                                        )
                                    },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("run_backtest_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isFa) "در حال شبیه‌سازی..." else "Running...",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MinimalPersianFontFamily
                                        )
                                    } else {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (isFa) "اجرای بک‌تست" else "Run Backtest",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MinimalPersianFontFamily
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onApplyParams(atrMult, selectedTimeframe) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppBullish.copy(alpha = 0.6f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = AppBullish, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (isFa) "اعمال به استراتژی" else "Apply Params",
                                        color = AppBullish,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }
                            }
                        }
                    }

                    // 2. Results View
                    when (backtestState) {
                        is BacktestLabState.Idle -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFa) "تنظیمات دلخواه را انتخاب کرده و روی «اجرای بک‌تست» بزنید." else "Configure parameters and tap Run Backtest.",
                                        color = AppTextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }
                            }
                        }
                        is BacktestLabState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(color = AppAccent, modifier = Modifier.size(30.dp))
                                        Text(
                                            text = if (isFa) "در حال دریافت کندل‌های تاریخی و محاسبه منحنی رشد سرمایه..." else "Fetching historical OHLCV bars & computing equity curve...",
                                            color = AppTextSecondary,
                                            fontSize = 11.5.sp,
                                            fontFamily = MinimalPersianFontFamily
                                        )
                                    }
                                }
                            }
                        }
                        is BacktestLabState.Error -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppBearish.copy(alpha = 0.12f))
                                        .border(1.dp, AppBearish.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = backtestState.message,
                                        color = AppBearish,
                                        fontSize = 11.5.sp,
                                        fontFamily = MinimalPersianFontFamily
                                    )
                                }
                            }
                        }
                        is BacktestLabState.Success -> {
                            val report = backtestState.report
                            val dateFmt = SimpleDateFormat("MM/dd HH:mm", Locale.US)

                            // Data Audit Banner
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppSurfaceVariant)
                                        .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${report.dataSource} • ${report.candleCount} کندل (${report.timeframe.uppercase()} + ${report.htfTimeframe.uppercase()})",
                                        color = AppAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${dateFmt.format(Date(report.startTimestamp))} → ${dateFmt.format(Date(report.endTimestamp))}",
                                        color = AppTextSecondary,
                                        fontSize = 9.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // KPI Grid
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        KpiTile(
                                            modifier = Modifier.weight(1f),
                                            label = if (isFa) "نرخ برد (Win Rate)" else "Win Rate",
                                            value = String.format(Locale.US, "%.1f%%", report.winRatePct),
                                            sub = "${report.wins}W / ${report.losses}L (${report.totalTrades})",
                                            accent = if (report.winRatePct >= 50.0) AppBullish else Amber400
                                        )
                                        KpiTile(
                                            modifier = Modifier.weight(1f),
                                            label = if (isFa) "ضریب سود (PF)" else "Profit Factor",
                                            value = String.format(Locale.US, "%.2f", report.profitFactor),
                                            sub = "Exp: ${String.format(Locale.US, "%+.2fR", report.expectancyR)}",
                                            accent = if (report.profitFactor >= 1.2) AppBullish else AppBearish
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        KpiTile(
                                            modifier = Modifier.weight(1f),
                                            label = if (isFa) "بازده خالص" else "Net Return",
                                            value = String.format(Locale.US, "%+.2f%%", report.netReturnPct),
                                            sub = "$${String.format(Locale.US, "%,.0f", report.initialEquity)} → $${String.format(Locale.US, "%,.0f", report.finalEquity)}",
                                            accent = if (report.netReturnPct >= 0.0) AppBullish else AppBearish
                                        )
                                        KpiTile(
                                            modifier = Modifier.weight(1f),
                                            label = if (isFa) "حداکثر افت (Max DD)" else "Max Drawdown",
                                            value = String.format(Locale.US, "-%.2f%%", report.maxDrawdownPct),
                                            sub = if (isFa) "امتیاز ≥ ${report.minScoreThreshold}" else "Score ≥ ${report.minScoreThreshold}",
                                            accent = if (report.maxDrawdownPct <= 10.0) AppBullish else AppBearish
                                        )
                                    }
                                }
                            }

                            // Equity Curve Chart
                            item {
                                EquityCurveCard(
                                    report = report,
                                    isFa = isFa
                                )
                            }

                            // Trades Header
                            item {
                                Text(
                                    text = if (isFa) "تاریخچه معاملات شبیه‌سازی‌شده (${report.trades.size} معامله)" else "Simulated Historical Trades (${report.trades.size})",
                                    color = AppText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MinimalPersianFontFamily,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (report.trades.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AppSurfaceVariant)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isFa) "با حداقل امتیاز ${report.minScoreThreshold} هیچ معامله‌ای در این بازه ثبت نشد. حداقل امتیاز را کاهش دهید." else "No trades met min score ${report.minScoreThreshold}. Try lowering Min Score.",
                                            color = AppTextSecondary,
                                            fontSize = 11.sp,
                                            fontFamily = MinimalPersianFontFamily
                                        )
                                    }
                                }
                            } else {
                                items(report.trades, key = { it.tradeNumber }) { tr ->
                                    BacktestTradeRow(
                                        trade = tr,
                                        isIrt = report.symbol.contains("IRT"),
                                        isFa = isFa
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SelectorStrip(
    label: String,
    options: List<Pair<T, String>>,
    selectedKey: T,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = AppTextSecondary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = MinimalPersianFontFamily
        )
        options.forEach { (key, title) ->
            val isSelected = key == selectedKey
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSelected) AppAccent.copy(alpha = 0.2f) else AppSurface)
                    .border(
                        1.dp,
                        if (isSelected) AppAccent else AppBorder,
                        RoundedCornerShape(7.dp)
                    )
                    .clickable { onSelect(key) }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = title,
                    color = if (isSelected) AppAccent else AppText,
                    fontSize = 10.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun KpiTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
    accent: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppSurfaceVariant)
            .border(1.dp, AppBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = AppTextSecondary,
            fontSize = 9.5.sp,
            fontFamily = MinimalPersianFontFamily
        )
        Text(
            text = value,
            color = accent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = sub,
            color = AppTextSecondary,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun EquityCurveCard(
    report: QuantScannerEngine.DetailedBacktestReport,
    isFa: Boolean
) {
    val curve = report.equityCurve
    val lineColor = if (report.netReturnPct >= 0.0) AppBullish else AppBearish
    val gridColor = AppBorder
    val baseColor = AppTextSecondary.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppSurfaceVariant)
            .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFa) "منحنی رشد سرمایه (Equity Curve)" else "Simulated Equity Curve",
                color = AppText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MinimalPersianFontFamily
            )
            Text(
                text = "Peak: $${String.format(Locale.US, "%,.0f", curve.maxOrNull() ?: report.initialEquity)}",
                color = lineColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            if (curve.size < 2) {
                drawLine(
                    color = baseColor,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 2f
                )
                return@Canvas
            }
            val minVal = curve.minOrNull() ?: report.initialEquity
            val maxVal = curve.maxOrNull() ?: report.initialEquity
            val span = (maxVal - minVal).coerceAtLeast(1.0)
            val padY = 8f
            val usableH = (size.height - padY * 2f).coerceAtLeast(1f)

            fun yOf(v: Double): Float {
                val norm = ((v - minVal) / span).toFloat()
                return size.height - padY - (norm * usableH)
            }

            // Grid & initial equity dashed baseline
            drawLine(
                color = gridColor,
                start = Offset(0f, size.height * 0.25f),
                end = Offset(size.width, size.height * 0.25f),
                strokeWidth = 1f
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, size.height * 0.75f),
                end = Offset(size.width, size.height * 0.75f),
                strokeWidth = 1f
            )
            val baseY = yOf(report.initialEquity)
            drawLine(
                color = baseColor,
                start = Offset(0f, baseY),
                end = Offset(size.width, baseY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            val strokePath = Path()
            val fillPath = Path()
            val stepX = size.width / (curve.size - 1).coerceAtLeast(1)

            curve.forEachIndexed { idx, value ->
                val x = idx * stepX
                val y = yOf(value)
                if (idx == 0) {
                    strokePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    strokePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.28f), Color.Transparent)
                )
            )
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun BacktestTradeRow(
    trade: QuantScannerEngine.BacktestTradeRecord,
    isIrt: Boolean,
    isFa: Boolean
) {
    val isWin = trade.pnlR > 0.0
    val outcomeColor = if (isWin) AppBullish else AppBearish
    val dirColor = if (trade.direction == "LONG") AppBullish else AppBearish
    val dateFmt = SimpleDateFormat("MM/dd HH:mm", Locale.US)

    fun fmtPrice(v: Double): String = if (isIrt) {
        "${NumberFormat.getNumberInstance(Locale.US).format(v.toLong())} ت"
    } else {
        when {
            v < 0.01 -> String.format(Locale.US, "$%.6f", v)
            v < 1.0 -> String.format(Locale.US, "$%.4f", v)
            else -> String.format(Locale.US, "$%,.2f", v)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppSurfaceVariant)
            .border(1.dp, outcomeColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "#${trade.tradeNumber}",
                    color = AppTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(dirColor.copy(alpha = 0.16f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = trade.direction,
                        color = dirColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Score ${trade.confluenceScore}",
                    color = AppAccent,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${dateFmt.format(Date(trade.entryTimestamp))} (${trade.durationBars}b)",
                    color = AppTextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(outcomeColor.copy(alpha = 0.16f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${trade.outcome} ${String.format(Locale.US, "%+.2fR", trade.pnlR)}",
                    color = outcomeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isFa) "ورود: ${fmtPrice(trade.entryPrice)} → خروج: ${fmtPrice(trade.exitPrice)}"
                else "Entry: ${fmtPrice(trade.entryPrice)} → Exit: ${fmtPrice(trade.exitPrice)}",
                color = AppText,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format(Locale.US, "%+$%.1f", trade.pnlUsd),
                color = outcomeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
