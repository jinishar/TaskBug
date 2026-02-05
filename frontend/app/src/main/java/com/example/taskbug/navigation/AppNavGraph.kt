package com.example.taskbug.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.taskbug.ui.dashboard.DashboardRoot

@Composable
fun AppNavGraph() {
    NavHost(
        navController = rememberNavController(),
        startDestination = "dashboard_root"
    ) {
        composable("dashboard_root") {
            DashboardRoot()
        }
    }
}
