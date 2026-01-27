package com.chaeny.busoda

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.chaeny.busoda.favorites.FavoritesScreen
import com.chaeny.busoda.nearbystops.NearbystopsScreen
import com.chaeny.busoda.stopdetail.StopDetailScreen
import com.chaeny.busoda.stoplist.StopListScreen

@Composable
fun BusodaNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "favorites"
    ) {
        composable(
            route = "favorites"
        ) {
            FavoritesScreen(
                navigateToStopList = {
                    navController.navigate("stop_list")
                },
                navigateToStopDetail = { stopId ->
                    navController.navigate("stop_detail/$stopId")
                },
                navigateToNearbyStops = {
                    navController.navigate("nearbystops")
                }
            )
        }
        composable(
            route = "nearbystops"
        ) {
            NearbystopsScreen()
        }
        composable(
            route = "stop_list"
        ) {
            StopListScreen(
                navigateToStopDetail = { stopId ->
                    navController.navigate("stop_detail/$stopId")
                },
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "stop_detail/{stopId}",
            arguments = listOf(navArgument("stopId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "busoda://stop_detail/{stopId}" })
        ) {
            StopDetailScreen()
        }
    }
}
