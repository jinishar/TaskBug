package com.example.taskbug.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskbug.ui.auth.AuthViewModel
import com.example.taskbug.ui.auth.LoginScreen
import com.example.taskbug.ui.dashboard.DashboardRoot

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val userLoggedIn by authViewModel.userLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.authError.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (userLoggedIn) "dashboard_root" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClicked = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onSignUpClicked = { name, email, phone, password ->
                    authViewModel.signUp(name, email, phone, password)
                },
                authError = authError,
                isLoading = isLoading
            )
            // Navigate on login
            if (userLoggedIn) {
                navController.navigate("dashboard_root") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("dashboard_root") {
            DashboardRoot(authViewModel = authViewModel)
            // Navigate to login on sign out
            if (!userLoggedIn) {
                navController.navigate("login") {
                    popUpTo("dashboard_root") { inclusive = true }
                }
            }
        }
    }
}