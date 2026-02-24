package com.example.taskbug.ui.profile

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.model.Event
import com.example.taskbug.model.Task
import com.example.taskbug.ui.auth.AuthViewModel
import com.example.taskbug.ui.events.EventViewModel
import com.example.taskbug.ui.tasks.TaskViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

/* ---------- DESIGN SYSTEM ---------- */

private val TaskBugTeal = Color(0xFF0F766E)
private val LightGrayBackground = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    var selectedAvatar by remember { mutableStateOf(bugAvatars.first()) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showMyTasksHistory by remember { mutableStateOf(false) }
    var showMyEventsHistory by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<com.example.taskbug.model.Event?>(null) }
    val context = LocalContext.current

    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.authError.collectAsState()
    var userLocation by remember(userProfile) { mutableStateOf(userProfile?.location ?: "") }

    // Observe user's own tasks from TaskViewModel
    val taskUiState by taskViewModel.uiState.collectAsState()
    val userTasks = taskUiState.userTasks
    val isTasksLoading = taskUiState.isLoading

    // Observe user's own events from EventViewModel
    val eventUiState by eventViewModel.uiState.collectAsState()
    val userEvents = eventUiState.userEvents
    val isEventsLoading = eventUiState.isLoading

    // Initialize Places SDK
    if (!Places.isInitialized()) {
        Places.initialize(context.applicationContext, "YOUR_API_KEY")
    }

    // Launcher for the Google Places Autocomplete activity
    val placesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            userLocation = place.address ?: ""
        }
    }

    fun launchPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(context)
        placesLauncher.launch(intent)
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { avatar ->
                selectedAvatar = avatar
                showAvatarDialog = false
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false },
            onEditProfile = {
                showSettings = false
                showEditProfile = true
            }
        )
    }

    if (showEditProfile) {
        EditProfileDialog(
            initialName = userProfile?.name ?: "",
            initialPhone = userProfile?.phone ?: "",
            onDismiss = { showEditProfile = false },
            onSave = { newName, newPhone ->
                authViewModel.updateUserProfile(newName, newPhone)
                showEditProfile = false
            },
            authViewModel = authViewModel
        )
    }

    editingEvent?.let { ev ->
        com.example.taskbug.ui.events.EditEventDialog(
            event     = ev,
            canEdit   = eventViewModel.canEditEvent(ev.eventDate),
            isLoading = eventUiState.isLoading || eventUiState.isUploading,
            onDismiss = { editingEvent = null },
            onSave    = { title, desc, date, time, venue, category, price, maxA, newUri ->
                eventViewModel.updateEvent(ev.id, ev.userId, title, desc, date, time, venue, category, price, maxA, newUri)
                editingEvent = null
            }
        )
    }

    Scaffold(
        containerColor = LightGrayBackground
    ) { padding ->
        when {
            isLoading && userProfile == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TaskBugTeal)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading profile...", color = Color.Gray)
                    }
                }
            }
            authError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(authError!!, color = Color.Red, textAlign = TextAlign.Center)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- TOP HEADER SECTION ---
                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                            .clickable { showAvatarDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(selectedAvatar.backgroundColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedAvatar.image,
                                contentDescription = selectedAvatar.contentDescription,
                                tint = selectedAvatar.backgroundColor,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Info
                    Text(
                        text = userProfile?.name ?: "Loading...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = userProfile?.email ?: "",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Community helper | Nature lover | Tech enthusiast. Always ready to lend a hand!",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 8.dp)
                    )

                    // Rating Section
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        }
                        Icon(Icons.AutoMirrored.Filled.StarHalf, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "4.9 (124 reviews)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- INFO CARDS ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Tasks Posted",
                            value = userTasks.size.toString(),
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Events Posted",
                            value = userEvents.size.toString(),
                            icon = Icons.Default.EventAvailable,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- PROFILE DETAILS (READ-ONLY) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Profile Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ReadonlyField(label = "Name", value = userProfile?.name ?: "")
                        ReadonlyField(label = "Email", value = userProfile?.email ?: "")
                        ReadonlyField(label = "Phone", value = userProfile?.phone ?: "")
                        LocationField(
                            location = userLocation,
                            onLocationChange = { userLocation = it },
                            onSearchLocation = { launchPlacePicker() },
                            onSaveLocation = { authViewModel.updateUserLocation(userLocation) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- MENU LIST ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    ) {
                        MenuListItem(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = "My Tasks",
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (userTasks.isNotEmpty()) {
                                        Surface(
                                            color = TaskBugTeal,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = userTasks.size.toString(),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Icon(
                                        imageVector = if (showMyTasksHistory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            onClick = { showMyTasksHistory = !showMyTasksHistory }
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        MenuListItem(
                            icon = Icons.Default.CalendarMonth,
                            title = "My Events",
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (userEvents.isNotEmpty()) {
                                        Surface(
                                            color = TaskBugTeal,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = userEvents.size.toString(),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Icon(
                                        imageVector = if (showMyEventsHistory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            onClick = { showMyEventsHistory = !showMyEventsHistory }
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        MenuListItem(icon = Icons.Default.AccountBalanceWallet, title = "Wallet")
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        MenuListItem(icon = Icons.Default.Settings, title = "Settings", onClick = { showSettings = true })
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        MenuListItem(icon = Icons.AutoMirrored.Filled.HelpCenter, title = "Help & Support")
                    }

                    // --- MY POSTED TASKS HISTORY ---
                    if (showMyTasksHistory) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Posted Tasks",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                if (userTasks.isNotEmpty()) {
                                    Text(
                                        text = "${userTasks.size} task${if (userTasks.size != 1) "s" else ""}",
                                        fontSize = 13.sp,
                                        color = TaskBugTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            when {
                                isTasksLoading && userTasks.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = TaskBugTeal, modifier = Modifier.size(28.dp))
                                    }
                                }
                                userTasks.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Assignment,
                                                contentDescription = null,
                                                tint = Color(0xFFD1D5DB),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "No tasks posted yet",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF6B7280)
                                            )
                                            Text(
                                                text = "Post your first task from the Tasks tab",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9CA3AF),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        userTasks.forEach { task ->
                                            MyTaskHistoryCard(
                                                task = task,
                                                onDelete = { taskViewModel.deleteTask(task.id, task.userId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- MY POSTED EVENTS HISTORY ---
                    if (showMyEventsHistory) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Posted Events",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                if (userEvents.isNotEmpty()) {
                                    Text(
                                        text = "${userEvents.size} event${if (userEvents.size != 1) "s" else ""}",
                                        fontSize = 13.sp,
                                        color = TaskBugTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            when {
                                isEventsLoading && userEvents.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = TaskBugTeal, modifier = Modifier.size(28.dp))
                                    }
                                }
                                userEvents.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = Color(0xFFD1D5DB),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "No events posted yet",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF6B7280)
                                            )
                                            Text(
                                                text = "Post your first event from the Events tab",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9CA3AF),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        userEvents.forEach { event ->
                                            MyEventHistoryCard(
                                                event = event,
                                                canEdit = eventViewModel.canEditEvent(event.eventDate),
                                                onEdit = { editingEvent = event },
                                                onDelete = { eventViewModel.deleteEvent(event.id, event.userId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- LOGOUT BUTTON ---
                    Button(
                        onClick = { authViewModel.signOut() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaskBugTeal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

/* ---------- MY TASK HISTORY CARD ---------- */

@Composable
fun MyTaskHistoryCard(
    task: Task,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val statusColor = when (task.status.lowercase()) {
        "completed" -> Color(0xFF059669)
        "cancelled" -> Color(0xFFDC2626)
        else -> TaskBugTeal // "active"
    }
    val statusLabel = task.status.replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    // Pay
                    if (task.pay > 0) {
                        Text(
                            text = "₹${task.pay.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TaskBugTeal
                        )
                    }
                }
                if (task.deadline.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = task.deadline,
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${task.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
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

/* ---------- MY EVENT HISTORY CARD ---------- */

@Composable
fun MyEventHistoryCard(
    event: Event,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Image thumbnail ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            ) {
                if (event.imageUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model              = event.imageUrl,
                        contentDescription = event.title,
                        contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(28.dp), tint = TaskBugTeal)
                    }
                }
            }

            // ── Content ────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text       = event.title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF111827),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                // Category
                if (event.category.isNotEmpty()) {
                    Surface(color = TaskBugTeal.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text     = event.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color    = TaskBugTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                // Date
                if (event.eventDate.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(11.dp), tint = Color(0xFF9CA3AF))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            buildString {
                                append(event.eventDate)
                                if (event.eventTime.isNotEmpty()) append("  ${event.eventTime}")
                            },
                            fontSize = 11.sp,
                            color    = Color(0xFF9CA3AF)
                        )
                    }
                }
                // Venue
                if (event.venue.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, Modifier.size(11.dp), tint = Color(0xFF9CA3AF))
                        Spacer(Modifier.width(3.dp))
                        Text(event.venue, fontSize = 11.sp, color = Color(0xFF9CA3AF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                // Price
                Text(
                    text       = if (event.ticketPrice <= 0) "Free" else "₹${event.ticketPrice.toInt()}",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (event.ticketPrice <= 0) Color(0xFF059669) else TaskBugTeal
                )
            }

            // ── Action buttons ─────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector        = if (canEdit) Icons.Default.Edit else Icons.Default.Lock,
                        contentDescription = if (canEdit) "Edit Event" else "Edit Locked",
                        tint               = if (canEdit) TaskBugTeal else Color(0xFF9CA3AF),
                        modifier           = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Delete Event", Modifier.size(18.dp), tint = Color(0xFFEF4444))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title            = { Text("Delete Event", fontWeight = FontWeight.Bold) },
            text             = { Text("Are you sure you want to delete \"${event.title}\"? This cannot be undone.") },
            confirmButton    = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/* ---------- SUPPORTING COMPOSABLES (unchanged) ---------- */

@Composable
fun LocationField(
    location: String,
    onLocationChange: (String) -> Unit,
    onSearchLocation: () -> Unit,
    onSaveLocation: () -> Unit
) {
    OutlinedTextField(
        value = location,
        onValueChange = onLocationChange,
        label = { Text("Location") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TaskBugTeal,
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = TaskBugTeal,
            focusedLabelColor = TaskBugTeal
        ),
        trailingIcon = {
            Row {
                IconButton(onClick = onSearchLocation) {
                    Icon(Icons.Default.Search, contentDescription = "Search Location", tint = TaskBugTeal)
                }
                IconButton(onClick = onSaveLocation) {
                    Icon(Icons.Default.Save, contentDescription = "Save Location", tint = TaskBugTeal)
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun AvatarSelectionDialog(
    onDismiss: () -> Unit,
    onAvatarSelected: (BugAvatar) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Choose your Avatar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bugAvatars) { avatar ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(avatar.backgroundColor.copy(alpha = 0.2f))
                                .clickable { onAvatarSelected(avatar) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = avatar.image,
                                contentDescription = avatar.contentDescription,
                                tint = avatar.backgroundColor,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TaskBugTeal.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TaskBugTeal, modifier = Modifier.size(22.dp))
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ReadonlyField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = Color(0xFF6B7280), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value.ifEmpty { "—" },
            fontSize = 16.sp,
            color = Color(0xFF111827),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(12.dp)
        )
    }
}

@Composable
fun MenuListItem(
    icon: ImageVector,
    title: String,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827),
            modifier = Modifier.weight(1f)
        )
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit, onEditProfile: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit Profile")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    authViewModel: AuthViewModel
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.authError.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (authError != null) {
                    Text(text = authError!!, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onSave(name.trim(), phone.trim()) },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White) else Text("Save")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
