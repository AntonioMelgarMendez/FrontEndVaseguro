package com.VaSeguro.ui.screens.Parents.Bus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups3
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.VaSeguro.ui.components.InfoBox
import com.VaSeguro.ui.components.ScheduleChip
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class microBus(
    val id: String,
    val name: String,
    val model: String,
    val busImage: String,
    val driver: String,
    val plaque: String,
    val schedule: String,
    val rate: String,
    val phoneNumber: String,
    val maxCapacity: Int,
    val currentPassengers: Int,
    val tripPriceDay: Double,
    val tripPriceMonth: Double
)

val microBusList = listOf(
    microBus(
        id = "1",
        name = "Toyota Hiace",
        model = "Model 2025",
        busImage = "https://www.toyota.com.sv/wp-content/uploads/2019/04/hiace_beige_v1.1.png",
        driver = "Juan Melgar",
        plaque = "599477",
        schedule = "06:00, 12:00, 17:30",
        rate = "Impressive",
        phoneNumber = "7269-8210",
        maxCapacity = 20,
        currentPassengers = 15,
        tripPriceDay = 2.0,
        tripPriceMonth = 29.0
    ),
)

fun String.toAmPmFormat(): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        val time = LocalTime.parse(this, inputFormatter)
        time.format(outputFormatter)
    } catch (_: Exception) {
        this
    }
}

@Composable
fun BusScreen() {
    var isLoading by remember { mutableStateOf(true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items(microBusList) { microBus ->
            val schedules = microBus.schedule.split(",").map { it.trim().toAmPmFormat() }
            Text(
                text = microBus.name,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = microBus.model,
                fontWeight = FontWeight.Light,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(microBus.busImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = microBus.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    onState = {
                        isLoading = when (it) {
                            is AsyncImagePainter.State.Loading -> true
                            is AsyncImagePainter.State.Success,
                            is AsyncImagePainter.State.Error -> false
                            else -> false
                        }
                    }
                )
                if (isLoading) {
                    CircularProgressIndicator()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoBox(
                icon = Icons.Default.Person,
                title = "Driver:",
                data = microBus.driver,
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.DirectionsCar,
                title = "Plaque:",
                data = microBus.plaque,
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.Groups3,
                title = "Max Capacity:",
                data = microBus.maxCapacity.toString(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.Phone,
                title = "Phone number:",
                data = microBus.phoneNumber,
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.AccessTime,
                title = "Schedule",
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(schedules) { time ->
                    ScheduleChip(time)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoBox(
                    title = "For Trip:",
                    data = "$${microBus.tripPriceDay.toInt()}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                InfoBox(
                    title = "For Month:",
                    data = "$${microBus.tripPriceMonth.toInt()}",
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusScreenPreview() {
    BusScreen()
}