package com.example.taskbug.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "tasks",
            onClick = { navController.navigate("tasks") },
            icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
            label = { Text("Tasks") }
        )

        NavigationBarItem(
            selected = currentRoute == "events",
            onClick = { navController.navigate("events") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
            label = { Text("Events") }
        )

        NavigationBarItem(
            selected = currentRoute == "chat",
            onClick = { navController.navigate("chat") },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
            label = { Text("Chat") }
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
