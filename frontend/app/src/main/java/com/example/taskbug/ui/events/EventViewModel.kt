package com.example.taskbug.ui.events

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskbug.data.repository.EventRepository
import com.example.taskbug.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val TAG = "EventViewModel"

data class EventUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,          // true while image is uploading
    val error: String? = null,
    val successMessage: String? = null,
    val events: List<Event> = emptyList(),      // all events (feed)
    val userEvents: List<Event> = emptyList()  // current user's posted events
)

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventRepository()
    private val auth = FirebaseAuth.getInstance()
    private val appContext = application.applicationContext

    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }
    }
    private val usersRef by lazy { database.getReference("users") }

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAllEvents()
        loadUserEvents()
    }

    // ── Feed ──────────────────────────────────────────────────────────────────

    private fun loadAllEvents() {
        viewModelScope.launch {
            try {
                repository.getAllEvents().collect { events ->
                    _uiState.value = _uiState.value.copy(events = events)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading events: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Failed to load events")
            }
        }
    }

    // ── Profile history ───────────────────────────────────────────────────────

    fun loadUserEvents() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                repository.getUserEvents(userId).collect { events ->
                    _uiState.value = _uiState.value.copy(userEvents = events)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user events: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Failed to load your events")
            }
        }
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Upload the banner image first, then persist the event to Firestore.
     */
    fun createEvent(
        title: String,
        description: String,
        eventDate: String,
        eventTime: String,
        venue: String,
        category: String,
        ticketPrice: Double,
        maxAttendees: Int,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(error = "User not logged in")
                return@launch
            }

            // Step 1 – upload image via Cloudinary
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            val uploadResult = repository.uploadEventImage(appContext, imageUri, currentUser.uid)
            if (uploadResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = uploadResult.exceptionOrNull()?.message ?: "Image upload failed"
                )
                return@launch
            }
            val imageUrl = uploadResult.getOrThrow()

            // Step 2 – fetch user name
            _uiState.value = _uiState.value.copy(isUploading = false, isLoading = true)
            try {
                val userSnapshot = usersRef.child(currentUser.uid).get().await()
                val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Anonymous"

                val event = Event(
                    title = title,
                    description = description,
                    eventDate = eventDate,
                    eventTime = eventTime,
                    venue = venue,
                    category = category,
                    ticketPrice = ticketPrice,
                    maxAttendees = maxAttendees,
                    imageUrl = imageUrl,
                    userId = currentUser.uid,
                    userName = userName
                )

                repository.createEvent(event)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Event posted!"
                        )
                        clearSuccessMessage()
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to create event"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    // ── Edit ──────────────────────────────────────────────────────────────────

    /**
     * Returns true if the event can still be edited (more than 10 days before event date).
     * eventDateStr is in "yyyy-MM-dd" format.
     */
    fun canEditEvent(eventDateStr: String): Boolean {
        return try {
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val eventDate = LocalDate.parse(eventDateStr, fmt)
            val today = LocalDate.now()
            ChronoUnit.DAYS.between(today, eventDate) > 10
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update editable fields of an event (only if owner and edit window is open).
     * Optionally re-upload a new image.
     */
    fun updateEvent(
        eventId: String,
        ownerId: String,
        title: String,
        description: String,
        eventDate: String,
        eventTime: String,
        venue: String,
        category: String,
        ticketPrice: Double,
        maxAttendees: Int,
        newImageUri: Uri? = null
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != ownerId) {
            _uiState.value = _uiState.value.copy(error = "You can only edit your own events")
            return
        }
        if (!canEditEvent(eventDate)) {
            _uiState.value = _uiState.value.copy(error = "Events cannot be edited within 10 days of the event date")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            var resolvedImageUrl: String? = null
            if (newImageUri != null) {
                _uiState.value = _uiState.value.copy(isUploading = true)
                val uploadResult = repository.uploadEventImage(appContext, newImageUri, currentUserId!!)
                _uiState.value = _uiState.value.copy(isUploading = false)
                if (uploadResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Image upload failed: ${uploadResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                resolvedImageUrl = uploadResult.getOrThrow()
            }

            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "description" to description,
                "eventDate" to eventDate,
                "eventTime" to eventTime,
                "venue" to venue,
                "category" to category,
                "ticketPrice" to ticketPrice,
                "maxAttendees" to maxAttendees
            )
            resolvedImageUrl?.let { updates["imageUrl"] = it }

            repository.updateEvent(eventId, updates)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Event updated!")
                    clearSuccessMessage()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Update failed")
                }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun deleteEvent(eventId: String, ownerId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != ownerId) {
            _uiState.value = _uiState.value.copy(error = "You can only delete your own events")
            return
        }
        viewModelScope.launch {
            repository.deleteEvent(eventId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Event deleted")
                    clearSuccessMessage()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to delete event")
                }
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun clearSuccessMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(successMessage = null)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
