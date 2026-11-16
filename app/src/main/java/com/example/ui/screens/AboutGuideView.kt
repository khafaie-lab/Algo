package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AuthorProfile
import com.example.ui.theme.*

@Composable
fun AboutGuideView(
    authorProfile: AuthorProfile,
    isFa: Boolean,
    onSaveAuthorProfile: (AuthorProfile) -> Unit
) {
    var isEditingAuthor by remember { mutableStateOf(false) }
    var draftName by remember(authorProfile) { mutableStateOf(authorProfile.name) }
    var draftTitle by remember(authorProfile) { mutableStateOf(authorProfile.title) }
    var draftContact by remember(authorProfile) { mutableStateOf(authorProfile.contact) }
    var draftNotes by remember(authorProfile) { mutableStateOf(authorProfile.notes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Author Card (موسی خفائی + Editable Profile)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppSurface)
                .border(1.dp, AppAccent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AppAccent, AppBullish)))
                            .border(1.5.dp, AppBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = authorProfile.name.ifBlank { if (isFa) "موسی خفائی" else "Mousa Khafaei" },
                                color = AppText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MinimalPersianFontFamily,
                                modifier = Modifier.testTag("author_name_text")
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(AppAccent.copy(alpha = 0.16f))
                                    .border(1.dp, AppAccent.copy(alpha = 0.45f), RoundedCornerShape(5.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isFa) "نویسنده برنامه" else "Author",
                                    color = AppAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MinimalPersianFontFamily
                                )
                            }
                        }

                        Text(
                            text = authorProfile.title.ifBlank {
                                if (isFa) "طراح و توسعه‌دهنده سیستم‌های معاملاتی کوانتوم"
                                else "Quant Trading Systems Architect & Developer"
                            },
                            color = AppTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = MinimalPersianFontFamily
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (!isEditingAuthor) {
                            draftName = authorProfile.name
                            draftTitle = authorProfile.title
                            draftContact = authorProfile.contact
                            draftNotes = authorProfile.notes
                        }
                        isEditingAuthor = !isEditingAuthor
                    },
                    shape = RoundedCornerShape(9.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppAccent.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_author_profile_button")
                ) {
                    Icon(
                        imageVector = if (isEditingAuthor) Icons.Filled.Close else Icons.Filled.Edit,
                        contentDescription = null,
                        tint = AppAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditingAuthor) (if (isFa) "بستن" else "Close") else (if (isFa) "ویرایش مشخصات" else "Edit Info"),
                        color = AppAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MinimalPersianFontFamily
                    )
                }
            }

            if (authorProfile.contact.isNotBlank()) {
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
                    Icon(Icons.Filled.AlternateEmail, contentDescription = null, tint = AppBullish, modifier = Modifier.size(14.dp))
                    Text(
                        text = authorProfile.contact,
                        color = AppText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (authorProfile.notes.isNotBlank()) {
                Text(
                    text = authorProfile.notes,
                    color = AppTextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 18.sp,
                    fontFamily = MinimalPersianFontFamily
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QuantPro Algo Terminal • v3.1",
                    color = AppText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isFa) "موتور MTF + SMC + Backtest Lab" else "MTF + SMC + Backtest Lab",
                    color = Amber400,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MinimalPersianFontFamily
                )
            }

            // Inline Author Editor
            AnimatedVisibility(visible = isEditingAuthor) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppSurfaceVariant)
                        .border(1.dp, AppAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isFa) "ویرایش و شخصی‌سازی مشخصات نویسنده" else "Customize Author Details",
                        color = AppAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MinimalPersianFontFamily
                    )

                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text(if (isFa) "نام و نام خانوادگی نویسنده" else "Author Name", fontSize = 11.sp, fontFamily = MinimalPersianFontFamily) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("author_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = AppBorder,
                            focusedTextColor = AppText,
                            unfocusedTextColor = AppText
                        )
                    )

                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        label = { Text(if (isFa) "عنوان / تخصص" else "Title / Role", fontSize = 11.sp, fontFamily = MinimalPersianFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = AppBorder,
                            focusedTextColor = AppText,
                            unfocusedTextColor = AppText
                        )
                    )

                    OutlinedTextField(
                        value = draftContact,
                        onValueChange = { draftContact = it },
                        label = { Text(if (isFa) "ایمیل، وب‌سایت یا شبکه اجتماعی (اختیاری)" else "Email / Website / Handle (Optional)", fontSize = 11.sp, fontFamily = MinimalPersianFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = AppBorder,
                            focusedTextColor = AppText,
                            unfocusedTextColor = AppText
                        )
                    )

                    OutlinedTextField(
                        value = draftNotes,
                        onValueChange = { draftNotes = it },
                        label = { Text(if (isFa) "توضیحات / امضای نویسنده" else "Author Bio / Notes", fontSize = 11.sp, fontFamily = MinimalPersianFontFamily) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppAccent,
                            unfocusedBorderColor = AppBorder,
                            focusedTextColor = AppText,
                            unfocusedTextColor = AppText
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditingAuthor = false },
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isFa) "انصراف" else "Cancel", color = AppTextSecondary, fontSize = 11.sp, fontFamily = MinimalPersianFontFamily)
                        }
                        Button(
                            onClick = {
                                onSaveAuthorProfile(
                                    AuthorProfile(
                                        name = draftName.trim().ifBlank { "موسی خفائی" },
                                        title = draftTitle.trim(),
                                        contact = draftContact.trim(),
                                        notes = draftNotes.trim()
                                    )
                                )
                                isEditingAuthor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppBullish),
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("save_author_profile_button")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (isFa) "ذخیره مشخصات" else "Save Profile", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = MinimalPersianFontFamily)
                        }
                    }
                }
            }
        }

        // 2. Summary of App Capabilities
        GuideSectionCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = if (isFa) "خلاصه کاربردهای برنامه (QuantPro)" else "App Overview & Key Capabilities",
            accent = AppAccent,
            initiallyExpanded = true
        ) {
            Text(
                text = if (isFa) {
                    "این ابزار یک ترمینال تحلیلی و معاملاتی الگوریتمی برای بازار ارزهای دیجیتال است که با هدف حذف تحلیل‌های احساسی و تصمیم‌گیری مبتنی بر داده‌های واقعی کندلی (OHLCV) طراحی شده است:"
                } else {
                    "QuantPro is a real-time algorithmic crypto trading & analysis terminal designed to replace guesswork with quantitative OHLCV market structure evaluation:"
                },
                color = AppTextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 18.sp,
                fontFamily = MinimalPersianFontFamily
            )
            Spacer(modifier = Modifier.height(6.dp))
            GuideBulletRow(
                badge = "۱",
                title = if (isFa) "دیده بان و دفتر سفارش زنده" else "Live Watchlist & Order Book",
                body = if (isFa) "دریافت قیمت لحظه‌ای رمزارزها و تتر تومانی (نوبیتکس، بایبیت، OKX، کوکوین و بایننس) همراه با Bid/Ask و مینی‌نمودار." else "Tracks live USD and Nobitex Toman rates with Bid/Ask spread and sparklines.",
                accent = AppAccent
            )
            GuideBulletRow(
                badge = "۲",
                title = if (isFa) "اسکنر چندتایم‌فریمی اسمارت‌مانی" else "MTF + Smart Money Scanner",
                body = if (isFa) "تحلیل هم‌زمان تایم‌فریم معامله و تایم‌فریم بالاتر بر پایه پرایس‌اکشن اسمارت‌مانی و اندیکاتورهای کمی." else "Evaluates lower-timeframe setups filtered by higher-timeframe trend bias.",
                accent = AppBullish
            )
            GuideBulletRow(
                badge = "۳",
                title = if (isFa) "نمودار دوگانه (TradingView + Quant)" else "Dual Chart Engine",
                body = if (isFa) "سوئیچ آنی بین نمودار ابری بایننس در TradingView و موتور کندلی محلی با نمایش خودکار EMA و اردربلاک‌ها." else "Switch between Binance TradingView cloud feed and local responsive Quant candlestick canvas.",
                accent = Amber400
            )
            GuideBulletRow(
                badge = "۴",
                title = if (isFa) "آزمایشگاه بک‌تست و مدیریت ریسک" else "Backtest Lab & Risk Engine",
                body = if (isFa) "اعتبارسنجی تاریخی استراتژی بدون نگاه به آینده، محاسبه حجم پوزیشن بر اساس ATR واقعی و ثبت در ژورنال شبیه‌سازی یا ارسال به MetaTrader." else "Runs bar-by-bar zero-lookahead historical simulations, ATR sizing, and paper journaling.",
                accent = Indigo400
            )
        }

        // 3. How Setups Are Identified
        GuideSectionCard(
            icon = Icons.Filled.Radar,
            title = if (isFa) "نحوه شناسایی موقعیت‌های معاملاتی (الگوریتم همگرایی)" else "How Trading Setups Are Identified",
            accent = AppBullish,
            initiallyExpanded = true
        ) {
            Text(
                text = if (isFa) {
                    "موتور اسکنر برای هر نماد، کندل‌های زنده تایم‌فریم معامله (LTF) و تایم‌فریم مرجع بالاتر (HTF) را دریافت کرده و امتیاز همگرایی (۰ تا ۱۰۰) را بر اساس ۷ فیلتر واقعی محاسبه می‌کند:"
                } else {
                    "For every symbol & timeframe, the engine fetches both LTF and HTF candles and scores 7 quantitative filters (0–100):"
                },
                color = AppTextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 18.sp,
                fontFamily = MinimalPersianFontFamily
            )
            Spacer(modifier = Modifier.height(6.dp))
            GuideBulletRow(
                badge = "22%",
                title = if (isFa) "هم‌راستایی روند تایم‌فریم بالا (HTF Bias)" else "Higher-Timeframe Trend Alignment",
                body = if (isFa) "بررسی EMA 50/200 و شاخص ADX در تایم‌فریم بالاتر (مثلاً ۴ ساعته برای معاملات ۱ ساعته) جهت ورود هم‌جهت با روند اصلی." else "Checks EMA 50/200 & ADX on the parent timeframe (e.g., 4H for 1H).",
                accent = AppAccent
            )
            GuideBulletRow(
                badge = "24%",
                title = if (isFa) "ناحیه اسمارت‌مانی (OB / FVG / Sweep)" else "Order Block / FVG / Liquidity Sweep",
                body = if (isFa) "تشخیص برخورد قیمت با اردربلاک معتبر، گپ ارزش منصفانه (FVG) یا جمع‌آوری نقدینگی کف/سقف‌های قبلی." else "Detects unmitigated Order Blocks, Fair Value Gaps, and stop-hunt sweeps.",
                accent = AppBullish
            )
            GuideBulletRow(
                badge = "16%",
                title = if (isFa) "آرایش میانگین‌ها (EMA 20 / 50 / 200)" else "EMA 20 / 50 / 200 Alignment",
                body = if (isFa) "تأیید ترتیب صعودی یا نزولی میانگین‌های متحرک نمایی و فاصله منطقی قیمت از تعادل." else "Verifies bullish/bearish EMA stack and pullback positioning.",
                accent = Amber400
            )
            GuideBulletRow(
                badge = "15%",
                title = if (isFa) "قدرت روند واقعی (+DI / -DI / ADX 14)" else "Trend Strength (Wilder ADX + DI)",
                body = if (isFa) "اندازه‌گیری مومنتوم جهت‌دار با فرمول وایلدر برای دوری از بازارهای خنثی و کم‌رمق." else "Wilder ADX(14) + directional indicators filter weak choppy ranges.",
                accent = AppAccent
            )
            GuideBulletRow(
                badge = "14%",
                title = if (isFa) "شکست ساختار (BOS / CHoCH) و واگرایی RSI" else "Structure Break & Pivot RSI Divergence",
                body = if (isFa) "ثبت شکست سقف/کف‌های تاییدشده (با قانون پیوت غیرآینده‌نگر) و واگرایی معمولی بین قیمت و RSI(14)." else "Identifies confirmed BOS/CHoCH and non-lookahead pivot RSI divergences.",
                accent = AppBullish
            )
            GuideBulletRow(
                badge = "10%",
                title = if (isFa) "جهش حجم و حد ضرر پویا با ATR(14)" else "Volume Spike & Dynamic ATR(14) SL/TP",
                body = if (isFa) "بررسی افزایش حجم نسبت به میانگین ۲۰ کندل اخیر و محاسبه خودکار حد ضرر بر اساس نوسان واقعی بازار (ATR × ضریب) و تارگت‌های TP1 تا TP3." else "Checks volume >= 1.15x 20-bar MA and sets SL/TP dynamically via ATR(14).",
                accent = Indigo400
            )
        }

        // 4. Section-by-Section Guide
        GuideSectionCard(
            icon = Icons.Filled.Dashboard,
            title = if (isFa) "راهنمای استفاده از بخش‌های مختلف ابزار" else "Guide to App Sections",
            accent = Amber400,
            initiallyExpanded = false
        ) {
            GuideBulletRow(
                badge = "۱",
                title = if (isFa) "بازارها (Markets)" else "Markets Watchlist",
                body = if (isFa) "لیست نمادهای منتخب همراه با نرخ دلاری و تومانی. با دکمه «+» ارز جدید اضافه کنید، با آیکون لایه‌ها دفتر سفارش نوبیتکس را ببینید و با «ثبت سریع» وارد پوزیشن آزمایشی شوید." else "Live watchlist with USD/IRT rates. Tap '+' to add coins or open Nobitex depth.",
                accent = AppAccent
            )
            GuideBulletRow(
                badge = "۲",
                title = if (isFa) "اسکنر (Scanner)" else "Quant Scanner",
                body = if (isFa) "فرصت‌های رتبه‌بندی‌شده با امتیاز و گرید (+A / B / FILTERED). روی هر کارت نوار آماری بک‌تست، RSI، ADX و ATR% دیده می‌شود. دکمه‌های «بک‌تست»، «ریسک»، «چارت» و «ثبت آزمایشی» در پایین هر کارت قرار دارند." else "Ranked setups with audit telemetry and quick buttons for Backtest Lab, Risk Calc, Chart, and Paper Order.",
                accent = AppBullish
            )
            GuideBulletRow(
                badge = "۳",
                title = if (isFa) "نمودار (Chart)" else "Dual Chart",
                body = if (isFa) "در بالای چارت می‌توانید بین «TradingView بایننس» و «موتور کوانت (محلی)» جابه‌جا شوید، اندیکاتورهای EMA / OB / Vol را روشن یا خاموش کنید و چارت را تمام‌صفحه کنید." else "Toggle between Binance TradingView and local Quant Engine, control overlays, and enter fullscreen.",
                accent = Amber400
            )
            GuideBulletRow(
                badge = "۴",
                title = if (isFa) "ژورنال (Journal)" else "Trade Journal",
                body = if (isFa) "معاملات ثبت‌شده با وضعیت شبیه‌سازی اینجا ذخیره می‌شوند. می‌توانید معاملات باز را با سود (WIN) یا ضرر (LOSS) ببندید تا آمار کل پورتفوی محاسبه شود." else "Tracks paper trades and calculates cumulative PnL when trades are closed.",
                accent = Indigo400
            )
            GuideBulletRow(
                badge = "۵",
                title = if (isFa) "تنظیمات و منوی کناری (☰)" else "Settings & Drawer Menu",
                body = if (isFa) "سوییچر ۴ قالب ظاهری زنده، تغییر زبان (فارسی/انگلیسی)، تنظیم سرمایه کل، درصد ریسک، ضریب ATR و فعال/غیرفعال‌سازی فیلترهای اسکنر." else "Switch themes, change language, adjust equity/risk/ATR, and toggle scanner confluence rules.",
                accent = AppAccent
            )
        }

        // 5. How to Perform Backtesting
        GuideSectionCard(
            icon = Icons.Filled.Science,
            title = if (isFa) "راهنمای گام‌به‌گام انجام بک‌تست (Backtest Lab)" else "Step-by-Step Backtesting Guide",
            accent = Indigo400,
            initiallyExpanded = true
        ) {
            GuideBulletRow(
                badge = "گام ۱",
                title = if (isFa) "ورود به آزمایشگاه بک‌تست" else "Open Backtest Lab",
                body = if (isFa) "از بالای صفحه اسکنر دکمه «آزمایشگاه بک‌تست»، از پایین هر کارت دکمه «بک‌تست»، یا از منوی کناری (☰) گزینه آزمایشگاه را بزنید." else "Tap 'Backtest Lab' in Scanner, 'Backtest' on a card, or open it from the Navigation Drawer.",
                accent = Amber400
            )
            GuideBulletRow(
                badge = "گام ۲",
                title = if (isFa) "انتخاب نماد، تایم‌فریم و عمق تاریخچه" else "Select Asset, Timeframe & Depth",
                body = if (isFa) "رمزارز (مثلاً BTC یا ETH)، تایم‌فریم (5m تا 1d) و تعداد کندل‌های تاریخی (۲۶۰، ۵۰۰ یا ۱۰۰۰ کندل) را مشخص کنید." else "Pick your coin, timeframe (5m–1d), and lookback bar depth (260, 500, or 1000 candles).",
                accent = AppAccent
            )
            GuideBulletRow(
                badge = "گام ۳",
                title = if (isFa) "تنظیم پارامترهای ریسک و فیلتر ورود" else "Tune Entry Score, ATR SL & Target R:R",
                body = if (isFa) "حداقل امتیاز همگرایی (+45 تا +78)، ضریب حد ضرر ATR (1.0x تا 2.5x)، نسبت ریسک به ریوارد (1:1.5 تا 1:3.0) و جهت معامله را انتخاب کنید." else "Configure Min Score, SL ATR multiplier, Target R:R, and side filter (Both/Long/Short).",
                accent = AppBullish
            )
            GuideBulletRow(
                badge = "گام ۴",
                title = if (isFa) "اجرای شبیه‌سازی بدون نگاه به آینده" else "Run Zero-Lookahead Simulation",
                body = if (isFa) "با لمس «اجرای بک‌تست»، سیستم روی هر کندل گذشته (با همگام‌سازی لحظه‌ای تایم‌فریم بالاتر و بدون دیدن آینده) معامله باز کرده و برخورد با SL یا TP را شبیه‌سازی می‌کند." else "Tap Run Backtest to simulate trades bar-by-bar with point-in-time HTF alignment.",
                accent = Indigo400
            )
            GuideBulletRow(
                badge = "گام ۵",
                title = if (isFa) "تحلیل خروجی و اعمال به استراتژی زنده" else "Analyze Metrics & Apply Params",
                body = if (isFa) "نرخ برد (Win Rate)، ضریب سود (Profit Factor > 1.2)، Expectancy، حداکثر افت سرمایه (Max DD) و منحنی رشد سرمایه را بررسی کرده و با دکمه «اعمال به استراتژی» پارامترهای بهینه را روی اسکنر زنده ثبت کنید." else "Review Win Rate, PF, Expectancy, Max DD, equity curve, and tap Apply Params to push tuned settings to the live scanner.",
                accent = AppBullish
            )
        }
    }
}

@Composable
private fun GuideSectionCard(
    icon: ImageVector,
    title: String,
    accent: Color,
    initiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppSurface)
            .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp),
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
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = title,
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MinimalPersianFontFamily
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = AppTextSecondary
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = content
            )
        }
    }
}

@Composable
private fun GuideBulletRow(
    badge: String,
    title: String,
    body: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(AppSurfaceVariant)
            .border(1.dp, AppBorder, RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.18f))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                color = accent,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MinimalPersianFontFamily
            )
            Text(
                text = body,
                color = AppTextSecondary,
                fontSize = 10.5.sp,
                lineHeight = 16.sp,
                fontFamily = MinimalPersianFontFamily
            )
        }
    }
}
