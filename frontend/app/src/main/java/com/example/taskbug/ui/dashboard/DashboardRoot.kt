package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskbug.ui.auth.AuthViewModel
import com.example.taskbug.ui.chat.ChatScreen
import com.example.taskbug.ui.events.EventsScreen
import com.example.taskbug.ui.profile.ProfileScreen
import com.example.taskbug.ui.tasks.TasksScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoot(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showSearchBar = currentRoute == "tasks" || currentRoute == "events"

    Scaffold(
        topBar = {
            if (showSearchBar) {
                TopSearchBar(navController)
            }
        },
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "tasks",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("tasks") { TasksScreen() }
            composable("events") { EventsScreen() }
            composable("chat") { ChatScreen() }
            composable("profile") { ProfileScreen(authViewModel = authViewModel) }
        }
    }
}
