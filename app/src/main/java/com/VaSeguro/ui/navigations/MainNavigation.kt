// com.VaSeguro.ui.navigations.MainNavigation.kt
package com.VaSeguro.ui.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.VaSeguro.ui.screens.Admin.Account.AccountAdminScreen
import com.VaSeguro.ui.screens.Admin.Children.ChildrenAdminScreen
import com.VaSeguro.ui.screens.Admin.Home.HomeAdminScreen
import com.VaSeguro.ui.screens.Admin.Routes.RoutesAdminScreen
import com.VaSeguro.ui.screens.Admin.Stops.StopsAdminScreen
import com.VaSeguro.ui.screens.Admin.Users.UsersAdminScreen
import com.VaSeguro.ui.screens.Admin.Vehicle.VehicleScreen
import com.VaSeguro.ui.screens.Driver.Route.RouteScreen
import com.VaSeguro.ui.screens.Parents.Bus.BusScreen
import com.VaSeguro.ui.screens.Parents.Children.ChildrenScreen
import com.VaSeguro.ui.screens.Parents.Configuration.ConfigurationScreen
import com.VaSeguro.ui.screens.Parents.History.HistoryScreen
import com.VaSeguro.ui.screens.Parents.Map.MapScreen
import kotlinx.serialization.Serializable

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = RouteScreenNavigation) {

        //PARENTS SCREENS
        composable<MapScreenNavigation> { MapScreen() }
        composable<HistoryScreenNavigation> { HistoryScreen() }
        composable<BusScreenNavigation> { BusScreen() }
        composable<ChildrenScreenNavigation> { ChildrenScreen() }
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
        composable<RouteScreenNavigation> { RouteScreen() }
        composable<BusDriverScreenNavigation>{BusScreen()}
        composable<ChildrenDriverScreenNavigation>{ChildrenScreen() }




    }
}

