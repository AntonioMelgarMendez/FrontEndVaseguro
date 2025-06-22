package com.VaSeguro.ui.Aux

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage

@Composable
fun ContentScreen(
    message: String,
    description: String,
    imageArg: String,
    buttonText: String,
    navController: NavController,
    destination: String
) {
    Box(
        modifier = Modifier.fillMaxSize()
        .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .align(Alignment.Start),
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
            val imageResId = imageArg.toIntOrNull()
            if (imageResId != null) {

               Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(375.dp)
                        .padding(bottom = 16.dp)
                )
            }else if (imageArg.isNotBlank()) {
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    AsyncImage(
                        model = imageArg,
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Button(
                onClick = { navController.navigate(destination) },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(
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
        description = "Before we begin, we need you to register your childrens",
        imageArg = "https://gzukutdyhavnvjpbmxxb.supabase.co/storage/v1/object/public/usersavatar/avatars/1750002809462-profile.jpg",
        buttonText = "Get Started",
        navController = NavHostController(context = LocalContext.current),
        destination = "home"
    )
}