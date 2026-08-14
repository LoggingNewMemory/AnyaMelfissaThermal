package com.kanagawa.yamada.anyathermal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Page3() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                horizontalAlignment = Alignment.Start
            ) {
                Text("Device Temperature", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Live Graph of Temperature", color = Color.LightGray, fontSize = 14.sp)
                Text("Current Temperature: [Temp]", color = Color.White, fontSize = 16.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Canvas for Mock Graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, Color(0xFFD3A47A), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, size.height)
                            lineTo(size.width * 0.25f, size.height * 0.4f)
                            lineTo(size.width * 0.5f, size.height * 0.8f)
                            lineTo(size.width * 0.75f, size.height * 0.5f)
                            lineTo(size.width, size.height * 0.7f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFEBC19B),
                            style = Stroke(width = 4f)
                        )
                    }
                    
                    // Labels on graph
                    Text("30°C", color = Color.White, fontSize = 14.sp, modifier = Modifier.offset(x = 40.dp, y = 20.dp))
                    Text("25°C", color = Color.White, fontSize = 14.sp, modifier = Modifier.offset(x = 180.dp, y = 30.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Themes Box
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
                Text("APP THEMES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { /* Set Theme */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBC19B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anya", color = Color(0xFF4A3438), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { /* Set Theme */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBC19B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("System", color = Color(0xFF4A3438), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
