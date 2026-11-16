package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AuthorProfile
import com.example.StrategyParams
import com.example.TradeRecord
import com.example.ui.theme.*

@Composable
fun MoreView(
    params: StrategyParams,
    trades: List<TradeRecord>,
    serverPriority: String,
    currentTheme: AppThemePreset = ThemeState.current.preset,
    authorProfile: AuthorProfile = AuthorProfile(),
    selectedSubSection: String = "PARAMS",
    isFa: Boolean,
    onSelectSubSection: (String) -> Unit = {},
    onSelectTheme: (AppThemePreset) -> Unit = {},
    onSaveAuthorProfile: (AuthorProfile) -> Unit = {},
    onUpdateRisk: (Float) -> Unit,
    onUpdateBalance: (Double) -> Unit,
    onUpdateAtr: (Float) -> Unit,
    onUpdateTimeframe: (String) -> Unit,
    onToggleRule: (String) -> Unit,
    onToggleLanguage: () -> Unit,
    onResetData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Live Theme Switcher Strip in Settings
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Palette, contentDescription = null, tint = AppAccent, modifier = Modifier.size(16.dp))
                Text(
                    text = if (isFa) "سوییچر زنده قالب (Live Theme Switcher)" else "Live Theme Switcher",
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MinimalPersianFontFamily
                )
            }
            com.example.LiveThemeSwitcherSection(
                currentPreset = currentTheme,
                isFa = isFa,
                onSelectPreset = onSelectTheme
            )
        }

        // Sub-navigation Switcher (Segmented control: Params / System / About & Guide)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppSurface)
                .border(1.dp, AppBorder, RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubSection == "PARAMS") AppAccent.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelectSubSection("PARAMS") }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "ریسک و استراتژی" else "Strategy",
                    color = if (selectedSubSection == "PARAMS") AppText else AppTextSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontFamily = MinimalPersianFontFamily
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubSection == "SYSTEM") AppAccent.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelectSubSection("SYSTEM") }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "سلامت سیستم" else "System",
                    color = if (selectedSubSection == "SYSTEM") AppText else AppTextSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontFamily = MinimalPersianFontFamily
                )
            }

            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubSection == "ABOUT") AppAccent.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelectSubSection("ABOUT") }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("more_tab_about"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "درباره و راهنما" else "About & Guide",
                    color = if (selectedSubSection == "ABOUT") AppAccent else AppTextSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontFamily = MinimalPersianFontFamily
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSubSection) {
                "PARAMS" -> ParamsView(
                    params = params,
                    isFa = isFa,
                    onUpdateRisk = onUpdateRisk,
                    onUpdateBalance = onUpdateBalance,
                    onUpdateAtr = onUpdateAtr,
                    onUpdateTimeframe = onUpdateTimeframe,
                    onToggleRule = onToggleRule
                )
                "ABOUT" -> AboutGuideView(
                    authorProfile = authorProfile,
                    isFa = isFa,
                    onSaveAuthorProfile = onSaveAuthorProfile
                )
                else -> ProfileView(
                    trades = trades,
                    params = params,
                    isFa = isFa,
                    onResetData = onResetData
                )
            }
        }
    }
}
