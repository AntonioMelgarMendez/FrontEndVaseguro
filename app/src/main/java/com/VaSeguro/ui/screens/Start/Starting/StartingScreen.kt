package com.VaSeguro.ui.screens.Start.Starting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StartingScreen(viewModel: StartingViewModel = viewModel()) {
    val currentIndex = viewModel.currentSlideIndex.collectAsState().value
    val slides = viewModel.slides.collectAsState().value
    val currentSlide = slides[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = currentSlide.imageRes),
            contentDescription = "Trip image",
            modifier = Modifier
                .width(320.dp)
                .height(320.dp)
        )
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentSlide.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .offset(y = currentSlide.textOffsetY.dp)

                )

                if (currentSlide.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentSlide.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .offset(y = (currentSlide.textOffsetY-5).dp)


                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (currentSlide.textOffsetY).dp)
                    ,
                    horizontalArrangement = Arrangement.Start
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RectangleShape)
                                .background(
                                    if (index == currentIndex) Color.Black else Color(0xFF6C63FF)
                                )
                                .clickable { viewModel.goToSlide(index) }
                                .padding(2.dp)
                        )
                        if (index != slides.lastIndex) Spacer(modifier = Modifier.size(6.dp))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(top = 32.dp)
                    .offset(y = (-80).dp)
            ) {
                Button(
                    onClick = { viewModel.nextSlide() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowRight,
                        contentDescription = "Arrow Right",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}