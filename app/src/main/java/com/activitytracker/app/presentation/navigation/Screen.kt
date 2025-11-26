package com.activitytracker.app.presentation.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object Permissions : Screen("permissions")
    object Home : Screen("home")
    object ActivityList : Screen("activity_list")
    object ActivityDetail : Screen("activity_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "activity_detail/$sessionId"
    }
    object BikeLocation : Screen("bike_location")
    object Statistics : Screen("statistics")
}
