package com.example.taskbug.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

private val Terra      = Color(0xFFC1603A)
private val TerraLt    = Color(0xFFF5EDE8)
private val Cream      = Color(0xFFFAF6F1)
private val Ink        = Color(0xFF1E1712)
private val Muted      = Color(0xFFA08878)

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        contentColor = Ink
    ) {
        NavigationBarItem(
            selected = currentRoute == "tasks",
            onClick = { navController.navigate("tasks") },
            icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Terra,
                selectedTextColor = Terra,
                indicatorColor = TerraLt,
                unselectedIconColor = Muted,
                unselectedTextColor = Muted
            )
        )
        NavigationBarItem(
            selected = currentRoute == "events",
            onClick = { navController.navigate("events") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
            label = { Text("Events") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Terra,
                selectedTextColor = Terra,
                indicatorColor = TerraLt,
                unselectedIconColor = Muted,
                unselectedTextColor = Muted
            )
        )
        NavigationBarItem(
            selected = currentRoute == "chat",
            onClick = { navController.navigate("chat") },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
            label = { Text("Chat") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Terra,
                selectedTextColor = Terra,
                indicatorColor = TerraLt,
                unselectedIconColor = Muted,
                unselectedTextColor = Muted
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Terra,
                selectedTextColor = Terra,
                indicatorColor = TerraLt,
                unselectedIconColor = Muted,
                unselectedTextColor = Muted
            )
        )
    }
}