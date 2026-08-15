package com.kanagawa.yamada.anyathermal

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanagawa.yamada.anyathermal.ui.theme.AnyaThermalTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val LocalAppTheme = compositionLocalOf { "anya" }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnyaThermalTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var appTheme by remember { mutableStateOf(sharedPref.getString("theme", "anya") ?: "anya") }

    var disableThermal by remember { mutableStateOf(false) } 
    val coroutineScope = rememberCoroutineScope()
    var isRooted by remember { mutableStateOf(false) }
    var isCheckingRoot by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            isRooted = CheckRoot.isRooted()
            if (isRooted) {
                disableThermal = CheckRoot.isThermalDisabled()
            }
            isCheckingRoot = false
        }
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val baseModifier = Modifier.fillMaxSize()
            val imageModifier = if (!isRooted && !isCheckingRoot) {
                baseModifier.blur(16.dp)
            } else {
                baseModifier
            }

            if (appTheme == "anya") {
                Image(
                    painter = painterResource(id = R.drawable.anya),
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
                
                // Dark overlay for readability and dimming
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (!isRooted && !isCheckingRoot) 0.6f else 0.2f))
                )
            }

            if (isCheckingRoot) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = if (appTheme == "anya") Color.White else MaterialTheme.colorScheme.primary)
                }
            } else if (!isRooted) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    androidx.compose.material3.Text(
                        text = "Root Needed To\nUse The App",
                        color = if (appTheme == "anya") Color.White else MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 36.sp
                    )
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { 2 })

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().systemBarsPadding()
                ) { page ->
                    when (page) {
                        0 -> Page1(
                            disableThermal = disableThermal,
                            onDisableThermalChange = { disableThermal = it },
                            coroutineScope = coroutineScope
                        )
                        1 -> Page2(
                            coroutineScope = coroutineScope,
                            onThemeChange = { newTheme ->
                                appTheme = newTheme
                                sharedPref.edit().putString("theme", newTheme).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}