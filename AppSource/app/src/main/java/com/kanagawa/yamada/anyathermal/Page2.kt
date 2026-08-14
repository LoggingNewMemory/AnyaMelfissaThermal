package com.kanagawa.yamada.anyathermal

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Page2(coroutineScope: CoroutineScope) {
    var applyOnBoot by remember { mutableStateOf(false) }
    var checkResult by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val res = Shell.cmd("cat /data/adb/modules/AnyaMelfissa/StartOnBoot.txt").exec()
            val out = res.out.joinToString("").trim()
            applyOnBoot = (out == "1")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Apply On Boot Box
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
                Column {
                    Text("Apply On Boot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Thermal On Boot", color = Color.LightGray, fontSize = 14.sp)
                }
                Switch(
                    checked = applyOnBoot,
                    onCheckedChange = { isChecked ->
                        applyOnBoot = isChecked
                        coroutineScope.launch(Dispatchers.IO) {
                            val v = if (isChecked) "1" else "0"
                            Shell.cmd("echo $v > /data/adb/modules/AnyaMelfissa/StartOnBoot.txt").exec()
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

        Spacer(modifier = Modifier.height(16.dp))
        
        // Check Thermal Service Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6D5257).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFD3A47A), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Check Thermal Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Official Way to Check Thermal Service", color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = Shell.cmd("ps -A | grep thermal").exec()
                            val out = res.out.joinToString("\n")
                            checkResult = if (out.isEmpty()) "No thermal service found" else out
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBC19B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Check Thermal", color = Color(0xFF4A3438), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Console Output
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFF4A3438), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFD3A47A), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = checkResult,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: Some thermal is not service based, some are \"bridge\" based or \"app\" based thermal, to do this. Some will appear do_signal_stop, it only can be stopped, not killed",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
