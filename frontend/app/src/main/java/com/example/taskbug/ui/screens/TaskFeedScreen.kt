package com.example.taskbug.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.model.Task
import com.example.taskbug.ui.dashboard.FilterBottomSheet
import com.example.taskbug.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val AppBorder = Color(0xFFE5E7EB)

@Composable
fun TaskFeedScreen(
    onEditTask: (String) -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()
    var showFilterSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header with Filter Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Active Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (uiState.selectedCategory != null) {
                    Text(
                        text = "Filtered by: ${uiState.selectedCategory}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            IconButton(onClick = { showFilterSheet = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = AppTeal)
            }
        }

        // Loading State
        if (uiState.isLoading && uiState.tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppTeal)
            }
        }
        // Empty State
        else if (uiState.tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Task,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tasks available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        }
        // Task List
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tasks) { task ->
                    TaskCard(
                        task = task,
                        isOwner = task.userId == currentUserId,
                        onEdit = { onEditTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id, task.userId) },
                        onEnroll = { viewModel.enrollTask(task.id) }
                    )
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                isTaskFilter = true,
                selectedCategory = uiState.selectedCategory,
                selectedPriceRange = uiState.selectedPriceRange,
                onApplyFilters = { category, priceRange ->
                    // Apply category filter
                    if (category != null) {
                        viewModel.filterByCategory(category)
                    }
                    // Apply price filter
                    viewModel.filterByPriceRange(priceRange)
                },
                onDismiss = { showFilterSheet = false },
                onClearAll = { viewModel.clearAllFilters() }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEnroll: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isOwner) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AppTeal.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Yours",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTeal,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "by ${task.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Menu Button (only for owner)
                if (isOwner) {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    onEdit()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showDeleteConfirm = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                    }
                }

                // Price
                if (task.pay > 0) {
                    Text(
                        text = "₹${task.pay.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enroll Button (for non-owners)
            if (!isOwner && task.status == "active") {
                Button(
                    onClick = onEnroll,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enroll in Task", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Tag
            Surface(
                color = AppTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = task.category.ifEmpty { "General" },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTeal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Divider
            HorizontalDivider(color = AppBorder)

            Spacer(modifier = Modifier.height(8.dp))

            // Details (Date and Location)
            if (task.deadline.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Task,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.deadline,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            if (task.location.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Created date
            if (task.createdAt != null) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val dateString = dateFormat.format(task.createdAt)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Posted on $dateString",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

