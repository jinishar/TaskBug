package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

private val Terra  = Color(0xFFC1603A)
private val Cream  = Color(0xFFFAF6F1)
private val Border = Color(0xFFEAE0D8)
private val Muted  = Color(0xFFA08878)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    navController: NavController,
    query: String,
    onQueryChange: (String) -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTaskFilter = currentRoute == "tasks"

    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        if (isTaskFilter) "Search tasks…" else "Search events…",
                        color = Muted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terra,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Cream,
                    cursorColor = Terra
                )
            )
            OutlinedButton(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(50.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Terra, containerColor = Cream)
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Terra)
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            isTaskFilter = isTaskFilter,
            onDismiss = { showFilterSheet = false },
            onApply = { showFilterSheet = false }
        )
    }
}