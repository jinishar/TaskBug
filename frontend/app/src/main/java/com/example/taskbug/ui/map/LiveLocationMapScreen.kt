package com.example.taskbug.ui.map

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.location.hasLocationPermission
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

private val AppTeal = Color(0xFF0F766E)
private val TextPrimary = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showTimerDialog by remember { mutableStateOf(false) }

    // Camera initial position (default to somewhere central if no location yet)
    val defaultLocation = LatLng(12.9351, 77.5360) // Bangalore approximate
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startSharingLocation(null)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let { loc ->
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLng(
                    LatLng(loc.latitude, loc.longitude)
                ),
                durationMs = 1000
            )
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Location Map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopSharingLocation() // Optional: auto stop on quit
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // MAP
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false // We'll build our own
                ),
                properties = MapProperties(
                    isMyLocationEnabled = context.hasLocationPermission() // Shows default blue dot if permission is there
                )
            ) {
                // OTHER USERS MARKERS (Red)
                uiState.otherUsers.forEach { otherUser ->
                    MarkerInfoWindow(
                        state = MarkerState(position = LatLng(otherUser.latitude, otherUser.longitude)),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        title = otherUser.userName,
                        snippet = "Active Now"
                    ) { marker ->
                        // Custom Info Window
                        Column(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(marker.title ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(marker.snippet ?: "", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // CURRENT USER MARKER (Blue)
                uiState.currentLocation?.let { me ->
                    Marker(
                        state = MarkerState(position = LatLng(me.latitude, me.longitude)),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        title = "You",
                        snippet = "Sharing Live"
                    )
                }
            }

            // RE-CENTER BUTTON
            FloatingActionButton(
                onClick = {
                    uiState.currentLocation?.let { loc ->
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLng(
                                    LatLng(loc.latitude, loc.longitude)
                                ), 1000
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 140.dp),
                containerColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center", tint = AppTeal)
            }

            // GLASSMORPHISM FLOATING PANEL
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Status Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isSharing) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isSharing) "Sharing Live" else "Not Sharing",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (uiState.isSharing && uiState.timeRemainingString != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Auto-stops in ${uiState.timeRemainingString}",
                            color = AppTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (!uiState.isSharing) {
                            Button(
                                onClick = {
                                    if (context.hasLocationPermission()) {
                                        viewModel.startSharingLocation(null) // Indefinite
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Start with timer button
                            OutlinedButton(
                                onClick = { showTimerDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(50.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTeal),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTeal)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("15 Min", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.stopSharingLocation() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop Sharing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Share Location") },
            text = { Text("Share your live location securely for 15 minutes? It will automatically turn off afterwards.") },
            confirmButton = {
                TextButton(onClick = {
                    if (context.hasLocationPermission()) {
                        viewModel.startSharingLocation(15)
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    showTimerDialog = false
                }) {
                    Text("Start", color = AppTeal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
