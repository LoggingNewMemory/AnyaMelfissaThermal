package com.kanagawa.yamada.anyathermal

import android.content.pm.PackageManager
import android.widget.TextClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
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
    val bias by animateFloatAsState(if (checked) 1f else -1f, label = "switchBias")
    val trackColor = if (LocalAppTheme.current == "anya") Color(0xFFEBC19B) else MaterialTheme.colorScheme.primary
    val thumbColor = if (LocalAppTheme.current == "anya") Color.White else MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .width(84.dp)
            .height(44.dp)
            .background(
                color = trackColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = BiasAlignment(horizontalBias = bias, verticalBias = 0f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(thumbColor, RoundedCornerShape(8.dp))
        )
    }
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
            if (LocalAppTheme.current == "anya") {
                Text("✦", color = Color(0xFFFBE4A1), fontSize = 42.sp, modifier = Modifier.align(Alignment.Center))
            }
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
                Text(
                    text = if (LocalAppTheme.current == "anya") "Anya Melfissa\nThermal." else "System\nThermal.",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 46.sp, lineHeight = 52.sp),
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "By: Kanagawa Yamada",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    color = textColor
                )
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(0.8f)
                        .height(2.dp)
                        .background(textColor)
                )
                Text(
                    text = "Version: $appVersion",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Swipe For More Options",
                color = textColor,
                fontSize = 12.sp
            )
            Text(
                text = "ᐱ",
                color = textColor,
                fontSize = 32.sp
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
                        text = if (LocalAppTheme.current == "anya") "Apply Anya Thermal" else "Apply Thermal",
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                    CustomSwitch(
                        checked = disableThermal,
                        onCheckedChange = { isChecked ->
                            onDisableThermalChange(isChecked)
                            coroutineScope.launch(Dispatchers.IO) {
                                val binaryPath = "/data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaMelfissa"
                                val arg = if (isChecked) "1" else "0"
                                Shell.cmd("$binaryPath $arg").exec()
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
