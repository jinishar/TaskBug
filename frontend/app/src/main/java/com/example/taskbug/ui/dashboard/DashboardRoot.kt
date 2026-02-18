package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.taskbug.ui.events.EventsScreen
import com.example.taskbug.ui.tasks.TasksScreen
import com.example.taskbug.ui.chat.ChatScreen
import com.example.taskbug.ui.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoot() {
    val navController = rememberNavController()

    Scaffold(
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
            composable("profile") { ProfileScreen() }
        }
    }
}
