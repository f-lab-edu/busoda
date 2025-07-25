package com.chaeny.busoda

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.chaeny.busoda.stopdetail.StopDetailScreen
import com.chaeny.busoda.stoplist.StopListScreen

@Composable
fun BusodaNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "stop_list"
    ) {
        composable(
            route = "stop_list"
        ) {
            StopListScreen(
                onStopClick = { stopId ->
                    navController.navigate("stop_detail/$stopId")
                }
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
