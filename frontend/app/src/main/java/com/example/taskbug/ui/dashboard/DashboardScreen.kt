package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = { TopSearchBar(navController) },
        bottomBar = { 
            BottomBar(navController = navController) 
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Backend feed will come here
            Text("Dashboard Feed", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
        }
    }
}
