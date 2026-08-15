package com.kanagawa.yamada.anyathermal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
            val res = Shell.cmd("cat /data/adb/modules/AnyaMelfissa/StartOnBoot.txt").exec()
            val out = res.out.joinToString("").trim()
            applyOnBoot = (out == "1")
        }
    }

    val currentTheme = LocalAppTheme.current
    val textColor = if (currentTheme == "anya") Color.White else MaterialTheme.colorScheme.onBackground
    val cardBgColor = if (currentTheme == "anya") Color(0xFF6D5257).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant
    val cardBorderColor = if (currentTheme == "anya") Color(0xFFD3A47A) else MaterialTheme.colorScheme.primary
    
    val selectedBtnColor = if (currentTheme == "anya") Color(0xFFEBC19B) else MaterialTheme.colorScheme.primary
    val unselectedBtnColor = if (currentTheme == "anya") Color(0xFF4A3438) else MaterialTheme.colorScheme.surface
    val selectedTxtColor = if (currentTheme == "anya") Color(0xFF6D5257) else MaterialTheme.colorScheme.onPrimary
    val unselectedTxtColor = if (currentTheme == "anya") Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))

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
                Column {
                    Text("Apply On Boot", color = textColor, fontWeight = FontWeight.Medium, fontSize = 20.sp)
                    Text("Apply Thermal On Boot", color = if (currentTheme == "anya") Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
                CustomSwitch(
                    checked = applyOnBoot,
                    onCheckedChange = { isChecked ->
                        applyOnBoot = isChecked
                        coroutineScope.launch(Dispatchers.IO) {
                            val v = if (isChecked) "1" else "0"
                            Shell.cmd("echo $v > /data/adb/modules/AnyaMelfissa/StartOnBoot.txt").exec()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // APP THEMES Box
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, cardBorderColor, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("APP THEMES", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onThemeChange("anya") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentTheme == "anya") selectedBtnColor else unselectedBtnColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Anya", color = if (currentTheme == "anya") selectedTxtColor else unselectedTxtColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = { onThemeChange("system") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentTheme == "system") selectedBtnColor else unselectedBtnColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("System", color = if (currentTheme == "system") selectedTxtColor else unselectedTxtColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
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
                text = "In this new version, Anya no longer kills Thermal Services. Instead, it spoof \"temp\" on /sys/class/thermal/thermal_zone* to 30. If you ask why using this app instead of just simple WebUI. It's because now you can enable / disable Anya Thermal on your Control Center",
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
