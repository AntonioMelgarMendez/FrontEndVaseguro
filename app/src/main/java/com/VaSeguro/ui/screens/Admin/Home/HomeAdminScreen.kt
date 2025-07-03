package com.VaSeguro.ui.screens.Admin.Home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.Aux.InfoCardData

@Composable
fun HomeAdminScreen() {
    val context = LocalContext.current
    val viewModel: HomeAdminViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return HomeAdminViewModel(
                    appProvider.provideAuthRepository(),
                    appProvider.provideUserPreferences(),
                    appProvider.provideRequestRepository(),
                    appProvider.provideUserDao(),
                    appProvider.provideChildrenRepository()
                ) as T
            }
        }
    )
    var showIcons by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showIcons = true }
    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
        viewModel.fetchUsersWithCodes()
    }
    val pending by viewModel.pendingUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val totalUsuarios by viewModel.totalUsers.collectAsState()
    val totalConductores by viewModel.totalDrivers.collectAsState()
    val totalHijos by viewModel.totalChildren.collectAsState()
    val totalPadres by viewModel.totalParents.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val animatedTotalUsuarios by animateIntAsState(
        targetValue = totalUsuarios,
        animationSpec = tween(durationMillis = 800)
    )
    val animatedTotalConductores by animateIntAsState(
        targetValue = totalConductores,
        animationSpec = tween(durationMillis = 800)
    )
    val animatedTotalHijos by animateIntAsState(
        targetValue = totalHijos,
        animationSpec = tween(durationMillis = 800)
    )
    val animatedTotalPadres by animateIntAsState(
        targetValue = totalPadres,
        animationSpec = tween(durationMillis = 800)
    )
    val cards = listOf(
        InfoCardData(Icons.Filled.Group, "Usuarios totales", animatedTotalUsuarios, Color(0xFFD1C4E9)),
        InfoCardData(Icons.Filled.DirectionsBus, "Conductores", animatedTotalConductores, Color(0xFFB2DFDB)),
        InfoCardData(Icons.Filled.Person, "Hijos", animatedTotalHijos, Color(0xFFFFF9C4)),
        InfoCardData(Icons.Filled.Person, "Padres", animatedTotalPadres, Color(0xFFFFCCBC))
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Panel de Administrador",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val cardModifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)

                for (i in 0 until cards.size step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (j in 0..1) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(cardModifier),
                                contentAlignment = Alignment.Center
                            ) {
                                this@Row.AnimatedVisibility(
                                    visible = showIcons,
                                    enter = fadeIn(animationSpec = tween(900)) + expandIn(tween(900)),
                                    exit = fadeOut(animationSpec = tween(900))
                                ) {
                                    InfoCard(
                                        icon = cards[i + j].icon,
                                        label = cards[i + j].label,
                                        value = cards[i + j].value,
                                        color = cards[i + j].color,
                                        modifier = Modifier.fillMaxSize(),
                                        animateIcon = true
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Solicitudes pendientes:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // --- Fixed height container for requests ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(388.dp)
                    .background(Color.Transparent, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red
                        )
                    }
                    pending.isEmpty() -> {
                        Text(
                            text = "No hay solicitudes pendientes",
                            color = Color.Gray
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 386.dp), // 220 - padding
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(pending) { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (user.profile_pic != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(user.profile_pic),
                                                contentDescription = "Foto de ${user.forenames}",
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(RoundedCornerShape(50)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Foto de ${user.forenames}",
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(RoundedCornerShape(50)),
                                                tint = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${user.forenames} ${user.surnames}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = user.email,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {viewModel.approveUser(user.id)},
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .padding(end = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = "Aceptar",
                                                    tint = Color(0xFF81C784)
                                                )
                                            }
                                            IconButton(
                                                onClick = {viewModel.rejectUser(user.id) },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Rechazar",
                                                    tint = Color(0xFFE57373)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeAdminScreenPreview() {
    HomeAdminScreen()
}