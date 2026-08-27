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
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
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
        launch {
            CheckRoot.thermalStateFlow.collect { newState ->
                disableThermal = newState
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (isRooted && !isCheckingRoot) {
                    coroutineScope.launch(Dispatchers.IO) {
                        disableThermal = CheckRoot.isThermalDisabled()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        val pagerState = rememberPagerState(pageCount = { 2 })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val baseModifier = Modifier.fillMaxSize()

            // Unblurred background
            Image(
                painter = painterResource(id = R.drawable.anya),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = baseModifier
            )
            
            // Blurred background layer (cross-fade for absolute buttery smoothness)
            Image(
                painter = painterResource(id = R.drawable.anya),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = baseModifier
                    .graphicsLayer {
                        val offset = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                        alpha = if (!isRooted && !isCheckingRoot) 1f else offset
                    }
                    .blur(8.dp)
            )
            
            // Dark overlay for readability and dimming
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val offset = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                        val alpha = if (!isRooted && !isCheckingRoot) 0.6f else (0.2f + offset * 0.2f)
                        drawRect(Color.Black.copy(alpha = alpha))
                    }
            )

            if (isCheckingRoot) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    SkeletonLoadingScreen()
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

                HorizontalPager(
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

@Composable
fun SkeletonLoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            // Title placeholder
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(40.dp).alpha(alphaAnim).background(Color.Gray, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.height(12.dp))
            // Subtitle placeholder
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).alpha(alphaAnim).background(Color.Gray, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)))
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Small text placeholder
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).alpha(alphaAnim).background(Color.Gray, androidx.compose.foundation.shape.RoundedCornerShape(4.dp)))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Card placeholder
        Box(modifier = Modifier.fillMaxWidth().height(72.dp).alpha(alphaAnim).background(Color.Gray, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)))
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}