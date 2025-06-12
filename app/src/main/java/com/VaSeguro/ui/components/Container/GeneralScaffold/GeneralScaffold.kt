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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.components.Container.BottomBar
import com.VaSeguro.ui.components.Container.TopBarContainer.TopBar
import com.VaSeguro.ui.navigations.*

@Composable
fun GeneralScaffold(NavControllerEX: NavController) {
    val context = LocalContext.current
    val viewModel: GeneralScaffoldViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return GeneralScaffoldViewModel(
                    userPreferencesRepository = appProvider.provideUserPreferences()
                ) as T
            }
        }
    )

    val role by viewModel.role.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    var title by remember { mutableStateOf("Map") }
    var selectedItem by remember { mutableStateOf("Map") }

    val navItems = when (role) {
        // 2 is admin, 3 is user and 4 is driver
        2 -> listOf("Home","Usuarios","Paradas","Rutas","Aprove","Autos")
        3 -> listOf("Mapa", "Historial", "Bus", "Hijos")
        4 -> listOf("Mapa", "Bus","Pasajeros", "Rutas")
        else -> listOf("Home","Users","Stops","Hijos","Routes","Solicitudes")
    }

    fun onItemSelected(currentItem: String) {
        selectedItem = currentItem
        title = currentItem

        when (currentItem) {
            //Usuario
            "Mapa" -> navController.navigate(MapScreenNavigation)
            "Historial" -> navController.navigate(HistoryScreenNavigation)
            "Bus" -> navController.navigate(BusScreenNavigation)
            "Hijos" -> navController.navigate(ChildrenScreenNavigation)
            //Administrador
            //Falta Home
            "Usuarios" -> navController.navigate(UsersAdminScreenNavigation)
            "Paradas" -> navController.navigate(StopsAdminScreenNavigation)
            "Rutas" -> navController.navigate(RoutesAdminScreenNavigation)
            "Vehiculos" -> navController.navigate(VehicleAdminScreenNavigation)
            //Driver



        }
    }

    Scaffold(
        topBar = { TopBar(navController,NavControllerEX,title = title) },
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