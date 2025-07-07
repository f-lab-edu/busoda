package com.chaeny.busoda

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            route = "stop_detail/{stopId}"
        ) { navBackStackEntry ->
            val stopId = navBackStackEntry.arguments?.getString("stopId") ?: ""
            Text(text = stopId)
        }
    }
}
