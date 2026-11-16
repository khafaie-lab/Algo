package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MarketDataViewModel = viewModel()
            val themePreset by mainViewModel.themePreset.collectAsState()
            val view = LocalView.current

            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? Activity)?.window
                    if (window != null) {
                        val controller = WindowCompat.getInsetsController(window, view)
                        controller.isAppearanceLightStatusBars = !themePreset.isDark
                        controller.isAppearanceLightNavigationBars = !themePreset.isDark
                    }
                }
            }

            MyApplicationTheme(themePreset = themePreset) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackground
                ) {
                    QuantProAppRoot(viewModel = mainViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantProAppRoot(viewModel: MarketDataViewModel = viewModel()) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isFa = currentLanguage == "FA"
    val layoutDirection = if (isFa) LayoutDirection.Rtl else LayoutDirection.Ltr

    val themePreset by viewModel.themePreset.collectAsState()
    val watchlistSymbols by viewModel.watchlistSymbols.collectAsState()

    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val price by viewModel.price.collectAsState()
    val change by viewModel.change.collectAsState()
    val isLoadingPrice by viewModel.isLoadingPrice.collectAsState()
    val advisorSetup by viewModel.advisorSetup.collectAsState()
    val params by viewModel.params.collectAsState()
    val trades by viewModel.trades.collectAsState()
    val nobitexOrderBook by viewModel.nobitexOrderBook.collectAsState()
    val isLoadingNobitex by viewModel.isLoadingNobitex.collectAsState()
    val nobitexMarkets by viewModel.nobitexMarkets.collectAsState()
    val serverPriority by viewModel.serverPriority.collectAsState()
    val scannerOpportunities by viewModel.scannerOpportunities.collectAsState()
    val scannerAuditStatus by viewModel.scannerAuditStatus.collectAsState()
    val activeChartOpportunity by viewModel.activeChartOpportunity.collectAsState()
    val requestOpenFullscreen by viewModel.requestOpenFullscreen.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val selectedScannerTimeframe by viewModel.selectedScannerTimeframe.collectAsState()
    val scannerDirectionFilter by viewModel.scannerDirectionFilter.collectAsState()
    val selectedOppForRiskCalc by viewModel.selectedOppForRiskCalc.collectAsState()
    val pendingOrderRequest by viewModel.pendingOrderRequest.collectAsState()
    val backtestLabState by viewModel.backtestLabState.collectAsState()
    val authorProfile by viewModel.authorProfile.collectAsState()
    val context = LocalContext.current

    val candles by viewModel.candles.collectAsState()
    val isLoadingCandles by viewModel.isLoadingCandles.collectAsState()
    val chartType by viewModel.chartType.collectAsState()
    val chartTimeframe by viewModel.chartTimeframe.collectAsState()
    val showEma by viewModel.showEma.collectAsState()
    val showOrderBlocks by viewModel.showOrderBlocks.collectAsState()
    val showVolume by viewModel.showVolume.collectAsState()
    val tomanPrice by viewModel.tomanPrice.collectAsState()
    val chartEngineMode by viewModel.chartEngineMode.collectAsState()

    var showOrderDialog by remember { mutableStateOf(false) }
    var showOrderBookDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAddCryptoDialog by remember { mutableStateOf(false) }
    var showBacktestLab by remember { mutableStateOf(false) }
    var backtestInitSymbol by remember { mutableStateOf("BTC") }
    var backtestInitTf by remember { mutableStateOf("1h") }
    var isFullscreenChart by remember { mutableStateOf(false) }
    var userMessage by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf("MARKET") }
    var moreSubSection by remember { mutableStateOf("PARAMS") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAuthorProfile(context)
    }

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

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isFullscreenChart,
            drawerContent = {
                AppNavigationDrawerContent(
                    currentTab = currentTab,
                    selectedAsset = selectedAsset,
                    price = price,
                    currentPreset = themePreset,
                    isFa = isFa,
                    onNavigateToTab = { tab -> currentTab = tab },
                    onOpenAddCrypto = { showAddCryptoDialog = true },
                    onOpenOrderBook = { showOrderBookDialog = true },
                    onOpenBacktestLab = {
                        backtestInitSymbol = if (selectedAsset.contains("IRT")) "BTC" else selectedAsset
                        backtestInitTf = chartTimeframe
                        showBacktestLab = true
                    },
                    onOpenAboutGuide = {
                        moreSubSection = "ABOUT"
                        currentTab = "MORE"
                    },
                    onSelectTheme = { preset ->
                        viewModel.setThemePreset(preset)
                        userMessage = if (isFa) "قالب به «${preset.nameFa}» تغییر یافت" else "Theme switched to ${preset.nameEn}"
                    },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onRefreshFeeds = {
                        viewModel.fetchRealTimeData()
                        userMessage = if (isFa) "در حال بروزرسانی قیمت‌های لحظه‌ای..." else "Refreshing live market feeds..."
                    },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        ) {
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
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onOpenThemeSwitcher = { showThemeDialog = true },
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
                            "MARKET" -> {
                                MarketView(
                                    markets = nobitexMarkets,
                                    selectedAsset = selectedAsset,
                                    serverPriority = serverPriority,
                                    isFa = isFa,
                                    onOpenAddCrypto = { showAddCryptoDialog = true },
                                    onSelectAsset = { viewModel.selectAsset(it) },
                                    onNavigateToChart = { sym ->
                                        viewModel.selectAsset(sym)
                                        currentTab = "CHART"
                                    },
                                    onOpenOrderBook = { sym ->
                                        viewModel.selectAsset(sym)
                                        showOrderBookDialog = true
                                    },
                                    onQuickOrder = { sym ->
                                        viewModel.selectAsset(sym)
                                        showOrderDialog = true
                                    }
                                )
                            }
                            "CHART" -> {
                                ChartView(
                                    selectedAsset = selectedAsset,
                                    price = price,
                                    tomanPrice = tomanPrice,
                                    change = change,
                                    candles = candles,
                                    isLoadingCandles = isLoadingCandles,
                                    chartType = chartType,
                                    chartTimeframe = chartTimeframe,
                                    showEma = showEma,
                                    showOrderBlocks = showOrderBlocks,
                                    showVolume = showVolume,
                                    isFa = isFa,
                                    chartEngineMode = chartEngineMode,
                                    watchlistSymbols = watchlistSymbols,
                                    onToggleChartEngine = { viewModel.setChartEngineMode(it) },
                                    activeOpportunity = activeChartOpportunity,
                                    isExternalFullscreen = isFullscreenChart,
                                    onSelectAsset = { viewModel.selectAsset(it) },
                                    onSelectTimeframe = { viewModel.setChartTimeframe(it) },
                                    onToggleChartType = { viewModel.setChartType(if (chartType == "CANDLES") "LINE" else "CANDLES") },
                                    onToggleEma = { viewModel.toggleEma() },
                                    onToggleOrderBlocks = { viewModel.toggleOrderBlocks() },
                                    onToggleVolume = { viewModel.toggleVolume() },
                                    onOpenFullscreen = { isFullscreenChart = true },
                                    onCloseFullscreen = { isFullscreenChart = false },
                                    onOpenRiskCalculator = { opp -> viewModel.openRiskCalculator(opp) },
                                    onOpenExecuteDialog = { showOrderDialog = true },
                                    onSendOrder = { opp -> viewModel.requestOrderFromOpportunity(opp) }
                                )
                            }
                            "SCANNER" -> {
                                ScannerView(
                                    opportunities = scannerOpportunities,
                                    isScanning = isScanning,
                                    selectedTimeframe = selectedScannerTimeframe,
                                    selectedDirection = scannerDirectionFilter,
                                    auditStatus = scannerAuditStatus,
                                    isFa = isFa,
                                    onSelectTimeframe = { viewModel.setScannerTimeframe(it) },
                                    onSelectDirection = { viewModel.setScannerDirectionFilter(it) },
                                    onRescan = { viewModel.runSmartScanner() },
                                    onOpenRiskCalculator = { opp -> viewModel.openRiskCalculator(opp) },
                                    onSendOrder = { opp -> viewModel.requestOrderFromOpportunity(opp) },
                                    onOpenBacktestLab = { sym, tf ->
                                        backtestInitSymbol = if (sym.contains("IRT")) "BTC" else sym
                                        backtestInitTf = tf
                                        showBacktestLab = true
                                    },
                                    onSelectOpportunity = { opp ->
                                        viewModel.selectOpportunity(opp, openFullscreen = false)
                                        currentTab = "CHART"
                                        userMessage = if (isFa) "جفت‌ارز ${opp.symbol} (${opp.timeframe}) با تحلیل روی چارت باز شد!" else "Loaded ${opp.symbol} (${opp.timeframe}) with setup on chart!"
                                    }
                                )
                            }
                            "JOURNAL" -> {
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
                            "MORE" -> {
                                MoreView(
                                    params = params,
                                    trades = trades,
                                    serverPriority = serverPriority,
                                    currentTheme = themePreset,
                                    authorProfile = authorProfile,
                                    selectedSubSection = moreSubSection,
                                    isFa = isFa,
                                    onSelectSubSection = { moreSubSection = it },
                                    onSelectTheme = { preset ->
                                        viewModel.setThemePreset(preset)
                                        userMessage = if (isFa) "قالب به «${preset.nameFa}» تغییر یافت" else "Theme switched to ${preset.nameEn}"
                                    },
                                    onSaveAuthorProfile = { updated ->
                                        viewModel.saveAuthorProfile(context, updated)
                                        userMessage = if (isFa) "مشخصات نویسنده (${updated.name}) با موفقیت ذخیره شد." else "Author profile (${updated.name}) saved."
                                    },
                                    onUpdateRisk = { viewModel.setAccountRisk(it) },
                                    onUpdateBalance = { viewModel.setAccountBalance(it) },
                                    onUpdateAtr = { viewModel.setAtrMultiplier(it) },
                                    onUpdateTimeframe = { viewModel.setPreferredTimeframe(it) },
                                    onToggleRule = { viewModel.toggleRule(it) },
                                    onToggleLanguage = { viewModel.toggleLanguage() },
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
        }

        if (showThemeDialog) {
            LiveThemeSwitcherDialog(
                currentPreset = themePreset,
                isFa = isFa,
                onSelectPreset = { preset ->
                    viewModel.setThemePreset(preset)
                    userMessage = if (isFa) "قالب به «${preset.nameFa}» تغییر یافت" else "Theme switched to ${preset.nameEn}"
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        if (showAddCryptoDialog) {
            AddCryptoSymbolDialog(
                watchlistSymbols = watchlistSymbols,
                isFa = isFa,
                onAddSymbol = { rawSym ->
                    val added = viewModel.addWatchlistSymbol(rawSym)
                    if (added) {
                        userMessage = if (isFa) "ارز «${rawSym.uppercase()}» به منتخب‌ها اضافه شد و در حال دریافت قیمت زنده است." else "${rawSym.uppercase()} added to Favorites; fetching live feed."
                    }
                },
                onRemoveSymbol = { sym ->
                    viewModel.removeWatchlistSymbol(sym)
                    userMessage = if (isFa) "نماد $sym از منتخب‌ها حذف شد." else "$sym removed from Favorites."
                },
                onSelectAndOpen = { sym ->
                    viewModel.selectAsset(sym)
                },
                onDismiss = { showAddCryptoDialog = false }
            )
        }

        if (showBacktestLab) {
            BacktestLabDialog(
                initialSymbol = backtestInitSymbol,
                initialTimeframe = backtestInitTf,
                watchlistSymbols = watchlistSymbols.filter { !it.contains("IRT") }.ifEmpty { listOf("BTC", "ETH", "SOL", "TON", "DOGE") },
                currentParams = params,
                backtestState = backtestLabState,
                isFa = isFa,
                onRunBacktest = { sym, tf, limit, minScore, atrMult, rr, dir ->
                    viewModel.runInteractiveBacktest(
                        symbol = sym,
                        timeframe = tf,
                        candleLimit = limit,
                        minScoreThreshold = minScore,
                        atrMultiplier = atrMult,
                        targetRr = rr,
                        directionFilter = dir
                    )
                },
                onApplyParams = { atrMult, tf ->
                    viewModel.setAtrMultiplier(atrMult)
                    viewModel.setPreferredTimeframe(tf.uppercase())
                    viewModel.runSmartScanner()
                    showBacktestLab = false
                    userMessage = if (isFa) "پارامترهای بهینه‌شده (ATR=${atrMult}x و TF=${tf.uppercase()}) اعمال شد و اسکن مجدد آغاز شد." else "Applied ATR=${atrMult}x & TF=${tf.uppercase()} to live strategy & started rescan."
                },
                onDismiss = {
                    showBacktestLab = false
                    viewModel.clearBacktestLabState()
                }
            )
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
                        direction = advisorSetup?.type ?: "LONG",
                        entryPrice = price,
                        riskPercent = params.riskPerTradePercent,
                        balance = params.accountBalance,
                        customSl = advisorSetup?.stopLoss?.takeIf { !it.contains("Calculating") && !it.contains("محاسبه") && it != "--" },
                        customTp = advisorSetup?.tp1?.takeIf { it != "--" },
                        customRiskReward = advisorSetup?.riskReward?.takeIf { it != "--" }
                    )
                    userMessage = if (isFa) "معامله آزمایشی (شبیه‌سازی) ${newTrade.id} در ژورنال ثبت شد!" else "Paper trade ${newTrade.id} recorded in simulation journal!"
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
                        "معامله آزمایشی ${newTrade.id} (${req.symbol}) در ژورنال شبیه‌سازی ثبت شد و پارامترها در کلیپ‌بورد کپی شدند."
                    else
                        "Paper trade ${newTrade.id} (${req.symbol}) recorded in simulation journal! Parameters copied to clipboard."
                }
            )
        }
    }
}

