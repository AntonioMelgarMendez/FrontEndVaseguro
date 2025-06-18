// com.VaSeguro.ui.navigations.MainNavigation.kt
package com.VaSeguro.ui.navigations

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.VaSeguro.data.repository.SavedRoutesRepository
import com.VaSeguro.ui.screens.Driver.Route.RouteScreen
import com.VaSeguro.ui.screens.Driver.Route.RouteScreenViewModel
import com.VaSeguro.ui.screens.Driver.SavedRoutes.SavedRoutesScreen
import com.VaSeguro.ui.screens.Driver.SavedRoutes.SavedRoutesViewModel
import com.VaSeguro.ui.screens.Parents.Bus.BusScreen
import com.VaSeguro.ui.screens.Parents.Children.ChildrenScreen
import com.VaSeguro.ui.screens.Parents.History.HistoryScreen
import com.VaSeguro.ui.screens.Parents.Map.MapScreen
import com.VaSeguro.ui.screens.Start.Login.LoginScreen
import com.VaSeguro.ui.screens.Start.SignUp.SignUpScreen
import com.VaSeguro.ui.screens.Start.Starting.StartingScreen
import com.VaSeguro.ui.screens.Utils.SplashScreen

@SuppressLint("WrongStartDestinationType")
@Composable
fun MainNavigation(navController: NavHostController) {
    // Creamos un repositorio compartido para las rutas guardadas
    val savedRoutesRepository = remember { SavedRoutesRepository() }

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

    val onRunRoute = { routeId: String ->
        println("DEBUG: Ejecutando ruta $routeId")
        navController.navigate(RouteScreenNavigation(routeId))
    }

    val onEditRoute = { routeId: String ->
        println("DEBUG: Editando ruta $routeId")
        navController.navigate(RouteScreenNavigation(routeId))
    }

    // Funciones de navegación para las pantallas de inicio
    val onNavigateToLogin = {
        navController.navigate("login_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    val onNavigateToSignUp = {
        navController.navigate("signup_screen")
    }

    val onNavigateToMain = {
        navController.navigate(RouteScreenNavigation()) {
            popUpTo("login_screen") { inclusive = true }
        }
    }

    // Usando navegación tipo seguro con RouteScreenNavigation() como destino inicial
    NavHost(navController = navController, startDestination = RouteScreenNavigation()) {
        // Rutas con navegación tipo seguro
        composable<RouteScreenNavigation> { backStackEntry ->
            // Obtenemos el parámetro routeId si existe
            val routeId = backStackEntry.arguments?.getString("routeId")

            RouteScreen(
                viewModel = routeViewModel,
                routeId = routeId,
                savedRoutesRepository = savedRoutesRepository,
                onNavigateToSavedRoutes = onNavigateToSavedRoutes
            )
        }

        composable<SavedRoutesScreenNavigation> {
            println("DEBUG: Mostrando pantalla de rutas guardadas")
            SavedRoutesScreen(
                navController = navController,
                viewModel = savedRoutesViewModel,
                onRunRoute = onRunRoute,
                onEditRoute = onEditRoute
            )
        }

        composable<MapScreenNavigation> {
            MapScreen()
        }

        composable<HistoryScreenNavigation> {
            HistoryScreen()
        }

        composable<BusScreenNavigation> {
            BusScreen()
        }

        composable<ChildrenScreenNavigation> {
            ChildrenScreen()
        }

        // Pantallas de inicio y autenticación (aún usando rutas de texto)
        composable("splash_screen") {
            SplashScreen(
                navController
            )
        }

        composable("login_screen") {
            LoginScreen(navController)
        }

        composable("signup_screen") {
            SignUpScreen(
                navController
            )
        }

        composable("starting_screen") {
            StartingScreen(
                navController)
        }
    }
}
