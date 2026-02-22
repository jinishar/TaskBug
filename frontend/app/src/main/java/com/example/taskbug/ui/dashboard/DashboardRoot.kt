package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ── Shared search state lifted here so TopSearchBar can drive both screens ──
    var searchQuery by remember { mutableStateOf("") }

    val showTopBar = currentRoute == "tasks" || currentRoute == "events"

    // Reset search when switching tabs
    LaunchedEffect(currentRoute) { searchQuery = "" }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopSearchBar(
                    navController = navController,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
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
            composable("tasks")   { TasksScreen(searchQuery = searchQuery) }
            composable("events")  { EventsScreen(searchQuery = searchQuery) }
            composable("chat")    { ChatScreen() }
            composable("profile") { ProfileScreen(authViewModel = authViewModel) }
        }
    }
}