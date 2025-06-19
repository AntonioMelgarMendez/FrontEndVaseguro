package com.VaSeguro.ui.components.Container.TopBarContainer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.navigations.ConfigurationScreenNavigation
import com.VaSeguro.ui.navigations.MapScreenNavigation
import com.VaSeguro.ui.screens.Parents.Configuration.ConfigurationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "VaSeguro",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    navController: NavController,
    navControllerx: NavController
) {
    val context = LocalContext.current
    val viewModel: TopBarViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return TopBarViewModel(appProvider.provideUserPreferences()) as T
            }
        }
    )

    TopAppBar(
        title = {},
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(onClick = { viewModel.openConfigDialog() }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Configuración"
                    )
                }
            }
        },
        actions = {
            Row {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones"
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Perfil"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )

    if (viewModel.isConfigDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeConfigDialog() },
            confirmButton = {},
            title = {},
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.closeConfigDialog() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-40).dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Configuración",
                            fontSize = 30.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                top = 16.dp,
                                bottom = 16.dp
                            )
                        )

                        ConfigOption("Cuenta", Icons.Outlined.AccountCircle)
                        {
                            viewModel.closeConfigDialog()
                            navControllerx.navigate(ConfigurationScreenNavigation)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfigOption("Transporte", Icons.Filled.DirectionsCar)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfigOption("Notificaciones", Icons.Filled.Notifications)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfigOption("Soporte", Icons.Filled.Help)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfigOption("Acerca de", Icons.Filled.Info)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfigOption("Lenguaje", Icons.Filled.Language)
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfigOption("Cerrar Sesion", Icons.Filled.ExitToApp) {
                            viewModel.closeConfigDialog()
                            viewModel.logout(context) {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}