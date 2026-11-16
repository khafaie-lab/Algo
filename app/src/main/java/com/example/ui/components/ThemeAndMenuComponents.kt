package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Live Theme Switcher Grid Section usable inside Settings, Drawer, or Modal Picker.
 */
@Composable
fun LiveThemeSwitcherSection(
    currentPreset: AppThemePreset,
    isFa: Boolean,
    onSelectPreset: (AppThemePreset) -> Unit
) {
    val presets = AppThemePreset.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.chunked(2).forEach { rowPresets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPresets.forEach { preset ->
                    val isSelected = preset == currentPreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_preset_${preset.id}")
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) IbkrBid.copy(alpha = 0.14f) else IbkrSurfaceVariant)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) IbkrBid else IbkrBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectPreset(preset) }
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    preset.previewSwatches.forEach { swatchColor ->
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                                .border(0.8.dp, IbkrBorder, CircleShape)
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = IbkrBid,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isFa) preset.nameFa else preset.nameEn,
                                color = IbkrTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = MinimalPersianFontFamily,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveThemeSwitcherDialog(
    currentPreset: AppThemePreset,
    isFa: Boolean,
    onSelectPreset: (AppThemePreset) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IbkrSurface)
                .border(1.dp, IbkrBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("live_theme_switcher_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = IbkrBid,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isFa) "سوییچر زنده قالب (Theme)" else "Live Theme Switcher",
                                color = IbkrTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MinimalPersianFontFamily
                            )
                            Text(
                                text = if (isFa) "تغییر آنی ظاهر تمام صفحات و نمودار" else "Instant palette switch across UI & charts",
                                color = IbkrTextSecondary,
                                fontSize = 10.5.sp,
                                fontFamily = MinimalPersianFontFamily
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = IbkrTextSecondary)
                    }
                }

                LiveThemeSwitcherSection(
                    currentPreset = currentPreset,
                    isFa = isFa,
                    onSelectPreset = onSelectPreset
                )
            }
        }
    }
}

/**
 * IBKR Institutional Navigation Drawer opened from the top-left 3-line Menu button.
 */
@Composable
fun AppNavigationDrawerContent(
    currentTab: String,
    selectedAsset: String,
    price: String,
    currentPreset: AppThemePreset,
    isFa: Boolean,
    onNavigateToTab: (String) -> Unit,
    onOpenAddCrypto: () -> Unit,
    onOpenOrderBook: () -> Unit,
    onOpenBacktestLab: () -> Unit = {},
    onOpenAboutGuide: () -> Unit = {},
    onSelectTheme: (AppThemePreset) -> Unit,
    onToggleLanguage: () -> Unit,
    onRefreshFeeds: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 310.dp)
            .testTag("app_navigation_drawer"),
        drawerContainerColor = IbkrSurface,
        drawerContentColor = IbkrTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drawer Brand Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(IbkrSurfaceVariant)
                    .border(1.dp, IbkrBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = if (isFa) "ترمینال معاملاتی QuantPro" else "QuantPro Terminal",
                            color = IbkrTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MinimalPersianFontFamily
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(IbkrGreen)
                            )
                            Text(
                                text = if (selectedAsset.contains("IRT")) "USDT/IRT • $price" else "$selectedAsset/USDT • $price",
                                color = IbkrTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onCloseDrawer, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Menu", tint = IbkrTextSecondary)
                    }
                }
            }

            Text(
                text = if (isFa) "ناوبری بخش‌ها" else "TERMINAL SECTIONS",
                color = IbkrTextSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MinimalPersianFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            val navItems = listOf(
                Triple("MARKET", if (isFa) "دیده‌بان بازارها" else "Watchlists", Icons.Filled.List),
                Triple("SCANNER", if (isFa) "اسکنر هوشمند همگرایی" else "Smart Scanner", Icons.Filled.Radar),
                Triple("CHART", if (isFa) "نمودار دوگانه تریدینگ‌ویو" else "Dual Chart Engine", Icons.Filled.CandlestickChart),
                Triple("JOURNAL", if (isFa) "پورتفوی و ژورنال" else "Portfolio & Journal", Icons.Filled.AccountBalanceWallet),
                Triple("MORE", if (isFa) "تنظیمات و ریسک" else "Settings & Risk", Icons.Filled.Settings)
            )

            navItems.forEach { (tabKey, title, icon) ->
                val isSelected = currentTab == tabKey
                DrawerMenuRow(
                    icon = icon,
                    label = title,
                    isSelected = isSelected,
                    onClick = {
                        onNavigateToTab(tabKey)
                        onCloseDrawer()
                    }
                )
            }

            HorizontalDivider(color = IbkrBorder, modifier = Modifier.padding(vertical = 2.dp))

            Text(
                text = if (isFa) "ابزارهای سریع دیده‌بان" else "QUICK ACTIONS",
                color = IbkrTextSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MinimalPersianFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            DrawerMenuRow(
                icon = Icons.Filled.AddCircleOutline,
                label = if (isFa) "افزودن ارز دیجیتال جدید به منتخب‌ها" else "Add Crypto to Favorites",
                isSelected = false,
                tintOverride = IbkrBid,
                onClick = {
                    onCloseDrawer()
                    onOpenAddCrypto()
                }
            )

            DrawerMenuRow(
                icon = Icons.Filled.Science,
                label = if (isFa) "آزمایشگاه بک‌تست و بهینه‌سازی Win Rate" else "Interactive Backtest Lab",
                isSelected = false,
                tintOverride = Amber400,
                onClick = {
                    onCloseDrawer()
                    onOpenBacktestLab()
                }
            )

            DrawerMenuRow(
                icon = Icons.Filled.Layers,
                label = if (isFa) "دفتر سفارشات و عمق بازار نوبیتکس" else "Nobitex Order Book Depth",
                isSelected = false,
                onClick = {
                    onCloseDrawer()
                    onOpenOrderBook()
                }
            )

            DrawerMenuRow(
                icon = Icons.Filled.Refresh,
                label = if (isFa) "بروزرسانی لحظه‌ای قیمت‌ها" else "Refresh Live Feeds",
                isSelected = false,
                onClick = {
                    onRefreshFeeds()
                    onCloseDrawer()
                }
            )

            DrawerMenuRow(
                icon = Icons.Filled.Language,
                label = if (isFa) "تغییر زبان (فارسی / English)" else "Switch Language (EN / FA)",
                isSelected = false,
                onClick = {
                    onToggleLanguage()
                    onCloseDrawer()
                }
            )

            DrawerMenuRow(
                icon = Icons.Filled.Info,
                label = if (isFa) "درباره برنامه و راهنمای جامع" else "About App & User Guide",
                isSelected = false,
                tintOverride = IbkrBid,
                onClick = {
                    onCloseDrawer()
                    onOpenAboutGuide()
                }
            )

            HorizontalDivider(color = IbkrBorder, modifier = Modifier.padding(vertical = 2.dp))

            Text(
                text = if (isFa) "سوییچر زنده قالب" else "LIVE THEME SWITCHER",
                color = IbkrTextSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MinimalPersianFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            LiveThemeSwitcherSection(
                currentPreset = currentPreset,
                isFa = isFa,
                onSelectPreset = onSelectTheme
            )
        }
    }
}

@Composable
private fun DrawerMenuRow(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    tintOverride: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) IbkrBid.copy(alpha = 0.16f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) IbkrBid.copy(alpha = 0.4f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintOverride ?: (if (isSelected) IbkrBid else IbkrTextSecondary),
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            color = if (isSelected) IbkrTextPrimary else IbkrTextPrimary.copy(alpha = 0.9f),
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = MinimalPersianFontFamily
        )
    }
}

/**
 * Interactive Modal Picker to Search & Add/Remove Cryptocurrencies in Watchlist / Favorites.
 */
@Composable
fun AddCryptoSymbolDialog(
    watchlistSymbols: List<String>,
    isFa: Boolean,
    onAddSymbol: (String) -> Unit,
    onRemoveSymbol: (String) -> Unit,
    onSelectAndOpen: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredCatalog = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) SUPPORTED_CRYPTO_CATALOG
        else SUPPORTED_CRYPTO_CATALOG.filter {
            it.symbol.contains(q, ignoreCase = true) ||
                it.nameFa.contains(q, ignoreCase = true) ||
                it.nameEn.contains(q, ignoreCase = true)
        }
    }

    val cleanedCustom = remember(query) {
        query.replace("USDT", "", ignoreCase = true)
            .replace("/IRT", "", ignoreCase = true)
            .replace("/", "")
            .trim()
            .uppercase()
    }
    val canAddCustom = cleanedCustom.length in 2..10 &&
        SUPPORTED_CRYPTO_CATALOG.none { it.symbol.equals(cleanedCustom, ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(18.dp))
                .background(IbkrSurface)
                .border(1.dp, IbkrBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("add_crypto_symbol_dialog")
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
                    Column {
                        Text(
                            text = if (isFa) "انتخاب و افزودن ارز دیجیتال" else "Add Cryptocurrency to Watchlist",
                            color = IbkrTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MinimalPersianFontFamily
                        )
                        Text(
                            text = if (isFa) "جستجو کنید یا نماد دلخواه خود را به منتخب‌ها اضافه کنید" else "Search popular coins or type any ticker symbol",
                            color = IbkrTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = MinimalPersianFontFamily
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = IbkrTextSecondary)
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            text = if (isFa) "جستجو (مثلاً XRP، ADA، کاردانو، PEPE...)" else "Search ticker or name (e.g. XRP, ADA, PEPE...)",
                            fontSize = 12.sp,
                            color = IbkrTextSecondary,
                            fontFamily = MinimalPersianFontFamily
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = IbkrTextSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = IbkrTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IbkrBid,
                        unfocusedBorderColor = IbkrBorder,
                        focusedTextColor = IbkrTextPrimary,
                        unfocusedTextColor = IbkrTextPrimary,
                        focusedContainerColor = IbkrSurfaceVariant,
                        unfocusedContainerColor = IbkrSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_crypto_search_input")
                )

                // Custom Symbol Add Banner
                if (canAddCustom) {
                    val alreadyAdded = watchlistSymbols.contains(cleanedCustom)
                    Button(
                        onClick = {
                            if (!alreadyAdded) onAddSymbol(cleanedCustom)
                            onSelectAndOpen(cleanedCustom)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IbkrBid)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFa) "افزودن نماد «$cleanedCustom/USDT» به دیده‌بان" else "Add Custom Pair $cleanedCustom/USDT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = MinimalPersianFontFamily
                        )
                    }
                }

                // Catalog List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCatalog, key = { it.symbol }) { item ->
                        val isInWatchlist = watchlistSymbols.contains(item.symbol)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isInWatchlist) IbkrBid.copy(alpha = 0.08f) else IbkrSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isInWatchlist) IbkrBid.copy(alpha = 0.45f) else IbkrBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (!isInWatchlist) {
                                        onAddSymbol(item.symbol)
                                    }
                                    onSelectAndOpen(item.symbol)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (item.symbol == "USDT/IRT") "USDT/IRT" else "${item.symbol}/USDT",
                                        color = IbkrTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (isInWatchlist) {
                                        Box(
                                            modifier = Modifier
                                                .background(IbkrGreen.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (isFa) "در لیست" else "ADDED",
                                                color = IbkrGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = MinimalPersianFontFamily
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isFa) "${item.nameFa} • ${item.nameEn}" else item.nameEn,
                                    color = IbkrTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = MinimalPersianFontFamily
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (isInWatchlist && watchlistSymbols.size > 2) {
                                    OutlinedButton(
                                        onClick = { onRemoveSymbol(item.symbol) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(IbkrRed.copy(alpha = 0.5f))
                                        )
                                    ) {
                                        Text(
                                            text = if (isFa) "حذف" else "Remove",
                                            color = IbkrRed,
                                            fontSize = 11.sp,
                                            fontFamily = MinimalPersianFontFamily
                                        )
                                    }
                                } else if (!isInWatchlist) {
                                    Button(
                                        onClick = { onAddSymbol(item.symbol) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = IbkrBid),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isFa) "افزودن" else "Add",
                                            color = Color.White,
                                            fontSize = 11.sp,
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
        }
    }
}
