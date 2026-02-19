package com.example.taskbug.navigation

import androidx.compose.runtime.*
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

    // React to login/logout state changes and navigate accordingly
    LaunchedEffect(userLoggedIn) {
        if (userLoggedIn) {
            navController.navigate("dashboard_root") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("dashboard_root") { inclusive = true }
            }
        }
    }

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
        }

        composable("dashboard_root") {
            DashboardRoot(authViewModel = authViewModel)
        }
    }
}