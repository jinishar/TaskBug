package com.example.taskbug.ui.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskbug.data.repository.FirebaseLocationRepository
import com.example.taskbug.location.DefaultLocationClient
import com.example.taskbug.location.LocationClient
import com.example.taskbug.model.UserLocation
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MapUiState(
    val isSharing: Boolean = false,
    val timeRemainingRaw: Int? = null, // In seconds, if auto-stop is active
    val currentLocation: UserLocation? = null,
    val otherUsers: List<UserLocation> = emptyList(),
    val error: String? = null
) {
    val timeRemainingString: String?
        get() {
            if (timeRemainingRaw == null) return null
            val min = timeRemainingRaw / 60
            val sec = timeRemainingRaw % 60
            return String.format("%02d:%02d", min, sec)
        }
}

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val locationClient: LocationClient
    private val repository = FirebaseLocationRepository()
    private val auth = FirebaseAuth.getInstance()
    private val usersRef by lazy {
        try {
            FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }.getReference("users")
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var observeJob: Job? = null
    private var timerJob: Job? = null

    init {
        locationClient = DefaultLocationClient(
            application.applicationContext,
            LocationServices.getFusedLocationProviderClient(application.applicationContext)
        )
        startObservingOthers()
    }

    private fun startObservingOthers() {
        observeJob?.cancel()
        val currentUserUid = auth.currentUser?.uid
        observeJob = repository.observeLocations()
            .onEach { locations ->
                val others = locations.filter { it.userId != currentUserUid }
                _uiState.value = _uiState.value.copy(otherUsers = others)
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            .launchIn(viewModelScope)
    }

    fun startSharingLocation(durationMinutes: Int? = null) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = "User not logged in")
            return
        }

        _uiState.value = _uiState.value.copy(isSharing = true, error = null)

        locationJob = locationClient.getLocationUpdates(5000L) // every 5 seconds
            .catch { e ->
                _uiState.value = _uiState.value.copy(isSharing = false, error = e.message)
            }
            .onEach { location ->
                // Fetch userName on the first emission (cachable logic can be added later)
                val userName = try {
                    val snap = usersRef.child(user.uid).child("name").get().await()
                    snap.getValue(String::class.java) ?: "User"
                } catch (e: Exception) { "User" }

                val userLocation = UserLocation(
                    userId = user.uid,
                    userName = userName,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )
                
                _uiState.value = _uiState.value.copy(currentLocation = userLocation)
                repository.updateLocation(userLocation)
            }
            .launchIn(viewModelScope)

        if (durationMinutes != null) {
            startTimer(durationMinutes * 60)
        } else {
            _uiState.value = _uiState.value.copy(timeRemainingRaw = null)
        }
    }

    fun stopSharingLocation() {
        locationJob?.cancel()
        timerJob?.cancel()
        
        _uiState.value = _uiState.value.copy(
            isSharing = false, 
            timeRemainingRaw = null,
            currentLocation = null
        )

        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                repository.removeLocation(user.uid)
            }
        }
    }

    private fun startTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(timeRemainingRaw = remaining)
                delay(1000L)
                remaining--
            }
            stopSharingLocation()
            _uiState.value = _uiState.value.copy(error = "Location sharing auto-stopped") // Info message
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopSharingLocation() // Always stop on destroy to prevent memory leaks/background drains
    }
}
