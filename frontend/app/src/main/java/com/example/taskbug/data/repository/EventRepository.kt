package com.example.taskbug.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.taskbug.model.Event
import com.example.taskbug.util.CloudinaryUploader
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "EventRepository"
private const val EVENTS_COLLECTION = "events"

class EventRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection(EVENTS_COLLECTION)

    /**
     * Upload an image via Cloudinary (free, no Firebase Storage required).
     * Returns the secure HTTPS URL of the uploaded image.
     */
    suspend fun uploadEventImage(context: Context, imageUri: Uri, userId: String): Result<String> {
        return CloudinaryUploader.uploadImage(context, imageUri)
    }

    /**
     * Create a new event in Firestore (imageUrl should be the Cloudinary URL).
     */
    suspend fun createEvent(event: Event): Result<String> = try {
        when {
            event.title.isBlank() -> Result.failure(Exception("Event title is required"))
            event.userId.isBlank() -> Result.failure(Exception("User ID is required. Please log in first."))
            event.imageUrl.isBlank() -> Result.failure(Exception("Event banner image is required"))
            else -> {
                val documentRef = eventsCollection.add(event).await()
                Log.d(TAG, "Event created: ${documentRef.id}")
                Result.success(documentRef.id)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error creating event: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Real-time stream of all events (for the Events feed screen), newest first.
     */
    fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val events = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    }
                    trySend(events)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Real-time stream of events posted by a specific user, for the Profile history section.
     */
    fun getUserEvents(userId: String): Flow<List<Event>> = callbackFlow {
        Log.d(TAG, "Setting up listener for user events: $userId")
        val listener = eventsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching user events: ${error.message}", error)
                    close(error); return@addSnapshotListener
                }
                if (snapshot != null) {
                    val events = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    }
                    Log.d(TAG, "Fetched ${events.size} events for user $userId")
                    trySend(events)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Partially update an event document (e.g. title, description, etc.)
     */
    suspend fun updateEvent(eventId: String, updates: Map<String, Any>): Result<Unit> = try {
        eventsCollection.document(eventId).update(updates).await()
        Log.d(TAG, "Event updated: $eventId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating event: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Delete an event by ID.
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> = try {
        eventsCollection.document(eventId).delete().await()
        Log.d(TAG, "Event deleted: $eventId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting event: ${e.message}", e)
        Result.failure(e)
    }
}
