package com.VaSeguro.ui.screens.Utils

import androidx.compose.animation.core.Animatable
import com.VaSeguro.R
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.VaSeguro.ui.navigations.HomeScreenNavigation
import com.VaSeguro.ui.navigations.SplashScreenNavigation
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource

import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        alpha.animateTo(1f, animationSpec = tween(1000))
        delay(2000)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
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
                text = "Cargando...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier
                    .offset(y = (-40).dp)
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
