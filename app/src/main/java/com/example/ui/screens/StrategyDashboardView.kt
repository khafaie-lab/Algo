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

// -------------------------------------------------------------
// 1. STRATEGY DASHBOARD VIEW (With TradingView Live Chart & Confluence Engine)
// -------------------------------------------------------------
@Composable
fun StrategyDashboardView(
    viewModel: MarketDataViewModel,
    selectedAsset: String,
    price: String,
    isLoadingPrice: Boolean,
    lastUpdated: String,
    checklist: List<ChecklistCondition>,
    advisorSetup: AdvisorSetup?,
    aiAnalysisState: AiAnalysisState,
    params: StrategyParams,
    isFa: Boolean,
    nobitexMarkets: List<NobitexMarketItem>,
    serverPriority: String,
    scannerOpportunities: List<ScannerOpportunity> = emptyList(),
    activeOpportunity: ScannerOpportunity? = null,
    isExternalFullscreen: Boolean = false,
    onOpenFullscreen: () -> Unit = {},
    onCloseFullscreen: () -> Unit = {},
    onOpenScannerTab: () -> Unit = {},
    onOpenRiskCalculator: ((ScannerOpportunity?) -> Unit) = {},
    onExecuteClick: () -> Unit
) {
    val candles by viewModel.candles.collectAsState()
    val isLoadingCandles by viewModel.isLoadingCandles.collectAsState()
    val chartType by viewModel.chartType.collectAsState()
    val chartTimeframe by viewModel.chartTimeframe.collectAsState()
    val showEma by viewModel.showEma.collectAsState()
    val showOrderBlocks by viewModel.showOrderBlocks.collectAsState()
    val showVolume by viewModel.showVolume.collectAsState()
    val nobitexOrderBook by viewModel.nobitexOrderBook.collectAsState()
    val isLoadingNobitex by viewModel.isLoadingNobitex.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()
    val tomanPrice by viewModel.tomanPrice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // 1. Live Sync Status Bar
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
                        .size(8.dp)
                        .background(if (isLoadingPrice) Amber400 else Emerald400, CircleShape)
                )
                Text(
                    text = if (isFa) "استعلام زنده بازار:" else "Live Market Feed:",
                    color = Slate300,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = VazirmatnFontFamily
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF141A29))
                        .border(1.dp, Color(0xFF26334D), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = dataSource,
                        color = Cyan400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
            if (lastUpdated.isNotEmpty()) {
                Text(
                    text = lastUpdated,
                    color = Slate400,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2. Asset Selector Carousel Chips (Full Width)
        FrostedAssetSelector(
            selectedAsset = selectedAsset,
            onSelect = { viewModel.selectAsset(it) },
            modifier = Modifier.fillMaxWidth()
        )

        // Dedicated Multi-Currency Live Nobitex Rates Watchlist
        NobitexLiveWatchlistCard(
            markets = nobitexMarkets,
            selectedAsset = selectedAsset,
            serverPriority = serverPriority,
            isFa = isFa,
            onSelectAsset = { viewModel.selectAsset(it) }
        )

        // TradingView-Style Interactive Live Candlestick Chart
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
            activeOpportunity = activeOpportunity,
            isExternalFullscreen = isExternalFullscreen,
            onOpenFullscreen = onOpenFullscreen,
            onCloseFullscreen = onCloseFullscreen,
            onSelectTimeframe = { viewModel.setChartTimeframe(it) },
            onToggleChartType = { viewModel.setChartType(if (chartType == "CANDLES") "LINE" else "CANDLES") },
            onToggleEma = { viewModel.toggleEma() },
            onToggleOrderBlocks = { viewModel.toggleOrderBlocks() },
            onToggleVolume = { viewModel.toggleVolume() },
            onSelectAsset = { viewModel.selectAsset(it) },
            onOpenRiskCalculator = onOpenRiskCalculator,
            onSendOpportunityOrder = { opp -> viewModel.requestOrderFromOpportunity(opp) }
        )

        // Top 5 Multi-Timeframe Scanner Recommendations Card
        TopScannerProposalsCard(
            opportunities = scannerOpportunities.take(5),
            isFa = isFa,
            onSendOrder = { opp -> viewModel.requestOrderFromOpportunity(opp) },
            onSelectOpportunity = { opp ->
                viewModel.selectOpportunity(opp, openFullscreen = true)
                onOpenFullscreen()
            },
            onOpenFullScanner = onOpenScannerTab
        )

        // Nobitex Order Book Depth Ladder
        FrostedOrderBookCard(
            nobitexOrderBook = nobitexOrderBook,
            isLoading = isLoadingNobitex,
            isFa = isFa,
            onExecuteClick = onExecuteClick
        )

        // Expert Advisor Confluence Score & Multi-Target Scaling
        advisorSetup?.let { setup ->
            ExpertAdvisorScoreCard(
                advisorSetup = setup,
                isFa = isFa,
                onSendToMetaTrader = { viewModel.requestOrderFromAdvisor(selectedAsset, price, setup) },
                onExecuteClick = onExecuteClick
            )
        }

        // Market Metrics Dual Row
        FrostedMetricsRow(
            asset = selectedAsset,
            price = price,
            tomanPrice = tomanPrice,
            isLoading = isLoadingPrice,
            atrMultiplier = params.atrMultiplier,
            isFa = isFa
        )

        // AI Strategy Audit Card
        FrostedAiStrategyCard(
            state = aiAnalysisState,
            isFa = isFa,
            onRunAudit = { viewModel.runAiStrategyAudit() },
            onDismiss = { viewModel.dismissAiAnalysis() }
        )

        // Weighted Execution Checklist Card
        FrostedExecutionChecklist(
            checklist = checklist,
            isFa = isFa,
            onToggleItem = { viewModel.toggleChecklistCondition(it) },
            onExecuteClick = onExecuteClick
        )

        Spacer(modifier = Modifier.height(90.dp))
    }
}


// -------------------------------------------------------------
// EXPERT ADVISOR SCORE & MULTI-TARGET SCALING CARD
// -------------------------------------------------------------
@Composable
fun ExpertAdvisorScoreCard(
    advisorSetup: AdvisorSetup,
    isFa: Boolean,
    onSendToMetaTrader: (() -> Unit)? = null,
    onExecuteClick: () -> Unit
) {
    val score = advisorSetup.confluenceScore
    val isHighQuality = score >= 80

    val scoreColor = when {
        score >= 80 -> Emerald400
        score >= 60 -> Amber400
        else -> Rose400
    }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = scoreColor
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Score Badge + Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(scoreColor.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, scoreColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$score",
                            color = scoreColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isFa) "امتیاز اعتبارسنجی مشاور کوانت" else "EXPERT ADVISOR CONFLUENCE",
                            color = Slate100,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                        Text(
                            advisorSetup.grade,
                            color = scoreColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(scoreColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, scoreColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${score}/100",
                        color = scoreColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Confluence Score Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(score / 100f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(if (score >= 80) Emerald500 else Blue500, scoreColor)
                            ),
                            CircleShape
                        )
                )
            }

            // Advisor Recommendation Statement
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(scoreColor.copy(alpha = 0.08f))
                    .border(1.dp, scoreColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    advisorSetup.recommendation,
                    color = Slate200,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = VazirmatnFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }

            // Multi-Target Scaling Levels (TP1, TP2, TP3 + Breakeven note)
            Text(
                if (isFa) "اهداف سود پلکانی و خروج هوشمند (Multi-Target Scaling)" else "MULTI-TARGET SCALING & INVALIDATION",
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirmatnFontFamily
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdvisorLevelPill(
                    label = if (isFa) "تارگت ۱ (۵۰٪)" else "TP 1 (50%)",
                    value = advisorSetup.tp1,
                    subtext = if (isFa) "+ریسک‌فری" else "Move BE",
                    color = Emerald400
                )
                AdvisorLevelPill(
                    label = if (isFa) "تارگت ۲ (۳۰٪)" else "TP 2 (30%)",
                    value = advisorSetup.tp2,
                    subtext = "1:2.5 R:R",
                    color = Emerald400
                )
                AdvisorLevelPill(
                    label = if (isFa) "تارگت ۳ (۲۰٪)" else "TP 3 (20%)",
                    value = advisorSetup.tp3,
                    subtext = if (isFa) "رانر" else "Runner",
                    color = Cyan400
                )
                AdvisorLevelPill(
                    label = if (isFa) "سطح ابطال" else "INVALIDATION",
                    value = advisorSetup.invalidationPrice,
                    subtext = if (isFa) "خروج فوری" else "Hard Exit",
                    color = Rose400
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onSendToMetaTrader != null) {
                    Button(
                        onClick = onSendToMetaTrader,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("advisor_send_to_metatrader_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🚀", fontSize = 12.sp)
                            Text(
                                if (isFa) "ارسال به متاتریدر" else "Send to MT5",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }
                }

                Button(
                    onClick = onExecuteClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("advisor_internal_execute_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighQuality) Emerald600 else Slate700,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isFa) "ثبت در ژورنال" else "Log Journal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun AdvisorLevelPill(label: String, value: String, subtext: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(subtext, color = Slate500, fontSize = 8.sp, fontFamily = VazirmatnFontFamily)
        }
    }
}

// -------------------------------------------------------------
// TOP SCANNER PROPOSALS CARD (Strategy Dashboard Widget)
// -------------------------------------------------------------
@Composable
fun TopScannerProposalsCard(
    opportunities: List<ScannerOpportunity>,
    isFa: Boolean,
    onSendOrder: ((ScannerOpportunity) -> Unit)? = null,
    onSelectOpportunity: (ScannerOpportunity) -> Unit,
    onOpenFullScanner: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = Amber400
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Amber400.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Radar, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isFa) "پیشنهادات برتر اسکنر هوشمند (Top 5)" else "Top 5 Scanner Opportunities",
                            color = Slate100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                        Text(
                            if (isFa) "چک‌لیست وزنی مولتی‌تایم‌فریم (۱د تا ۴س)" else "Multi-TF 5-Factor Confluence",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Amber400.copy(alpha = 0.15f))
                        .border(1.dp, Amber400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenFullScanner() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isFa) "اسکنر کامل >" else "All Signals >",
                        color = Amber400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }

            HorizontalDivider(color = Color(0x18FFFFFF), thickness = 1.dp)

            // 5 Ranked Proposal Rows
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opportunities.forEachIndexed { index, opp ->
                    val rankColor = when (index) {
                        0 -> Amber400
                        1 -> Cyan400
                        2 -> Blue400
                        else -> Slate300
                    }
                    val isA = opp.confluenceScore >= 80
                    val scoreColor = if (isA) Emerald400 else Amber400

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x18121927))
                            .border(1.dp, if (index == 0) Amber400.copy(alpha = 0.4f) else Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                            .clickable { onSelectOpportunity(opp) }
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Rank Badge
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(rankColor.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, rankColor.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "#${index + 1}",
                                            color = rankColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    // Symbol
                                    Text(
                                        "${opp.symbol}/USDT",
                                        color = Slate100,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    // Timeframe Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Blue600.copy(alpha = 0.25f))
                                            .border(0.8.dp, Blue400.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            opp.timeframe.uppercase(),
                                            color = Cyan400,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    // Direction Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Emerald500.copy(alpha = 0.2f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            opp.direction,
                                            color = Emerald400,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Confluence Score Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(scoreColor.copy(alpha = 0.18f))
                                        .border(1.dp, scoreColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${opp.confluenceScore}% ${if (isA) "A+" else "B"}",
                                        color = scoreColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Prices Row: Entry & R:R
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "${if (isFa) "ورود:" else "Entry:"} ${opp.entryPrice}",
                                        color = Slate300,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        "R:R ${opp.riskReward}",
                                        color = Amber400,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (onSendOrder != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (opp.direction == "SHORT") Rose500.copy(alpha = 0.25f) else Emerald500.copy(alpha = 0.25f))
                                                .border(1.dp, if (opp.direction == "SHORT") Rose400 else Emerald400, RoundedCornerShape(6.dp))
                                                .clickable { onSendOrder(opp) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .testTag("top_proposal_send_${opp.id}")
                                        ) {
                                            Text(
                                                if (isFa) "⚡ آزمایشی" else "⚡ Paper",
                                                color = if (opp.direction == "SHORT") Rose300 else Emerald300,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = VazirmatnFontFamily
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.clickable { onSelectOpportunity(opp) }
                                    ) {
                                        Text(
                                            if (isFa) "چارت" else "Chart",
                                            color = Cyan400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = VazirmatnFontFamily
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Cyan400,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            // Top condition preview
                            opp.passedConditions.firstOrNull()?.let { cond ->
                                Text(
                                    "✓ $cond",
                                    color = Emerald400.copy(alpha = 0.85f),
                                    fontSize = 9.sp,
                                    fontFamily = VazirmatnFontFamily,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedAssetSelector(
    selectedAsset: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val assets = listOf(
        "BTC" to "₿ بیت‌کوین",
        "ETH" to "Ξ اتریوم",
        "SOL" to "◎ سولانا",
        "USDT/IRT" to "🇮🇷 تتر/تومان",
        "TON" to "💎 تون",
        "DOGE" to "🐕 دوج"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(16.dp))
            .padding(4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        assets.forEach { (asset, label) ->
            val isSelected = asset == selectedAsset
            val isIrt = asset.contains("IRT")
            val chipBg = if (isSelected) {
                if (isIrt) Amber400.copy(alpha = 0.35f) else Blue600.copy(alpha = 0.5f)
            } else Color.Transparent
            val chipBorder = if (isSelected) {
                if (isIrt) Amber400.copy(alpha = 0.7f) else Cyan400.copy(alpha = 0.6f)
            } else Color.Transparent

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                    .clickable { onSelect(asset) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) (if (isIrt) Amber400 else Slate50) else Slate400,
                    fontFamily = VazirmatnFontFamily
                )
            }
        }
    }
}

/**
 * Dedicated Nobitex Multi-Currency Exchange Rates Card.
 * Queries and displays prices for Bitcoin, Ethereum, Solana, Tether, Toncoin, and Dogecoin directly from Nobitex servers.
 */
@Composable
fun NobitexLiveWatchlistCard(
    markets: List<NobitexMarketItem>,
    selectedAsset: String,
    serverPriority: String,
    isFa: Boolean,
    onSelectAsset: (String) -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = Amber400
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header with Nobitex Branding & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Amber400.copy(alpha = 0.2f), RoundedCornerShape(7.dp))
                            .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🇮🇷", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isFa) "استعلام زنده از سرور نوبیتکس" else "NOBITEX LIVE MARKET FEED",
                            color = Slate100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                        Text(
                            text = if (isFa) "اولویت ۱ (جایگزین بایننس مسدود) • بدون نیاز به VPN" else "Priority #1 (Bypasses Binance blocks) • Direct Gateway",
                            color = Slate400,
                            fontSize = 9.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                val isCached = serverPriority.contains("معتبر") || serverPriority.contains("آفلاین")
                val isError = serverPriority.contains("خطا") || serverPriority.contains("عدم")
                val badgeColor = if (isError) Rose400 else if (isCached) Amber400 else Emerald400
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(badgeColor, CircleShape)
                        )
                        Text(
                            text = serverPriority.ifEmpty { if (isFa) "🟢 نوبیتکس زنده" else "🟢 Nobitex Live" },
                            color = badgeColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }
            }

            // Crypto Rates List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                markets.forEach { item ->
                    val isSelected = item.symbol == selectedAsset
                    val isPositive = item.dayChange >= 0
                    val changeColor = if (isPositive) Emerald400 else Rose400
                    val changeSign = if (isPositive) "+" else ""

                    val rowBg = if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(Amber400.copy(alpha = 0.18f), Blue500.copy(alpha = 0.12f))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0x0EFFFFFF), Color(0x06FFFFFF))
                        )
                    }
                    val rowBorder = if (isSelected) Amber400.copy(alpha = 0.7f) else FrostedBorderBottom

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(rowBg)
                            .border(1.dp, rowBorder, RoundedCornerShape(14.dp))
                            .clickable { onSelectAsset(item.symbol) }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Coin Logo / Symbol & Name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val coinSymbolIcon = when (item.symbol) {
                                    "BTC" -> "₿"
                                    "ETH" -> "Ξ"
                                    "SOL" -> "◎"
                                    "TON" -> "💎"
                                    "DOGE" -> "🐕"
                                    else -> "₮"
                                }
                                val iconColor = when (item.symbol) {
                                    "BTC" -> Amber400
                                    "ETH" -> Cyan400
                                    "SOL" -> Indigo400
                                    "TON" -> Blue400
                                    "DOGE" -> Amber400
                                    else -> Emerald400
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(iconColor.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = coinSymbolIcon,
                                        color = iconColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isFa) item.nameFa else item.nameEn,
                                            color = Slate100,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = VazirmatnFontFamily
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${item.symbol})",
                                            color = Slate400,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text(
                                        text = if (item.symbol == "USDT/IRT") "$1.00 USDT" else String.format(Locale.US, "$%,.2f", item.priceUsdt),
                                        color = Cyan400,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Right: Toman Price + 24h Change Pill
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "%,.0f تومان", item.priceToman),
                                    color = if (item.symbol == "USDT/IRT") Amber400 else Slate100,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .background(changeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, changeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$changeSign${String.format(Locale.US, "%.2f", item.dayChange)}%",
                                        color = changeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
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
fun FrostedMetricsRow(
    asset: String,
    price: String,
    tomanPrice: String = "",
    isLoading: Boolean,
    atrMultiplier: Float,
    isFa: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FrostedGlassCard(
            modifier = Modifier
                .weight(1f)
                .height(115.dp),
            accentGlow = Blue600
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (isFa) "سطح اسپات $asset / POC" else "$asset SPOT / POC",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )
                Column {
                    Text(
                        if (isLoading && price == "...") (if (isFa) "در حال بررسی..." else "Scanning...") else price,
                        color = Slate100,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    if (tomanPrice.isNotEmpty() && !asset.contains("IRT")) {
                        Text(
                            tomanPrice,
                            color = Amber400,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (isFa) "استعلام از نوبیتکس" else "Nobitex Live Rate",
                        color = Cyan400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }

        FrostedGlassCard(
            modifier = Modifier
                .weight(1f)
                .height(115.dp),
            accentGlow = Indigo600
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (isFa) "نوسان‌پذیری ATR" else "ATR VOLATILITY",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )
                Column {
                    Text(
                        if (asset == "BTC") "182.42" else if (asset == "ETH") "14.80" else "2.15",
                        color = Slate100,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (isFa) "ضریب: ${atrMultiplier}x پویا" else "Multiplier: ${atrMultiplier}x Dynamic",
                        color = Indigo400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun FrostedAiStrategyCard(
    state: AiAnalysisState,
    isFa: Boolean,
    onRunAudit: () -> Unit,
    onDismiss: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = Cyan500
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Amber400.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Amber400.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI",
                            tint = Amber400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (isFa) "تحلیلگر هوشمند کوانت (AI)" else "AI QUANT ANALYST",
                        color = Slate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                if (state is AiAnalysisState.Success) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            when (state) {
                is AiAnalysisState.Idle -> {
                    Text(
                        if (isFa) "بررسی چند تایم‌فریمی جریان سفارشات اسمارت مانی با هوش مصنوعی و تایید قوانین ورود به معامله."
                        else "Run multi-timeframe smart money audits with live price grounding to validate your trading rules.",
                        color = Slate300,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = VazirmatnFontFamily
                    )

                    Button(
                        onClick = onRunAudit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_ai_audit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isFa) "اسکن هوشمند بازار و کالیبراسیون ستاپ" else "Audit Strategy & Calibrate Setup",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                is AiAnalysisState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = Cyan400,
                            trackColor = Blue600.copy(alpha = 0.2f)
                        )
                        Text(
                            if (isFa) "در حال بررسی نقدینگی زنده، ساختار ۴ ساعته و اردر بلاک‌ها..." else "Analyzing live liquidity, 4H structures & order blocks...",
                            color = Slate300,
                            fontSize = 12.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                is AiAnalysisState.Success -> {
                    val res = state.result
                    val biasColor = when (res.bias) {
                        "BULLISH" -> Emerald400
                        "BEARISH" -> Rose400
                        else -> Amber400
                    }

                    val biasText = when (res.bias) {
                        "BULLISH" -> if (isFa) "روند صعودی" else "BULLISH"
                        "BEARISH" -> if (isFa) "روند نزولی" else "BEARISH"
                        else -> if (isFa) "خنثی / رنج" else "NEUTRAL"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(biasColor.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                .border(1.dp, biasColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "$biasText (${res.confidence}% ${if (isFa) "اطمینان" else "Conf."})",
                                color = biasColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                        }

                        Text("${if (isFa) "ثبت:" else "Synced"} ${res.generatedAt}", color = Slate400, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                    }

                    Text(
                        res.summary,
                        color = Slate200,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = VazirmatnFontFamily
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FrostedGlassSurface)
                            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (isFa) "سطوح کلیدی سازمانی" else "KEY INSTITUTIONAL LEVELS",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                            Text(
                                res.keyLevels,
                                color = Cyan400,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (res.isQuotaFallback) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Amber400.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .border(1.dp, Amber400.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = Amber400, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isFa) "محاسبه سطوح به صورت هوشمند توسط موتور کوانت داخلی انجام شد."
                                    else "QuantPro Engine generated levels locally based on live spot price.",
                                    color = Amber400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = VazirmatnFontFamily
                                )
                            }
                        }
                    }

                    Text(
                        if (isFa) "شروط چک‌لیست و امتیاز هم‌پوشانی بر اساس نتایج تحلیل کالیبره شدند." else "Strategy checklist calibrated with real-time audit findings.",
                        color = Emerald400.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                is AiAnalysisState.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${if (isFa) "خطای بررسی:" else "Audit failed:"} ${state.message}",
                            color = Rose400,
                            fontSize = 12.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                        OutlinedButton(
                            onClick = onRunAudit,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isFa) "تلاش مجدد" else "Retry AI Scan", fontSize = 12.sp, fontFamily = VazirmatnFontFamily)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedExecutionChecklist(
    checklist: List<ChecklistCondition>,
    isFa: Boolean,
    onToggleItem: (String) -> Unit,
    onExecuteClick: () -> Unit
) {
    val passed = checklist.count { it.isPassed }
    val total = checklist.size
    val totalWeight = checklist.filter { it.isPassed }.sumOf { it.weight }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = Emerald500
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isFa) "چک‌لیست وزنی شرایط معامله" else "WEIGHTED CONFLUENCE CHECKLIST",
                    color = Slate300,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily,
                    letterSpacing = 1.sp
                )

                Text(
                    "$passed ${if (isFa) "از" else "of"} $total ($totalWeight%)",
                    color = if (totalWeight >= 80) Emerald400 else Cyan400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((totalWeight / 100f).coerceIn(0.05f, 1f))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Cyan500, if (totalWeight >= 80) Emerald400 else Blue500)
                            ),
                            CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                checklist.forEach { item ->
                    FrostedChecklistRow(
                        item = item,
                        isFa = isFa,
                        onToggle = { onToggleItem(item.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onExecuteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_order_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (totalWeight >= 80) Emerald600 else if (totalWeight >= 60) Blue600 else Slate800,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (isFa) {
                            if (totalWeight >= 80) "ثبت آزمایشی طلایی (شبیه‌سازی)" else "ثبت آزمایشی معامله (شبیه‌سازی)"
                        } else {
                            if (totalWeight >= 80) "RECORD GOLDEN PAPER ORDER" else "RECORD PAPER TRADE SETUP"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Execute",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FrostedChecklistRow(
    item: ChecklistCondition,
    isFa: Boolean,
    onToggle: () -> Unit
) {
    val rowBg = if (item.isPassed) Emerald500.copy(alpha = 0.1f) else FrostedGlassSurface
    val rowBorder = if (item.isPassed) Emerald500.copy(alpha = 0.35f) else FrostedBorderBottom

    val faTitle = when (item.id) {
        "ema_cross" -> "تقاطع میانگین متحرک ۵۰ و ۲۰۰ (EMA Cross)"
        "order_block" -> "برخورد به اردر بلاک تایم ۴ ساعته"
        "rsi_div" -> "واگرایی معمولی صعودی در اندیکاتور RSI"
        "choch" -> "تغییر ساختار روند در تایم پایین (ChoCH)"
        "vol_expansion" -> "افزایش حجم بیش از ۱.۵ برابر میانگین"
        else -> item.title
    }

    val faDesc = when (item.id) {
        "ema_cross" -> "تایید روند صعودی با تقاطع میانگین‌های متحرک"
        "order_block" -> "برخورد با دیماند سازمانی همراه با سایه بازگشتی"
        "rsi_div" -> "خروج از ناحیه اشباع فروش با کف‌های بالاتر"
        "choch" -> "شکست آخرین سقف نزولی در تایم‌فریم ۱۵ دقیقه"
        "vol_expansion" -> "تزریق نقدینگی قدرتمند خریداران سازمانی"
        else -> item.description
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (item.isPassed) Emerald500.copy(alpha = 0.25f) else Color(0x18FFFFFF),
                    CircleShape
                )
                .border(
                    1.dp,
                    if (item.isPassed) Emerald400 else Color(0x30FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isPassed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Passed",
                    tint = Emerald400,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isFa) faTitle else item.title,
                    color = if (item.isPassed) Slate100 else Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = VazirmatnFontFamily
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0x18FFFFFF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("+${item.weight}%", color = Cyan400, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isFa) faDesc else item.description,
                color = Slate400,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontFamily = VazirmatnFontFamily
            )
        }
    }
}

@Composable
fun FrostedOrderBookCard(
    nobitexOrderBook: NobitexOrderBook?,
    isLoading: Boolean,
    isFa: Boolean,
    onExecuteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        accentGlow = Amber400
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isLoading) Amber400 else Emerald400, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFa) "دفتر سفارشات زنده نوبیتکس (عمق بازار)" else "NOBITEX LIVE ORDER BOOK DEPTH",
                        color = Slate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                val activeSymbol = nobitexOrderBook?.symbol ?: "USDT/IRT"
                Box(
                    modifier = Modifier
                        .background(Amber400.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, Amber400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (activeSymbol.contains("IRT")) activeSymbol else "$activeSymbol / IRT",
                        color = Amber400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (nobitexOrderBook == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Amber400, modifier = Modifier.size(28.dp))
                }
            } else {
                val book = nobitexOrderBook

                // Summary Row: Best Bid, Spread, Best Ask
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Best Bid Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Emerald500.copy(alpha = 0.12f))
                            .border(1.dp, Emerald500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                if (isFa) "بهترین خرید (Bid)" else "Best Bid",
                                color = Emerald400,
                                fontSize = 10.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                String.format(Locale.US, "%,.0f ت", book.bestBid),
                                color = Slate100,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Spread Box
                    val spreadPct = if (book.lastTradePrice > 0) (book.spread / book.lastTradePrice) * 100 else 0.0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FrostedGlassSurface)
                            .border(1.dp, Cyan400.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                if (isFa) "اسپرد (اختلاف)" else "Spread",
                                color = Cyan400,
                                fontSize = 10.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${String.format(Locale.US, "%,.0f", book.spread)} ت (${String.format(Locale.US, "%.2f", spreadPct)}%)",
                                color = Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Best Ask Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Rose500.copy(alpha = 0.12f))
                            .border(1.dp, Rose500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                if (isFa) "بهترین فروش (Ask)" else "Best Ask",
                                color = Rose400,
                                fontSize = 10.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                String.format(Locale.US, "%,.0f ت", book.bestAsk),
                                color = Slate100,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 24h Stats Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x15FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${if (isFa) "سقف ۲۴س: " else "24h High: "}${String.format(Locale.US, "%,.0f", book.dayHigh)}",
                        color = Emerald400,
                        fontSize = 10.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                    Text(
                        "${if (isFa) "کف ۲۴س: " else "24h Low: "}${String.format(Locale.US, "%,.0f", book.dayLow)}",
                        color = Rose400,
                        fontSize = 10.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                    Text(
                        "${if (isFa) "حجم: " else "Vol: "}${String.format(Locale.US, "%,.0f USDT", book.volume24h)}",
                        color = Slate300,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                HorizontalDivider(color = Color(0x18FFFFFF))

                // Depth Ladder Table (Dual Columns: Bids vs Asks)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bids Column (Left)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isFa) "خرید (تومان)" else "Bid (Toman)", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                            Text(if (isFa) "حجم" else "Vol", color = Slate400, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                        }

                        book.bids.take(5).forEach { entry ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x0EFFFFFF))
                                    .height(26.dp)
                            ) {
                                // Proportional depth bar fill
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(entry.depthPercent)
                                        .align(Alignment.CenterStart)
                                        .background(Emerald500.copy(alpha = 0.22f))
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        String.format(Locale.US, "%,.0f", entry.price),
                                        color = Emerald400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        String.format(Locale.US, "%,.0f", entry.volume),
                                        color = Slate300,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Asks Column (Right)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isFa) "فروش (تومان)" else "Ask (Toman)", color = Rose400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                            Text(if (isFa) "حجم" else "Vol", color = Slate400, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                        }

                        book.asks.take(5).forEach { entry ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x0EFFFFFF))
                                    .height(26.dp)
                            ) {
                                // Proportional depth bar fill
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(entry.depthPercent)
                                        .align(Alignment.CenterEnd)
                                        .background(Rose500.copy(alpha = 0.22f))
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        String.format(Locale.US, "%,.0f", entry.price),
                                        color = Rose400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        String.format(Locale.US, "%,.0f", entry.volume),
                                        color = Slate300,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Button: Instant Toman Trade
                Button(
                    onClick = onExecuteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("execute_toman_order_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber400.copy(alpha = 0.9f),
                        contentColor = DarkCanvasBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Execute Toman Trade",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFa) "ثبت آزمایشی آنی تتر/تومان (شبیه‌سازی)" else "Simulate Instant USDT/IRT Order",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }
    }
}

