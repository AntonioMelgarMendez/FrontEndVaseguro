// com.VaSeguro.ui.navigations.MainNavigation.kt
package com.VaSeguro.ui.navigations


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.map.repository.SavedRoutesRepositoryImpl
import com.VaSeguro.ui.screens.Admin.Account.AccountAdminScreen
import com.VaSeguro.ui.screens.Admin.Children.ChildrenAdminScreen
import com.VaSeguro.ui.screens.Admin.Home.HomeAdminScreen
import com.VaSeguro.ui.screens.Admin.Routes.RoutesAdminScreen
import com.VaSeguro.ui.screens.Admin.Stops.StopsAdminScreen
import com.VaSeguro.ui.screens.Admin.Users.UsersAdminScreen
import com.VaSeguro.ui.screens.Admin.Vehicle.VehicleScreen
import com.VaSeguro.ui.screens.Driver.Chat.Calls.AgoraCallScreen

import com.VaSeguro.ui.screens.Driver.Chat.ChatScreen
import com.VaSeguro.ui.screens.Driver.Route.RouteScreen
import com.VaSeguro.ui.screens.Driver.Route.RouteScreenViewModel
import com.VaSeguro.ui.screens.Driver.SavedRoutes.SavedRoutesScreen
import com.VaSeguro.ui.screens.Driver.SavedRoutes.SavedRoutesViewModel
import com.VaSeguro.ui.screens.Parents.Bus.BusScreen
import com.VaSeguro.ui.screens.Parents.Children.ChildrenScreen
import com.VaSeguro.ui.screens.Parents.Configuration.ConfigurationScreen
import com.VaSeguro.ui.screens.Parents.History.HistoryScreen
import com.VaSeguro.ui.screens.Parents.Map.MapScreen

@Composable
fun MainNavigation(navController: NavHostController, isAdmin: Boolean) {
    val startDestination = if (isAdmin) HomeAdminScreenNavigation else MapScreenNavigation
    // Creamos un repositorio compartido para las rutas guardadas
    val savedRoutesRepository = remember { SavedRoutesRepositoryImpl() }

    // Compartimos un ViewModel para la pantalla principal de Rutas
    val routeViewModel: RouteScreenViewModel = viewModel(factory = RouteScreenViewModel.Factory)

    // Compartimos el ViewModel para la pantalla de rutas guardadas
    val savedRoutesViewModel: SavedRoutesViewModel = viewModel(
        factory = SavedRoutesViewModel.Factory
    )


    // Definimos las acciones de navegación
    val onNavigateToSavedRoutes = {
        println("DEBUG: Navegando a rutas guardadas")
        navController.navigate(SavedRoutesScreenNavigation)
    }

    val onRunRoute = { routeId: Int ->
        println("DEBUG: Ejecutando ruta $routeId")
        navController.navigate(RouteScreenNavigation(routeId))
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<MapScreenNavigation> { MapScreen() }
        composable<HistoryScreenNavigation> { HistoryScreen() }
        composable<BusScreenNavigation> { BusScreen() }
        composable<ChildrenScreenNavigation> { ChildrenScreen(navController) }
        composable<ConfigurationScreenNavigation> { ConfigurationScreen() }
        //ADMIN SCREENS
        composable <HomeAdminScreenNavigation>{ HomeAdminScreen() }
        composable<AccountScreenNavigation> { AccountAdminScreen() }
        composable<ChildrenAdminScreenNavigation> { ChildrenAdminScreen() }
        composable<RoutesAdminScreenNavigation> { RoutesAdminScreen() }
        composable<StopsAdminScreenNavigation> { StopsAdminScreen() }
        composable<UsersAdminScreenNavigation> { UsersAdminScreen() }
        composable<VehiclesAdminScreenNavigation> { VehicleScreen() }

        //DRIVER SCREENS
        composable<RouteScreenNavigation> { backStackEntry ->
            // Obtenemos el parámetro routeId si existe
            val routeId = backStackEntry.arguments?.getInt("routeId")

            RouteScreen(
                routeId = routeId,
                onNavigateToSavedRoutes = onNavigateToSavedRoutes
            )
        }
        composable<BusDriverScreenNavigation>{ BusScreen() }
        composable<ChildrenDriverScreenNavigation>{ ChildrenScreen(navController) }
        composable<HistoryScreenNavigation>{ HistoryScreen() }

        composable<SavedRoutesScreenNavigation> {
            println("DEBUG: Mostrando pantalla de rutas guardadas")
            SavedRoutesScreen(
                navController = navController,
                viewModel = savedRoutesViewModel,
                onRunRoute = onRunRoute,
            )
        }
        composable<ChatScreenNavigation> { backStackEntry ->
            val args = backStackEntry.toRoute<ChatScreenNavigation>()

            ChatScreen(
                navController = navController,
                id= args.id,
            )
        }
        composable<CallScreenNavigation> { backStackEntry ->
            val args = backStackEntry.toRoute<CallScreenNavigation>()
            AgoraCallScreen(
                channelName = args.roomName,
                personName = args.personName,
                personPhotoUrl = args.personPhotoUrl,
                onCallEnd = {
                    navController.navigate(ChatScreenNavigation(args.id)) {
                        popUpTo(CallScreenNavigation(args.roomName, args.id, args.personName, args.personPhotoUrl)) { inclusive = true }
                    }
                }
            )
        }
    }
}