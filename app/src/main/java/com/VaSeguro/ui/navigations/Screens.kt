package com.VaSeguro.ui.navigations

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
data class RouteScreenNavigation(val routeId: Int = -1)

@Serializable
object SavedRoutesScreenNavigation
@Serializable
object RouteMenuScreenNavigation

@Serializable
data class  ChatScreenNavigation(val id: String)
@Serializable
object BusDriverScreenNavigation
@Serializable
object ChildrenDriverScreenNavigation

// Admin

@Serializable
object AccountScreenNavigation
@Serializable
object ChildrenAdminScreenNavigation
@Serializable
object RoutesAdminScreenNavigation
@Serializable
object StopsAdminScreenNavigation
@Serializable
object UsersAdminScreenNavigation
@Serializable
object VehiclesAdminScreenNavigation
@Serializable
object HomeAdminScreenNavigation
@Serializable
object StopsScreenNavigation
//Auxiliary
@Serializable
object SplashScreenNavigation
@Serializable
object HomeScreenNavigation
@Serializable
object ChanScreenNavigation
@Serializable
data class CallScreenNavigation(
    val roomName: String,
    val id: String,
    val personName: String,
    val personPhotoUrl: String? = null,
    val callerOneSignalId: String,
    val calleeOneSignalId: String?
)