package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTaskFilter = currentRoute == "tasks"

    CenterAlignedTopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 🔍 SEARCH FIELD (80%)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search tasks, events…") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(0.8f)
                        .height(52.dp)
                )

                // 🎛 FILTER ICON (20%)
                OutlinedButton(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(0.2f)
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter"
                    )
                }
            }
        }
    )

    if (showFilterSheet) {
        FilterBottomSheet(
            isTaskFilter = isTaskFilter,
            onDismiss = { showFilterSheet = false },
            onApply = { showFilterSheet = false }
        )
    }
}
