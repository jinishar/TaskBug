package com.example.taskbug.ui.events

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

/* ---------- DESIGN SYSTEM ---------- */
private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val AppBorder = Color(0xFFE5E7EB)

data class EventItem(
    val title: String,
    val description: String,
    val dateTime: String,
    val venue: String,
    val category: String,
    val currentParticipants: Int = 0,
    val maxParticipants: Int = 50
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Technology", "Volunteer", "Networking")
    
    val eventsList = remember {
        mutableStateListOf(
            EventItem("Tech Workshop 2024", "Join us for a deep dive into modern Android development.", "Oct 25, 10:00 AM", "Innovation Hub, NY", "Technology", 42, 50),
            EventItem("Community Clean-up", "Helping to keep our local parks clean and green.", "Oct 27, 08:30 AM", "Central Park, NY", "Volunteer", 15, 30),
            EventItem("Startup Pitch Night", "Networking and pitches from local startups.", "Nov 02, 06:00 PM", "Co-working Space, NY", "Networking", 85, 100)
        )
    }

    val filteredEvents = remember(searchQuery, selectedCategory, eventsList.size) {
        eventsList.filter { event ->
            val matchesSearch = event.title.contains(searchQuery, ignoreCase = true) || 
                              event.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || event.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = AppTeal,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Upcoming Events", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search events...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder)
                )
                
                Spacer(Modifier.height(12.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No events found", color = TextSecondary)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredEvents) { event ->
                        EventCard(event, onClick = { selectedEvent = event })
                    }
                }
            }
        }

        if (showAddEventDialog) {
            AddEventDialog(
                onDismiss = { showAddEventDialog = false },
                onPost = { newEvent ->
                    eventsList.add(0, newEvent)
                    showAddEventDialog = false
                }
            )
        }

        selectedEvent?.let { EventDetailsPopup(it, onDismiss = { selectedEvent = null }) }
    }
}

@Composable
fun EventCard(event: EventItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                CategoryTag(event.category)
            }
            Spacer(Modifier.height(4.dp))
            Text(event.description, fontSize = 14.sp, color = TextSecondary, maxLines = 1)
            Spacer(Modifier.height(12.dp))
            EventDetailItem(Icons.Default.DateRange, event.dateTime)
            EventDetailItem(Icons.Default.Place, event.venue)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(onDismiss: () -> Unit, onPost: (EventItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technology") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Add New Event", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth())

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onPost(EventItem(title, description, "$date, $time", venue, category))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text("Submit Event", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryTag(text: String) {
    Surface(color = AppTeal.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = AppTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EventDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(16.dp), tint = AppTeal)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
fun EventDetailsPopup(event: EventItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Event Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                CategoryTag(event.category)
                
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = event.description, fontSize = 15.sp, color = TextSecondary, lineHeight = 22.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = AppBorder)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(Icons.Default.DateRange, "Date & Time", event.dateTime)
                DetailRow(Icons.Default.Place, "Venue", event.venue)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val progress = event.currentParticipants.toFloat() / event.maxParticipants.toFloat()
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Capacity", fontSize = 12.sp, color = TextSecondary)
                        Text("${event.currentParticipants}/${event.maxParticipants} joined", fontSize = 12.sp, color = AppTeal, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = if (progress > 0.9f) Color.Red else AppTeal,
                        trackColor = AppBorder,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text("Join Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(20.dp), tint = AppTeal)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
        }
    }
}
