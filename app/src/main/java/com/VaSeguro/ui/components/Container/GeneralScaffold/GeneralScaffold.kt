package com.VaSeguro.ui.components.Container.GeneralScaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.components.Container.BottomBar.BottomBar
import com.VaSeguro.ui.components.Container.TopBarContainer.TopBar
import com.VaSeguro.ui.navigations.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*

import kotlinx.coroutines.launch

@Composable
fun GeneralScaffold(navControllerx: NavController) {
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

    if (user == null) return

    val isAdmin = user?.role_id == 2

    val navItems = when (user?.role_id) {
        2 -> listOf("Inicio", "Hijos", "Rutas", "Paradas", "Usuarios", "Buses")
        3 -> listOf("Mapa", "Historial", "Bus", "Hijo")
        4 -> listOf("Mapa", "Mis Rutas", "Mi Bus", "Clientes")
        else -> listOf("Map")
    }

    // Set initial selected item and title based on role
    val initialSelected = when (user?.role_id) {
        2 -> "Inicio"
        3 -> "Mapa"
        4 -> "Mapa"
        else -> "Map"
    }
    var selectedItem by remember { mutableStateOf(initialSelected) }
    var title by remember { mutableStateOf(initialSelected) }

    fun onItemSelected(currentItem: String) {
        selectedItem = currentItem
        title = currentItem
        when (currentItem) {
            "Mapa" -> navController.navigate(MapScreenNavigation)
            "Historial" -> navController.navigate(HistoryScreenNavigation)
            "Mi Bus" -> navController.navigate(BusDriverScreenNavigation)
            "Bus" -> navController.navigate(BusScreenNavigation)
            "Hijos" -> navController.navigate(ChildrenAdminScreenNavigation)
            "Hijo" -> navController.navigate(ChildrenScreenNavigation)
            "Clientes" -> navController.navigate(ChildrenDriverScreenNavigation)
            "Inicio" -> navController.navigate(HomeAdminScreenNavigation)
            "Rutas" -> navController.navigate(RoutesAdminScreenNavigation)
            "Mis Rutas" -> navController.navigate(RouteScreenNavigation)
            "Paradas" -> navController.navigate(StopsAdminScreenNavigation)
            "Usuarios" -> navController.navigate(UsersAdminScreenNavigation)
            "Buses" -> navController.navigate(VehiclesAdminScreenNavigation)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = title,
                navController = navControllerx,
                navControllerx = navController
            )
        },
        bottomBar = {
            BottomBar(
                selectedItem = selectedItem,
                onItemSelected = { onItemSelected(it) },
                navItems = navItems
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // 👈 Esto respeta top/bottom bars
            ) {
                MainNavigation(navController = navController, isAdmin = isAdmin)
            }
        }
    )
}