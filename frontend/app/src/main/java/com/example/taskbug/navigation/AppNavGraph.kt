package com.example.taskbug.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskbug.ui.auth.AuthViewModel
import com.example.taskbug.ui.dashboard.DashboardRoot
import com.example.taskbug.ui.map.LiveLocationMapScreen
import com.example.taskbug.ui.map.MapPickerScreen

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    NavHost(
        navController = rememberNavController(),
        startDestination = "dashboard_root"
    ) {
        composable("dashboard_root") {
            DashboardRoot(authViewModel = authViewModel)
        }
        composable("live_location") {
            LiveLocationMapScreen(onNavigateBack = { /* Need to pass navController down to DashboardRoot to handle this, or handle at Root level */ })
        }
    }
}
