package com.example.taskbug.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.taskbug.ui.auth.AuthViewModel
import com.example.taskbug.ui.events.EventItem
import com.example.taskbug.ui.tasks.Task
import com.example.taskbug.ui.tasks.AddTaskDialog
import com.example.taskbug.ui.events.AddEventDialog

/* ---------- DESIGN SYSTEM ---------- */
private val TaskBugTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val AppBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val scrollState = rememberScrollState()
    
    var selectedAvatar by remember { mutableStateOf(bugAvatars.first()) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    
    // Mock Data
    val myTasks = remember { mutableStateListOf(Task("Fix Leaking Tap", "Kitchen tap is dripping constantly.", "Today, 5:00 PM", "Home", "Maintenance", "₹500"))}
    val myEvents = remember { mutableStateListOf(EventItem("Code Sprint", "Collaborative coding session.", "Oct 30, 2:00 PM", "Tech Park", "Technology", 10, 20))}
    val completedTasks = listOf(Task("Paint Fence", "Garden fence needs a fresh coat.", "Oct 15", "Home", "Maintenance", "₹1000"))
    val pastEvents = listOf(EventItem("Yoga Morning", "Relaxing yoga session in the park.", "Oct 10", "Central Park", "Health", 25, 30))

    var activeTab by remember { mutableIntStateOf(0) }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { avatar ->
                selectedAvatar = avatar
                showAvatarDialog = false
            }
        )
    }

    Scaffold(containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileHeader(
                avatar = selectedAvatar,
                onAvatarClick = { showAvatarDialog = true }
            )

            StatsRow(
                posted = myTasks.size + myEvents.size,
                completed = completedTasks.size,
                events = myEvents.size + pastEvents.size
            )

            // --- MY ACTIVE POSTS ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("My Active Posts")
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = TaskBugTeal,
                    indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[activeTab]), color = TaskBugTeal) }
                ) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                        Text("My Tasks", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if(activeTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                        Text("My Events", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if(activeTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                if (activeTab == 0) {
                    if (myTasks.isEmpty()) EmptyState("You haven't posted any tasks yet")
                    else myTasks.forEach { task -> EditableTaskCard(task, onDelete = { myTasks.remove(task) }, onUpdate = { updated -> myTasks[myTasks.indexOf(task)] = updated }) }
                } else {
                    if (myEvents.isEmpty()) EmptyState("You haven't posted any events yet")
                    else myEvents.forEach { event -> EditableEventCard(event, onDelete = { myEvents.remove(event) }, onUpdate = { updated -> myEvents[myEvents.indexOf(event)] = updated }) }
                }
            }
            
            SectionHeader("Completed Tasks")
            if (completedTasks.isEmpty()) EmptyState("No completed tasks yet")
            else completedTasks.forEach { CompletedTaskCard(it) }

            SectionHeader("Past Events")
            if (pastEvents.isEmpty()) EmptyState("No past events yet")
            else pastEvents.forEach { PastEventCard(it) }
            
            // --- SETTINGS & NAVIGATION ---
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppSurface)) {
                MenuListItem(icon = Icons.Default.Settings, title = "Settings")
                HorizontalDivider(color = AppBorder)
                MenuListItem(icon = Icons.Default.Info, title = "Help & Support")
                HorizontalDivider(color = AppBorder)
                MenuListItem(icon = Icons.Default.Lock, title = "Privacy Policy")
            }

            // --- LOGOUT ---
            Button(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TaskBugTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileHeader(avatar: BugAvatar, onAvatarClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(avatar.backgroundColor.copy(alpha = 0.1f))
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(avatar.image, null, tint = avatar.backgroundColor, modifier = Modifier.size(60.dp))
            Box(
                Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).background(TaskBugTeal, CircleShape).padding(6.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("John Doe", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("john.doe@taskbug.com", color = TextSecondary)
        Text("+91 98765 43210", color = TextSecondary)
        Text("123, Innovation Way, Tech City", color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = { /* Edit Profile */ }, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Edit Profile")
        }
    }
}

@Composable
fun EditableTaskCard(task: Task, onDelete: () -> Unit, onUpdate: (Task) -> Unit) { 
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
    if (showEditDialog) {
        AddTaskDialog(initialTask = task, onDismiss = { showEditDialog = false }, onPost = { onUpdate(it); showEditDialog = false })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    CategoryTag(task.category)
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("✏️ Edit") }, onClick = { showMenu = false; showEditDialog = true })
                        DropdownMenuItem(text = { Text("🗑 Delete") }, onClick = { showMenu = false; showDeleteDialog = true })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Text(task.deadline, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.width(16.dp))
                Badge(containerColor = TaskBugTeal.copy(0.1f)) {
                    Text("Active", color = TaskBugTeal, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}

@Composable
fun EditableEventCard(event: EventItem, onDelete: () -> Unit, onUpdate: (EventItem) -> Unit) { 
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
         AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete this event?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
    if (showEditDialog) {
        AddEventDialog(initialEvent = event, onDismiss = { showEditDialog = false }, onPost = { onUpdate(it); showEditDialog = false })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    CategoryTag(event.category)
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text(" Edit") }, onClick = { showMenu = false; showEditDialog = true })
                        DropdownMenuItem(text = { Text(" Delete") }, onClick = { showMenu = false; showDeleteDialog = true })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Text(event.dateTime, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.width(16.dp))
                Badge(containerColor = TaskBugTeal.copy(0.1f)) {
                    Text("Ongoing", color = TaskBugTeal, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}

@Composable
fun StatsRow(posted: Int, completed: Int, events: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("Posted", posted.toString())
        StatItem("Completed", completed.toString())
        StatItem("Events", events.toString())
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TaskBugTeal)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.fillMaxWidth())
}

@Composable
fun CompletedTaskCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextSecondary)
                Text("Completed on ${task.deadline}", fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Default.CheckCircle, null, tint = TaskBugTeal)
        }
    }
}

@Composable
fun PastEventCard(event: EventItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(event.title, fontWeight = FontWeight.Bold, color = TextSecondary)
                Badge(containerColor = Color.LightGray.copy(0.3f)) { Text("Past Event", color = TextSecondary) }
            }
            Text(event.category, fontSize = 12.sp, color = TextSecondary)
            Text(event.dateTime, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Text(text = message, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, color = TextSecondary, fontSize = 14.sp)
}

@Composable
fun AvatarSelectionDialog(onDismiss: () -> Unit, onAvatarSelected: (BugAvatar) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(AppSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Choose your Avatar", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), color = TextPrimary)
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(300.dp)) {
                    items(bugAvatars) { avatar ->
                        Box(
                            modifier = Modifier.size(80.dp).padding(8.dp).clip(CircleShape).background(avatar.backgroundColor.copy(0.1f)).clickable { onAvatarSelected(avatar) },
                            contentAlignment = Alignment.Center
                        ) { Icon(avatar.image, null, tint = avatar.backgroundColor, modifier = Modifier.size(40.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuListItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable {}.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TaskBugTeal, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CategoryTag(text: String) {
    Surface(color = TaskBugTeal.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = TaskBugTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
