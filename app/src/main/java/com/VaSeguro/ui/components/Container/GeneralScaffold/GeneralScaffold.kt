package com.VaSeguro.ui.components.Container.GeneralScaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.data.AppProvider

import com.VaSeguro.ui.components.Container.BottomBar.BottomBar
import com.VaSeguro.ui.components.Container.TopBarContainer.TopBar
import com.VaSeguro.ui.navigations.*

@Composable
fun GeneralScaffold() {
    val context = LocalContext.current
    val viewModel: GeneralScaffoldViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return GeneralScaffoldViewModel(
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    val user by viewModel.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    var title by remember { mutableStateOf("Map") }
    var selectedItem by remember { mutableStateOf("Map") }

    if (user == null) return

    val navItems = when (user?.role_id) {
        2 -> listOf("Inicio", "Hijos", "Rutas", "Paradas", "Usuarios", "Vehiculos")
        3 -> listOf("Mapa", "Historial", "Bus", "Hijos")
        4 -> listOf("Mapa", "Mis Rutas", "Mi Bus", "Clientes")
        else -> listOf("Map")
    }

    fun onItemSelected(currentItem: String) {
        selectedItem = currentItem
        title = currentItem
        when (currentItem) {
            "Mapa" -> navController.navigate(MapScreenNavigation)
            "Historial" -> navController.navigate(HistoryScreenNavigation)
            "Mi Bus" -> navController.navigate(BusDriverScreenNavigation)
            "Bus" -> navController.navigate(BusScreenNavigation)
            "Hijos" -> navController.navigate(ChildrenScreenNavigation)
            "Clientes" -> navController.navigate(ChildrenDriverScreenNavigation)
            "Inicio" -> navController.navigate(HomeAdminScreenNavigation)
            "Rutas" -> navController.navigate(RoutesAdminScreenNavigation)
            "Mis Rutas" -> navController.navigate(RouteScreenNavigation)
            "Paradas" -> navController.navigate(StopsScreenNavigation)
            "Usuarios" -> navController.navigate(UsersAdminScreenNavigation)
            "Vehiculos" -> navController.navigate(VehiclesAdminScreenNavigation)
        }
    }

    Scaffold(
        topBar = { TopBar(title = title) },
        bottomBar = {
            BottomBar(
                selectedItem = selectedItem,
                onItemSelected = { onItemSelected(it) },
                navItems = navItems
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MainNavigation(navController = navController)
        }
    }
}