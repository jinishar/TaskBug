package com.example.taskbug.ui.map

import android.Manifest
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskbug.location.hasLocationPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val AppTeal = Color(0xFF0F766E)
private val TextPrimary = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onLocationSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Default to a central location if we don't have permission yet
    val defaultLocation = LatLng(12.9351, 77.5360) // Bangalore approximate
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var isDragging by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("Locating...") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    // Fused Location for centering on current location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude), 15f
                                ), 1000
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Ignore, permission was literally just granted or denied
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Attempt to center on user when the screen first opens
    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(location.latitude, location.longitude), 15f
                        )
                    }
                }
            } catch (e: SecurityException) { }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Geocode camera position when it stops moving
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            isDragging = true
            currentAddress = "Searching..."
        } else {
            isDragging = false
            val pos = cameraPositionState.position.target
            coroutineScope.launch {
                val address = getAddress(geocoder, pos.latitude, pos.longitude)
                currentAddress = address ?: "Unknown Location"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            // Google Map Behind Everything
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(
                    isMyLocationEnabled = context.hasLocationPermission()
                )
            )

            // Center Pin Marker
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Small "Bounce" effect when dragging
                    Box(
                        modifier = Modifier
                            .offset(y = if (isDragging) (-15).dp else 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = "Pin",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    // Pin shadow
                    Box(
                        modifier = Modifier
                            .size(12.dp, 4.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f))
                    )
                }
            }

            // RE-CENTER BUTTON
            FloatingActionButton(
                onClick = {
                    if (context.hasLocationPermission()) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            com.google.android.gms.maps.CameraUpdateFactory.newLatLng(
                                                LatLng(location.latitude, location.longitude)
                                            ), 1000
                                        )
                                    }
                                }
                            }
                        } catch (e: SecurityException) { }
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
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

            // Bottom Panel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Selected Location",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentAddress,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (currentAddress.isNotBlank() && currentAddress != "Searching..." && currentAddress != "Locating...") {
                                onLocationSelected(currentAddress)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                        enabled = !isDragging && currentAddress != "Searching..." && currentAddress != "Locating..."
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Location", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    }
}

private suspend fun getAddress(geocoder: Geocoder, lat: Double, lng: Double): String? {
    return withContext(Dispatchers.IO) {
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Combine address lines
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(", ")
                }
                sb.toString().removeSuffix(", ")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
