package com.example.taskbug.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.taskbug.ui.auth.AuthViewModel

private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val AppBorder = Color(0xFFE5E7EB)

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.collectAsState()
    var showAvatarPicker by remember { mutableStateOf(false) }
    var selectedAvatarId by remember { mutableStateOf(1) }
    val selectedAvatar = bugAvatars.find { it.id == selectedAvatarId } ?: bugAvatars.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Teal header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTeal)
                .padding(top = 40.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(selectedAvatar.backgroundColor)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable { showAvatarPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(selectedAvatar.image, selectedAvatar.contentDescription, Modifier.size(48.dp), Color.White)
                }
                Spacer(Modifier.height(4.dp))
                Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { showAvatarPicker = true }) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, null, Modifier.size(12.dp), Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Change Avatar", fontSize = 11.sp, color = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(userProfile?.name?.ifBlank { "TaskBug User" } ?: "TaskBug User", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(userProfile?.email ?: "user@example.com", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                if (!userProfile?.location.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), Color.White.copy(0.8f))
                        Text(userProfile!!.location, fontSize = 13.sp, color = Color.White.copy(0.8f))
                    }
                }
            }
        }

        // Stats card (overlapping)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceEvenly) {
                StatItem("8", "Posted", Icons.Default.Assignment)
                VerticalDivider(modifier = Modifier.height(48.dp), color = AppBorder)
                StatItem("14", "Done", Icons.Default.CheckCircle)
                VerticalDivider(modifier = Modifier.height(48.dp), color = AppBorder)
                StatItem("4.7", "Rating", Icons.Default.Star)
                VerticalDivider(modifier = Modifier.height(48.dp), color = AppBorder)
                StatItem("₹3250", "Earned", Icons.Default.Payments)
            }
        }

        Spacer(Modifier.height(4.dp))

        ProfileSection("Account") {
            ProfileMenuItem(Icons.Default.Person, "Edit Profile", "Update your name & info")
            ProfileMenuItem(Icons.Default.Phone, "Phone Number", userProfile?.phone?.ifBlank { "Not set" } ?: "Not set")
            ProfileMenuItem(Icons.Default.LocationOn, "My Location", userProfile?.location?.ifBlank { "Not set" } ?: "Not set")
        }

        Spacer(Modifier.height(12.dp))

        ProfileSection("Activity") {
            ProfileMenuItem(Icons.Default.List, "My Posted Tasks", "Tasks you've posted")
            ProfileMenuItem(Icons.Default.CheckCircle, "Completed Tasks", "History of finished tasks")
            ProfileMenuItem(Icons.Default.Payments, "Payment History", "Transactions & escrow")
            ProfileMenuItem(Icons.Default.Star, "Reviews & Ratings", "See your feedback")
        }

        Spacer(Modifier.height(12.dp))

        ProfileSection("App") {
            ProfileMenuItem(Icons.Default.Notifications, "Notifications", "Manage alerts")
            ProfileMenuItem(Icons.Default.Security, "Privacy & Security", "Data and permissions")
            ProfileMenuItem(Icons.Default.Help, "Help & Support", "FAQ and contact us")
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedButton(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDC2626)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
            ) {
                Icon(Icons.Default.Logout, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showAvatarPicker) {
        AvatarPickerDialog(currentId = selectedAvatarId, onSelect = { selectedAvatarId = it; showAvatarPicker = false }, onDismiss = { showAvatarPicker = false })
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, Modifier.size(20.dp), tint = AppTeal)
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppTeal.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, Modifier.size(18.dp), tint = AppTeal) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), tint = TextSecondary)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 64.dp), color = AppBorder)
}

@Composable
fun AvatarPickerDialog(currentId: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Choose Your Avatar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = TextSecondary) }
                }
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(bugAvatars) { avatar ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(avatar.backgroundColor)
                                .then(if (avatar.id == currentId) Modifier.border(3.dp, AppTeal, CircleShape) else Modifier)
                                .clickable { onSelect(avatar.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(avatar.image, avatar.contentDescription, Modifier.size(28.dp), Color.White)
                        }
                    }
                }
            }
        }
    }
}