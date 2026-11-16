package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FrostedGlassCard
import com.example.StrategyParams
import com.example.ui.theme.*
import java.util.Locale

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
