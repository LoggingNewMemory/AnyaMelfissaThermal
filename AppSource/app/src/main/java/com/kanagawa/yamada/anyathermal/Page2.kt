package com.kanagawa.yamada.anyathermal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Page2(
    coroutineScope: CoroutineScope,
    onThemeChange: (String) -> Unit
) {
    var applyOnBoot by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val res1 = Shell.cmd("grep '^START_ON_BOOT ' /data/adb/modules/AnyaMelfissa/AnyaConfig.txt | awk '{print $2}'").exec()
            applyOnBoot = (res1.out.joinToString("").trim() == "1")
        }
    }

    val currentTheme = LocalAppTheme.current
    val textColor = if (currentTheme == "anya") Color.White else MaterialTheme.colorScheme.onBackground
    val cardBgColor = if (currentTheme == "anya") Color(0xFF6D5257).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant
    val cardBorderColor = if (currentTheme == "anya") Color(0xFFD3A47A) else MaterialTheme.colorScheme.primary
    
    val selectedBtnColor = if (currentTheme == "anya") Color(0xFFEBC19B) else MaterialTheme.colorScheme.primary
    val unselectedBtnColor = if (currentTheme == "anya") Color(0xFF897296) else MaterialTheme.colorScheme.surface
    val selectedTxtColor = if (currentTheme == "anya") Color(0xFF6D5257) else MaterialTheme.colorScheme.onPrimary
    val unselectedTxtColor = if (currentTheme == "anya") Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(5.dp))

            // Apply On Boot Box
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
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Apply On Boot", color = textColor, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Apply Thermal On Boot", color = if (currentTheme == "anya") Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 16.sp)
                    }
                    CustomSwitch(
                        checked = applyOnBoot,
                        onCheckedChange = { isChecked ->
                            applyOnBoot = isChecked
                            coroutineScope.launch(Dispatchers.IO) {
                                val v = if (isChecked) "1" else "0"
                                Shell.cmd("sed -i 's/^START_ON_BOOT .*/START_ON_BOOT $v/' /data/adb/modules/AnyaMelfissa/AnyaConfig.txt").exec()
                            }
                        }
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // DISCLAIMER
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "DISCLAIMER",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "In this new version, Anya no longer kills Thermal Services. Instead, it spoofs \"temp\" on /sys/class/thermal/thermal_zone* to 30 cels and modifies \"trip_point_0_temp\" to 200 cels. If you ask why using this app instead of just simple WebUI. It's because now you can enable / disable Anya Thermal on your Control Center",
                color = textColor,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
