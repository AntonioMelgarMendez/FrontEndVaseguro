// com.VaSeguro.ui.navigations.MainNavigation.kt
package com.VaSeguro.ui.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.VaSeguro.ui.screens.Driver.Route.RouteScreen
import com.VaSeguro.ui.screens.Parents.Bus.BusScreen
import com.VaSeguro.ui.screens.Parents.Children.ChildrenScreen
import com.VaSeguro.ui.screens.Parents.History.HistoryScreen
import com.VaSeguro.ui.screens.Parents.Map.MapScreen

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = RouteScreenNavigation) {
        composable<MapScreenNavigation> { MapScreen() }
        composable<HistoryScreenNavigation> { HistoryScreen() }
        composable<BusScreenNavigation> { BusScreen() }
        composable<ChildrenScreenNavigation> { ChildrenScreen() }
        composable<RouteScreenNavigation> { RouteScreen() }
    }
}

