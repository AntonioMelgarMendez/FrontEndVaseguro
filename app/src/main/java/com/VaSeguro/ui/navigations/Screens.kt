package com.VaSeguro.ui.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable

// Parents
@Serializable
object MapScreenNavigation

@Serializable
object HistoryScreenNavigation

@Serializable
object BusScreenNavigation

@Serializable
object ChildrenScreenNavigation

@Serializable
object ConfigurationScreenNavigation

// Driver
@Serializable
class RouteScreenNavigation(val routeId: String? = null) {
    companion object {
        const val routeIdArg = "routeId"
        operator fun invoke() = "route_screen"
        operator fun invoke(routeId: String) = "route_screen/$routeId"
    }
}

@Serializable
object SavedRoutesScreenNavigation

@Serializable
object RouteMenuScreenNavigation

@Serializable
object ChatScreenNavigation

// Admin
@Serializable
object HomeAdminScreenNavigation

@Serializable
object StopsScreenNavigation

//Auxiliary
@Serializable
object SplashScreenNavigation

@Serializable
object HomeScreenNavigation

// Extension para añadir destinos composables con navegacion tipo seguro
internal inline fun <reified T : Any> NavGraphBuilder.composable(
    crossinline content: @Composable (NavBackStackEntry) -> Unit
) {
    when (T::class) {
        RouteScreenNavigation::class -> {
            composable(
                route = "route_screen?$routeIdArg={$routeIdArg}",
                arguments = listOf(
                    navArgument(RouteScreenNavigation.routeIdArg) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                content = { content(it) }
            )
        }
        SavedRoutesScreenNavigation::class -> {
            composable(
                route = "saved_routes_screen",
                content = { content(it) }
            )
        }
        MapScreenNavigation::class -> {
            composable(
                route = "map_screen",
                content = { content(it) }
            )
        }
        HistoryScreenNavigation::class -> {
            composable(
                route = "history_screen",
                content = { content(it) }
            )
        }
        BusScreenNavigation::class -> {
            composable(
                route = "bus_screen",
                content = { content(it) }
            )
        }
        ChildrenScreenNavigation::class -> {
            composable(
                route = "children_screen",
                content = { content(it) }
            )
        }
        ConfigurationScreenNavigation::class -> {
            composable(
                route = "configuration_screen",
                content = { content(it) }
            )
        }
        RouteMenuScreenNavigation::class -> {
            composable(
                route = "route_menu_screen",
                content = { content(it) }
            )
        }
        ChatScreenNavigation::class -> {
            composable(
                route = "chat_screen",
                content = { content(it) }
            )
        }
        HomeAdminScreenNavigation::class -> {
            composable(
                route = "home_admin_screen",
                content = { content(it) }
            )
        }
        StopsScreenNavigation::class -> {
            composable(
                route = "stops_screen",
                content = { content(it) }
            )
        }
        SplashScreenNavigation::class -> {
            composable(
                route = "splash_screen",
                content = { content(it) }
            )
        }
        HomeScreenNavigation::class -> {
            composable(
                route = "home_screen",
                content = { content(it) }
            )
        }
    }
}

// Variable para evitar duplicar el nombre del argumento
private const val routeIdArg = "routeId"
