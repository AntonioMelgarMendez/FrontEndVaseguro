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
import com.VaSeguro.data.model.Routes.RoutesData
import com.VaSeguro.data.model.Routes.RouteStatus
import com.VaSeguro.data.model.Routes.RouteType
import com.VaSeguro.data.model.User.UserData
import com.VaSeguro.data.model.User.UserRole
import com.VaSeguro.ui.components.InfoBox
import com.VaSeguro.ui.components.ScheduleChip
import com.VaSeguro.data.model.Vehicle.Vehicle
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val routeList = listOf(
    RoutesData(
        id = "1",
        name = "Route 1",
        start_date = "2023-10-01",
        vehicule_id = "1",
        status_id = RouteStatus(
            id = "1",
            status = "Active",
        ),
        type_id = RouteType(
            id = "1",
            type = "Regular",
        ),
        end_date = "2023-10-31",
    )
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
        items(routeList) { microBus ->
            // val schedules = microBus.schedule.split(",").map { it.trim().toAmPmFormat() }
            Text(
                text = microBus.name,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = microBus.vehicule_id,
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
                        .data("")
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
                data = microBus.vehicule_id,
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.DirectionsCar,
                title = "Plaque:",
                data = microBus.vehicule_id,
            )
            /*
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.Groups3,
                title = "Max Capacity:",
                data = microBus.maxCapacity.toString(),
            )
            */
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.Phone,
                title = "Phone number:",
                data = microBus.vehicule_id,
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                icon = Icons.Default.AccessTime,
                title = "Schedule",
            )
            /*
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
            */
            /*
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
            */

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusScreenPreview() {
    BusScreen()
}