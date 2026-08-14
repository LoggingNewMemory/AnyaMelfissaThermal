package com.kanagawa.yamada.anyathermal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kanagawa.yamada.anyathermal.R

val HarmonyOSFontFamily = FontFamily(
    Font(R.font.harmonyos_sans_regular, FontWeight.Normal),
    Font(R.font.harmonyos_sans_bold, FontWeight.Bold),
    Font(R.font.harmonyos_sans_medium, FontWeight.Medium),
    Font(R.font.harmonyos_sans_light, FontWeight.Light),
    Font(R.font.harmonyos_sans_thin, FontWeight.Thin),
    Font(R.font.harmonyos_sans_black, FontWeight.Black)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = HarmonyOSFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HarmonyOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HarmonyOSFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)