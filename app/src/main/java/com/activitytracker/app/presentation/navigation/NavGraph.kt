package com.activitytracker.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.activitytracker.app.presentation.permissions.PermissionsScreen

/**
 * Main navigation graph for the app.
 * Defines all screen destinations and navigation routes.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Permissions.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Permissions screen
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home screen
        composable(Screen.Home.route) {
            com.activitytracker.app.presentation.home.HomeScreen(navController = navController)
        }
        
        // Activity list screen
        composable(Screen.ActivityList.route) {
            // ActivityListScreen will be implemented in Task 7
            // ActivityListScreen(navController = navController)
        }
        
        // Activity detail screen with sessionId argument
        composable(
            route = Screen.ActivityDetail.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
            // ActivityDetailScreen will be implemented in Task 8
            // ActivityDetailScreen(sessionId = sessionId, navController = navController)
        }
        
        // Bike location screen
        composable(Screen.BikeLocation.route) {
            // BikeLocationScreen will be implemented in Task 9
            // BikeLocationScreen(navController = navController)
        }
        
        // Statistics screen
        composable(Screen.Statistics.route) {
            // StatisticsScreen will be implemented in Task 10
            // StatisticsScreen(navController = navController)
        }
    }
}
