package com.example.taskbug.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/* ---------- DESIGN SYSTEM REFINED ---------- */
private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val AppBorder = Color(0xFFE5E7EB)

data class Task(
    val title: String,
    val description: String,
    val deadline: String,
    val location: String,
    val category: String,
    val reward: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen() {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Shopping", "Home", "Pets", "Technical")
    
    val tasksList = remember {
        mutableStateListOf(
            Task("Groceries Delivery", "Need someone to pick up groceries from the local store.", "Oct 25, 5:00 PM", "Brooklyn, NY", "Shopping", "₹1,200"),
            Task("Furniture Assembly", "Help needed to assemble a new IKEA desk.", "Oct 26, 10:00 AM", "Queens, NY", "Home", "₹2,500"),
            Task("Dog Walking", "Walk my friendly golden retriever for 30 minutes.", "Oct 26, 4:00 PM", "Central Park", "Pets", "₹800")
        )
    }

    val filteredTasks = remember(searchQuery, selectedCategory, tasksList.size) {
        tasksList.filter { task ->
            val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) || 
                              task.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || task.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with Search and Filter
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Tasks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTeal,
                        unfocusedBorderColor = AppBorder
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTeal.copy(alpha = 0.1f),
                                selectedLabelColor = AppTeal,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = AppBorder,
                                selectedBorderColor = AppTeal,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No tasks found", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskCard(task, onClick = { selectedTask = task })
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onPost = { newTask ->
                    tasksList.add(0, newTask)
                    showAddTaskDialog = false
                }
            )
        }

        selectedTask?.let {
            TaskDetailsPopup(task = it, onDismiss = { selectedTask = null })
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppBorder.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(40.dp), tint = TextSecondary)
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = task.reward,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTeal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.description, fontSize = 14.sp, color = TextSecondary, maxLines = 1)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppBorder)
                Spacer(modifier = Modifier.height(12.dp))
                
                TaskDetailItem(Icons.Default.AccessTime, task.deadline)
                TaskDetailItem(Icons.Default.Place, task.location)
            }
        }
    }
}

@Composable
fun TaskDetailsPopup(task: Task, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Header with Close Icon
                Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 8.dp)) {
                    Text(
                        text = "Task Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart),
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Image (90% width approx via padding)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppBorder.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = task.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text(text = task.reward, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppTeal)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryTag(task.category)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = task.description, fontSize = 15.sp, color = TextSecondary, lineHeight = 22.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = AppBorder)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow(Icons.Default.CalendarToday, "Deadline", task.deadline)
                    DetailRow(Icons.Default.Place, "Location", task.location)

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { /* Save */ },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AppTeal)
                        ) {
                            Text("Save", color = AppTeal)
                        }
                        Button(
                            onClick = { /* Apply */ },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                        ) {
                            Text("Apply Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = AppTeal)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
fun TaskDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppTeal)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
fun CategoryTag(category: String) {
    Surface(color = AppTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(text = category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTeal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onPost: (Task) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pay by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Post a New Task", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                // Photo Upload Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppBorder.copy(alpha = 0.3f))
                        .clickable { /* Photo Picker Logic */ },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = AppTeal, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Upload Task Photo", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Task Title") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder), 
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text("Description") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 3, 
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder), 
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = pay, 
                    onValueChange = { pay = it }, 
                    label = { Text("Pay (₹)") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    prefix = { Text("₹") },
                    placeholder = { Text("Enter amount in Rupees") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder), 
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder)
                    )
                }

                OutlinedTextField(
                    value = location, 
                    onValueChange = { location = it }, 
                    label = { Text("Location") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder), 
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { 
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onPost(Task(title, description, "$date, $time", location, "Task", "₹$pay"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text("Post Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
