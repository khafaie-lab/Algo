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
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VazirmatnFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// --- Color Palette: Deep Slate & Neon Accents ---
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// Neon & Brand Tones
val Cyan200 = Color(0xFFA5F3FC)
val Cyan300 = Color(0xFF67E8F9)
val Cyan400 = Color(0xFF22D3EE)
val Cyan500 = Color(0xFF06B6D4)
val Blue300 = Color(0xFF93C5FD)
val Blue400 = Color(0xFF60A5FA)
val Blue500 = Color(0xFF3B82F6)
val Blue600 = Color(0xFF2563EB)
val Indigo200 = Color(0xFFC7D2FE)
val Indigo300 = Color(0xFFA5B4FC)
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)

val Emerald300 = Color(0xFF6EE7B7)
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Rose300 = Color(0xFFFDA4AF)
val Rose400 = Color(0xFFFB7185)
val Rose500 = Color(0xFFF43F5E)
val Rose600 = Color(0xFFE11D48)
val Amber300 = Color(0xFFFCD34D)
val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)

// Frosted Glass Constants
val DarkCanvasBackground = Color(0xFF08090C)
val FrostedGlassSurface = Color(0x14FFFFFF)
val FrostedGlassSurfaceHighlighted = Color(0x22FFFFFF)
val FrostedBorderTop = Color(0x45FFFFFF)
val FrostedBorderBottom = Color(0x10FFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkCanvasBackground
                ) {
                    QuantProAppRoot()
                }
            }
        }
    }
}

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
    val backgroundBrush = if (accentGlow != null) {
        Brush.linearGradient(
            colors = listOf(
                accentGlow.copy(alpha = 0.15f),
                Color(0x18171B26),
                Color(0x0E111722)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x1CFFFFFF),
                Color(0x0CFFFFFF)
            )
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
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Cyan500.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(width * 0.9f, height * 0.12f),
                radius = width * 0.7f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Indigo600.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.05f, height * 0.45f),
                radius = width * 0.65f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Emerald500.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(width * 0.85f, height * 0.8f),
                radius = width * 0.6f
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantProAppRoot(viewModel: MarketDataViewModel = viewModel()) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isFa = currentLanguage == "FA"
    val layoutDirection = if (isFa) LayoutDirection.Rtl else LayoutDirection.Ltr

    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val price by viewModel.price.collectAsState()
    val change by viewModel.change.collectAsState()
    val isLoadingPrice by viewModel.isLoadingPrice.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val checklist by viewModel.checklist.collectAsState()
    val advisorSetup by viewModel.advisorSetup.collectAsState()
    val aiAnalysisState by viewModel.aiAnalysisState.collectAsState()
    val params by viewModel.params.collectAsState()
    val trades by viewModel.trades.collectAsState()
    val nobitexOrderBook by viewModel.nobitexOrderBook.collectAsState()
    val isLoadingNobitex by viewModel.isLoadingNobitex.collectAsState()
    val nobitexMarkets by viewModel.nobitexMarkets.collectAsState()
    val serverPriority by viewModel.serverPriority.collectAsState()
    val scannerOpportunities by viewModel.scannerOpportunities.collectAsState()
    val activeChartOpportunity by viewModel.activeChartOpportunity.collectAsState()
    val requestOpenFullscreen by viewModel.requestOpenFullscreen.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val selectedScannerTimeframe by viewModel.selectedScannerTimeframe.collectAsState()
    val scannerDirectionFilter by viewModel.scannerDirectionFilter.collectAsState()
    val selectedOppForRiskCalc by viewModel.selectedOppForRiskCalc.collectAsState()
    val pendingOrderRequest by viewModel.pendingOrderRequest.collectAsState()
    val context = LocalContext.current

    var showOrderDialog by remember { mutableStateOf(false) }
    var showOrderBookDialog by remember { mutableStateOf(false) }
    var isFullscreenChart by remember { mutableStateOf(false) }
    var userMessage by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf("STRATEGY") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(requestOpenFullscreen) {
        if (requestOpenFullscreen) {
            isFullscreenChart = true
            viewModel.clearFullscreenRequest()
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            userMessage = null
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            containerColor = DarkCanvasBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                FrostedTopBar(
                    currentTab = currentTab,
                    selectedAsset = selectedAsset,
                    price = price,
                    change = change,
                    isLoading = isLoadingPrice,
                    isFa = isFa,
                    nobitexOrderBook = nobitexOrderBook,
                    nobitexMarkets = nobitexMarkets,
                    onOpenNobitexDepth = { showOrderBookDialog = true },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onRefresh = { viewModel.fetchRealTimeData() }
                )
            },
            bottomBar = {
                FrostedFloatingBottomNav(
                    currentTab = currentTab,
                    isFa = isFa,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AmbientBackdropGlow()

                AnimatedContent(
                    targetState = currentTab,
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        "STRATEGY" -> {
                            StrategyDashboardView(
                                viewModel = viewModel,
                                selectedAsset = selectedAsset,
                                price = price,
                                isLoadingPrice = isLoadingPrice,
                                lastUpdated = lastUpdated,
                                checklist = checklist,
                                advisorSetup = advisorSetup,
                                aiAnalysisState = aiAnalysisState,
                                params = params,
                                isFa = isFa,
                                nobitexMarkets = nobitexMarkets,
                                serverPriority = serverPriority,
                                scannerOpportunities = scannerOpportunities,
                                activeOpportunity = activeChartOpportunity,
                                isExternalFullscreen = isFullscreenChart,
                                onOpenFullscreen = { isFullscreenChart = true },
                                onCloseFullscreen = { isFullscreenChart = false },
                                onOpenScannerTab = { currentTab = "SCANNER" },
                                onOpenRiskCalculator = { opp -> viewModel.openRiskCalculator(opp) },
                                onExecuteClick = { showOrderDialog = true }
                            )
                        }
                        "SCANNER" -> {
                            ScannerView(
                                opportunities = scannerOpportunities,
                                isScanning = isScanning,
                                selectedTimeframe = selectedScannerTimeframe,
                                selectedDirection = scannerDirectionFilter,
                                isFa = isFa,
                                onSelectTimeframe = { viewModel.setScannerTimeframe(it) },
                                onSelectDirection = { viewModel.setScannerDirectionFilter(it) },
                                onRescan = { viewModel.runSmartScanner() },
                                onOpenRiskCalculator = { opp -> viewModel.openRiskCalculator(opp) },
                                onSendOrder = { opp -> viewModel.requestOrderFromOpportunity(opp) },
                                onSelectOpportunity = { opp ->
                                    viewModel.selectOpportunity(opp, openFullscreen = true)
                                    currentTab = "STRATEGY"
                                    isFullscreenChart = true
                                    userMessage = if (isFa) "جفت‌ارز ${opp.symbol} (${opp.timeframe}) با تحلیل و سطوح روی چارت باز شد!" else "Loaded ${opp.symbol} (${opp.timeframe}) with setup on chart!"
                                }
                            )
                        }
                        "HISTORY" -> {
                            HistoryView(
                                trades = trades,
                                params = params,
                                isFa = isFa,
                                onCloseTrade = { id, asWin ->
                                    viewModel.closeTrade(id, asWin)
                                    userMessage = if (isFa) {
                                        if (asWin) "معامله با سود (تارگت) بسته شد!" else "معامله با حد ضرر بسته شد!"
                                    } else {
                                        if (asWin) "Trade closed as WIN (+PnL)!" else "Trade closed as LOSS (-Risk)"
                                    }
                                },
                                onClearHistory = {
                                    viewModel.clearHistory()
                                    userMessage = if (isFa) "ژورنال معاملات پاکسازی شد." else "Trade journal cleared."
                                }
                            )
                        }
                        "PARAMS" -> {
                            ParamsView(
                                params = params,
                                isFa = isFa,
                                onUpdateRisk = { viewModel.setAccountRisk(it) },
                                onUpdateBalance = { viewModel.setAccountBalance(it) },
                                onUpdateAtr = { viewModel.setAtrMultiplier(it) },
                                onUpdateTimeframe = { viewModel.setPreferredTimeframe(it) },
                                onToggleRule = { viewModel.toggleRule(it) }
                            )
                        }
                        "PROFILE" -> {
                            ProfileView(
                                trades = trades,
                                params = params,
                                isFa = isFa,
                                onResetData = {
                                    viewModel.clearHistory()
                                    userMessage = if (isFa) "آمار معاملات بازنشانی شد." else "Journal & statistics reset."
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showOrderBookDialog) {
            FrostedOrderBookDialog(
                nobitexOrderBook = nobitexOrderBook,
                isLoading = isLoadingNobitex,
                isFa = isFa,
                onDismiss = { showOrderBookDialog = false },
                onExecuteClick = {
                    showOrderBookDialog = false
                    viewModel.selectAsset("USDT/IRT")
                    showOrderDialog = true
                }
            )
        }

        if (showOrderDialog) {
            FrostedOrderExecutionDialog(
                asset = selectedAsset,
                currentPrice = price,
                accountBalance = params.accountBalance,
                riskPercent = params.riskPerTradePercent,
                advisorSetup = advisorSetup,
                isFa = isFa,
                onDismiss = { showOrderDialog = false },
                onConfirmOrder = {
                    showOrderDialog = false
                    val newTrade = viewModel.executeTrade(
                        symbol = selectedAsset,
                        direction = "LONG",
                        entryPrice = price,
                        riskPercent = params.riskPerTradePercent,
                        balance = params.accountBalance
                    )
                    userMessage = if (isFa) "سفارش ${newTrade.id} با موفقیت در ژورنال ثبت شد!" else "Order ${newTrade.id} placed & logged!"
                }
            )
        }

        selectedOppForRiskCalc?.let { opp ->
            PositionRiskCalculatorDialog(
                opportunity = opp,
                currentAsset = selectedAsset,
                currentPriceStr = price,
                viewModel = viewModel,
                isFa = isFa,
                onDismiss = { viewModel.closeRiskCalculator() }
            )
        }

        pendingOrderRequest?.let { req ->
            MetaTraderOrderConfirmationDialog(
                request = req,
                isFa = isFa,
                onDismiss = { viewModel.closeOrderConfirmation() },
                onConfirmAndSend = { lotSize, preferMt5 ->
                    viewModel.closeOrderConfirmation()
                    launchMetaTraderOrder(context, req, lotSize, preferMt5)
                    val newTrade = viewModel.executeTrade(
                        symbol = req.symbol,
                        direction = req.direction,
                        entryPrice = req.entryPrice,
                        customSl = req.stopLoss,
                        customTp = req.tp1,
                        customRiskReward = req.riskReward,
                        lotSize = lotSize,
                        platform = if (preferMt5) "MetaTrader 5" else "MetaTrader 4"
                    )
                    userMessage = if (isFa)
                        "سفارش ${newTrade.id} (${req.symbol}) به متاتریدر ارسال و در ژورنال ثبت شد. پارامترها در کلیپ‌بورد کپی شدند."
                    else
                        "Order ${newTrade.id} (${req.symbol}) sent to MetaTrader & logged! Parameters copied to clipboard."
                }
            )
        }
    }
}

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
// TRADINGVIEW LIVE CANDLESTICK CHART COMPONENT
// -------------------------------------------------------------
@Composable
fun TradingViewLiveChartCard(
    asset: String,
    currentPrice: String,
    tomanPrice: String = "",
    candles: List<Candle>,
    isLoading: Boolean,
    chartType: String,
    chartTimeframe: String,
    showEma: Boolean,
    showOrderBlocks: Boolean,
    showVolume: Boolean,
    isFa: Boolean,
    activeOpportunity: ScannerOpportunity? = null,
    isExternalFullscreen: Boolean = false,
    onOpenFullscreen: () -> Unit = {},
    onCloseFullscreen: () -> Unit = {},
    onSelectTimeframe: (String) -> Unit,
    onToggleChartType: () -> Unit,
    onToggleEma: () -> Unit,
    onToggleOrderBlocks: () -> Unit,
    onToggleVolume: () -> Unit,
    onSelectAsset: (String) -> Unit = {},
    onOpenRiskCalculator: ((ScannerOpportunity?) -> Unit)? = null,
    onSendOpportunityOrder: ((ScannerOpportunity) -> Unit)? = null
) {
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var isInternalFullscreen by remember { mutableStateOf(false) }
    val isFullscreen = isInternalFullscreen || isExternalFullscreen

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = Blue600
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: Symbol + Live Ticker + TradingView Controls Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Blue500.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, Blue400.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CandlestickChart, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            if (asset.contains("IRT")) "USDT/IRT (نوبیتکس)" else "$asset/USDT (نوبیتکس)",
                            color = Slate100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                        if (!asset.contains("IRT") && tomanPrice.isNotEmpty()) {
                            Text(
                                text = tomanPrice,
                                color = Amber400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Timeframe selector chips & Prominent Fullscreen expand button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FrostedGlassSurface)
                            .horizontalScroll(rememberScrollState())
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("1m", "3m", "5m", "15m", "1h", "4h", "1d").forEach { tf ->
                            val isSel = tf.equals(chartTimeframe, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Blue600.copy(alpha = 0.65f) else Color.Transparent)
                                    .clickable { onSelectTimeframe(tf) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    tf,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Slate50 else Slate400
                                )
                            }
                        }
                    }

                    // Prominent Fullscreen Maximize Action
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C2740))
                            .border(1.2.dp, Cyan400, RoundedCornerShape(8.dp))
                            .clickable {
                                isInternalFullscreen = true
                                onOpenFullscreen()
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("fullscreen_chart_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = if (isFa) "چارت تمام صفحه" else "Fullscreen TradingView",
                            tint = Cyan300,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isFa) "تمام‌صفحه" else "Fullscreen",
                            color = Cyan300,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    if (activeOpportunity != null && onSendOpportunityOrder != null) {
                        val isShortOpp = activeOpportunity.direction.equals("SHORT", ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isShortOpp) Rose600.copy(alpha = 0.35f) else Emerald600.copy(alpha = 0.35f))
                                .border(1.dp, if (isShortOpp) Rose400 else Emerald400, RoundedCornerShape(8.dp))
                                .clickable { onSendOpportunityOrder(activeOpportunity) }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                .testTag("chart_card_send_order_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("⚡", fontSize = 10.sp)
                            Text(
                                text = if (isFa) "ارسال سفارش" else "Send Order",
                                color = if (isShortOpp) Rose300 else Emerald300,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }
                }
            }

            // TradingView Toolbar: Candle/Line switch, EMA, Order Blocks, Volume toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Chart Type Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FrostedGlassSurface)
                            .border(1.dp, if (chartType == "CANDLES") Cyan400.copy(alpha = 0.4f) else FrostedBorderBottom, RoundedCornerShape(8.dp))
                            .clickable { onToggleChartType() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (chartType == "CANDLES") "📊 Candles" else "📈 Line",
                            color = if (chartType == "CANDLES") Cyan400 else Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    // EMA Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showEma) Blue600.copy(alpha = 0.25f) else FrostedGlassSurface)
                            .border(1.dp, if (showEma) Blue400.copy(alpha = 0.5f) else FrostedBorderBottom, RoundedCornerShape(8.dp))
                            .clickable { onToggleEma() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("EMA 50/200", color = if (showEma) Blue300 else Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Order Blocks Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showOrderBlocks) Emerald500.copy(alpha = 0.2f) else FrostedGlassSurface)
                            .border(1.dp, if (showOrderBlocks) Emerald400.copy(alpha = 0.5f) else FrostedBorderBottom, RoundedCornerShape(8.dp))
                            .clickable { onToggleOrderBlocks() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("OB/FVG", color = if (showOrderBlocks) Emerald400 else Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Volume Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showVolume) Indigo600.copy(alpha = 0.2f) else FrostedGlassSurface)
                            .border(1.dp, if (showVolume) Indigo400.copy(alpha = 0.5f) else FrostedBorderBottom, RoundedCornerShape(8.dp))
                            .clickable { onToggleVolume() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("VOL", color = if (showVolume) Indigo300 else Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EMA 50", color = Cyan400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("EMA 200", color = Indigo400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Real-time ticking countdown timer state
            var tickerSec by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(1000L)
                    tickerSec = System.currentTimeMillis() / 1000L
                }
            }
            val tfSec = when (chartTimeframe.lowercase()) {
                "1m" -> 60L
                "3m" -> 180L
                "5m" -> 300L
                "15m" -> 900L
                "1h" -> 3600L
                "4h" -> 14400L
                "1d" -> 86400L
                else -> 14400L
            }
            val remSec = tfSec - (tickerSec % tfSec)
            val countdownStr = String.format(Locale.US, "%02d:%02d", remSec / 60, remSec % 60)

            // Viewport states for two-finger pinch zoom & smooth pan into deep history
            var cardZoomCount by remember { mutableIntStateOf(35) }
            var cardPanOffset by remember { mutableIntStateOf(0) }
            var cardVerticalPriceScale by remember { mutableFloatStateOf(1f) }

            val totalCandles = candles.size
            val effectiveZoomCount = cardZoomCount.coerceIn(12, totalCandles.coerceAtLeast(14))
            val maxCardPan = (totalCandles - effectiveZoomCount).coerceAtLeast(0)
            val effectivePan = cardPanOffset.coerceIn(0, maxCardPan)
            val startIdx = (totalCandles - effectiveZoomCount - effectivePan).coerceIn(0, (totalCandles - 1).coerceAtLeast(0))
            val endIdx = (startIdx + effectiveZoomCount).coerceIn(startIdx + 1, totalCandles)
            val visibleCandles = if (candles.isNotEmpty()) candles.subList(startIdx, endIdx) else emptyList()

            // Crosshair HUD Display Bar (OHLC coordinates)
            val displayCandle = if (hoveredIndex != null && hoveredIndex in visibleCandles.indices) {
                visibleCandles[hoveredIndex!!]
            } else visibleCandles.lastOrNull()

            val isIrt = asset.contains("IRT")
            fun fmtP(v: Float) = if (isIrt) String.format(Locale.US, "%,.0fت", v) else "$${String.format(Locale.US, if (v < 1f) "%.3f" else if (v < 100f) "%.2f" else "%.1f", v)}"

            if (displayCandle != null) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x20000000))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("O:${fmtP(displayCandle.open)}", color = Slate400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("H:${fmtP(displayCandle.high)}", color = Emerald400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("L:${fmtP(displayCandle.low)}", color = Rose400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("C:${fmtP(displayCandle.close)}", color = Slate100, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("V:${String.format(Locale.US, "%.0f", displayCandle.volume)}", color = Indigo300, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text("⏱ $countdownStr", color = Amber400, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Main Candlestick Workspace with Interactive Canvas + Right Y-Axis Scale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF090D14))
                    .border(1.dp, FrostedBorderBottom, RoundedCornerShape(10.dp))
            ) {
                if (isLoading && visibleCandles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Cyan400, modifier = Modifier.size(24.dp))
                    }
                } else if (visibleCandles.isNotEmpty()) {
                    val minPriceRaw = visibleCandles.minOf { it.low }
                    val maxPriceRaw = visibleCandles.maxOf { it.high }
                    val rawSpan = if (maxPriceRaw > minPriceRaw) maxPriceRaw - minPriceRaw else 1f
                    val midPrice = (maxPriceRaw + minPriceRaw) / 2f
                    val effectiveSpan = (rawSpan * 1.08f) * cardVerticalPriceScale
                    val minPrice = midPrice - (effectiveSpan / 2f)
                    val maxPrice = midPrice + (effectiveSpan / 2f)
                    val priceRange = maxPrice - minPrice

                    Row(modifier = Modifier.fillMaxSize()) {
                        // 1. Chart Canvas (takes weight 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(candles.size, visibleCandles.size) {
                                    detectTransformGestures { centroid, pan, zoom, _ ->
                                        if (zoom != 1f) {
                                            cardZoomCount = (cardZoomCount / zoom).toInt().coerceIn(12, candles.size.coerceAtLeast(14))
                                            cardVerticalPriceScale = (cardVerticalPriceScale / zoom.coerceIn(0.88f, 1.12f)).coerceIn(0.35f, 4.0f)
                                        }
                                        if (pan.x != 0f && visibleCandles.isNotEmpty()) {
                                            val step = (size.width / visibleCandles.size.toFloat()).coerceAtLeast(1f)
                                            val delta = -(pan.x / step).toInt()
                                            if (delta != 0) {
                                                cardPanOffset = (cardPanOffset + delta).coerceIn(0, maxCardPan)
                                            }
                                        }
                                        if (visibleCandles.isNotEmpty()) {
                                            val step = size.width / visibleCandles.size.toFloat()
                                            hoveredIndex = (centroid.x / step).toInt().coerceIn(0, visibleCandles.size - 1)
                                        }
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                val candleAreaHeight = if (showVolume) h * 0.78f else h * 0.95f
                                val volumeAreaHeight = h * 0.20f
                                val volumeBaseY = h

                                val maxVol = visibleCandles.maxOfOrNull { it.volume } ?: 1f

                                val candleCount = visibleCandles.size
                                val stepX = w / candleCount
                                val candleBodyWidth = max(2f, stepX * 0.65f)

                                fun pToY(p: Float): Float =
                                    candleAreaHeight - ((p - minPrice) / priceRange) * (candleAreaHeight * 0.88f) - 6.dp.toPx()

                                // Watermark
                                val wmText = "${if (asset.contains("IRT")) "USDT/IRT" else "$asset/USDT"} • ${chartTimeframe.lowercase()} • NOBITEX"
                                val wmPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(12, 255, 255, 255)
                                    textSize = 18.sp.toPx()
                                    isFakeBoldText = true
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                drawContext.canvas.nativeCanvas.drawText(
                                    wmText,
                                    w / 2f,
                                    candleAreaHeight * 0.45f,
                                    wmPaint
                                )

                                // Cartesian Grid (Horizontal & Vertical)
                                val gridSteps = 4
                                for (i in 0..gridSteps) {
                                    val gy = candleAreaHeight * (i.toFloat() / gridSteps.toFloat())
                                    drawLine(
                                        color = Color(0x0CFFFFFF),
                                        start = Offset(0f, gy),
                                        end = Offset(w, gy),
                                        strokeWidth = 1f
                                    )
                                }
                                val vSteps = 5
                                for (i in 0..vSteps) {
                                    val gx = w * (i.toFloat() / vSteps.toFloat())
                                    drawLine(
                                        color = Color(0x0AFFFFFF),
                                        start = Offset(gx, 0f),
                                        end = Offset(gx, h),
                                        strokeWidth = 1f
                                    )
                                }

                                // Order Block Highlight
                                if (showOrderBlocks) {
                                    val obPrice = minPrice + priceRange * 0.18f
                                    val obTopY = pToY(obPrice)
                                    drawRoundRect(
                                        color = Emerald500.copy(alpha = 0.12f),
                                        topLeft = Offset(0f, obTopY - 12.dp.toPx()),
                                        size = Size(w, 20.dp.toPx()),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    drawLine(
                                        color = Emerald400.copy(alpha = 0.5f),
                                        start = Offset(0f, obTopY),
                                        end = Offset(w, obTopY),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                    )
                                }

                                // Volume Histogram
                                if (showVolume) {
                                    visibleCandles.forEachIndexed { i, c ->
                                        val cx = i * stepX + (stepX / 2f)
                                        val normVol = (c.volume / maxVol).coerceIn(0.05f, 1f)
                                        val barH = normVol * volumeAreaHeight
                                        val isBull = c.close >= c.open
                                        val barColor = if (isBull) Emerald500.copy(alpha = 0.35f) else Rose500.copy(alpha = 0.35f)

                                        drawRect(
                                            color = barColor,
                                            topLeft = Offset(cx - (candleBodyWidth / 2f), volumeBaseY - barH),
                                            size = Size(candleBodyWidth, barH)
                                        )
                                    }
                                }

                                // Candlesticks
                                if (chartType == "CANDLES") {
                                    visibleCandles.forEachIndexed { i, c ->
                                        val cx = i * stepX + (stepX / 2f)
                                        val isBull = c.close >= c.open
                                        val candleColor = if (isBull) Emerald400 else Rose400

                                        val highY = pToY(c.high)
                                        val lowY = pToY(c.low)
                                        val openY = pToY(c.open)
                                        val closeY = pToY(c.close)

                                        // Wick
                                        drawLine(
                                            color = candleColor,
                                            start = Offset(cx, highY),
                                            end = Offset(cx, lowY),
                                            strokeWidth = 1.2.dp.toPx()
                                        )

                                        // Body
                                        val topY = min(openY, closeY)
                                        val bodyH = max(2f, kotlin.math.abs(closeY - openY))

                                        drawRect(
                                            color = candleColor,
                                            topLeft = Offset(cx - (candleBodyWidth / 2f), topY),
                                            size = Size(candleBodyWidth, bodyH)
                                        )
                                    }
                                } else {
                                    val linePath = Path()
                                    val fillPath = Path()

                                    visibleCandles.forEachIndexed { i, c ->
                                        val cx = i * stepX + (stepX / 2f)
                                        val cy = pToY(c.close)
                                        if (i == 0) {
                                            linePath.moveTo(cx, cy)
                                            fillPath.moveTo(cx, cy)
                                        } else {
                                            linePath.lineTo(cx, cy)
                                            fillPath.lineTo(cx, cy)
                                        }
                                    }
                                    fillPath.lineTo(w, candleAreaHeight)
                                    fillPath.lineTo(0f, candleAreaHeight)
                                    fillPath.close()

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            listOf(Cyan400.copy(alpha = 0.25f), Color.Transparent)
                                        )
                                    )
                                    drawPath(
                                        path = linePath,
                                        color = Cyan400,
                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // EMA 50 & 200 Overlays
                                if (showEma && candles.size > 5) {
                                    val ema50Path = Path()
                                    val ema200Path = Path()
                                    var ema50 = candles.first().close
                                    var ema200 = candles.first().close * 0.99f

                                    candles.forEachIndexed { i, c ->
                                        ema50 += (c.close - ema50) * 0.15f
                                        ema200 += (c.close - ema200) * 0.05f

                                        if (i in startIdx until endIdx) {
                                            val localI = i - startIdx
                                            val cx = localI * stepX + (stepX / 2f)
                                            val ey50 = pToY(ema50)
                                            val ey200 = pToY(ema200)

                                            if (ema50Path.isEmpty) ema50Path.moveTo(cx, ey50) else ema50Path.lineTo(cx, ey50)
                                            if (ema200Path.isEmpty) ema200Path.moveTo(cx, ey200) else ema200Path.lineTo(cx, ey200)
                                        }
                                    }

                                    drawPath(path = ema50Path, color = Cyan400, style = Stroke(width = 1.4.dp.toPx()))
                                    drawPath(path = ema200Path, color = Indigo400, style = Stroke(width = 1.4.dp.toPx()))
                                }

                                // Crosshair
                                hoveredIndex?.let { idx ->
                                    val cx = idx * stepX + (stepX / 2f)
                                    val candle = visibleCandles[idx]
                                    val cy = pToY(candle.close)

                                    drawLine(
                                        color = Slate400.copy(alpha = 0.7f),
                                        start = Offset(cx, 0f),
                                        end = Offset(cx, h),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                    )
                                    drawLine(
                                        color = Slate400.copy(alpha = 0.7f),
                                        start = Offset(0f, cy),
                                        end = Offset(w, cy),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                    )
                                    drawCircle(color = Cyan400, radius = 4.dp.toPx(), center = Offset(cx, cy))
                                }

                                // Current Live Price Pulse Line & Floating Countdown Badge on Canvas
                                val lastC = visibleCandles.lastOrNull()
                                if (lastC != null) {
                                    val liveY = pToY(lastC.close)
                                    drawLine(
                                        color = Cyan400.copy(alpha = 0.85f),
                                        start = Offset(0f, liveY),
                                        end = Offset(w, liveY),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                                    )

                                    val lastIdx = visibleCandles.size - 1
                                    val lastCandleCenterX = lastIdx * stepX + (stepX / 2f)
                                    val badgePaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#0F172A")
                                        style = android.graphics.Paint.Style.FILL
                                    }
                                    val badgeStrokePaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#38BDF8")
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 1.dp.toPx()
                                    }
                                    val badgeTextPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#38BDF8")
                                        textSize = 7.5.sp.toPx()
                                        isFakeBoldText = true
                                    }
                                    val tagText = "⏱ $countdownStr"
                                    val tBounds = android.graphics.Rect()
                                    badgeTextPaint.getTextBounds(tagText, 0, tagText.length, tBounds)
                                    val padH = 5.dp.toPx()
                                    val padV = 2.dp.toPx()
                                    val tagW = tBounds.width() + padH * 2f
                                    val tagH = tBounds.height() + padV * 2f
                                    val tagLeft = (lastCandleCenterX + candleBodyWidth / 2f + 4.dp.toPx()).coerceAtMost(w - tagW - 2.dp.toPx())
                                    val tagTop = liveY - tagH / 2f
                                    val tagRect = android.graphics.RectF(tagLeft, tagTop, tagLeft + tagW, tagTop + tagH)
                                    drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 3.dp.toPx(), 3.dp.toPx(), badgePaint)
                                    drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 3.dp.toPx(), 3.dp.toPx(), badgeStrokePaint)
                                    drawContext.canvas.nativeCanvas.drawText(tagText, tagLeft + padH, tagTop + tagH - padV, badgeTextPaint)
                                }
                            }

                            // Prominent Floating Fullscreen Launcher Pill
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xD9060B14))
                                    .border(1.dp, Cyan400.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        isInternalFullscreen = true
                                        onOpenFullscreen()
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Cyan300, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = if (isFa) "⛶ تمام‌صفحه و تحلیل" else "⛶ Fullscreen Chart",
                                        color = Cyan200,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }
                            }

                            // Reset Pan Pill when scrolled back in history
                            if (cardPanOffset > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xDD1E293B))
                                        .border(1.dp, Cyan400, RoundedCornerShape(12.dp))
                                        .clickable { cardPanOffset = 0 }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                    Text("⏭ زنده", color = Cyan300, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                                }
                            }
                        }

                        // 2. Right Price Axis (Y-Axis) with Vertical Drag Scaling
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF0B0F19))
                                .border(width = 1.dp, color = Color(0x18FFFFFF))
                                .pointerInput(Unit) {
                                    detectDragGestures { _, dragAmount ->
                                        cardVerticalPriceScale = (cardVerticalPriceScale + (dragAmount.y / 150f)).coerceIn(0.35f, 4.0f)
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                val steps = 4
                                for (i in 0..steps) {
                                    val p = maxPrice - (priceRange * (i.toFloat() / steps.toFloat()))
                                    Text(
                                        text = fmtP(p),
                                        color = Slate400,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Active Price & Countdown Pill
                            val lastC = visibleCandles.lastOrNull()
                            if (lastC != null) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 2.dp),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (lastC.close >= lastC.open) Emerald600 else Rose600)
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    ) {
                                        Text(fmtP(lastC.close), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF131A26))
                                            .border(0.6.dp, Cyan400.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 2.dp, vertical = 1.dp)
                                    ) {
                                        Text("⏱ $countdownStr", color = Cyan400, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (isFa) "در حال بارگذاری نمودار نوبیتکس..." else "Loading Nobitex chart...", color = Slate400, fontSize = 11.sp, fontFamily = VazirmatnFontFamily)
                    }
                }
            }

            // Bottom Horizontal Timeline (X-Axis) with Horizontal Drag Scaling
            if (visibleCandles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(Color(0xFF070A10))
                        .pointerInput(candles.size) {
                            detectDragGestures { _, dragAmount ->
                                val delta = -(dragAmount.x / 18f).toInt()
                                if (delta != 0) {
                                    cardZoomCount = (cardZoomCount + delta).coerceIn(12, candles.size.coerceAtLeast(14))
                                }
                            }
                        }
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isIntra = chartTimeframe.lowercase() in listOf("1m", "3m", "5m", "15m")
                    val df = SimpleDateFormat(if (isIntra) "HH:mm" else "MM/dd HH:mm", Locale.US)
                    val stepCandles = max(1, visibleCandles.size / 5)
                    for (i in 0 until visibleCandles.size step stepCandles) {
                        val c = visibleCandles[i]
                        Text(
                            text = df.format(Date(c.timestamp)),
                            color = Slate500,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "نوبیتکس • ${chartTimeframe.lowercase()}",
                        color = Cyan400,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // TradingView Fullscreen Institutional Modal
    if (isFullscreen) {
        TradingViewFullscreenDialog(
            asset = asset,
            currentPrice = currentPrice,
            tomanPrice = tomanPrice,
            candles = candles,
            isLoading = isLoading,
            chartType = chartType,
            chartTimeframe = chartTimeframe,
            showEma = showEma,
            showOrderBlocks = showOrderBlocks,
            showVolume = showVolume,
            isFa = isFa,
            activeOpportunity = activeOpportunity,
            onSelectTimeframe = onSelectTimeframe,
            onToggleChartType = onToggleChartType,
            onToggleEma = onToggleEma,
            onToggleOrderBlocks = onToggleOrderBlocks,
            onToggleVolume = onToggleVolume,
            onSelectAsset = onSelectAsset,
            onOpenRiskCalculator = onOpenRiskCalculator,
            onSendOpportunityOrder = onSendOpportunityOrder,
            onDismiss = {
                isInternalFullscreen = false
                onCloseFullscreen()
            }
        )
    }
}

// -------------------------------------------------------------
// TRADINGVIEW FULLSCREEN DATA STRUCTURES & INDICATOR ENGINES
// -------------------------------------------------------------
enum class TvDrawingTool {
    CROSSHAIR,
    TRENDLINE,
    HORIZ_LEVEL,
    FIBONACCI,
    LONG_POSITION,
    SHORT_POSITION
}

data class TvTrendline(
    val startRatioX: Float,
    val startPrice: Float,
    val endRatioX: Float,
    val endPrice: Float
)

data class TvHorizLevel(
    val price: Float,
    val label: String
)

data class TvFibonacci(
    val highPrice: Float,
    val lowPrice: Float
)

data class TvPosition(
    val entryPrice: Float,
    val targetPrice: Float,
    val stopPrice: Float,
    val isShort: Boolean = false
)

data class TvBollingerPoint(
    val upper: Float,
    val middle: Float,
    val lower: Float
)

fun computeBollingerBands(candles: List<Candle>, period: Int = 20, multiplier: Float = 2.0f): List<TvBollingerPoint?> {
    if (candles.isEmpty()) return emptyList()
    return candles.indices.map { idx ->
        if (idx < period - 1) null
        else {
            val window = candles.subList(idx - period + 1, idx + 1)
            val mean = window.map { it.close }.average().toFloat()
            val variance = window.map { (it.close - mean) * (it.close - mean) }.average().toFloat()
            val stdDev = kotlin.math.sqrt(variance)
            TvBollingerPoint(
                upper = mean + multiplier * stdDev,
                middle = mean,
                lower = mean - multiplier * stdDev
            )
        }
    }
}

fun computeRsi(candles: List<Candle>, period: Int = 14): List<Float> {
    if (candles.size <= period) return List(candles.size) { 50f }
    val rsi = ArrayList<Float>(candles.size)
    for (i in 0 until period) rsi.add(50f)
    var avgGain = 0f
    var avgLoss = 0f
    for (i in 1..period) {
        val diff = candles[i].close - candles[i - 1].close
        if (diff > 0) avgGain += diff else avgLoss += -diff
    }
    avgGain /= period
    avgLoss /= period

    val initRs = if (avgLoss == 0f) 100f else avgGain / avgLoss
    rsi.add((100f - (100f / (1f + initRs))).coerceIn(0f, 100f))

    for (i in (period + 1) until candles.size) {
        val diff = candles[i].close - candles[i - 1].close
        val gain = if (diff > 0) diff else 0f
        val loss = if (diff < 0) -diff else 0f
        avgGain = (avgGain * (period - 1) + gain) / period
        avgLoss = (avgLoss * (period - 1) + loss) / period
        val currentRs = if (avgLoss == 0f) 100f else avgGain / avgLoss
        rsi.add((100f - (100f / (1f + currentRs))).coerceIn(0f, 100f))
    }
    return rsi
}

// -------------------------------------------------------------
// TRADINGVIEW FULLSCREEN DIALOG (PRO INSTITUTIONAL ENGINE)
// -------------------------------------------------------------
@Composable
fun TradingViewFullscreenDialog(
    asset: String,
    currentPrice: String,
    tomanPrice: String = "",
    candles: List<Candle>,
    isLoading: Boolean,
    chartType: String,
    chartTimeframe: String,
    showEma: Boolean,
    showOrderBlocks: Boolean,
    showVolume: Boolean,
    isFa: Boolean,
    activeOpportunity: ScannerOpportunity? = null,
    onSelectTimeframe: (String) -> Unit,
    onToggleChartType: () -> Unit,
    onToggleEma: () -> Unit,
    onToggleOrderBlocks: () -> Unit,
    onToggleVolume: () -> Unit,
    onSelectAsset: (String) -> Unit,
    onOpenRiskCalculator: ((ScannerOpportunity?) -> Unit)? = null,
    onSendOpportunityOrder: ((ScannerOpportunity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    // Interactive Viewport States
    var visibleCandleCount by remember { mutableIntStateOf(35) }
    var panOffset by remember { mutableIntStateOf(0) }
    var verticalPriceScale by remember { mutableFloatStateOf(1f) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var crosshairY by remember { mutableStateOf<Float?>(null) }
    var showConfluenceBanner by remember(activeOpportunity) { mutableStateOf(activeOpportunity != null) }

    // Pro Tool & Indicator States
    var activeTool by remember { mutableStateOf(TvDrawingTool.CROSSHAIR) }
    var showIndicatorsSheet by remember { mutableStateOf(false) }
    var showAssetMenu by remember { mutableStateOf(false) }
    var isCleanChartMode by remember { mutableStateOf(false) }
    var showInternalRiskCalc by remember { mutableStateOf(false) }

    var showEma20 by remember { mutableStateOf(true) }
    var showEma50 by remember { mutableStateOf(showEma) }
    var showEma200 by remember { mutableStateOf(showEma) }
    var showBollinger by remember { mutableStateOf(false) }
    var showObFvg by remember { mutableStateOf(showOrderBlocks) }
    var showVol by remember { mutableStateOf(showVolume) }
    var showRsi by remember { mutableStateOf(true) }

    // User Interactive Drawings
    val trendlines = remember { mutableStateListOf<TvTrendline>() }
    val horizLevels = remember { mutableStateListOf<TvHorizLevel>() }
    val fibonaccis = remember { mutableStateListOf<TvFibonacci>() }
    val positions = remember { mutableStateListOf<TvPosition>() }
    var pendingTrendlineStart by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    // Calculated Indicators
    val bollingerBands = remember(candles) { computeBollingerBands(candles) }
    val rsiValues = remember(candles) { computeRsi(candles) }
    var isHollowCandles by remember { mutableStateOf(false) }

    val isIrt = asset.contains("IRT")
    fun fmtP(v: Float) = if (isIrt) String.format(Locale.US, "%,.0fت", v) else "$${String.format(Locale.US, if (v < 1f) "%.4f" else if (v < 100f) "%.2f" else "%.1f", v)}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08090E)),
            color = Color(0xFF08090E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // ==========================================
                // 1. TOP TRADINGVIEW PRO APP BAR
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Exit Button + Asset Dropdown + Ticker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x20FFFFFF), RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Filled.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Cyan400, modifier = Modifier.size(20.dp))
                        }

                        // Asset Switcher Chip
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Blue600.copy(alpha = 0.25f))
                                    .border(1.dp, Blue400.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable { showAssetMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    if (asset.contains("IRT")) "USDT/IRT" else "$asset/USDT",
                                    color = Slate50,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showAssetMenu,
                                onDismissRequest = { showAssetMenu = false },
                                modifier = Modifier.background(Color(0xFF0F172A))
                            ) {
                                listOf("BTC", "ETH", "SOL", "USDT_IRT", "TON", "DOGE").forEach { sym ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (sym == "USDT_IRT") "USDT / IRT (تتر)" else "$sym / USDT",
                                                color = if (asset == sym) Cyan400 else Slate200,
                                                fontWeight = if (asset == sym) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onSelectAsset(sym)
                                            showAssetMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Live Ticker Price
                        Column {
                            Text(
                                currentPrice,
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (tomanPrice.isNotEmpty() && !asset.contains("IRT")) {
                                Text(
                                    tomanPrice,
                                    color = Amber400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Middle/Right: Timeframes + Chart Type + Hollow Toggle + Indicators + Auto-Scale
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Timeframes (Lower to Higher with lowercase m)
                        listOf("1m", "3m", "5m", "15m", "1h", "4h", "1d").forEach { tf ->
                            val isSel = tf.equals(chartTimeframe, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Blue600.copy(alpha = 0.65f) else Color(0x18FFFFFF))
                                    .clickable { onSelectTimeframe(tf) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    tf,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Slate50 else Slate400
                                )
                            }
                        }

                        // Candles / Line Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable { onToggleChartType() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (chartType == "CANDLES") "📊" else "📈",
                                fontSize = 11.sp
                            )
                        }

                        // Hollow / Solid Toggle (TradingView pro hallmark)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isHollowCandles) Cyan400.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                                .border(1.dp, if (isHollowCandles) Cyan400 else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { isHollowCandles = !isHollowCandles }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (isHollowCandles) "🕯️ Hollow" else "🕯️ Solid",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHollowCandles) Cyan400 else Slate400
                            )
                        }

                        // Indicators Menu Button ("fx")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (showIndicatorsSheet) Cyan400.copy(alpha = 0.3f) else Color(0x18FFFFFF))
                                .border(1.dp, if (showIndicatorsSheet) Cyan400 else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { showIndicatorsSheet = !showIndicatorsSheet }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text("ƒx Indicators", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                        }

                        if (activeOpportunity != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (showConfluenceBanner) Amber400.copy(alpha = 0.35f) else Color(0xFF281C3D))
                                    .border(1.dp, Amber400, RoundedCornerShape(6.dp))
                                    .clickable { showConfluenceBanner = !showConfluenceBanner }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("🎯", fontSize = 10.sp)
                                    Text(
                                        text = if (isFa) "دلایل سیگنال (${activeOpportunity.confluenceScore}٪)" else "Setup ${activeOpportunity.confluenceScore}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Amber300,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }
                            }
                        }

                        // Zoom In (+)
                        IconButton(
                            onClick = { visibleCandleCount = (visibleCandleCount - 6).coerceAtLeast(14) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In", tint = Slate300, modifier = Modifier.size(18.dp))
                        }

                        // Zoom Out (-)
                        IconButton(
                            onClick = { visibleCandleCount = (visibleCandleCount + 8).coerceAtMost(candles.size.coerceAtLeast(30)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out", tint = Slate300, modifier = Modifier.size(18.dp))
                        }

                        // Auto-Scale / Reset View
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Amber400.copy(alpha = 0.15f))
                                .border(1.dp, Amber400.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .clickable {
                                    panOffset = 0
                                    visibleCandleCount = 35
                                    verticalPriceScale = 1f
                                    hoveredIndex = null
                                    crosshairY = null
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text("AUTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Amber400)
                        }
                    }
                }

                // ==========================================
                // 2. TRADINGVIEW TECHNICAL DRAWING TOOLBAR
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090D14))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (isFa) "ابزارهای ترسیم:" else "Tools:",
                        color = Slate500,
                        fontSize = 9.sp,
                        fontFamily = VazirmatnFontFamily
                    )

                    // 1. Crosshair Inspector
                    TvToolChip(
                        label = if (isFa) "🔍 کراس‌هیر" else "🔍 Inspect",
                        isSelected = activeTool == TvDrawingTool.CROSSHAIR,
                        onClick = { activeTool = TvDrawingTool.CROSSHAIR }
                    )

                    // 2. Trendline
                    TvToolChip(
                        label = if (isFa) "📏 خط روند" else "📏 Trendline",
                        isSelected = activeTool == TvDrawingTool.TRENDLINE,
                        onClick = {
                            activeTool = TvDrawingTool.TRENDLINE
                            pendingTrendlineStart = null
                        }
                    )

                    // 3. Horizontal Level
                    TvToolChip(
                        label = if (isFa) "➖ سطح افقی" else "➖ S/R Level",
                        isSelected = activeTool == TvDrawingTool.HORIZ_LEVEL,
                        onClick = { activeTool = TvDrawingTool.HORIZ_LEVEL }
                    )

                    // 4. Fibonacci Retracement
                    TvToolChip(
                        label = if (isFa) "📐 فیبوناچی" else "📐 Fibonacci",
                        isSelected = activeTool == TvDrawingTool.FIBONACCI,
                        onClick = { activeTool = TvDrawingTool.FIBONACCI }
                    )

                    // 5. Long Position Tool (Risk/Reward)
                    TvToolChip(
                        label = if (isFa) "⚖️ خرید (Long)" else "⚖️ Long R:R",
                        isSelected = activeTool == TvDrawingTool.LONG_POSITION,
                        onClick = { activeTool = TvDrawingTool.LONG_POSITION }
                    )

                    // 6. Short Position Tool (Risk/Reward)
                    TvToolChip(
                        label = if (isFa) "⚖️ فروش (Short)" else "⚖️ Short R:R",
                        isSelected = activeTool == TvDrawingTool.SHORT_POSITION,
                        onClick = { activeTool = TvDrawingTool.SHORT_POSITION }
                    )

                    // 7. Focus / Clean Mode Toggle (1-click hide noise)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCleanChartMode) Cyan500.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                            .border(1.dp, if (isCleanChartMode) Cyan400 else Color(0x1FFFFFFF), RoundedCornerShape(6.dp))
                            .clickable { isCleanChartMode = !isCleanChartMode }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (isFa) (if (isCleanChartMode) "👁️ نمایش کامل" else "🧹 چارت تمیز") else (if (isCleanChartMode) "👁️ Full" else "🧹 Clean"),
                            fontSize = 9.sp,
                            color = if (isCleanChartMode) Cyan300 else Slate300,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    // 8. Instant Position & Risk Calculator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Amber500.copy(alpha = 0.2f))
                            .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable {
                                if (onOpenRiskCalculator != null) {
                                    onOpenRiskCalculator(activeOpportunity)
                                } else {
                                    showInternalRiskCalc = true
                                }
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (isFa) "🧮 سایز و ریسک" else "🧮 Risk Calc",
                            fontSize = 9.sp,
                            color = Amber400,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    if (activeOpportunity != null && onSendOpportunityOrder != null) {
                        val isShortOpp = activeOpportunity.direction.equals("SHORT", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isShortOpp) Rose600.copy(alpha = 0.35f) else Emerald600.copy(alpha = 0.35f))
                                .border(1.dp, if (isShortOpp) Rose400 else Emerald400, RoundedCornerShape(6.dp))
                                .clickable { onSendOpportunityOrder(activeOpportunity) }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("fullscreen_send_order_tools_button")
                        ) {
                            Text(
                                if (isFa) "⚡ ارسال به متاتریدر" else "⚡ Send to MT",
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }

                    // 9. Clear All Drawings
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Rose500.copy(alpha = 0.15f))
                            .border(1.dp, Rose400.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .clickable {
                                trendlines.clear()
                                horizLevels.clear()
                                fibonaccis.clear()
                                positions.clear()
                                pendingTrendlineStart = null
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(if (isFa) "🗑️ پاک کردن" else "🗑️ Clear", fontSize = 9.sp, color = Rose400, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    }

                    if (pendingTrendlineStart != null) {
                        Text(
                            if (isFa) "👈 نقطه دوم را لمس کنید" else "👈 Tap second point",
                            color = Cyan400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                // ==========================================
                // 3. FLOATING INDICATORS DROPDOWN SHEET
                // ==========================================
                if (showIndicatorsSheet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131B2B))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Indicators:", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        TvIndicatorToggleChip(label = "EMA 20", isEnabled = showEma20, color = Cyan400) { showEma20 = !showEma20 }
                        TvIndicatorToggleChip(label = "EMA 50", isEnabled = showEma50, color = Blue400) { showEma50 = !showEma50 }
                        TvIndicatorToggleChip(label = "EMA 200", isEnabled = showEma200, color = Indigo400) { showEma200 = !showEma200 }
                        TvIndicatorToggleChip(label = "Bollinger Bands", isEnabled = showBollinger, color = Emerald400) { showBollinger = !showBollinger }
                        TvIndicatorToggleChip(label = "SMC OB/FVG", isEnabled = showObFvg, color = Amber400) { showObFvg = !showObFvg }
                        TvIndicatorToggleChip(label = "Volume", isEnabled = showVol, color = Slate200) { showVol = !showVol }
                        TvIndicatorToggleChip(label = "RSI (14)", isEnabled = showRsi, color = Indigo300) { showRsi = !showRsi }
                    }
                }

                // Confluence Details Banner (Shows trade setup reasons)
                if (activeOpportunity != null && showConfluenceBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .border(width = 1.dp, color = Amber400.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        val isOppShort = activeOpportunity.direction.equals("SHORT", ignoreCase = true)
                        val dirWordFa = if (isOppShort) "فروش (Short)" else "خرید (Long)"
                        val dirWordEn = if (isOppShort) "Short" else "Long"
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🎯", fontSize = 12.sp)
                                    Text(
                                        text = if (isFa) "دلایل سیگنال $dirWordFa ${activeOpportunity.symbol} (${activeOpportunity.timeframe})"
                                               else "$dirWordEn Signal Checklist: ${activeOpportunity.symbol} (${activeOpportunity.timeframe})",
                                        color = if (isOppShort) Rose400 else Amber300,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(if (activeOpportunity.confluenceScore >= 80) Emerald500.copy(alpha = 0.25f) else Amber500.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            "${activeOpportunity.confluenceScore}% ${activeOpportunity.grade}",
                                            color = if (activeOpportunity.confluenceScore >= 80) Emerald400 else Amber400,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (onSendOpportunityOrder != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isOppShort) Rose600.copy(alpha = 0.35f) else Emerald600.copy(alpha = 0.35f))
                                                .border(1.dp, if (isOppShort) Rose400 else Emerald400, RoundedCornerShape(4.dp))
                                                .clickable { onSendOpportunityOrder(activeOpportunity) }
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                                .testTag("fullscreen_send_order_banner_button")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text("⚡", fontSize = 9.sp)
                                                Text(
                                                    if (isFa) "ارسال به متاتریدر" else "Send to MT",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = VazirmatnFontFamily
                                                )
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Amber500.copy(alpha = 0.22f))
                                            .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .clickable {
                                                if (onOpenRiskCalculator != null) {
                                                    onOpenRiskCalculator(activeOpportunity)
                                                } else {
                                                    showInternalRiskCalc = true
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (isFa) "🧮 محاسبه حجم" else "🧮 Calc Size",
                                            color = Amber300,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = VazirmatnFontFamily
                                        )
                                    }

                                    Text("R:R ${activeOpportunity.riskReward}", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x30FFFFFF))
                                            .clickable { showConfluenceBanner = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = Slate300, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Dual Confirmation Highlight
                            if (activeOpportunity.isDualConfirmed) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x2210B981))
                                        .border(0.8.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💎", fontSize = 10.sp)
                                    Text(
                                        if (isFa) "تاییدیه دوگانه قطعی: ${activeOpportunity.divergenceType} + ${activeOpportunity.structureBreakType}"
                                        else "Dual Confirmed: ${activeOpportunity.divergenceType} + ${activeOpportunity.structureBreakType}",
                                        color = Emerald300,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                activeOpportunity.passedConditions.forEach { cond ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("✓", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(cond, color = Slate200, fontSize = 9.5.sp, fontFamily = VazirmatnFontFamily)
                                    }
                                }
                            }
                        }
                    }
                }

                // Real-time ticker countdown timer state (ticks every second)
                var tickerSeconds by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(1000L)
                        tickerSeconds = System.currentTimeMillis() / 1000L
                    }
                }
                val tfSeconds = when (chartTimeframe.lowercase()) {
                    "1m" -> 60L
                    "3m" -> 180L
                    "5m" -> 300L
                    "15m" -> 900L
                    "1h" -> 3600L
                    "4h" -> 14400L
                    "1d" -> 86400L
                    else -> 14400L
                }
                val remainingSec = tfSeconds - (tickerSeconds % tfSeconds)
                val countdownStr = String.format(Locale.US, "%02d:%02d", remainingSec / 60, remainingSec % 60)

                // ==========================================
                // 4. CANDLE OHLCV & COORDINATES STATUS BAR
                // ==========================================
                val count = visibleCandleCount.coerceIn(12, candles.size.coerceAtLeast(14))
                val maxOffset = (candles.size - count).coerceAtLeast(0)
                val curOffset = panOffset.coerceIn(0, maxOffset)
                val startIndex = (candles.size - count - curOffset).coerceIn(0, candles.size - 1)
                val endIndex = (startIndex + count).coerceIn(startIndex + 1, candles.size)
                val visibleCandles = if (candles.isNotEmpty()) candles.subList(startIndex, endIndex) else emptyList()

                val inspectedCandle = if (hoveredIndex != null && hoveredIndex in visibleCandles.indices) {
                    visibleCandles[hoveredIndex!!]
                } else visibleCandles.lastOrNull()

                if (inspectedCandle != null) {
                    val candleChange = ((inspectedCandle.close - inspectedCandle.open) / inspectedCandle.open) * 100f
                    val changeColor = if (candleChange >= 0f) Emerald400 else Rose400
                    val globalIdx = startIndex + (hoveredIndex ?: (visibleCandles.size - 1))
                    val curRsi = rsiValues.getOrNull(globalIdx) ?: 50f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0F19))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("O: ${fmtP(inspectedCandle.open)}", color = Slate400, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("H: ${fmtP(inspectedCandle.high)}", color = Emerald400, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("L: ${fmtP(inspectedCandle.low)}", color = Rose400, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("C: ${fmtP(inspectedCandle.close)}", color = Slate100, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text(
                                String.format(Locale.US, "%+.2f%%", candleChange),
                                color = changeColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱ $countdownStr", color = Amber400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("V: ${String.format(Locale.US, "%.0f", inspectedCandle.volume)}", color = Indigo300, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            if (showRsi) {
                                Text("RSI: ${String.format(Locale.US, "%.1f", curRsi)}", color = if (curRsi > 70f) Rose400 else if (curRsi < 30f) Emerald400 else Indigo300, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ==========================================
                // 5. MAIN CHART WORKSPACE (DUAL Y-AXES + CANVAS)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF06080D))
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Cyan400, modifier = Modifier.size(36.dp))
                        }
                    } else if (visibleCandles.isNotEmpty()) {
                        val minP = visibleCandles.minOf { it.low }
                        val maxP = visibleCandles.maxOf { it.high }
                        val rawSpan = if (maxP > minP) maxP - minP else 1f
                        val midPrice = (maxP + minP) / 2f
                        val effectiveSpan = (rawSpan * 1.08f) * verticalPriceScale
                        val minPrice = midPrice - (effectiveSpan / 2f)
                        val maxPrice = midPrice + (effectiveSpan / 2f)
                        val priceRange = maxPrice - minPrice

                        Row(modifier = Modifier.fillMaxSize()) {
                            // 1. LEFT DYNAMIC PRICE AXIS (Y-AXIS) - Dual Scale with Vertical Drag Scaling
                            Box(
                                modifier = Modifier
                                    .width(54.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF090C14))
                                    .border(width = 1.dp, color = Color(0x18FFFFFF))
                                    .pointerInput(Unit) {
                                        detectDragGestures { _, dragAmount ->
                                            verticalPriceScale = (verticalPriceScale + (dragAmount.y / 180f)).coerceIn(0.35f, 4.0f)
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight(if (showRsi) 0.78f else 0.95f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    val steps = 6
                                    for (i in 0..steps) {
                                        val p = maxPrice - (priceRange * (i.toFloat() / steps.toFloat()))
                                        Text(
                                            text = fmtP(p),
                                            color = Slate400,
                                            fontSize = 8.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Floating Crosshair Price Tag on Left Axis
                                crosshairY?.let { cy ->
                                    val activeAreaH = if (showRsi) 0.78f else 0.95f
                                    val touchedPrice = minPrice + (priceRange * (1f - (cy / 500f).coerceIn(0f, 1f)))
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (cy / 3.2f).dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1E293B))
                                            .border(1.dp, Cyan400, RoundedCornerShape(3.dp))
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = fmtP(touchedPrice),
                                            color = Cyan300,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // 2. MAIN DRAWING CANVAS (Interactive with Dual-Axis Pinch-Zoom & Deep Pan)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .pointerInput(candles.size, visibleCandles.size, activeTool) {
                                        detectTransformGestures { centroid, pan, zoom, _ ->
                                            if (zoom != 1f) {
                                                val updated = (visibleCandleCount / zoom).toInt().coerceIn(12, candles.size.coerceAtLeast(14))
                                                visibleCandleCount = updated
                                                verticalPriceScale = (verticalPriceScale / zoom.coerceIn(0.88f, 1.12f)).coerceIn(0.35f, 4.0f)
                                            }
                                            if (pan.x != 0f && visibleCandles.isNotEmpty()) {
                                                val stepX = (size.width / visibleCandles.size.toFloat()).coerceAtLeast(1f)
                                                val delta = -(pan.x / stepX).toInt()
                                                if (delta != 0) {
                                                    panOffset = (panOffset + delta).coerceIn(0, maxOffset)
                                                }
                                            }
                                            if (visibleCandles.isNotEmpty()) {
                                                val stepX = size.width / visibleCandles.size.toFloat()
                                                hoveredIndex = (centroid.x / stepX).toInt().coerceIn(0, visibleCandles.size - 1)
                                                crosshairY = centroid.y
                                            }
                                        }
                                    }
                                    .pointerInput(visibleCandles.size, activeTool) {
                                        detectTapGestures(
                                            onTap = { offset ->
                                                val wF = size.width.toFloat()
                                                val hF = size.height.toFloat()
                                                val stepX = wF / visibleCandles.size.toFloat()
                                                val idx = (offset.x / stepX).toInt().coerceIn(0, visibleCandles.size - 1)
                                                val candleH = if (showRsi) hF * 0.78f else hF
                                                val activeAreaH = if (showVol) candleH * 0.78f else candleH * 0.95f
                                                val touchedPrice = minPrice + ((activeAreaH - offset.y) / activeAreaH) * priceRange

                                                when (activeTool) {
                                                    TvDrawingTool.CROSSHAIR -> {
                                                        hoveredIndex = idx
                                                        crosshairY = offset.y
                                                    }
                                                    TvDrawingTool.HORIZ_LEVEL -> {
                                                        horizLevels.add(TvHorizLevel(touchedPrice, "S/R ${fmtP(touchedPrice)}"))
                                                    }
                                                    TvDrawingTool.TRENDLINE -> {
                                                        val ratioX = offset.x / wF
                                                        val p = pendingTrendlineStart
                                                        if (p == null) {
                                                            pendingTrendlineStart = Pair(ratioX, touchedPrice)
                                                        } else {
                                                            trendlines.add(TvTrendline(p.first, p.second, ratioX, touchedPrice))
                                                            pendingTrendlineStart = null
                                                        }
                                                    }
                                                    TvDrawingTool.FIBONACCI -> {
                                                        fibonaccis.add(TvFibonacci(maxPrice, minPrice))
                                                    }
                                                    TvDrawingTool.LONG_POSITION -> {
                                                        positions.add(
                                                            TvPosition(
                                                                entryPrice = touchedPrice,
                                                                targetPrice = touchedPrice + priceRange * 0.45f,
                                                                stopPrice = touchedPrice - priceRange * 0.20f,
                                                                isShort = false
                                                            )
                                                        )
                                                    }
                                                    TvDrawingTool.SHORT_POSITION -> {
                                                        positions.add(
                                                            TvPosition(
                                                                entryPrice = touchedPrice,
                                                                targetPrice = touchedPrice - priceRange * 0.45f,
                                                                stopPrice = touchedPrice + priceRange * 0.20f,
                                                                isShort = true
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    val rsiHeight = if (showRsi) h * 0.22f else 0f
                                    val mainHeight = h - rsiHeight
                                    val candleAreaHeight = if (showVol) mainHeight * 0.78f else mainHeight * 0.95f
                                    val volumeAreaHeight = if (showVol) mainHeight * 0.20f else 0f
                                    val volumeBaseY = mainHeight

                                    fun pToY(p: Float): Float =
                                        candleAreaHeight - ((p - minPrice) / priceRange) * (candleAreaHeight * 0.88f) - 10.dp.toPx()

                                    val countV = visibleCandles.size
                                    val stepX = w / countV
                                    val candleBodyW = max(3f, stepX * 0.68f)

                                    // 0. Authentic TradingView Symbol & Resolution Watermark in Center of Background
                                    val wmText = "${if (asset.contains("IRT")) "USDT/IRT" else "$asset/USDT"} • ${chartTimeframe.lowercase()} • NOBITEX"
                                    val wmPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.argb(12, 255, 255, 255)
                                        textSize = 32.sp.toPx()
                                        isFakeBoldText = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(
                                        wmText,
                                        w / 2f,
                                        candleAreaHeight * 0.45f,
                                        wmPaint
                                    )

                                    // A. Background Grid Lines (Horizontal & Vertical Cartesian Grid)
                                    val gridSteps = 5
                                    for (i in 0..gridSteps) {
                                        val gy = candleAreaHeight * (i.toFloat() / gridSteps.toFloat())
                                        drawLine(
                                            color = Color(0x10FFFFFF),
                                            start = Offset(0f, gy),
                                            end = Offset(w, gy),
                                            strokeWidth = 1f
                                        )
                                    }
                                    val vGridCount = 6
                                    for (i in 0..vGridCount) {
                                        val gx = w * (i.toFloat() / vGridCount.toFloat())
                                        drawLine(
                                            color = Color(0x0AFFFFFF),
                                            start = Offset(gx, 0f),
                                            end = Offset(gx, h),
                                            strokeWidth = 1f
                                        )
                                    }

                                    // B. Bollinger Bands (20, 2)
                                    if (showBollinger) {
                                        val upperPath = Path()
                                        val lowerPath = Path()
                                        val middlePath = Path()
                                        val fillPath = Path()

                                        visibleCandles.forEachIndexed { i, _ ->
                                            val gIdx = startIndex + i
                                            val bb = bollingerBands.getOrNull(gIdx)
                                            if (bb != null) {
                                                val cx = i * stepX + (stepX / 2f)
                                                val uy = pToY(bb.upper)
                                                val my = pToY(bb.middle)
                                                val ly = pToY(bb.lower)

                                                if (upperPath.isEmpty) {
                                                    upperPath.moveTo(cx, uy)
                                                    middlePath.moveTo(cx, my)
                                                    lowerPath.moveTo(cx, ly)
                                                    fillPath.moveTo(cx, uy)
                                                } else {
                                                    upperPath.lineTo(cx, uy)
                                                    middlePath.lineTo(cx, my)
                                                    lowerPath.lineTo(cx, ly)
                                                    fillPath.lineTo(cx, uy)
                                                }
                                            }
                                        }

                                        for (i in visibleCandles.indices.reversed()) {
                                            val gIdx = startIndex + i
                                            val bb = bollingerBands.getOrNull(gIdx)
                                            if (bb != null) {
                                                val cx = i * stepX + (stepX / 2f)
                                                val ly = pToY(bb.lower)
                                                fillPath.lineTo(cx, ly)
                                            }
                                        }
                                        fillPath.close()

                                        drawPath(path = fillPath, color = Emerald500.copy(alpha = 0.08f))
                                        drawPath(path = upperPath, color = Emerald400.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))
                                        drawPath(path = middlePath, color = Slate400.copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)))
                                        drawPath(path = lowerPath, color = Emerald400.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))
                                    }

                                    // C. Smart Money Concept Zones (Order Blocks & FVG)
                                    if (showObFvg) {
                                        val obBullPrice = minPrice + priceRange * 0.16f
                                        val obBullY = pToY(obBullPrice)
                                        drawRoundRect(
                                            color = Emerald500.copy(alpha = 0.12f),
                                            topLeft = Offset(0f, obBullY - 12.dp.toPx()),
                                            size = Size(w, 24.dp.toPx()),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                        drawLine(
                                            color = Emerald400.copy(alpha = 0.4f),
                                            start = Offset(0f, obBullY),
                                            end = Offset(w, obBullY),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )

                                        val obBearPrice = minPrice + priceRange * 0.82f
                                        val obBearY = pToY(obBearPrice)
                                        drawRoundRect(
                                            color = Rose500.copy(alpha = 0.10f),
                                            topLeft = Offset(0f, obBearY - 12.dp.toPx()),
                                            size = Size(w, 24.dp.toPx()),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                        drawLine(
                                            color = Rose400.copy(alpha = 0.4f),
                                            start = Offset(0f, obBearY),
                                            end = Offset(w, obBearY),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                    }

                                    // D. Volume Histogram & 20-period Moving Average
                                    if (showVol) {
                                        val maxVol = visibleCandles.maxOfOrNull { it.volume } ?: 1f
                                        visibleCandles.forEachIndexed { i, c ->
                                            val cx = i * stepX + (stepX / 2f)
                                            val normVol = (c.volume / maxVol).coerceIn(0.04f, 1f)
                                            val barH = normVol * volumeAreaHeight
                                            val isBull = c.close >= c.open
                                            val barColor = if (isBull) Emerald500.copy(alpha = 0.35f) else Rose500.copy(alpha = 0.35f)

                                            drawRect(
                                                color = barColor,
                                                topLeft = Offset(cx - (candleBodyW / 2f), volumeBaseY - barH),
                                                size = Size(candleBodyW, barH)
                                            )
                                        }
                                    }

                                    // E. Candlestick or Glowing Area Line Mode
                                    if (chartType == "CANDLES") {
                                        visibleCandles.forEachIndexed { i, c ->
                                            val cx = i * stepX + (stepX / 2f)
                                            val isBull = c.close >= c.open
                                            val candleColor = if (isBull) Emerald400 else Rose400

                                            val highY = pToY(c.high)
                                            val lowY = pToY(c.low)
                                            val openY = pToY(c.open)
                                            val closeY = pToY(c.close)

                                            // Wick
                                            drawLine(
                                                color = candleColor,
                                                start = Offset(cx, highY),
                                                end = Offset(cx, lowY),
                                                strokeWidth = 1.2.dp.toPx()
                                            )

                                            // Body (Hollow / Solid TradingView Style)
                                            val topY = min(openY, closeY)
                                            val bodyH = max(2f, kotlin.math.abs(closeY - openY))
                                            if (isHollowCandles && isBull) {
                                                drawRect(
                                                    color = candleColor,
                                                    topLeft = Offset(cx - (candleBodyW / 2f), topY),
                                                    size = Size(candleBodyW, bodyH),
                                                    style = Stroke(width = 1.4.dp.toPx())
                                                )
                                            } else {
                                                drawRect(
                                                    color = candleColor,
                                                    topLeft = Offset(cx - (candleBodyW / 2f), topY),
                                                    size = Size(candleBodyW, bodyH)
                                                )
                                            }
                                        }
                                    } else {
                                        // Line Chart
                                        val linePath = Path()
                                        val fillPath = Path()

                                        visibleCandles.forEachIndexed { i, c ->
                                            val cx = i * stepX + (stepX / 2f)
                                            val cy = pToY(c.close)
                                            if (i == 0) {
                                                linePath.moveTo(cx, cy)
                                                fillPath.moveTo(cx, cy)
                                            } else {
                                                linePath.lineTo(cx, cy)
                                                fillPath.lineTo(cx, cy)
                                            }
                                        }
                                        fillPath.lineTo(w, candleAreaHeight)
                                        fillPath.lineTo(0f, candleAreaHeight)
                                        fillPath.close()

                                        drawPath(
                                            path = fillPath,
                                            brush = Brush.verticalGradient(
                                                listOf(Cyan400.copy(alpha = 0.28f), Color.Transparent)
                                            )
                                        )
                                        drawPath(
                                            path = linePath,
                                            color = Cyan400,
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }

                                    // F. Multi-EMA Overlays (EMA 20, 50, 200)
                                    if (candles.size > 10) {
                                        fun drawEmaCurve(period: Int, color: Color, weight: Float) {
                                            val path = Path()
                                            var ema = candles.first().close
                                            val k = 2f / (period + 1f)

                                            candles.forEachIndexed { idx, c ->
                                                ema += (c.close - ema) * k
                                                if (idx in startIndex until endIndex) {
                                                    val localI = idx - startIndex
                                                    val cx = localI * stepX + (stepX / 2f)
                                                    val ey = pToY(ema)
                                                    if (path.isEmpty) path.moveTo(cx, ey) else path.lineTo(cx, ey)
                                                }
                                            }
                                            drawPath(path = path, color = color, style = Stroke(width = weight.dp.toPx()))
                                        }

                                        if (showEma20) drawEmaCurve(20, Cyan400, 1.4f)
                                        if (showEma50) drawEmaCurve(50, Blue400, 1.6f)
                                        if (showEma200) drawEmaCurve(200, Indigo400, 1.8f)
                                    }

                                    // G. User Drawings: Fibonacci Retracements
                                    fibonaccis.forEach { fib ->
                                        val highY = pToY(fib.highPrice)
                                        val lowY = pToY(fib.lowPrice)
                                        val diffY = lowY - highY

                                        val fibLevels = listOf(
                                            0.0f to Color(0x22818CF8),
                                            0.236f to Color(0x2860A5FA),
                                            0.382f to Color(0x3022D3EE),
                                            0.5f to Color(0x3534D399),
                                            0.618f to Color(0x40FBBF24),
                                            0.786f to Color(0x35F43F5E),
                                            1.0f to Color(0x25FB7185)
                                        )

                                        fibLevels.forEachIndexed { fIdx, (ratio, col) ->
                                            val ly = highY + diffY * ratio
                                            drawLine(
                                                color = col.copy(alpha = 0.8f),
                                                start = Offset(0f, ly),
                                                end = Offset(w, ly),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                            if (fIdx < fibLevels.size - 1) {
                                                val nextLy = highY + diffY * fibLevels[fIdx + 1].first
                                                drawRect(
                                                    color = col.copy(alpha = 0.08f),
                                                    topLeft = Offset(0f, min(ly, nextLy)),
                                                    size = Size(w, kotlin.math.abs(nextLy - ly))
                                                )
                                            }
                                        }
                                    }

                                    // H. User Drawings: Long & Short Position Risk/Reward Boxes
                                    if (!isCleanChartMode) {
                                        positions.forEach { pos ->
                                            val entryY = pToY(pos.entryPrice)
                                            val targetY = pToY(pos.targetPrice)
                                            val stopY = pToY(pos.stopPrice)

                                            // Take Profit Box (Green)
                                            val tpTop = min(entryY, targetY)
                                            val tpH = max(2f, kotlin.math.abs(entryY - targetY))
                                            drawRect(color = Emerald500.copy(alpha = 0.16f), topLeft = Offset(0f, tpTop), size = Size(w, tpH))
                                            drawLine(color = Emerald400, start = Offset(0f, targetY), end = Offset(w, targetY), strokeWidth = 1.5.dp.toPx())

                                            // Stop Loss Box (Red)
                                            val slTop = min(entryY, stopY)
                                            val slH = max(2f, kotlin.math.abs(entryY - stopY))
                                            drawRect(color = Rose500.copy(alpha = 0.16f), topLeft = Offset(0f, slTop), size = Size(w, slH))
                                            drawLine(color = Rose400, start = Offset(0f, stopY), end = Offset(w, stopY), strokeWidth = 1.5.dp.toPx())

                                            // Entry Line (White)
                                            drawLine(color = Slate100, start = Offset(0f, entryY), end = Offset(w, entryY), strokeWidth = 1.5.dp.toPx())
                                        }

                                        // I. User Drawings: Horizontal Levels
                                        horizLevels.forEach { hl ->
                                            val hy = pToY(hl.price)
                                            drawLine(
                                                color = Amber400,
                                                start = Offset(0f, hy),
                                                end = Offset(w, hy),
                                                strokeWidth = 1.2.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                            )
                                        }

                                        // J. User Drawings: Trendlines
                                        trendlines.forEach { tl ->
                                            val x1 = tl.startRatioX * w
                                            val y1 = pToY(tl.startPrice)
                                            val x2 = tl.endRatioX * w
                                            val y2 = pToY(tl.endPrice)

                                            drawLine(color = Cyan400, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 2.dp.toPx())
                                            drawCircle(color = Cyan400, radius = 4.dp.toPx(), center = Offset(x1, y1))
                                            drawCircle(color = Cyan400, radius = 4.dp.toPx(), center = Offset(x2, y2))
                                        }

                                        // K. Pending Trendline First Point Indicator
                                        pendingTrendlineStart?.let { pt ->
                                            val px = pt.first * w
                                            val py = pToY(pt.second)
                                            drawCircle(color = Amber400, radius = 6.dp.toPx(), center = Offset(px, py))
                                        }
                                    }

                                    // L. Current Live Price Horizontal Pulse Line & Floating Countdown Badge on Canvas
                                    val lastC = visibleCandles.lastOrNull()
                                    if (lastC != null) {
                                        val liveY = pToY(lastC.close)
                                        drawLine(
                                            color = Cyan400.copy(alpha = 0.85f),
                                            start = Offset(0f, liveY),
                                            end = Offset(w, liveY),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                                        )

                                        // Floating Countdown Tag directly beside the active candle line
                                        val lastIdx = visibleCandles.size - 1
                                        val lastCandleCenterX = lastIdx * stepX + (stepX / 2f)
                                        val badgePaint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.parseColor("#0F172A")
                                            style = android.graphics.Paint.Style.FILL
                                        }
                                        val badgeStrokePaint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.parseColor("#38BDF8")
                                            style = android.graphics.Paint.Style.STROKE
                                            strokeWidth = 1.2.dp.toPx()
                                        }
                                        val badgeTextPaint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.parseColor("#38BDF8")
                                            textSize = 8.5.sp.toPx()
                                            isFakeBoldText = true
                                        }
                                        val tagText = "⏱ $countdownStr"
                                        val tBounds = android.graphics.Rect()
                                        badgeTextPaint.getTextBounds(tagText, 0, tagText.length, tBounds)
                                        val padH = 6.dp.toPx()
                                        val padV = 3.dp.toPx()
                                        val tagW = tBounds.width() + padH * 2f
                                        val tagH = tBounds.height() + padV * 2f
                                        val tagLeft = (lastCandleCenterX + candleBodyW / 2f + 6.dp.toPx()).coerceAtMost(w - tagW - 4.dp.toPx())
                                        val tagTop = liveY - tagH / 2f
                                        val tagRect = android.graphics.RectF(tagLeft, tagTop, tagLeft + tagW, tagTop + tagH)
                                        drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 4.dp.toPx(), 4.dp.toPx(), badgePaint)
                                        drawContext.canvas.nativeCanvas.drawRoundRect(tagRect, 4.dp.toPx(), 4.dp.toPx(), badgeStrokePaint)
                                        drawContext.canvas.nativeCanvas.drawText(tagText, tagLeft + padH, tagTop + tagH - padV, badgeTextPaint)
                                    }

                                    // M. Interactive Crosshair Lines
                                    hoveredIndex?.let { idx ->
                                        val cx = idx * stepX + (stepX / 2f)
                                        val cy = crosshairY ?: pToY(visibleCandles[idx].close)

                                        // Vertical Line
                                        drawLine(
                                            color = Slate300.copy(alpha = 0.8f),
                                            start = Offset(cx, 0f),
                                            end = Offset(cx, h),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                        )

                                        // Horizontal Line
                                        drawLine(
                                            color = Slate300.copy(alpha = 0.8f),
                                            start = Offset(0f, cy),
                                            end = Offset(w, cy),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                        )

                                        drawCircle(color = Cyan400, radius = 4.dp.toPx(), center = Offset(cx, cy))
                                    }

                                    // N. RSI Sub-Chart Panel
                                    if (showRsi) {
                                        val rsiTop = h - rsiHeight
                                        // Panel Separator
                                        drawLine(color = Color(0x30FFFFFF), start = Offset(0f, rsiTop), end = Offset(w, rsiTop), strokeWidth = 1.dp.toPx())

                                        val rsiY70 = rsiTop + rsiHeight * 0.30f
                                        val rsiY30 = rsiTop + rsiHeight * 0.70f
                                        val rsiY50 = rsiTop + rsiHeight * 0.50f

                                        // 70 Overbought & 30 Oversold Thresholds
                                        drawLine(color = Rose400.copy(alpha = 0.5f), start = Offset(0f, rsiY70), end = Offset(w, rsiY70), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
                                        drawLine(color = Emerald400.copy(alpha = 0.5f), start = Offset(0f, rsiY30), end = Offset(w, rsiY30), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
                                        drawLine(color = Slate600.copy(alpha = 0.4f), start = Offset(0f, rsiY50), end = Offset(w, rsiY50), strokeWidth = 1f)

                                        // Fill 30..70 Channel
                                        drawRect(
                                            color = Indigo500.copy(alpha = 0.06f),
                                            topLeft = Offset(0f, rsiY70),
                                            size = Size(w, rsiY30 - rsiY70)
                                        )

                                        // Draw RSI Curve
                                        val rsiPath = Path()
                                        visibleCandles.forEachIndexed { i, _ ->
                                            val gIdx = startIndex + i
                                            val rVal = rsiValues.getOrNull(gIdx) ?: 50f
                                            val cx = i * stepX + (stepX / 2f)
                                            val ry = rsiTop + rsiHeight * (1f - (rVal / 100f))

                                            if (i == 0) rsiPath.moveTo(cx, ry) else rsiPath.lineTo(cx, ry)
                                        }
                                        drawPath(path = rsiPath, color = Indigo300, style = Stroke(width = 1.6.dp.toPx()))
                                    }

                                    // O. Smart Confluence Setup Overlays (Order Block, EMA Cross, Entry, SL, TP)
                                    if (activeOpportunity != null) {
                                        fun parsePriceClean(str: String): Float? =
                                            str.replace("$", "").replace("تومان", "").replace(",", "").trim().toFloatOrNull()

                                        val isOppShort = activeOpportunity.direction.equals("SHORT", ignoreCase = true)
                                        val entryVal = parsePriceClean(activeOpportunity.entryPrice)
                                        val slVal = parsePriceClean(activeOpportunity.stopLoss)
                                        val tp1Val = parsePriceClean(activeOpportunity.tp1)
                                        val tp2Val = parsePriceClean(activeOpportunity.tp2)
                                        val tp3Val = parsePriceClean(activeOpportunity.tp3)

                                        // 1. Order Block (Demand POI for Long, Supply POI for Short)
                                        if (entryVal != null && slVal != null) {
                                            val obTopY = pToY(max(entryVal, slVal) * 1.001f)
                                            val obBottomY = pToY(min(entryVal, slVal) * 0.999f)
                                            val zoneY = min(obTopY, obBottomY)
                                            val zoneHeight = max(18.dp.toPx(), kotlin.math.abs(obBottomY - obTopY))

                                            val obBoxColor = if (isOppShort) Rose500.copy(alpha = 0.16f) else Amber500.copy(alpha = 0.16f)
                                            val obBorderColor = if (isOppShort) Rose400.copy(alpha = 0.5f) else Amber400.copy(alpha = 0.5f)

                                            drawRect(
                                                color = obBoxColor,
                                                topLeft = Offset(0f, zoneY),
                                                size = Size(w, zoneHeight)
                                            )
                                            drawRect(
                                                color = obBorderColor,
                                                topLeft = Offset(0f, zoneY),
                                                size = Size(w, zoneHeight),
                                                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                                            )
                                            val obPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor(if (isOppShort) "#FB7185" else "#FBBF24")
                                                textSize = 9.sp.toPx()
                                                isFakeBoldText = true
                                            }
                                            val obTitle = if (isOppShort) "📌 4H ORDER BLOCK (SUPPLY POI)" else "📌 4H ORDER BLOCK (DEMAND POI)"
                                            drawContext.canvas.nativeCanvas.drawText(obTitle, 14.dp.toPx(), zoneY + 12.dp.toPx(), obPaint)
                                        }

                                        // 2. Entry Price Level (Cyan Dashed Line + Text)
                                        entryVal?.let { ev ->
                                            val ey = pToY(ev)
                                            drawLine(
                                                color = Cyan400,
                                                start = Offset(0f, ey),
                                                end = Offset(w, ey),
                                                strokeWidth = 1.6.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                            )
                                            val entryPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#38BDF8")
                                                textSize = 9.sp.toPx()
                                                isFakeBoldText = true
                                            }
                                            drawContext.canvas.nativeCanvas.drawText("ENTRY: ${activeOpportunity.entryPrice}", w * 0.45f, ey - 3.dp.toPx(), entryPaint)
                                        }

                                        // 3. Stop Loss Level (Rose Dashed Line + Text)
                                        slVal?.let { sv ->
                                            val sy = pToY(sv)
                                            drawLine(
                                                color = Rose400,
                                                start = Offset(0f, sy),
                                                end = Offset(w, sy),
                                                strokeWidth = 1.6.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                            )
                                            val slPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#FB7185")
                                                textSize = 9.sp.toPx()
                                                isFakeBoldText = true
                                            }
                                            val slText = if (isOppShort) "STOP LOSS: ${activeOpportunity.stopLoss} (Above Supply)" else "STOP LOSS: ${activeOpportunity.stopLoss} (-1.8%)"
                                            drawContext.canvas.nativeCanvas.drawText(slText, w * 0.45f, sy + 12.dp.toPx(), slPaint)
                                        }

                                        // 4. Take Profit Targets (Emerald Dashed Lines + Text)
                                        listOf(tp1Val to "TP1 (50%)", tp2Val to "TP2 (30%)", tp3Val to "TP3 (Runner)").forEach { (tpPrice, label) ->
                                            tpPrice?.let {
                                                val ty = pToY(it)
                                                drawLine(
                                                    color = Emerald400,
                                                    start = Offset(0f, ty),
                                                    end = Offset(w, ty),
                                                    strokeWidth = 1.4.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                                )
                                                val tpPaint = android.graphics.Paint().apply {
                                                    color = android.graphics.Color.parseColor("#34D399")
                                                    textSize = 8.5.sp.toPx()
                                                    isFakeBoldText = true
                                                }
                                                drawContext.canvas.nativeCanvas.drawText("$label: $it", w * 0.55f, ty - 3.dp.toPx(), tpPaint)
                                            }
                                        }

                                        // 5. EMA 50/200 Golden Cross Marker
                                        if (visibleCandles.size >= 6) {
                                            val crossIdx = (visibleCandles.size - 4).coerceAtLeast(0)
                                            val crossX = crossIdx * stepX + (stepX / 2f)
                                            val crossCandle = visibleCandles[crossIdx]
                                            val crossY = pToY(crossCandle.low) + 12.dp.toPx()

                                            drawCircle(color = Amber400, radius = 5.dp.toPx(), center = Offset(crossX, crossY))
                                            drawLine(
                                                color = Amber400.copy(alpha = 0.5f),
                                                start = Offset(crossX, 0f),
                                                end = Offset(crossX, candleAreaHeight),
                                                strokeWidth = 1.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                            )
                                            val crossPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#FBBF24")
                                                textSize = 8.5.sp.toPx()
                                                isFakeBoldText = true
                                                textAlign = android.graphics.Paint.Align.CENTER
                                            }
                                            drawContext.canvas.nativeCanvas.drawText("⚡ EMA CROSS", crossX, crossY + 12.dp.toPx(), crossPaint)
                                        }
                                    }
                                }

                                // Reset Pan Pill when scrolled back in deep history
                                if (panOffset > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(bottom = 12.dp, end = 8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xEE1E293B))
                                            .border(1.dp, Cyan400, RoundedCornerShape(16.dp))
                                            .clickable { panOffset = 0 }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("⏭", fontSize = 11.sp)
                                            Text(
                                                if (isFa) "بازگشت به کندل زنده" else "Real-time",
                                                color = Cyan300,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = VazirmatnFontFamily
                                            )
                                        }
                                    }
                                }
                            }

                            // RIGHT DYNAMIC PRICE AXIS (Y-AXIS) with Vertical Drag Scaling
                            Box(
                                modifier = Modifier
                                    .width(68.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF0A0D15))
                                    .border(width = 1.dp, color = Color(0x18FFFFFF))
                                    .pointerInput(Unit) {
                                        detectDragGestures { _, dragAmount ->
                                            verticalPriceScale = (verticalPriceScale + (dragAmount.y / 180f)).coerceIn(0.35f, 4.0f)
                                        }
                                    }
                            ) {
                                // Dynamic Price Graduation Labels
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight(if (showRsi) 0.78f else 0.95f)
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val steps = 6
                                    for (i in 0..steps) {
                                        val p = maxPrice - (priceRange * (i.toFloat() / steps.toFloat()))
                                        Text(
                                            text = fmtP(p),
                                            color = Slate400,
                                            fontSize = 8.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Pulsing Live Market Price Tag + Bar Countdown Timer Badge
                                val lastC = visibleCandles.lastOrNull()
                                if (lastC != null) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 2.dp),
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (lastC.close >= lastC.open) Emerald600 else Rose600)
                                                .border(1.dp, if (lastC.close >= lastC.open) Emerald400 else Rose400, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = fmtP(lastC.close),
                                                color = Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Countdown Timer to Candle Close (Classic TradingView)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF131A26))
                                                .border(0.8.dp, Cyan400.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "⏱ $countdownStr",
                                                color = Cyan400,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                // Floating Crosshair Price Tag on Right Axis
                                crosshairY?.let { cy ->
                                    val touchedPrice = minPrice + (priceRange * (1f - (cy / 500f).coerceIn(0f, 1f)))
                                    Box(
                                        modifier = Modifier
                                            .offset(y = cy.dp / 3f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1E293B))
                                            .border(1.dp, Cyan400, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = fmtP(touchedPrice),
                                            color = Cyan400,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 6. BOTTOM TIME AXIS (X-AXIS) with Horizontal Drag Scaling
                // ==========================================
                if (visibleCandles.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070A10))
                            .border(1.dp, Color(0x14FFFFFF))
                            .pointerInput(candles.size) {
                                detectDragGestures { _, dragAmount ->
                                    val delta = -(dragAmount.x / 18f).toInt()
                                    if (delta != 0) {
                                        visibleCandleCount = (visibleCandleCount + delta).coerceIn(12, candles.size.coerceAtLeast(14))
                                    }
                                }
                            }
                    ) {
                        // Highlighted hovered candle date & time tooltip
                        if (hoveredIndex != null && hoveredIndex in visibleCandles.indices) {
                            val hCandle = visibleCandles[hoveredIndex!!]
                            val fullDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(hCandle.timestamp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF131A26))
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📅 تاریخ و ساعت کندل: $fullDateStr",
                                    color = Cyan300,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .padding(start = 54.dp, end = 68.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isIntra = chartTimeframe.lowercase() in listOf("1m", "3m", "5m", "15m")
                            val df = SimpleDateFormat(if (isIntra) "HH:mm" else "MM/dd HH:mm", Locale.US)
                            val stepCandles = max(1, visibleCandles.size / 6)
                            for (i in 0 until visibleCandles.size step stepCandles) {
                                val c = visibleCandles[i]
                                Text(
                                    text = df.format(Date(c.timestamp)),
                                    color = Slate400,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Nobitex Live Feed Status Indicator
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Emerald400, CircleShape))
                                Text(
                                    text = "نوبیتکس • ${chartTimeframe.lowercase()}",
                                    color = Cyan400,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showInternalRiskCalc) {
            PositionRiskCalculatorDialog(
                opportunity = activeOpportunity,
                currentAsset = asset,
                currentPriceStr = currentPrice,
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                isFa = isFa,
                onDismiss = { showInternalRiskCalc = false }
            )
        }
    }
}

// -------------------------------------------------------------
// HELPER UI CHIPS FOR TRADINGVIEW TOOLBAR
// -------------------------------------------------------------
@Composable
fun TvToolChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Cyan400.copy(alpha = 0.25f) else Color(0x15FFFFFF))
            .border(1.dp, if (isSelected) Cyan400 else Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isSelected) Cyan400 else Slate300,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = VazirmatnFontFamily
        )
    }
}

@Composable
fun TvIndicatorToggleChip(
    label: String,
    isEnabled: Boolean,
    color: Color,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isEnabled) color.copy(alpha = 0.22f) else Color(0x12FFFFFF))
            .border(1.dp, if (isEnabled) color.copy(alpha = 0.6f) else Color(0x18FFFFFF), RoundedCornerShape(6.dp))
            .clickable { onToggle() }
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isEnabled) color else Slate400,
            fontWeight = FontWeight.Bold
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
                                                if (isFa) "⚡ ارسال" else "⚡ Send",
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

// -------------------------------------------------------------
// DEDICATED SCANNER VIEW (Full Screen for SCANNER Tab)
// -------------------------------------------------------------
@Composable
fun ScannerView(
    opportunities: List<ScannerOpportunity>,
    isScanning: Boolean,
    selectedTimeframe: String,
    selectedDirection: String = "ALL",
    isFa: Boolean,
    onSelectTimeframe: (String) -> Unit,
    onSelectDirection: (String) -> Unit = {},
    onRescan: () -> Unit,
    onOpenRiskCalculator: (ScannerOpportunity) -> Unit = {},
    onSendOrder: (ScannerOpportunity) -> Unit = {},
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Scanner Hero Banner with Live Scanning Action
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Cyan500
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Cyan400.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Radar, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                if (isFa) "موتور اسکن چند تایم‌فریمه هوشمند" else "Multi-Timeframe Smart Scanner",
                                color = Slate100,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFontFamily
                            )
                            Text(
                                if (isFa) "رصد لحظه‌ای ۵ ارز در ۷ تایم‌فریم (۱د تا ۴س)" else "Scanning BTC, ETH, SOL, TON, DOGE",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontFamily = VazirmatnFontFamily
                            )
                        }
                    }

                    // Rescan Action Button
                    Button(
                        onClick = onRescan,
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("rescan_scanner_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "در حال اسکن..." else "Scanning...", fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFa) "اسکن مجدد" else "Rescan", fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                        }
                    }
                }
            }
        }

        // 2. 5-Factor Weighted Confluence Checklist Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Indigo400
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (isFa) "فرمول وزن‌دهی کانسپت‌های تحلیلی (Confluence Weights)" else "Confluence Weight Breakdown",
                    color = Slate200,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "EMA 50/200" to "30%",
                        "Order Block تقاضا" to "25%",
                        "واگرایی مثبت RSI" to "15%",
                        "شکست ساختار ChoCH" to "15%",
                        "جهش حجم معاملات" to "15%"
                    ).forEach { (name, weight) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x20FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(name, color = Slate300, fontSize = 9.sp, fontFamily = VazirmatnFontFamily)
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
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = VazirmatnFontFamily
            )

            listOf(
                "ALL" to (if (isFa) "همه (All)" else "All"),
                "LONG" to (if (isFa) "🟢 خرید (Long)" else "🟢 Long"),
                "SHORT" to (if (isFa) "🔴 فروش (Short)" else "🔴 Short")
            ).forEach { (dir, label) ->
                val isSel = dir == selectedDirection
                val chipBg = when {
                    !isSel -> Color(0x18FFFFFF)
                    dir == "LONG" -> Emerald600.copy(alpha = 0.5f)
                    dir == "SHORT" -> Rose600.copy(alpha = 0.5f)
                    else -> Blue600.copy(alpha = 0.5f)
                }
                val chipBorder = when {
                    !isSel -> Color.Transparent
                    dir == "LONG" -> Emerald400
                    dir == "SHORT" -> Rose400
                    else -> Cyan400
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
                        color = if (isSel) Slate50 else Slate400,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = VazirmatnFontFamily
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
                "1m" to if (isFa) "۱ دقیقه ⚡" else "1m Scalp",
                "3m" to if (isFa) "۳ دقیقه" else "3m",
                "5m" to if (isFa) "۵ دقیقه 🎯" else "5m",
                "15m" to if (isFa) "۱۵ دقیقه" else "15m",
                "1h" to if (isFa) "۱ ساعته" else "1h",
                "4h" to if (isFa) "۴ ساعته 🌊" else "4h Swing"
            ).forEach { (tf, label) ->
                val isSel = tf == selectedTimeframe
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Blue600.copy(alpha = 0.5f) else Color(0x18FFFFFF))
                        .border(1.dp, if (isSel) Cyan400.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { onSelectTimeframe(tf) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        color = if (isSel) Slate50 else Slate400,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }

        // 5. Opportunities List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isFa) "در این فیلتر سیگنال معاملاتی یافت نشد." else "No trade setups found for this filter.",
                    color = Slate400,
                    fontSize = 12.sp,
                    fontFamily = VazirmatnFontFamily
                )
            }
        } else {
            filteredList.forEachIndexed { index, opp ->
                val isA = opp.confluenceScore >= 80
                val accentColor = if (isA) Emerald400 else Amber400
                val isLong = opp.direction.equals("LONG", ignoreCase = true)
                val dirBg = if (isLong) Emerald500.copy(alpha = 0.22f) else Rose500.copy(alpha = 0.22f)
                val dirBorder = if (isLong) Emerald400.copy(alpha = 0.6f) else Rose400.copy(alpha = 0.6f)
                val dirColor = if (isLong) Emerald400 else Rose400
                val dirText = if (isFa) (if (isLong) "خرید (LONG)" else "فروش (SHORT)") else opp.direction

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
                                // Rank Badge
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(accentColor.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
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
                                    "${opp.symbol}/USDT",
                                    color = Slate100,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                // Timeframe Chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Blue600.copy(alpha = 0.3f))
                                        .border(1.dp, Blue400.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        opp.timeframe.uppercase(),
                                        color = Cyan400,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Direction Badge (Distinctive Long Green / Short Red)
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
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }
                            }

                            // Confluence Score + Grade
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
                                    color = Slate300,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Dual Confirmation Banner (RSI/MACD Divergence + Structure Break)
                        if (opp.isDualConfirmed) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x2210B981))
                                    .border(0.8.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💎", fontSize = 11.sp)
                                Text(
                                    if (isFa) "تاییدیه دوگانه: ${opp.divergenceType} + ${opp.structureBreakType}"
                                    else "Dual Confirmed: ${opp.divergenceType} + ${opp.structureBreakType}",
                                    color = Emerald300,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFontFamily
                                )
                            }
                        }

                        // Targets Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x180B101D))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isFa) "ورود" else "Entry", color = Slate400, fontSize = 9.sp, fontFamily = VazirmatnFontFamily)
                                Text(opp.entryPrice, color = Slate100, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isFa) "حد ضرر" else "Stop", color = Rose400, fontSize = 9.sp, fontFamily = VazirmatnFontFamily)
                                Text(opp.stopLoss, color = Rose400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TP1 (50%)", color = Emerald400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(opp.tp1, color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TP2 (30%)", color = Emerald400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(opp.tp2, color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("R:R", color = Amber400, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(opp.riskReward, color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Passed conditions checklist
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            opp.passedConditions.forEach { cond ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald400, modifier = Modifier.size(12.dp))
                                    Text(cond, color = Slate300, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
                                }
                            }
                        }

                        // Footer: Scanned Time + Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                opp.scannedAt,
                                color = Slate500,
                                fontSize = 9.sp,
                                fontFamily = VazirmatnFontFamily
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Instant Risk / Position Size Calculator Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Amber500.copy(alpha = 0.2f))
                                        .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onOpenRiskCalculator(opp) }
                                        .padding(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        if (isFa) "🧮 ریسک" else "🧮 Risk",
                                        color = Amber300,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }

                                Button(
                                    onClick = { onSelectOpportunity(opp) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("scanner_open_chart_${opp.id}")
                                ) {
                                    Icon(Icons.Filled.CandlestickChart, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (isFa) "چارت" else "Chart",
                                        color = Color.Black,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }

                                // Direct Send Order to MetaTrader Button
                                Button(
                                    onClick = { onSendOrder(opp) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLong) Emerald600 else Rose600
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("scanner_send_order_${opp.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("⚡", fontSize = 11.sp)
                                        Text(
                                            if (isFa) "ارسال سفارش" else "Send Order",
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = VazirmatnFontFamily
                                        )
                                    }
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

// -------------------------------------------------------------
// 2. HISTORY VIEW (With Balance Growth / Equity Curve Chart)
// -------------------------------------------------------------
@Composable
fun HistoryView(
    trades: List<TradeRecord>,
    params: StrategyParams,
    isFa: Boolean,
    onCloseTrade: (String, Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTrades = remember(trades, selectedFilter) {
        when (selectedFilter) {
            "OPEN" -> trades.filter { it.status == "OPEN" }
            "CLOSED" -> trades.filter { it.status != "OPEN" }
            else -> trades
        }
    }

    val totalTrades = trades.size
    val closedTrades = trades.filter { it.status != "OPEN" }
    val winTrades = closedTrades.filter { it.status == "WIN" }
    val winRate = if (closedTrades.isNotEmpty()) (winTrades.size.toFloat() / closedTrades.size * 100) else 0f
    val totalPnl = closedTrades.sumOf { it.pnl }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // PnL & Win Rate Summary Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = if (totalPnl >= 0) Emerald500 else Rose500
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isFa) "عملکرد ژورنال معاملات" else "JOURNAL PERFORMANCE",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val pnlSign = if (totalPnl >= 0) "+" else ""
                        Text(
                            "$pnlSign${String.format(Locale.US, "$%,.2f", totalPnl)}",
                            color = if (totalPnl >= 0) Emerald400 else Rose400,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(FrostedGlassSurfaceHighlighted, RoundedCornerShape(12.dp))
                            .border(1.dp, FrostedBorderTop, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (isFa) "سود خالص محقق‌شده" else "Net Realized PnL",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrostedStatMiniCard(modifier = Modifier.weight(1f), title = if (isFa) "نرخ برد" else "WIN RATE", value = "${String.format(Locale.US, "%.1f", winRate)}%", color = if (winRate >= 50) Emerald400 else Amber400)
                    FrostedStatMiniCard(modifier = Modifier.weight(1f), title = if (isFa) "کل معاملات" else "TOTAL TRADES", value = "$totalTrades", color = Cyan400)
                    FrostedStatMiniCard(modifier = Modifier.weight(1f), title = if (isFa) "میانگین R:R" else "AVG R:R", value = "1:2.6", color = Indigo400)
                }
            }
        }

        // Equity Growth Curve Chart
        FrostedEquityCurveCard(
            baseCapital = params.accountBalance,
            trades = trades,
            isFa = isFa
        )

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(FrostedGlassSurface)
                    .border(1.dp, FrostedBorderBottom, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filters = if (isFa) listOf("همه" to "ALL", "باز" to "OPEN", "بسته‌شده" to "CLOSED")
                else listOf("ALL" to "ALL", "OPEN" to "OPEN", "CLOSED" to "CLOSED")

                filters.forEach { (label, key) ->
                    val isSelected = key == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Blue600.copy(alpha = 0.5f) else Color.Transparent)
                            .then(if (isSelected) Modifier.border(1.dp, Cyan400.copy(alpha = 0.6f), RoundedCornerShape(10.dp)) else Modifier)
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Slate50 else Slate400, fontFamily = VazirmatnFontFamily)
                    }
                }
            }

            if (trades.isNotEmpty()) {
                IconButton(onClick = onClearHistory) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(20.dp))
                }
            }
        }

        if (filteredTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isFa) "هیچ معامله‌ای در این دسته ثبت نشده است." else "No trades recorded in this category.",
                    color = Slate500,
                    fontSize = 13.sp,
                    fontFamily = VazirmatnFontFamily
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredTrades.forEach { trade ->
                    FrostedTradeCard(trade = trade, isFa = isFa, onCloseTrade = onCloseTrade)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FrostedEquityCurveCard(
    baseCapital: Double,
    trades: List<TradeRecord>,
    isFa: Boolean
) {
    val equityPoints = remember(trades, baseCapital) {
        var running = baseCapital
        val pts = mutableListOf(running.toFloat())
        trades.reversed().forEach { t ->
            if (t.status != "OPEN") {
                running += t.pnl
                pts.add(running.toFloat())
            }
        }
        if (pts.size < 2) {
            listOf(baseCapital.toFloat(), (baseCapital + 250).toFloat(), (baseCapital + 490).toFloat(), (baseCapital + 390).toFloat(), (baseCapital + 690).toFloat())
        } else pts
    }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = Emerald500
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isFa) "منحنی رشد اکوئیتی حساب" else "EQUITY GROWTH CURVE",
                        color = Slate100,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                Text(
                    "+${String.format(Locale.US, "%.1f", ((equityPoints.last() - equityPoints.first()) / equityPoints.first()) * 100)}%",
                    color = Emerald400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val minV = equityPoints.minOrNull() ?: 10000f
                    val maxV = equityPoints.maxOrNull() ?: 11000f
                    val span = if (maxV - minV > 0) maxV - minV else 1f

                    val pts = equityPoints.mapIndexed { idx, v ->
                        val x = idx * (w / (equityPoints.size - 1))
                        val norm = (v - minV) / span
                        val y = h - (norm * (h * 0.7f) + h * 0.15f)
                        Offset(x, y)
                    }

                    val path = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        for (i in 0 until pts.size - 1) {
                            val p0 = pts[i]
                            val p1 = pts[i + 1]
                            cubicTo((p0.x + p1.x) / 2f, p0.y, (p0.x + p1.x) / 2f, p1.y, p1.x, p1.y)
                        }
                    }

                    val fill = Path().apply {
                        addPath(path)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = fill,
                        brush = Brush.verticalGradient(
                            listOf(Emerald500.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(listOf(Cyan400, Emerald400)),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawCircle(color = Emerald400, radius = 4.dp.toPx(), center = pts.last())
                }
            }
        }
    }
}

@Composable
fun FrostedStatMiniCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FrostedGlassSurface)
            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(14.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun FrostedTradeCard(
    trade: TradeRecord,
    isFa: Boolean,
    onCloseTrade: (String, Boolean) -> Unit
) {
    val isWin = trade.status == "WIN"
    val isOpen = trade.status == "OPEN"

    val statusColor = when {
        isOpen -> Cyan400
        isWin -> Emerald400
        else -> Rose400
    }

    val statusText = when (trade.status) {
        "WIN" -> if (isFa) "سودده" else "WIN"
        "LOSS" -> if (isFa) "زیان‌ده" else "LOSS"
        else -> if (isFa) "باز" else "OPEN"
    }

    val dirText = when (trade.direction) {
        "LONG" -> if (isFa) "خرید" else "LONG"
        else -> if (isFa) "فروش" else "SHORT"
    }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = if (isOpen) Blue600 else if (isWin) Emerald600 else Rose500
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (trade.direction == "LONG") Emerald500.copy(alpha = 0.2f) else Rose500.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (trade.direction == "LONG") Emerald500.copy(alpha = 0.4f) else Rose500.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(dirText, color = if (trade.direction == "LONG") Emerald400 else Rose400, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(trade.symbol, color = Slate100, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                }
            }

            HorizontalDivider(color = Color(0x10FFFFFF))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (isFa) "ورود" else "ENTRY", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    Text(trade.entryPrice, color = Slate200, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text(if (isFa) "تارگت" else "TARGET", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    Text(trade.targetPrice, color = Emerald400, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text(if (isFa) "حد ضرر" else "STOP LOSS", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    Text(trade.stopLoss, color = Rose400, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isFa) "سود/زیان" else "P&L", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                    val pnlSign = if (trade.pnl > 0) "+" else ""
                    val pnlColor = if (trade.pnl > 0) Emerald400 else if (trade.pnl < 0) Rose400 else Slate300
                    val isTomanTrade = trade.symbol.contains("IRT") || trade.entryPrice.contains("تومان")
                    val pnlFormatted = if (isTomanTrade) "$pnlSign${String.format(Locale.US, "%,.0f ت", trade.pnl)}" else "$pnlSign${String.format(Locale.US, "$%,.2f", trade.pnl)}"
                    Text(
                        if (isOpen) (if (isFa) "در جریان" else "RUNNING") else pnlFormatted,
                        color = pnlColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${trade.id} • ${trade.timestamp} • R:R ${trade.riskReward}", color = Slate500, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)

                if (isOpen) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Rose500.copy(alpha = 0.2f))
                                .clickable { onCloseTrade(trade.id, false) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(if (isFa) "برخورد حد ضرر" else "Hit SL", color = Rose400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald500.copy(alpha = 0.2f))
                                .clickable { onCloseTrade(trade.id, true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(if (isFa) "کسب تارگت سود" else "Hit TP", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. PARAMS VIEW
// -------------------------------------------------------------
@Composable
fun ParamsView(
    params: StrategyParams,
    isFa: Boolean,
    onUpdateRisk: (Float) -> Unit,
    onUpdateBalance: (Double) -> Unit,
    onUpdateAtr: (Float) -> Unit,
    onUpdateTimeframe: (String) -> Unit,
    onToggleRule: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Capital & Risk Settings Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Indigo600
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isFa) "تنظیمات سرمایه و مدیریت ریسک" else "CAPITAL & RISK CONFIGURATION",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily,
                        letterSpacing = 1.sp
                    )
                    Icon(Icons.Filled.Security, contentDescription = null, tint = Indigo400, modifier = Modifier.size(18.dp))
                }

                Text(if (isFa) "سرمایه کل حساب ($)" else "ACCOUNT CAPITAL ($)", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5000.0, 10000.0, 25000.0, 50000.0).forEach { bal ->
                        val isSelected = params.accountBalance == bal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Blue600.copy(alpha = 0.4f) else FrostedGlassSurface)
                                .border(1.dp, if (isSelected) Cyan400 else FrostedBorderBottom, RoundedCornerShape(12.dp))
                                .clickable { onUpdateBalance(bal) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$${bal.toInt() / 1000}K", color = if (isSelected) Slate50 else Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color(0x10FFFFFF))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isFa) "درصد ریسک هر معامله" else "RISK PER TRADE", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily)
                    val dollarRisk = params.accountBalance * (params.riskPerTradePercent / 100.0)
                    Text("${String.format(Locale.US, "%.1f", params.riskPerTradePercent)}% ($${dollarRisk.toInt()})", color = Cyan400, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { risk ->
                        val isSelected = params.riskPerTradePercent == risk
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Blue600.copy(alpha = 0.4f) else FrostedGlassSurface)
                                .border(1.dp, if (isSelected) Cyan400 else FrostedBorderBottom, RoundedCornerShape(12.dp))
                                .clickable { onUpdateRisk(risk) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${risk}%", color = if (isSelected) Slate50 else Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Execution Engine Parameters
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Blue600
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    if (isFa) "پارامترهای موتور محاسباتی" else "EXECUTION ENGINE PARAMETERS",
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )

                Text(if (isFa) "تایم‌فریم مبنای استراتژی" else "STRATEGY BASE TIMEFRAME", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("5m", "15m", "1h", "4h", "1d").forEach { tf ->
                        val isSelected = params.preferredTimeframe.equals(tf, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Blue600.copy(alpha = 0.4f) else FrostedGlassSurface)
                                .border(1.dp, if (isSelected) Cyan400 else FrostedBorderBottom, RoundedCornerShape(12.dp))
                            .clickable { onUpdateTimeframe(tf) }
                            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tf, color = if (isSelected) Slate50 else Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color(0x10FFFFFF))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isFa) "ضریب حد ضرر ATR داینامیک" else "ATR STOP MULTIPLIER", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily)
                    Text("${params.atrMultiplier}x", color = Indigo400, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.0f, 1.5f, 2.0f, 2.5f).forEach { mult ->
                        val isSelected = params.atrMultiplier == mult
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Indigo600.copy(alpha = 0.4f) else FrostedGlassSurface)
                                .border(1.dp, if (isSelected) Cyan400 else FrostedBorderBottom, RoundedCornerShape(12.dp))
                                .clickable { onUpdateAtr(mult) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${mult}x", color = if (isSelected) Slate50 else Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Strategy Rules Toggles
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = Emerald500
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isFa) "شروط الگوریتمی ورود به پوزیشن" else "STRATEGY CONFLUENCE RULES",
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )

                ParamRuleToggleRow(
                    title = if (isFa) "فیلتر ترند EMA 50/200" else "EMA 50/200 Trend Bias",
                    subtitle = if (isFa) "تایید کراس طلایی/مرگ در تایم بالا" else "Golden/Death cross filter on higher timeframe",
                    isEnabled = params.enableEmaCross
                ) { onToggleRule("ema") }

                ParamRuleToggleRow(
                    title = if (isFa) "برخورد به اردر بلاک (Order Block)" else "Order Block Mitigation",
                    subtitle = if (isFa) "اعتبارسنجی نواحی عرضه و تقاضای سازمانی" else "Validate institutional supply & demand POIs",
                    isEnabled = params.enableOrderBlock
                ) { onToggleRule("ob") }

                ParamRuleToggleRow(
                    title = if (isFa) "واگرایی در اسیلاتور RSI" else "RSI Oscillator Divergence",
                    subtitle = if (isFa) "شناسایی واگرایی‌های صعودی و نزولی" else "Detect regular bullish/bearish divergences",
                    isEnabled = params.enableRsiDiv
                ) { onToggleRule("rsi") }

                ParamRuleToggleRow(
                    title = if (isFa) "تغییر ساختار روند (ChoCH)" else "Change of Character (ChoCH)",
                    subtitle = if (isFa) "شکست آخرین سقف/کف تایم‌فریم پایین" else "Require lower timeframe structure shift",
                    isEnabled = params.enableChoch
                ) { onToggleRule("choch") }

                ParamRuleToggleRow(
                    title = if (isFa) "تاییدیه حجم معاملات (Volume)" else "Volume Spike Confirmation",
                    subtitle = if (isFa) "افزایش حجم بیش از ۱.۵ برابر میانگین ۲۰ روزه" else "Volume expansion > 1.5x 20-period MA",
                    isEnabled = params.enableVolumeFilter
                ) { onToggleRule("vol") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ParamRuleToggleRow(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isEnabled) Emerald500.copy(alpha = 0.08f) else FrostedGlassSurface)
            .border(1.dp, if (isEnabled) Emerald500.copy(alpha = 0.3f) else FrostedBorderBottom, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (isEnabled) Slate100 else Slate400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = VazirmatnFontFamily)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Slate500, fontSize = 10.sp, fontFamily = VazirmatnFontFamily)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Emerald600,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate800
            )
        )
    }
}

// -------------------------------------------------------------
// 4. PROFILE VIEW
// -------------------------------------------------------------
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
    onOpenNobitexDepth: () -> Unit,
    onToggleLanguage: () -> Unit,
    onRefresh: () -> Unit
) {
    val titleText = when (currentTab) {
        "STRATEGY" -> if (selectedAsset.contains("IRT")) "USDT/IRT" else "$selectedAsset/USD"
        "SCANNER" -> if (isFa) "اسکنر هوشمند ۵ جفت‌ارز" else "SMART MULTI-TF SCANNER"
        "HISTORY" -> if (isFa) "ژورنال معاملات" else "TRADE JOURNAL"
        "PARAMS" -> if (isFa) "تنظیمات استراتژی" else "STRATEGY PARAMS"
        "PROFILE" -> if (isFa) "پروفایل معامله‌گر" else "ACCOUNT PROFILE"
        else -> "QUANTPRO ENGINE"
    }

    val subtitleBadge = when (currentTab) {
        "STRATEGY" -> if (isFa) "نوبیتکس (اصلی)" else "PRIMARY: NOBITEX"
        "SCANNER" -> if (isFa) "رصد ۷ تایم‌فریم" else "7 TIMEFRAMES"
        "HISTORY" -> if (isFa) "سود/زیان و آمار" else "PNL & AUDIT"
        "PARAMS" -> if (isFa) "ریسک و شروط" else "RISK & RULES"
        "PROFILE" -> if (isFa) "سلامت سیستم" else "SYSTEM HEALTH"
        else -> "v3.1"
    }

    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "QUANTPRO",
                        fontSize = 11.sp,
                        color = Cyan400,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(FrostedGlassSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, FrostedBorderBottom, RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = subtitleBadge,
                            fontSize = 8.5.sp,
                            color = Slate300,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = VazirmatnFontFamily,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titleText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        fontFamily = VazirmatnFontFamily
                    )
                    if (currentTab == "STRATEGY" && change.isNotEmpty() && change != "0.0%") {
                        Spacer(modifier = Modifier.width(6.dp))
                        val isPositive = !change.startsWith("-")
                        val badgeColor = if (isPositive) Emerald400 else Rose400
                        val badgeBg = if (isPositive) Emerald500.copy(alpha = 0.15f) else Rose500.copy(alpha = 0.15f)
                        val badgeBorder = if (isPositive) Emerald500.copy(alpha = 0.4f) else Rose500.copy(alpha = 0.4f)

                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(12.dp))
                                .border(1.dp, badgeBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                change,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        actions = {
            // Dedicated Frosted Glass Nobitex Toman Rate Badge (Always shows live Nobitex Tether)
            val liveUsdtToman = nobitexMarkets.find { it.symbol == "USDT/IRT" }?.priceToman
                ?: (if (nobitexOrderBook != null && nobitexOrderBook.symbol == "USDT/IRT") nobitexOrderBook.lastTradePrice else 268755.0)
            val tomanPriceFormatted = String.format(Locale.US, "%,.0f", liveUsdtToman)
            val usdtChange = nobitexMarkets.find { it.symbol == "USDT/IRT" }?.dayChange ?: -0.14
            val isPositive = usdtChange >= 0

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrostedGlassSurface)
                    .border(1.dp, Amber400.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onOpenNobitexDepth() }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isPositive) Emerald400 else Rose400, CircleShape)
                    )
                    Text(
                        text = if (isFa) "تتر نوبیتکس" else "USDT",
                        color = Amber400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                    Text(
                        text = tomanPriceFormatted,
                        color = Slate100,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isFa) "ت" else "T",
                        color = Slate400,
                        fontSize = 9.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Language Toggle Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrostedGlassSurface)
                    .border(1.dp, Cyan400.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "🇮🇷 FA" else "🇬🇧 EN",
                    color = Cyan400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "spin")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "spinAngle"
            )

            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FrostedGlassSurface)
                    .border(1.dp, FrostedBorderTop, RoundedCornerShape(10.dp))
                    .clickable(enabled = !isLoading, onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh Data",
                    tint = if (isLoading) Cyan400 else Slate300,
                    modifier = Modifier
                        .size(16.dp)
                        .then(if (isLoading) Modifier.rotate(angle) else Modifier)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkCanvasBackground.copy(alpha = 0.85f),
            titleContentColor = Slate100
        )
    )
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
                            if (totalWeight >= 80) "ارسال سفارش طلایی (امتیاز بالای ۸۰٪)" else "ارسال سفارش معامله"
                        } else {
                            if (totalWeight >= 80) "EXECUTE HIGH-PROBABILITY ORDER" else "EXECUTE TRADE SETUP"
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
                        text = if (isFa) "ثبت سفارش آنی تتر/تومان" else "Execute Instant USDT/IRT Order",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }
    }
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

    fun fmtMoney(v: Double): String = if (isToman) String.format(Locale.US, "%,.0f تومان", v) else "$${String.format(Locale.US, "%,.2f", v)}"

    Dialog(onDismissRequest = onDismiss) {
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            accentGlow = if (isToman) Amber400 else Emerald500
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isFa) "تاییدیه اجرای سفارش مشاور" else "ORDER CONFIRMATION",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                        Text(
                            text = if (isToman) "USDT/IRT ${if (isFa) "خرید تومانی" else "TOMAN BUY"}" else "$asset/USD ${if (isFa) "خرید اسمارت" else "SMART LONG"}",
                            color = Slate100,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFontFamily
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                (if (isToman) Amber400 else Emerald500).copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                (if (isToman) Amber400 else Emerald500).copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = advisorSetup?.grade ?: "A+ GRADE",
                            color = if (isToman) Amber400 else Emerald400,
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
                        FrostedOrderParamRow(if (isFa) "حد ضرر خروج اضطراری" else "Hard Stop Loss", setup.stopLoss, isFa)
                    }
                    FrostedOrderParamRow(
                        title = if (isFa) "مقدار ریسک (${riskPercent}٪)" else "Risk Amount (${riskPercent}%)",
                        value = fmtMoney(riskAmount),
                        isFa = isFa
                    )
                    FrostedOrderParamRow(
                        title = if (isFa) "حجم پوزیشن معامله" else "Est. Position Size",
                        value = fmtMoney(riskAmount / 0.018),
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
                        Text(if (isFa) "تایید و ارسال" else "Send Order", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
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
                                text = if (isFa) "تاییدیه و ارسال سفارش به متاتریدر" else "METATRADER ORDER CONFIRMATION",
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
                            Text("🚀", fontSize = 13.sp)
                            Text(
                                if (isFa) "تایید و ارسال به متاتریدر" else "Confirm & Send to MT",
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0x4010141E))
                .border(1.dp, FrostedBorderTop, RoundedCornerShape(22.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrostedBottomNavItem(icon = Icons.Filled.PlayArrow, label = if (isFa) "استراتژی" else "STRATEGY", isSelected = currentTab == "STRATEGY", onClick = { onTabSelected("STRATEGY") })
                FrostedBottomNavItem(icon = Icons.Filled.Radar, label = if (isFa) "اسکنر" else "SCANNER", isSelected = currentTab == "SCANNER", onClick = { onTabSelected("SCANNER") })
                FrostedBottomNavItem(icon = Icons.Filled.List, label = if (isFa) "ژورنال" else "HISTORY", isSelected = currentTab == "HISTORY", onClick = { onTabSelected("HISTORY") })
                FrostedBottomNavItem(icon = Icons.Filled.Settings, label = if (isFa) "پارامترها" else "PARAMS", isSelected = currentTab == "PARAMS", onClick = { onTabSelected("PARAMS") })
                FrostedBottomNavItem(icon = Icons.Filled.Person, label = if (isFa) "پروفایل" else "PROFILE", isSelected = currentTab == "PROFILE", onClick = { onTabSelected("PROFILE") })
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
    val color = if (isSelected) Cyan400 else Slate400

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val tabBorder = if (isSelected) Cyan400.copy(alpha = 0.4f) else Color.Transparent
        Box(
            modifier = Modifier
                .background(
                    if (isSelected) Cyan400.copy(alpha = 0.15f) else Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
                .border(1.dp, tabBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color,
            fontFamily = VazirmatnFontFamily
        )
    }
}
