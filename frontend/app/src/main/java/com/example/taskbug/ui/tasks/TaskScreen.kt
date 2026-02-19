package com.example.taskbug.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/* ---------- DESIGN TOKENS ---------- */
private val AppTeal = Color(0xFFC1603A)
private val AppBackground = Color(0xFFFAF6F1)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF1E1712)
private val TextSecondary = Color(0xFFA08878)
private val AppBorder = Color(0xFFEAE0D8)
private val StatusPosted = Color(0xFF2563EB)
private val StatusAccepted = Color(0xFFD97706)
private val StatusCompleted = Color(0xFF16A34A)

/* ---------- DATA ---------- */
data class TaskItem(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val budget: Int,
    val location: String,
    val deadline: String,
    val posterName: String,
    val posterAvatar: String,
    val status: String = "Posted",   // Posted | Accepted | In Progress | Completed
    val distance: String = "0.5 km"
)

val sampleTasks = listOf(
    TaskItem(1, "Help me move a sofa", "Need 2 people to move a 3-seat sofa from ground floor to 2nd floor. Shouldn't take more than 30 min.", "Home", 300, "Koramangala, Bengaluru", "Today, 5 PM", "Arjun S.", "", "Posted", "1.2 km"),
    TaskItem(2, "Grocery run from Dmart", "Pick up ~10 items from Dmart HSR and drop at my apartment. Parking available.", "Errands", 150, "HSR Layout, Bengaluru", "Today, 2 PM", "Priya M.", "", "Posted", "0.8 km"),
    TaskItem(3, "Tutor for Math – 10th grade", "Need a tutor for 2 hours to help my son with algebra and geometry. Prefer someone with prior experience.", "Academic", 500, "BTM Layout, Bengaluru", "Tomorrow, 10 AM", "Ramesh K.", "", "Accepted", "2.3 km"),
    TaskItem(4, "Fix leaky kitchen tap", "Small repair job. Tools available at home. Should be a 20–30 min task.", "Home", 200, "Indiranagar, Bengaluru", "This week", "Sneha P.", "", "In Progress", "3.1 km"),
    TaskItem(5, "Deliver documents to Whitefield", "Office documents to be dropped at a law firm in Whitefield. Envelope is sealed.", "Delivery", 250, "MG Road, Bengaluru", "Urgent", "Vikram N.", "", "Posted", "4.5 km"),
    TaskItem(6, "Dog walking – 45 min", "Walk my golden retriever Biscuit around the park. He's friendly and leash-trained.", "Pets", 100, "Jayanagar, Bengaluru", "Daily 7 AM", "Ananya T.", "", "Completed", "1.9 km"),
)

/* ---------- SCREEN ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen() {
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
    var showAddTask by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val taskList = remember { sampleTasks.toMutableStateList() }
    val categories = listOf("All", "Home", "Errands", "Academic", "Delivery", "Pets", "Others")

    val filtered = remember(selectedCategory, taskList.size) {
        if (selectedCategory == "All") taskList.toList()
        else taskList.filter { it.category == selectedCategory }
    }

    Scaffold(
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTask = true },
                containerColor = AppTeal,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Post Task") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                Text("Nearby Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Find tasks in your area", fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                // Category chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTeal.copy(alpha = 0.12f),
                                selectedLabelColor = AppTeal
                            )
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No tasks in this category", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { task ->
                        TaskCard(task = task, onClick = { selectedTask = task })
                    }
                    item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                }
            }
        }

        // Task detail dialog
        selectedTask?.let {
            TaskDetailDialog(task = it, onDismiss = { selectedTask = null })
        }

        // Add task dialog
        if (showAddTask) {
            AddTaskDialog(
                onDismiss = { showAddTask = false },
                onPost = { newTask ->
                    taskList.add(0, newTask)
                    showAddTask = false
                }
            )
        }
    }
}

/* ---------- TASK CARD ---------- */
@Composable
fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    val statusColor = when (task.status) {
        "Posted" -> StatusPosted
        "Accepted" -> StatusAccepted
        "In Progress" -> StatusAccepted
        "Completed" -> StatusCompleted
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Top row: category tag + status
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Surface(
                    color = AppTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        task.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = AppTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        task.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(task.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(task.description, fontSize = 13.sp, color = TextSecondary, maxLines = 2, lineHeight = 18.sp)

            Spacer(Modifier.height(12.dp))

            // Meta info row
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
                MetaChip(Icons.Default.Place, task.distance)
                MetaChip(Icons.Default.AccessTime, task.deadline)
                Spacer(Modifier.weight(1f))
                Text(
                    "₹${task.budget}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppTeal
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppBorder)

            // Poster info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        task.posterName.take(1),
                        color = AppTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(task.posterName, fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(task.location, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary)
    }
}

/* ---------- TASK DETAIL DIALOG ---------- */
@Composable
fun TaskDetailDialog(task: TaskItem, onDismiss: () -> Unit) {
    val statusColor = when (task.status) {
        "Posted" -> StatusPosted
        "Accepted" -> StatusAccepted
        "In Progress" -> StatusAccepted
        "Completed" -> StatusCompleted
        else -> TextSecondary
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(Modifier.padding(24.dp)) {
                // Header
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Task Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Surface(color = AppTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(task.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = AppTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(task.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(task.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(task.description, fontSize = 15.sp, color = TextSecondary, lineHeight = 22.sp)

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = AppBorder)
                Spacer(Modifier.height(16.dp))

                // Details grid
                DetailRowTask(Icons.Default.Place, "Location", task.location)
                DetailRowTask(Icons.Default.AccessTime, "Deadline", task.deadline)
                DetailRowTask(Icons.Default.NearMe, "Distance", task.distance)
                DetailRowTask(Icons.Default.Person, "Posted by", task.posterName)

                Spacer(Modifier.height(20.dp))

                // Budget highlight
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTeal.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text("Reward / Budget", fontSize = 14.sp, color = TextSecondary)
                    Text("₹${task.budget}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AppTeal)
                }

                Spacer(Modifier.height(20.dp))

                // CTA buttons
                if (task.status == "Posted") {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Accept Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AppTeal)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppTeal)
                        Spacer(Modifier.width(8.dp))
                        Text("Message Poster", color = AppTeal, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowTask(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(18.dp), tint = AppTeal)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

/* ---------- ADD TASK DIALOG ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onPost: (TaskItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Home") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Home", "Errands", "Academic", "Delivery", "Pets", "Others")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Post a New Task", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppTeal)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category dropdown
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { category = opt; categoryExpanded = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { if (it.all { c -> c.isDigit() }) budget = it },
                        label = { Text("Budget (₹)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Text("₹", color = AppTeal, fontWeight = FontWeight.Bold) }
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("e.g. Today 5 PM") }
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Place, null, tint = AppTeal) }
                )

                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank() && location.isNotBlank()) {
                            onPost(
                                TaskItem(
                                    id = (1000..9999).random(),
                                    title = title,
                                    description = description,
                                    category = category,
                                    budget = budget.toIntOrNull() ?: 0,
                                    location = location,
                                    deadline = deadline.ifBlank { "Flexible" },
                                    posterName = "You",
                                    posterAvatar = "",
                                    status = "Posted",
                                    distance = "0.0 km"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text("Post Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}