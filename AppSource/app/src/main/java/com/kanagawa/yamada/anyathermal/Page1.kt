package com.kanagawa.yamada.anyathermal

import android.content.pm.PackageManager
import android.widget.TextClock
import androidx.compose.animation.core.*
import androidx.compose.ui.res.painterResource
import com.kanagawa.yamada.anyathermal.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CustomSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val currentTheme = LocalAppTheme.current
    val checkedThumbColor = if (currentTheme == "anya") Color.White else MaterialTheme.colorScheme.onPrimary
    val checkedTrackColor = if (currentTheme == "anya") Color(0xFFEBC19B) else MaterialTheme.colorScheme.primary
    val uncheckedThumbColor = if (currentTheme == "anya") Color.LightGray else MaterialTheme.colorScheme.outline
    val uncheckedTrackColor = if (currentTheme == "anya") Color(0xFF897296) else MaterialTheme.colorScheme.surfaceVariant
    val uncheckedBorderColor = if (currentTheme == "anya") Color.Transparent else MaterialTheme.colorScheme.outline

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = checkedThumbColor,
            checkedTrackColor = checkedTrackColor,
            uncheckedThumbColor = uncheckedThumbColor,
            uncheckedTrackColor = uncheckedTrackColor,
            uncheckedBorderColor = uncheckedBorderColor
        )
    )
}

@Composable
fun Page1(
    disableThermal: Boolean,
    onDisableThermalChange: (Boolean) -> Unit,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val appVersion = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    val textColor = if (LocalAppTheme.current == "anya") Color.White else MaterialTheme.colorScheme.onBackground
    val cardBgColor = if (LocalAppTheme.current == "anya") Color(0xFF6D5257).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant
    val cardBorderColor = if (LocalAppTheme.current == "anya") Color(0xFFD3A47A) else MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, end = 24.dp)) {
            AndroidView(
                factory = { ctx ->
                    TextClock(ctx).apply {
                        format12Hour = "hh:mm a"
                        format24Hour = "HH:mm"
                        setTextColor(textColor.toArgb())
                        textSize = 16f
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                val textShadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 23f
                )

                Text(
                    text = "Anya Melfissa Thermal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 36.sp, 
                        lineHeight = 42.sp,
                        shadow = if (LocalAppTheme.current == "anya") textShadow else null
                    ),
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "By: Kanagawa Yamada",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        shadow = if (LocalAppTheme.current == "anya") textShadow else null
                    ),
                    color = textColor
                )
                
                // Add shadow to the horizontal line by wrapping it in a Box with shadow modifier or drawing a shadow behind it
                Box(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(0.8f)) {
                    if (LocalAppTheme.current == "anya") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Black)
                                // Create the same blur effect for the line
                                .blur(8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(textColor)
                    )
                }

                Text(
                    text = "Version: $appVersion",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        shadow = if (LocalAppTheme.current == "anya") textShadow else null
                    ),
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Swipe Right For More Options",
                color = textColor,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Thermal Switch
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, cardBorderColor, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apply Anya Thermal",
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    CustomSwitch(
                        checked = disableThermal,
                        onCheckedChange = { isChecked ->
                            onDisableThermalChange(isChecked)
                            coroutineScope.launch(Dispatchers.IO) {
                                val scriptCommand = if (isChecked) "su -M -c '/data/adb/modules/AnyaMelfissa/AnyaMelfissa 1'" else "su -M -c '/data/adb/modules/AnyaMelfissa/AnyaMelfissa 0'"
                                Shell.cmd(scriptCommand).exec()
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
