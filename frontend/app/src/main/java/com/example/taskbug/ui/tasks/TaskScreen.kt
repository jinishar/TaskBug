package com.example.taskbug.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.ui.screens.AddTaskScreen
import com.example.taskbug.ui.screens.TaskFeedScreen

private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TaskViewModel = viewModel()
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = AppTeal,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        TaskFeedScreen(
            onEditTask = { taskId ->
                // Handle edit task here
            }
        )

        if (showAddTaskDialog) {
            Dialog(
                onDismissRequest = { showAddTaskDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                AddTaskScreen(
                    onTaskCreated = { showAddTaskDialog = false },
                    onDismiss = { showAddTaskDialog = false }
                )
            }
        }
    }
}
