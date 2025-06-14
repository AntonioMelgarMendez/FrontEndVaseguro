package com.VaSeguro.ui.screens.Admin.Home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.VaSeguro.R
import com.VaSeguro.data.model.Aux.InfoCardData
import com.VaSeguro.data.model.Aux.UsuarioSolicitud

@Composable
fun HomeAdminScreen() {
    val totalUsuarios = 42
    val totalConductores = 15
    val totalHijos = 8
    val totalDrivers = 19
    val solicitudes = listOf(
        UsuarioSolicitud("Juan Pérez", "juan@email.com", R.drawable.ic_launcher_foreground),
        UsuarioSolicitud("Ana Gómez", "ana@email.com", R.drawable.ic_launcher_foreground),
        UsuarioSolicitud("Carlos Ruiz", "carlos@email.com", R.drawable.ic_launcher_foreground)
    )

    val cards = listOf(
        InfoCardData(Icons.Filled.Group, "Usuarios", totalUsuarios, Color(0xFFD1C4E9)),
        InfoCardData(Icons.Filled.DirectionsBus, "Conductores", totalConductores, Color(0xFFB2DFDB)),
        InfoCardData(Icons.Filled.Person, "Hijos", totalHijos, Color(0xFFFFF9C4)),
        InfoCardData(Icons.Filled.PersonAdd, "Drivers", totalDrivers, Color(0xFFFFCCBC))
    )

    var showIcons by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showIcons = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (solicitudes.isEmpty()) {
                    Text(
                        text = "No hay solicitudes pendientes",
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(solicitudes) { solicitud ->
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
                                    Image(
                                        painter = painterResource(id = solicitud.fotoRes),
                                        contentDescription = "Foto de ${solicitud.nombre}",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(50)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = solicitud.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(text = solicitud.email, fontSize = 14.sp, color = Color.Gray)
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(end = 4.dp)
                                        ) {
                                            Icon(Icons.Filled.Check, contentDescription = "Aceptar", tint = Color(0xFF81C784))
                                        }
                                        IconButton(
                                            onClick = {},
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Rechazar", tint = Color(0xFFE57373))
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
