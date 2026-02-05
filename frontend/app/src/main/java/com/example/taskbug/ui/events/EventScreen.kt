package com.example.taskbug.ui.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

data class Event(
    val title: String,
    val description: String,
    val dateTime: String,
    val venue: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    
    val events = remember {
        listOf(
            Event("Tech Workshop 2024", "Join us for a deep dive into modern Android development with expert speakers from the industry.", "Oct 25, 10:00 AM", "Innovation Hub, NY", "Technology"),
            Event("Community Clean-up", "Helping to keep our local parks clean and green. All equipment provided.", "Oct 27, 08:30 AM", "Central Park, NY", "Volunteer"),
            Event("Startup Pitch Night", "Networking and pitches from the hottest local startups. Great opportunity to meet founders.", "Nov 02, 06:00 PM", "Co-working Space, NY", "Networking")
        )
    }

    Scaffold(
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = AppTeal,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Upcoming Events",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(events) { event ->
                EventCard(event, onClick = { selectedEvent = event })
            }
        }

        if (showAddEventDialog) {
            AddEventDialog(onDismiss = { showAddEventDialog = false })
        }

        selectedEvent?.let {
            EventDetailsPopup(event = it, onDismiss = { selectedEvent = null })
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
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
                    .height(160.dp)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppBorder.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp), tint = TextSecondary)
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    CategoryTag(event.category)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = event.description, fontSize = 14.sp, color = TextSecondary, maxLines = 1)
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppBorder)
                Spacer(modifier = Modifier.height(12.dp))
                EventDetailItem(Icons.Default.CalendarToday, event.dateTime)
                EventDetailItem(Icons.Default.Place, event.venue)
            }
        }
    }
}

@Composable
fun EventDetailsPopup(event: Event, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 8.dp)) {
                    Text(text = "Event Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart), color = TextPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppBorder.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(60.dp), tint = TextSecondary)
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryTag(event.category)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.description, fontSize = 15.sp, color = TextSecondary, lineHeight = 22.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = AppBorder)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow(Icons.Default.CalendarToday, "Date & Time", event.dateTime)
                    DetailRow(Icons.Default.Place, "Venue", event.venue)

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AppTeal)) {
                            Text("Save", color = AppTeal)
                        }
                        Button(onClick = {}, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AppTeal)) {
                            Text("Join Event")
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
fun EventDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppTeal)
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
fun AddEventDialog(onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Add New Event", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                
                // Photo Upload Placeholder
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
                        Text("Upload Event Banner", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Event Title") }, 
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
                    value = venue, 
                    onValueChange = { venue = it }, 
                    label = { Text("Venue") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().height(50.dp), 
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal), 
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
