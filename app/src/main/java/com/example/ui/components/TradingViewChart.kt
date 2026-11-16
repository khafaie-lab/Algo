package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.ui.theme.*
import java.util.Locale

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

/**
 * Dual-Engine TradingView Chart Card (IBKR Institutional Style).
 * Engine A: Binance TradingView Cloud Feed (Streamed directly via TradingView CDN, bypassing regional IP filtering)
 * Engine B: Quant / Nobitex Engine (Standalone Lightweight Charts with SL/TP overlays and Toman conversion)
 */
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
    chartEngineMode: String = "BINANCE_TV_CLOUD",
    onToggleChartEngine: (String) -> Unit = {},
    activeOpportunity: ScannerOpportunity? = null,
    isExternalFullscreen: Boolean = false,
    onOpenFullscreen: () -> Unit = {},
    onCloseFullscreen: () -> Unit = {},
    onSelectTimeframe: (String) -> Unit,
    onToggleChartType: () -> Unit = {},
    onToggleEma: () -> Unit = {},
    onToggleOrderBlocks: () -> Unit = {},
    onToggleVolume: () -> Unit = {},
    onSelectAsset: (String) -> Unit = {},
    onOpenRiskCalculator: ((ScannerOpportunity?) -> Unit)? = null,
    onSendOpportunityOrder: ((ScannerOpportunity) -> Unit)? = null
) {
    var isInternalFullscreen by remember { mutableStateOf(false) }
    val isFullscreen = isInternalFullscreen || isExternalFullscreen

    // Auto default to Quant Local for USDT/IRT since it is a domestic Toman pair
    val effectiveEngineMode = if (asset == "USDT/IRT") "QUANT_LOCAL" else chartEngineMode

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = IbkrBid
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Dual Engine Mode Selector (Binance Cloud vs Quant Local)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IbkrSurfaceVariant)
                    .border(1.dp, IbkrBorder, RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("engine_toggle_binance")
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (effectiveEngineMode == "BINANCE_TV_CLOUD") IbkrBid.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { onToggleChartEngine("BINANCE_TV_CLOUD") }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (effectiveEngineMode == "BINANCE_TV_CLOUD") IbkrGreen else IbkrTextSecondary)
                        )
                        Text(
                            text = if (isFa) "تریدینگ‌ویو بایننس (بدون فیلتر)" else "Binance TradingView Cloud",
                            fontSize = 11.sp,
                            fontWeight = if (effectiveEngineMode == "BINANCE_TV_CLOUD") FontWeight.Bold else FontWeight.Normal,
                            color = if (effectiveEngineMode == "BINANCE_TV_CLOUD") IbkrTextPrimary else IbkrTextSecondary,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("engine_toggle_quant")
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (effectiveEngineMode == "QUANT_LOCAL") IbkrBid.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { onToggleChartEngine("QUANT_LOCAL") }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (effectiveEngineMode == "QUANT_LOCAL") IbkrBid else IbkrTextSecondary)
                        )
                        Text(
                            text = if (isFa) "موتور کوانت / نوبیتکس" else "Quant / Nobitex Engine",
                            fontSize = 11.sp,
                            fontWeight = if (effectiveEngineMode == "QUANT_LOCAL") FontWeight.Bold else FontWeight.Normal,
                            color = if (effectiveEngineMode == "QUANT_LOCAL") IbkrTextPrimary else IbkrTextSecondary,
                            fontFamily = VazirmatnFontFamily
                        )
                    }
                }
            }

            // Row 2: Symbol Info & Timeframe Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(IbkrBidBg, RoundedCornerShape(6.dp))
                            .border(1.dp, IbkrBid.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CandlestickChart, contentDescription = null, tint = IbkrBid, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (asset.contains("IRT")) "USDT/IRT" else "$asset/USDT",
                                color = IbkrTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                if (effectiveEngineMode == "BINANCE_TV_CLOUD") "BINANCE" else "NOBITEX",
                                color = IbkrBid,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (tomanPrice.isNotEmpty()) {
                            Text(
                                text = tomanPrice,
                                color = IbkrTextSecondary,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Timeframe Chips Horizontal Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IbkrSurface)
                        .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("1m", "5m", "15m", "1h", "4h", "1d").forEach { tf ->
                        val isSelected = chartTimeframe.equals(tf, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) IbkrBid.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { onSelectTimeframe(tf) }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tf,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) IbkrTextPrimary else IbkrTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Row 3: Indicators Toggle (Quant mode) or Cloud Stream Status + Fullscreen Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (effectiveEngineMode == "QUANT_LOCAL") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TvToolChip(
                            label = "EMA 20",
                            isEnabled = showEma,
                            activeColor = IbkrBid,
                            onToggle = onToggleEma
                        )
                        TvToolChip(
                            label = "VOL",
                            isEnabled = showVolume,
                            activeColor = IbkrGreen,
                            onToggle = onToggleVolume
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IbkrBidBg)
                                .border(1.dp, IbkrBid.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "TRADINGVIEW CLOUD FEED",
                                fontSize = 9.sp,
                                color = IbkrBid,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("fullscreen_chart_button")
                        .clip(RoundedCornerShape(6.dp))
                        .background(IbkrSurface)
                        .border(1.dp, IbkrBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            isInternalFullscreen = true
                            onOpenFullscreen()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = IbkrTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Responsive Chart View Container (unmounts inline WebView during Fullscreen to prevent contention)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val responsiveChartHeight = if (maxWidth < 480.dp) {
                    (maxWidth * 0.88f).coerceIn(310.dp, 400.dp)
                } else {
                    (maxWidth * 0.54f).coerceIn(340.dp, 500.dp)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(responsiveChartHeight)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IbkrCanvas)
                        .border(1.dp, IbkrBorder, RoundedCornerShape(10.dp))
                ) {
                    if (!isFullscreen) {
                    if (effectiveEngineMode == "BINANCE_TV_CLOUD") {
                        TradingViewBinanceCloudWebView(
                            asset = asset,
                            timeframe = chartTimeframe,
                            isFa = isFa,
                            onSwitchToQuant = { onToggleChartEngine("QUANT_LOCAL") },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        TradingViewWebView(
                            candles = candles,
                            showEma = showEma,
                            showVolume = showVolume,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isLoading && candles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(IbkrCanvas.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(color = IbkrBid, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                                    Text(
                                        if (isFa) "در حال استخراج کندل‌ها..." else "Loading candles...",
                                        color = IbkrTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                }
                            }
                        } else if (candles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(IbkrCanvas.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.SignalWifiOff, contentDescription = null, tint = IbkrTextSecondary, modifier = Modifier.size(24.dp))
                                    Text(
                                        if (isFa) "داده‌های کندل‌استیک در دسترس نیست" else "No candle data available",
                                        color = IbkrTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = VazirmatnFontFamily
                                    )
                                    Button(
                                        onClick = { onSelectTimeframe(chartTimeframe) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = IbkrSurface),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(if (isFa) "تلاش مجدد" else "Retry", fontSize = 11.sp, color = IbkrTextPrimary, fontFamily = VazirmatnFontFamily)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (isFullscreen) {
        TradingViewFullscreenDialog(
            asset = asset,
            currentPrice = currentPrice,
            candles = candles,
            isLoading = isLoading,
            chartEngineMode = effectiveEngineMode,
            chartTimeframe = chartTimeframe,
            showEma = showEma,
            showVolume = showVolume,
            isFa = isFa,
            onSelectTimeframe = onSelectTimeframe,
            onToggleEma = onToggleEma,
            onToggleVolume = onToggleVolume,
            onToggleChartEngine = onToggleChartEngine,
            onClose = {
                isInternalFullscreen = false
                onCloseFullscreen()
            }
        )
    }
}

private fun buildTradingViewEmbedUrl(asset: String, timeframe: String, isDark: Boolean): String {
    val cleanSym = asset.replace("/", "").replace("IRT", "").replace("-", "").uppercase().trim()
    val binanceSym = if (cleanSym == "USDT" || cleanSym.isEmpty()) "BINANCE:BTCUSDT" else "BINANCE:${cleanSym}USDT"
    val tvInterval = when (timeframe.lowercase().trim()) {
        "1m" -> "1"
        "3m" -> "3"
        "5m" -> "5"
        "15m" -> "15"
        "30m" -> "30"
        "1h" -> "60"
        "2h" -> "120"
        "4h" -> "240"
        "1d", "d" -> "D"
        "1w", "w" -> "W"
        else -> "60"
    }
    val themeStr = if (isDark) "dark" else "light"
    val toolbarHex = if (isDark) "0A0D14" else "FFFFFF"
    return "https://s.tradingview.com/widgetembed/" +
        "?frameElementId=tradingview_mobile" +
        "&symbol=${Uri.encode(binanceSym)}" +
        "&interval=${Uri.encode(tvInterval)}" +
        "&hidesidetoolbar=0" +
        "&hidetoptoolbar=0" +
        "&symboledit=1" +
        "&saveimage=0" +
        "&toolbarbg=$toolbarHex" +
        "&studies=%5B%5D" +
        "&theme=$themeStr" +
        "&style=1" +
        "&timezone=${Uri.encode("Asia/Tehran")}" +
        "&withdateranges=1" +
        "&locale=en"
}

/**
 * Direct TradingView Cloud Embed for Binance Pairs.
 * Loads https://s.tradingview.com/widgetembed/ directly to avoid file:// cross-origin / iframe blocks.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TradingViewBinanceCloudWebView(
    asset: String,
    timeframe: String,
    isFa: Boolean = true,
    onSwitchToQuant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = ThemeState.current.isDark
    val targetUrl = remember(asset, timeframe, isDark) { buildTradingViewEmbedUrl(asset, timeframe, isDark) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf("") }
    var isLoadingPage by remember { mutableStateOf(true) }
    var hasLoadError by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetUrl, reloadKey) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (lastLoadedUrl != targetUrl || hasLoadError) {
            hasLoadError = false
            isLoadingPage = true
            lastLoadedUrl = targetUrl
            wv.loadUrl(targetUrl)
        }
    }

    Box(modifier = modifier.background(IbkrCanvas)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                    setBackgroundColor(
                        android.graphics.Color.rgb(
                            (ThemeState.current.background.red * 255).toInt(),
                            (ThemeState.current.background.green * 255).toInt(),
                            (ThemeState.current.background.blue * 255).toInt()
                        )
                    )

                    val webViewInstance = this
                    try {
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(webViewInstance, true)
                    } catch (_: Exception) {}

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoadingPage = true
                            hasLoadError = false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoadingPage = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoadingPage = false
                                hasLoadError = true
                            }
                        }

                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                            isLoadingPage = false
                            hasLoadError = true
                            webViewRef = null
                            view?.destroy()
                            return true
                        }
                    }

                    lastLoadedUrl = targetUrl
                    loadUrl(targetUrl)
                    webViewRef = this
                }
            },
            update = { wv ->
                webViewRef = wv
                wv.setBackgroundColor(
                    android.graphics.Color.rgb(
                        (ThemeState.current.background.red * 255).toInt(),
                        (ThemeState.current.background.green * 255).toInt(),
                        (ThemeState.current.background.blue * 255).toInt()
                    )
                )
            },
            onRelease = { wv ->
                wv.stopLoading()
                wv.destroy()
                if (webViewRef == wv) webViewRef = null
            }
        )

        if (isLoadingPage && !hasLoadError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(IbkrCanvas.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = IbkrBid, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    Text(
                        text = if (isFa) "در حال اتصال به نمودار ابری TradingView..." else "Loading TradingView Cloud...",
                        color = IbkrTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }

        if (hasLoadError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(IbkrCanvas.copy(alpha = 0.94f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.CloudOff, contentDescription = null, tint = IbkrRed, modifier = Modifier.size(28.dp))
                    Text(
                        text = if (isFa) "اتصال به سرور ابری TradingView برقرار نشد" else "Failed to reach TradingView Cloud",
                        color = IbkrTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                lastLoadedUrl = ""
                                reloadKey++
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IbkrBid)
                        ) {
                            Text(if (isFa) "تلاش مجدد" else "Retry", fontSize = 11.sp, color = Color.White, fontFamily = VazirmatnFontFamily)
                        }
                        OutlinedButton(
                            onClick = onSwitchToQuant,
                            shape = RoundedCornerShape(6.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IbkrBorder))
                        ) {
                            Text(if (isFa) "استفاده از موتور کوانت" else "Use Quant Engine", fontSize = 11.sp, color = IbkrTextPrimary, fontFamily = VazirmatnFontFamily)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TradingViewWebView(
    candles: List<Candle>,
    showEma: Boolean,
    showVolume: Boolean,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }

    fun fmtPrice(v: Float): String {
        val absV = kotlin.math.abs(v)
        return when {
            absV < 1f -> String.format(Locale.US, "%.6f", v)
            absV < 100f -> String.format(Locale.US, "%.4f", v)
            else -> String.format(Locale.US, "%.2f", v)
        }
    }

    fun sendCandlesToWeb(wv: WebView, list: List<Candle>, ema: Boolean, vol: Boolean) {
        if (list.isEmpty()) return
        val sorted = list.sortedBy { it.timestamp }.distinctBy { it.timestamp }
        val sub = if (sorted.size > 500) sorted.takeLast(500) else sorted

        val sb = StringBuilder("[")
        for (i in sub.indices) {
            val c = sub[i]
            val timeSec = c.timestamp / 1000L
            sb.append("{\"time\":").append(timeSec)
                .append(",\"open\":").append(fmtPrice(c.open))
                .append(",\"high\":").append(fmtPrice(c.high))
                .append(",\"low\":").append(fmtPrice(c.low))
                .append(",\"close\":").append(fmtPrice(c.close))
                .append(",\"volume\":").append(String.format(Locale.US, "%.2f", c.volume))
                .append("}")
            if (i < sub.size - 1) sb.append(",")
        }
        sb.append("]")
        val jsonStr = sb.toString()
        val jsCall = "if (window.setChartData) { window.setChartData('$jsonStr', $ema, $vol); }"
        wv.post {
            try {
                wv.evaluateJavascript(jsCall, null)
            } catch (ignored: Exception) {}
        }
    }

    val activePalette = ThemeState.current

    fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)
        return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
    }

    fun applyThemeToWeb(wv: WebView, palette: SemanticColors) {
        val bgHex = colorToHex(palette.background)
        val textHex = colorToHex(palette.text)
        val borderHex = colorToHex(palette.border)
        val legendBg = if (palette.isDark) "rgba(22, 27, 34, 0.78)" else "rgba(255, 255, 255, 0.90)"
        val js = "if (window.setChartTheme) { window.setChartTheme('$bgHex', '$textHex', '$borderHex', '$legendBg', ${palette.isDark}); }"
        wv.post {
            try {
                wv.setBackgroundColor(android.graphics.Color.parseColor(bgHex))
                wv.evaluateJavascript(js, null)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(candles, showEma, showVolume, isPageLoaded, activePalette) {
        if (isPageLoaded && webViewRef != null) {
            applyThemeToWeb(webViewRef!!, activePalette)
            sendCandlesToWeb(webViewRef!!, candles, showEma, showVolume)
        }
    }

    AndroidView(
        modifier = modifier
            .testTag("quantum_chart_webview")
            .onSizeChanged { size ->
                if (isPageLoaded && size.width > 0 && size.height > 0) {
                    webViewRef?.post {
                        try {
                            webViewRef?.evaluateJavascript(
                                "if (window.onViewportResized) { window.onViewportResized(); }",
                                null
                            )
                        } catch (_: Exception) {}
                    }
                }
            },
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                setBackgroundColor(
                    android.graphics.Color.rgb(
                        (ThemeState.current.background.red * 255).toInt(),
                        (ThemeState.current.background.green * 255).toInt(),
                        (ThemeState.current.background.blue * 255).toInt()
                    )
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isPageLoaded = true
                        webViewRef = view
                        if (view != null) {
                            applyThemeToWeb(view, ThemeState.current)
                            sendCandlesToWeb(view, candles, showEma, showVolume)
                            view.postDelayed({
                                try {
                                    view.evaluateJavascript(
                                        "if (window.onViewportResized) { window.onViewportResized(); }",
                                        null
                                    )
                                } catch (_: Exception) {}
                                applyThemeToWeb(view, ThemeState.current)
                                sendCandlesToWeb(view, candles, showEma, showVolume)
                            }, 220)
                        }
                    }

                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                        isPageLoaded = false
                        webViewRef = null
                        view?.destroy()
                        return true
                    }
                }
                loadUrl("file:///android_asset/tradingview_chart.html")
                webViewRef = this
            }
        },
        update = { wv ->
            webViewRef = wv
        },
        onRelease = { wv ->
            wv.stopLoading()
            wv.destroy()
            if (webViewRef == wv) {
                webViewRef = null
                isPageLoaded = false
            }
        }
    )
}

@Composable
fun TvToolChip(
    label: String,
    isEnabled: Boolean,
    activeColor: Color,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isEnabled) activeColor.copy(alpha = 0.2f) else IbkrSurface)
            .border(1.dp, if (isEnabled) activeColor else IbkrBorder, RoundedCornerShape(4.dp))
            .clickable { onToggle() }
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = if (isEnabled) IbkrTextPrimary else IbkrTextSecondary,
            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TradingViewFullscreenDialog(
    asset: String,
    currentPrice: String,
    candles: List<Candle>,
    isLoading: Boolean = false,
    chartEngineMode: String = "BINANCE_TV_CLOUD",
    chartTimeframe: String = "1h",
    showEma: Boolean,
    showVolume: Boolean,
    isFa: Boolean,
    onSelectTimeframe: (String) -> Unit = {},
    onToggleEma: () -> Unit = {},
    onToggleVolume: () -> Unit = {},
    onToggleChartEngine: (String) -> Unit = {},
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.let { window ->
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(IbkrCanvas)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag("fullscreen_chart_container")
        ) {
            val isCompactLandscape = maxHeight < 500.dp || maxWidth > maxHeight

            Column(modifier = Modifier.fillMaxSize()) {
                if (isCompactLandscape) {
                    // Single adaptive unified top toolbar for landscape / compact heights to maximize vertical chart visibility
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IbkrSurface)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (asset.contains("IRT")) "USDT/IRT" else "$asset/USDT",
                            color = IbkrTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = currentPrice,
                            color = IbkrGreen,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (asset != "USDT/IRT") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IbkrBidBg)
                                    .border(1.dp, IbkrBid.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        val nextMode = if (chartEngineMode == "BINANCE_TV_CLOUD") "QUANT_LOCAL" else "BINANCE_TV_CLOUD"
                                        onToggleChartEngine(nextMode)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (chartEngineMode == "BINANCE_TV_CLOUD") "BINANCE TV" else "QUANT ENGINE",
                                    color = IbkrBid,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("1m", "5m", "15m", "1h", "4h", "1d").forEach { tf ->
                                val isSelected = chartTimeframe.equals(tf, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) IbkrBid.copy(alpha = 0.28f) else IbkrSurfaceVariant)
                                        .border(1.dp, if (isSelected) IbkrBid else IbkrBorder, RoundedCornerShape(4.dp))
                                        .clickable { onSelectTimeframe(tf) }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = tf,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) IbkrTextPrimary else IbkrTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (chartEngineMode == "QUANT_LOCAL" || asset == "USDT/IRT") {
                                TvToolChip(
                                    label = "EMA 20",
                                    isEnabled = showEma,
                                    activeColor = IbkrBid,
                                    onToggle = onToggleEma
                                )
                                TvToolChip(
                                    label = "VOL",
                                    isEnabled = showVolume,
                                    activeColor = IbkrGreen,
                                    onToggle = onToggleVolume
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("fullscreen_close_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = IbkrTextPrimary)
                        }
                    }
                } else {
                    // Portrait / Expanded Tablet Header Row 1: Symbol + Price + Engine Badge + Close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IbkrSurface)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (asset.contains("IRT")) "USDT/IRT" else "$asset/USDT",
                                color = IbkrTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = currentPrice,
                                color = IbkrGreen,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (asset != "USDT/IRT") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IbkrBidBg)
                                        .border(1.dp, IbkrBid.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .clickable {
                                            val nextMode = if (chartEngineMode == "BINANCE_TV_CLOUD") "QUANT_LOCAL" else "BINANCE_TV_CLOUD"
                                            onToggleChartEngine(nextMode)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (chartEngineMode == "BINANCE_TV_CLOUD") "BINANCE TV" else "QUANT ENGINE",
                                        color = IbkrBid,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("fullscreen_close_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = IbkrTextPrimary)
                        }
                    }

                    // Portrait Header Row 2: Timeframes + Indicator Toggles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IbkrSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("1m", "5m", "15m", "1h", "4h", "1d").forEach { tf ->
                                val isSelected = chartTimeframe.equals(tf, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) IbkrBid.copy(alpha = 0.28f) else IbkrSurface)
                                        .border(1.dp, if (isSelected) IbkrBid else IbkrBorder, RoundedCornerShape(4.dp))
                                        .clickable { onSelectTimeframe(tf) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tf,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) IbkrTextPrimary else IbkrTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        if (chartEngineMode == "QUANT_LOCAL" || asset == "USDT/IRT") {
                            Row(
                                modifier = Modifier.padding(start = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TvToolChip(
                                    label = "EMA 20",
                                    isEnabled = showEma,
                                    activeColor = IbkrBid,
                                    onToggle = onToggleEma
                                )
                                TvToolChip(
                                    label = "VOL",
                                    isEnabled = showVolume,
                                    activeColor = IbkrGreen,
                                    onToggle = onToggleVolume
                                )
                            }
                        }
                    }
                }

                // Aspect-Ratio Preserving Fullscreen Chart Container
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(IbkrCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    val slotAspect = if (maxHeight > 0.dp) maxWidth / maxHeight else 1.5f
                    val chartSlotModifier = if (slotAspect > 2.35f) {
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = maxHeight * 2.35f)
                            .fillMaxWidth()
                            .border(1.dp, IbkrBorder.copy(alpha = 0.45f))
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Box(modifier = chartSlotModifier) {
                        if (chartEngineMode == "BINANCE_TV_CLOUD" && asset != "USDT/IRT") {
                            TradingViewBinanceCloudWebView(
                                asset = asset,
                                timeframe = chartTimeframe,
                                isFa = isFa,
                                onSwitchToQuant = { onToggleChartEngine("QUANT_LOCAL") },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            TradingViewWebView(
                                candles = candles,
                                showEma = showEma,
                                showVolume = showVolume,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isLoading && candles.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(IbkrCanvas.copy(alpha = 0.85f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = IbkrBid)
                                }
                            } else if (candles.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(IbkrCanvas.copy(alpha = 0.88f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.SignalWifiOff,
                                            contentDescription = null,
                                            tint = IbkrTextSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            if (isFa) "داده‌های کندل‌استیک در دسترس نیست" else "No candle data available",
                                            color = IbkrTextSecondary,
                                            fontSize = 12.sp,
                                            fontFamily = VazirmatnFontFamily
                                        )
                                        Button(
                                            onClick = { onSelectTimeframe(chartTimeframe) },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IbkrBid)
                                        ) {
                                            Text(
                                                if (isFa) "تلاش مجدد" else "Retry",
                                                fontSize = 12.sp,
                                                color = Color.White,
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
        }
    }
}
