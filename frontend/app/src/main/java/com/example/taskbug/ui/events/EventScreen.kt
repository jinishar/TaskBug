package com.example.taskbug.ui.events

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.taskbug.model.Event
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.compose.runtime.saveable.rememberSaveable
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

/* ------------------------------------------------------------------ */
/*  DESIGN TOKENS                                                       */
/* ------------------------------------------------------------------ */
private val AppTeal        = Color(0xFF0F766E)
private val AppBackground  = Color(0xFFF9FAFB)
private val AppSurface     = Color.White
private val TextPrimary    = Color(0xFF1F2937)
private val TextSecondary  = Color(0xFF6B7280)
private val AppBorder      = Color(0xFFE5E7EB)

/* ================================================================== */
/*  EVENTS SCREEN (Feed)                                               */
/* ================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    eventViewModel: EventViewModel = viewModel()
) {
    var showAddEventDialog  by rememberSaveable { mutableStateOf(false) }
    var selectedEvent       by remember { mutableStateOf<Event?>(null) }
    var editingEvent        by remember { mutableStateOf<Event?>(null) }

    val uiState by eventViewModel.uiState.collectAsState()
    val events  = uiState.events
    val currentUserId = eventViewModel.getCurrentUserId()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddEventDialog = true },
                containerColor = AppTeal,
                contentColor   = Color.White,
                shape          = CircleShape,
                elevation      = FloatingActionButtonDefaults.elevation(8.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Add Event") }
        }
    ) { padding ->

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text       = "Upcoming Events",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    modifier   = Modifier.padding(bottom = 4.dp)
                )
            }

            if (uiState.isLoading && events.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = AppTeal) }
                }
            } else if (events.isEmpty()) {
                item { EmptyEventsState() }
            } else {
                items(events) { event ->
                HorizontalEventCard(
                        event         = event,
                        currentUserId = currentUserId,
                        eventViewModel = eventViewModel,
                        onCardClick   = { selectedEvent = event },
                        onEditClick   = { editingEvent = event }
                    )
                }
            }
            // Bottom padding for FAB
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showAddEventDialog) {
        AddEventDialog(
            navController = navController,
            isLoading  = uiState.isLoading || uiState.isUploading,
            onDismiss  = { showAddEventDialog = false },
            onSubmit   = { title, desc, date, time, venue, category, price, maxA, uri ->
                eventViewModel.createEvent(title, desc, date, time, venue, category, price, maxA, uri)
                showAddEventDialog = false
            }
        )
    }

    selectedEvent?.let { ev ->
        EventDetailsPopup(
            event     = ev,
            currentUserId = currentUserId,
            onDismiss = { selectedEvent = null }
        )
    }

    editingEvent?.let { ev ->
        EditEventDialog(
            event      = ev,
            canEdit    = eventViewModel.canEditEvent(ev.eventDate),
            isLoading  = uiState.isLoading || uiState.isUploading,
            onDismiss  = { editingEvent = null },
            onSave     = { title, desc, date, time, venue, category, price, maxA, newUri ->
                eventViewModel.updateEvent(
                    ev.id, ev.userId,
                    title, desc, date, time, venue, category, price, maxA,
                    newUri
                )
                editingEvent = null
            }
        )
    }
}

/* ================================================================== */
/*  HORIZONTAL EVENT CARD                                              */
/* ================================================================== */

@Composable
fun HorizontalEventCard(
    event: Event,
    currentUserId: String?,
    eventViewModel: EventViewModel,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onCardClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AppSurface),
        border    = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier            = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            // ── Left: Banner image ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .heightIn(min = 120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            ) {
                if (event.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model             = event.imageUrl,
                        contentDescription = event.title,
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(AppBorder.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, null, Modifier.size(36.dp), tint = TextSecondary)
                    }
                }
                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x88000000)),
                                startY = 60f
                            )
                        )
                )
                // Category chip over image bottom
                if (event.category.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                        color    = AppTeal,
                        shape    = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text     = event.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color    = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // ── Right: Info ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Date + time row at top
                if (event.eventDate.isNotEmpty() || event.eventTime.isNotEmpty()) {
                    Text(
                        text       = buildString {
                            if (event.eventDate.isNotEmpty()) append(formatDisplayDate(event.eventDate))
                            if (event.eventDate.isNotEmpty() && event.eventTime.isNotEmpty()) append("  •  ")
                            if (event.eventTime.isNotEmpty()) append(event.eventTime)
                        },
                        fontSize   = 11.sp,
                        color      = AppTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Title
                Text(
                    text       = event.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                // Posted by
                if (event.userName.isNotEmpty()) {
                    Text(
                        text     = "by ${event.userName}",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                }

                // Venue
                if (event.venue.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, Modifier.size(12.dp), tint = TextSecondary)
                        Spacer(Modifier.width(3.dp))
                        Text(event.venue, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Price + edit button row
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = if (event.ticketPrice <= 0) "Free" else "₹${event.ticketPrice.toInt()} onwards",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (event.ticketPrice <= 0) Color(0xFF059669) else TextPrimary
                    )
                    // Show edit button only to the owner
                    if (currentUserId == event.userId) {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector        = if (eventViewModel.canEditEvent(event.eventDate)) Icons.Default.Edit else Icons.Default.Lock,
                                contentDescription = "Edit event",
                                tint               = if (eventViewModel.canEditEvent(event.eventDate)) AppTeal else TextSecondary,
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper: yyyy-MM-dd → "Feb 24, 2026"
private fun formatDisplayDate(dateStr: String): String {
    if (dateStr.isEmpty()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val d = parser.parse(dateStr)
        if (d != null) formatter.format(d) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

/* ================================================================== */
/*  EMPTY STATE                                                         */
/* ================================================================== */

@Composable
private fun EmptyEventsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EventBusy, null, Modifier.size(52.dp), tint = Color(0xFFD1D5DB))
            Spacer(Modifier.height(12.dp))
            Text("No events yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Text("Be the first to post an event!", fontSize = 13.sp, color = Color(0xFF9CA3AF))
        }
    }
}

/* ================================================================== */
/*  EVENT DETAILS POPUP                                                 */
/* ================================================================== */

@Composable
fun EventDetailsPopup(event: Event, currentUserId: String?, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Header bar
                Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 8.dp)) {
                    Text("Event Details", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart), color = TextPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }

                // Banner
                if (event.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = event.imageUrl,
                        contentDescription = event.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp)).background(AppBorder.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Image, null, Modifier.size(60.dp), tint = TextSecondary) }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    if (event.category.isNotEmpty()) {
                        Surface(color = AppTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(event.category, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTeal)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    Text(event.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    if (event.userName.isNotEmpty()) {
                        Text("Organised by ${event.userName}", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                    Spacer(Modifier.height(14.dp))

                    Text("About", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(event.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)

                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = AppBorder)
                    Spacer(Modifier.height(14.dp))

                    if (event.eventDate.isNotEmpty())
                        DetailRow(Icons.Default.CalendarToday, "Date", formatDisplayDate(event.eventDate))
                    if (event.eventTime.isNotEmpty())
                        DetailRow(Icons.Default.AccessTime, "Time", event.eventTime)
                    if (event.venue.isNotEmpty())
                        DetailRow(Icons.Default.Place, "Venue", event.venue)
                    DetailRow(
                        Icons.Default.ConfirmationNumber, "Ticket",
                        if (event.ticketPrice <= 0) "Free" else "₹${event.ticketPrice.toInt()}"
                    )
                    if (event.maxAttendees > 0)
                        DetailRow(Icons.Default.People, "Capacity", "${event.maxAttendees} attendees")

                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {}, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AppTeal)) {
                            Text("Save", color = AppTeal)
                        }
                        if (currentUserId != null && currentUserId != event.userId) {
                            Button(onClick = {}, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AppTeal)) {
                                Text("Join Event")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ================================================================== */
/*  ADD EVENT DIALOG                                                    */
/* ================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    navController: NavController,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, date: String, time: String, venue: String, category: String, ticketPrice: Double, maxAttendees: Int, imageUri: Uri) -> Unit
) {
    var title        by rememberSaveable { mutableStateOf("") }
    var description  by rememberSaveable { mutableStateOf("") }
    var venue        by rememberSaveable { mutableStateOf("") }
    var date         by rememberSaveable { mutableStateOf("") }   // yyyy-MM-dd
    var time         by rememberSaveable { mutableStateOf("") }   // display string
    var category     by rememberSaveable { mutableStateOf("") }
    var priceStr     by rememberSaveable { mutableStateOf("") }
    var maxStr       by rememberSaveable { mutableStateOf("") }
    var imageUri     by remember { mutableStateOf<Uri?>(null) }
    var titleError   by rememberSaveable { mutableStateOf(false) }
    var imageError   by rememberSaveable { mutableStateOf(false) }
    var dateError    by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                time = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // 12 hour format
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri = it; imageError = false }
    }

    // Observe result from MapPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedLocation = savedStateHandle?.getLiveData<String>("selected_location")?.observeAsState()
    
    LaunchedEffect(selectedLocation?.value) {
        val loc = selectedLocation?.value
        if (!loc.isNullOrBlank()) {
            venue = loc
            savedStateHandle?.remove<String>("selected_location")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Post New Event", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                // ── Image picker ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (imageError) Color(0xFFFEE2E2) else AppBorder.copy(alpha = 0.3f))
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model              = imageUri,
                            contentDescription = "Banner preview",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                        // Edit overlay
                        Box(Modifier.fillMaxSize().background(Color(0x44000000)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, null, Modifier.size(28.dp), tint = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, Modifier.size(34.dp),
                                tint = if (imageError) Color(0xFFDC2626) else AppTeal)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Upload Event Banner*",
                                fontSize = 13.sp,
                                color    = if (imageError) Color(0xFFDC2626) else TextSecondary
                            )
                        }
                    }
                }
                if (imageError) Text("Banner image is required", fontSize = 12.sp, color = Color(0xFFDC2626))

                // ── Title ─────────────────────────────────────────────────────
                EventTextField(value = title, onValueChange = { title = it; titleError = false },
                    label = "Event Title*", isError = titleError,
                    errorMsg = if (titleError) "Title is required" else null)

                // ── Description ───────────────────────────────────────────────
                EventTextField(value = description, onValueChange = { description = it },
                    label = "Description", minLines = 3)

                // ── Category ──────────────────────────────────────────────────
                EventTextField(value = category, onValueChange = { category = it },
                    label = "Category (e.g. Tech, Volunteer, Networking)")

                // ── Date & Time ───────────────────────────────────────────────
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EventDatePickerButton(
                        date      = date,
                        onDateSelected = { selected -> date = selected; dateError = false },
                        isError   = dateError,
                        modifier  = Modifier.weight(1f)
                    )
                    EventTextField(
                        value         = time,
                        onValueChange = { time = it },
                        label         = "Time (e.g. 06:00 PM)",
                        modifier      = Modifier.weight(1f),
                        leadingIcon   = Icons.Default.AccessTime,
                        readOnly      = true,
                        onClick       = { timePickerDialog.show() }
                    )
                }
                if (dateError) Text("Event date is required", fontSize = 12.sp, color = Color(0xFFDC2626))

                // ── Venue ─────────────────────────────────────────────────────
                EventTextField(
                    value = venue, 
                    onValueChange = { venue = it },
                    label = "Venue / Location", 
                    leadingIcon = Icons.Default.Place,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = "Pick Location",
                            modifier = Modifier.size(24.dp).clickable { 
                                navController.navigate("map_picker") 
                            },
                            tint = AppTeal
                        )
                    }
                )

                // ── Price & Capacity ──────────────────────────────────────────
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EventTextField(
                        value         = priceStr,
                        onValueChange = { priceStr = it },
                        label         = "Ticket Price (₹)",
                        modifier      = Modifier.weight(1f),
                        keyboardType  = KeyboardType.Number,
                        leadingIcon   = Icons.Default.ConfirmationNumber,
                        placeholder   = "0 = Free"
                    )
                    EventTextField(
                        value         = maxStr,
                        onValueChange = { maxStr = it },
                        label         = "Max Attendees",
                        modifier      = Modifier.weight(1f),
                        keyboardType  = KeyboardType.Number,
                        leadingIcon   = Icons.Default.People,
                        placeholder   = "0 = Unlimited"
                    )
                }

                // ── Submit ────────────────────────────────────────────────────
                Button(
                    onClick = {
                        titleError = title.isBlank()
                        imageError = imageUri == null
                        dateError  = date.isBlank()
                        if (!titleError && !imageError && !dateError) {
                            onSubmit(
                                title, description, date, time, venue, category,
                                priceStr.toDoubleOrNull() ?: 0.0,
                                maxStr.toIntOrNull() ?: 0,
                                imageUri!!
                            )
                        }
                    },
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppTeal),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading & saving…")
                    } else {
                        Text("Post Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/* ================================================================== */
/*  EDIT EVENT DIALOG                                                   */
/* ================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    event: Event,
    canEdit: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, date: String, time: String, venue: String, category: String, ticketPrice: Double, maxAttendees: Int, newImageUri: Uri?) -> Unit
) {
    var title       by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var venue       by remember { mutableStateOf(event.venue) }
    var date        by remember { mutableStateOf(event.eventDate) }
    var time        by remember { mutableStateOf(event.eventTime) }
    var category    by remember { mutableStateOf(event.category) }
    var priceStr    by remember { mutableStateOf(if (event.ticketPrice > 0) event.ticketPrice.toInt().toString() else "") }
    var maxStr      by remember { mutableStateOf(if (event.maxAttendees > 0) event.maxAttendees.toString() else "") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newImageUri = it }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Event", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = TextSecondary) }
                }

                if (!canEdit) {
                    // Locked state
                    Surface(
                        color  = Color(0xFFFEF3C7),
                        shape  = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, Modifier.size(20.dp), tint = Color(0xFFB45309))
                            Text(
                                "Editing is locked — the event is within 10 days.",
                                fontSize = 13.sp,
                                color    = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = onDismiss, Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AppTeal)) {
                        Text("Got It")
                    }
                    return@Column
                }

                // ── Change banner ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppBorder.copy(alpha = 0.3f))
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (newImageUri != null) {
                        AsyncImage(
                            model              = newImageUri,
                            contentDescription = "New banner",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else if (event.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model              = event.imageUrl,
                            contentDescription = event.title,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, Modifier.size(34.dp), tint = AppTeal)
                    }
                    Box(Modifier.fillMaxSize().background(Color(0x44000000)), contentAlignment = Alignment.BottomEnd) {
                        Surface(Modifier.padding(8.dp), color = AppTeal, shape = RoundedCornerShape(8.dp)) {
                            Text("Change", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                EventTextField(value = title, onValueChange = { title = it }, label = "Event Title*")
                EventTextField(value = description, onValueChange = { description = it }, label = "Description", minLines = 3)
                EventTextField(value = category, onValueChange = { category = it }, label = "Category")

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EventDatePickerButton(
                        date           = date,
                        onDateSelected = { selected -> date = selected },
                        modifier       = Modifier.weight(1f)
                    )
                    EventTextField(value = time, onValueChange = { time = it }, label = "Time (e.g. 06:00 PM)", modifier = Modifier.weight(1f), leadingIcon = Icons.Default.AccessTime)
                }

                EventTextField(value = venue, onValueChange = { venue = it }, label = "Venue", leadingIcon = Icons.Default.Place)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EventTextField(value = priceStr, onValueChange = { priceStr = it }, label = "Ticket Price (₹)", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number, leadingIcon = Icons.Default.ConfirmationNumber)
                    EventTextField(value = maxStr, onValueChange = { maxStr = it }, label = "Max Attendees", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number, leadingIcon = Icons.Default.People)
                }

                Button(
                    onClick  = {
                        if (title.isNotBlank() && date.isNotBlank()) {
                            onSave(title, description, date, time, venue, category,
                                priceStr.toDoubleOrNull() ?: 0.0,
                                maxStr.toIntOrNull() ?: 0,
                                newImageUri)
                        }
                    },
                    enabled  = !isLoading && title.isNotBlank() && date.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppTeal),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/* ================================================================== */
/*  DATE PICKER BUTTON                                                  */
/* ================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDatePickerButton(
    date: String,           // yyyy-MM-dd or empty
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }

    // Convert stored yyyy-MM-dd to epoch millis for pre-selection
    val initialMillis = remember(date) {
        if (date.isNotEmpty()) {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val d = parser.parse(date)
                d?.time
            } catch (e: Exception) { null }
        } else null
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    // Tappable field that looks like an OutlinedTextField
    OutlinedCard(
        onClick    = { showPicker = true },
        modifier   = modifier.height(56.dp),
        shape      = RoundedCornerShape(12.dp),
        border     = BorderStroke(
            width = if (isError) 2.dp else 1.dp,
            color = if (isError) Color(0xFFDC2626) else AppBorder
        ),
        colors     = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint     = if (isError) Color(0xFFDC2626) else AppTeal
            )
            Column {
                Text(
                    text     = "Event Date*",
                    fontSize = 10.sp,
                    color    = if (isError) Color(0xFFDC2626) else AppTeal,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text     = if (date.isEmpty()) "Pick a date" else formatDisplayDate(date),
                    fontSize = 13.sp,
                    color    = if (date.isEmpty()) Color(0xFF9CA3AF) else TextPrimary,
                    fontWeight = if (date.isEmpty()) FontWeight.Normal else FontWeight.SemiBold
                )
            }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val selected = formatter.format(java.util.Date(millis))
                        onDateSelected(selected)
                    }
                    showPicker = false
                }) { Text("OK", color = AppTeal, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = AppSurface
            )
        ) {
            DatePicker(
                state  = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor    = AppTeal,
                    todayDateBorderColor         = AppTeal,
                    selectedYearContainerColor   = AppTeal
                )
            )
        }
    }
}

/* ================================================================== */
/*  REUSABLE TEXT FIELD                                                 */
/* ================================================================== */

@Composable
private fun EventTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    isError: Boolean = false,
    errorMsg: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String = "",
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        val interactionModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
        Box(modifier = Modifier.fillMaxWidth().then(interactionModifier)) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                label         = { Text(label) },
                modifier      = Modifier.fillMaxWidth(),
                minLines      = minLines,
                isError       = isError,
                readOnly      = readOnly || onClick != null,
                enabled       = onClick == null, 
                shape         = RoundedCornerShape(12.dp),
                placeholder   = if (placeholder.isNotEmpty()) ({ Text(placeholder, color = Color(0xFF9CA3AF)) }) else null,
                leadingIcon   = leadingIcon?.let { icon -> { Icon(icon, null, Modifier.size(20.dp)) } },
                trailingIcon  = trailingIcon,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AppTeal,
                    unfocusedBorderColor = AppBorder,
                    focusedLabelColor    = AppTeal,
                    cursorColor          = AppTeal,
                    disabledTextColor    = TextPrimary,
                    disabledBorderColor  = AppBorder,
                    disabledLabelColor   = TextSecondary,
                    disabledLeadingIconColor = AppTeal,
                    disabledTrailingIconColor = AppTeal
                )
            )
        }
        if (errorMsg != null) {
            Text(errorMsg, fontSize = 11.sp, color = Color(0xFFDC2626), modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

/* ================================================================== */
/*  DETAIL ROW (Detail popup)                                           */
/* ================================================================== */

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(20.dp), tint = AppTeal)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
fun EventDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, null, Modifier.size(16.dp), tint = AppTeal)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
fun CategoryTag(category: String) {
    Surface(color = AppTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(category, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTeal)
    }
}
