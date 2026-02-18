package com.example.taskbug.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskbug.ui.auth.AuthViewModel
import com.example.taskbug.ui.dashboard.DashboardRoot

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    NavHost(
        navController = rememberNavController(),
        startDestination = "dashboard_root"
    ) {
        composable("dashboard_root") {
            DashboardRoot(authViewModel = authViewModel)
        }
    }
}
