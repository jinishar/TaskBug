package com.example.taskbug.data.repository

import com.example.taskbug.model.UserLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseLocationRepository {
    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }
    }
    private val locationRef = database.getReference("live_locations")

    suspend fun updateLocation(location: UserLocation): Result<Unit> {
        return try {
            locationRef.child(location.userId).setValue(location).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeLocation(userId: String): Result<Unit> {
        return try {
            locationRef.child(userId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeLocations(): Flow<List<UserLocation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableListOf<UserLocation>()
                for (child in snapshot.children) {
                    val loc = child.getValue(UserLocation::class.java)
                    if (loc != null) {
                        // Optional: Filter out stale locations older than 2 minutes here
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - loc.timestamp < 120_000) {
                            locations.add(loc)
                        } else {
                            // Automatically remove stale data
                            child.ref.removeValue()
                        }
                    }
                }
                trySend(locations)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        locationRef.addValueEventListener(listener)
        awaitClose { locationRef.removeEventListener(listener) }
    }
}
