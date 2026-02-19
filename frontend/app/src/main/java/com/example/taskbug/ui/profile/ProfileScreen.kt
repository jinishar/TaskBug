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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.ui.auth.AuthViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

/* ---------- DESIGN SYSTEM ---------- */

private val TaskBugTeal = Color(0xFF0F766E)
private val LightGrayBackground = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    var selectedAvatar by remember { mutableStateOf(bugAvatars.first()) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    var userLocation by remember(userProfile) { mutableStateOf(userProfile?.location ?: "") }

    // Refresh profile when screen is first loaded
    LaunchedEffect(Unit) {
        authViewModel.refreshUserProfile()
    }

    // Initialize Places SDK - IMPORTANT: Replace "YOUR_API_KEY" with your actual key
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

    // Function to launch the Place Picker
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

    Scaffold(
        containerColor = LightGrayBackground
    ) { padding ->
        if (isLoading && userProfile == null) {
            // Show loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(color = TaskBugTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading profile...", color = Color.Gray)
                }
            }
        } else {
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
                    title = "Tasks Completed",
                    value = "156",
                    icon = Icons.Default.Verified,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Events Joined",
                    value = "28",
                    icon = Icons.Default.EventAvailable,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- EDITABLE DETAILS SECTION ---
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

                EditableField(label = "Name", initialValue = userProfile?.name ?: "")
                EditableField(label = "Email", initialValue = userProfile?.email ?: "")
                EditableField(label = "Phone", initialValue = userProfile?.phone ?: "")
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
                MenuListItem(icon = Icons.AutoMirrored.Filled.Assignment, title = "My Tasks")
                HorizontalDivider(color = Color(0xFFF3F4F6))
                MenuListItem(icon = Icons.Default.CalendarMonth, title = "My Events")
                HorizontalDivider(color = Color(0xFFF3F4F6))
                MenuListItem(icon = Icons.Default.AccountBalanceWallet, title = "Wallet")
                HorizontalDivider(color = Color(0xFFF3F4F6))
                MenuListItem(icon = Icons.Default.Settings, title = "Settings")
                HorizontalDivider(color = Color(0xFFF3F4F6))
                MenuListItem(icon = Icons.AutoMirrored.Filled.HelpCenter, title = "Help & Support")
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
fun EditableField(label: String, initialValue: String) {
    var text by remember(initialValue) { mutableStateOf(initialValue) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label, color = Color(0xFF6B7280)) },
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
        singleLine = true
    )
}

@Composable
fun MenuListItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate */ }
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
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
    }
}
