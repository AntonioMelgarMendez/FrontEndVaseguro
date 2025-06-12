// com.VaSeguro.ui.navigations.MainNavigation.kt
package com.VaSeguro.ui.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.VaSeguro.ui.screens.Admin.Account.AccountAdminScreen
import com.VaSeguro.ui.screens.Admin.Children.ChildrenAdminScreen
import com.VaSeguro.ui.screens.Admin.Routes.RoutesAdminScreen
import com.VaSeguro.ui.screens.Admin.Stops.StopsAdminScreen
import com.VaSeguro.ui.screens.Admin.Users.UsersAdminScreen
import com.VaSeguro.ui.screens.Admin.Vehicle.VehicleScreen
import com.VaSeguro.ui.screens.Parents.Bus.BusScreen
import com.VaSeguro.ui.screens.Parents.Children.ChildrenScreen
import com.VaSeguro.ui.screens.Parents.History.HistoryScreen
import com.VaSeguro.ui.screens.Parents.Map.MapScreen

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = MapScreenNavigation) {
        composable<MapScreenNavigation> { MapScreen() }
        composable<HistoryScreenNavigation> { HistoryScreen() }
        composable<BusScreenNavigation> { BusScreen() }
        composable<ChildrenScreenNavigation> { ChildrenScreen() }
        //admin
        composable<AccountAdmminScreenNavigation>{ AccountAdminScreen() }
        composable<ChildrenAdminScreenNavigation> { ChildrenAdminScreen() }
        composable<RoutesAdminScreenNavigation>{RoutesAdminScreen()}
        composable <StopsAdminScreenNavigation>{ StopsAdminScreen() }
        composable<UsersAdminScreenNavigation>{ UsersAdminScreen() }
        composable<VehicleAdminScreenNavigation>{ VehicleScreen() }
    }
}

