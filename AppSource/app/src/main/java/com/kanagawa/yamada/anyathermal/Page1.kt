package com.kanagawa.yamada.anyathermal

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun Page1(
    disableThermal: Boolean,
    onDisableThermalChange: (Boolean) -> Unit,
    coroutineScope: CoroutineScope
) {
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
                text = "Anya Melfissa\nDisable Thermal",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 42.sp, lineHeight = 48.sp),
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "By: Kanagawa Yamada",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                color = Color.White
            )
            Text(
                text = "Version: 1.0",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Disable Thermal Switch
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6D5257).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFD3A47A), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Disable Thermal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Switch(
                    checked = disableThermal,
                    onCheckedChange = { isChecked ->
                        onDisableThermalChange(isChecked)
                        coroutineScope.launch(Dispatchers.IO) {
                            val binaryPath = "/data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaMelfissa"
                            val arg = if (isChecked) "1" else "0"
                            Shell.cmd("$binaryPath $arg").exec()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFEBC19B),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Swipe For More Options",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "ᐱ",
            color = Color.White,
            fontSize = 32.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
