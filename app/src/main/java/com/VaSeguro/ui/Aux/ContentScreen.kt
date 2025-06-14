package com.VaSeguro.ui.Aux

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ContentScreen(
    message: String,
    description: String,
    imageRes: Int,
    buttonText: String,
    navController: NavHostController,
    destination: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 4.dp) .align(Alignment.Start),
            )
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.Start),
                lineHeight = 24.sp,
                maxLines = Int.MAX_VALUE
            )
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(375.dp).padding(bottom = 16.dp)
            )
            Button(
                onClick = { navController.navigate(destination) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
                shape = MaterialTheme.shapes.small,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    color = Color.White
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                    contentDescription = "Arrow Right",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun ContentScreenPreview() {
    ContentScreen(
        message = "Welcome to VaSeguro!",
        description = "Before we begin," +
                "we need you to register your childrens",
        imageRes = com.VaSeguro.R.drawable.bus_stop,
        buttonText = "Get Started",
        navController = NavHostController(context = androidx.compose.ui.platform.LocalContext.current),
        destination = "home"
    )
}
