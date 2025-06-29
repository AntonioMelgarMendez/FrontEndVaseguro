
package com.VaSeguro.ui.components.Container.GeneralScaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.components.Container.BottomBar.BottomBar
import com.VaSeguro.ui.components.Container.TopBarContainer.TopBar
import com.VaSeguro.ui.navigations.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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

    val navRoutes = when (user?.role_id) {
        2 -> listOf(
            HomeAdminScreenNavigation,
            ChildrenAdminScreenNavigation,
            RoutesAdminScreenNavigation,
            StopsAdminScreenNavigation,
            UsersAdminScreenNavigation,
            VehiclesAdminScreenNavigation
        )
        3 -> listOf(
            MapScreenNavigation,
            HistoryScreenNavigation,
            BusScreenNavigation,
            ChildrenScreenNavigation
        )
        4 -> listOf(
            MapScreenNavigation,
            HistoryScreenNavigation, // "Mis Rutas" mapped to History
            BusDriverScreenNavigation,
            ChildrenDriverScreenNavigation
        )
        else -> listOf(MapScreenNavigation)
    }

    val initialSelected = navItems.first()
    var selectedItem by remember { mutableStateOf(initialSelected) }
    var title by remember { mutableStateOf(initialSelected) }

    val pagerState = rememberPagerState(
        pageCount = { navItems.size },
        initialPage = navItems.indexOf(initialSelected)
    )
    val coroutineScope = rememberCoroutineScope()

    // Sync bottom bar and pager
    fun onItemSelected(currentItem: String) {
        val pageIndex = navItems.indexOf(currentItem)
        selectedItem = currentItem
        title = currentItem
        coroutineScope.launch {
            pagerState.animateScrollToPage(pageIndex)
        }
        navController.navigate(navRoutes[pageIndex]) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Sync pager swipe with navigation and bottom bar
    LaunchedEffect(pagerState.currentPage) {
        val newItem = navItems[pagerState.currentPage]
        if (selectedItem != newItem) {
            selectedItem = newItem
            title = newItem
            navController.navigate(navRoutes[pagerState.currentPage]) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    fun isChatRoute(route: String?): Boolean {
        return route?.contains("ChatScreenNavigation") == true
    }
    fun isCallRoute(route: String?): Boolean {
        return route?.contains("CallScreenNavigation") == true
    }

    if (!isChatRoute(currentRoute) && !isCallRoute(currentRoute)) {
        Scaffold(
            containerColor = Color.White,
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { page ->
                    Box(Modifier.fillMaxSize()) {
                        MainNavigation(
                            navController = navController,
                            isAdmin = isAdmin
                        )
                    }
                }
            }
        )
    } else {
        Scaffold(
            containerColor = Color.White,
            topBar = {},
            bottomBar = {},
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MainNavigation(navController = navController, isAdmin = isAdmin)
                }
            }
        )
    }
}