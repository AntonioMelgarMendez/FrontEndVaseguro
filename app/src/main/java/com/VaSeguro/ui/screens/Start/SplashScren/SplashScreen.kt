// File: app/src/main/java/com/VaSeguro/ui/screens/Start/SplashScren/SplashScreen.kt
package com.VaSeguro.ui.screens.Start.SplashScren

import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import com.VaSeguro.R
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.AppProvider

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: SplashViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return SplashViewModel(
                    appProvider.provideUserPreferences(),

                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val alpha = remember { Animatable(0f) }
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(true) {
        alpha.animateTo(1f, animationSpec = tween(1000))
    }
    LaunchedEffect(Unit) {
        while (true) {
            dotCount = (dotCount + 1) % 4
            delay(500)
        }
    }
    LaunchedEffect(uiState) {
        when (uiState) {
            is SplashUiState.GoToHome -> {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SplashUiState.GoToStarting -> {
                navController.navigate("starting") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    val dots = buildString {
        repeat(dotCount) { append('.') }
        repeat(3 - dotCount) { append(' ') }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Su tranquilidad en\n el camino",
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha.value)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = R.drawable.bus_stop),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(300.dp)
                    .height(300.dp)
                    .alpha(alpha.value)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cargando$dots",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.offset(y = (-40).dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .width(240.dp)
                    .height(6.dp),
                color = Color.Black,
                trackColor = Color(0xFF6C63FF)
            )
        }
    }
}