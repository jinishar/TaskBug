package com.example.taskbug.data.repository

import android.util.Log
import com.example.taskbug.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "TaskRepository"
private const val TASKS_COLLECTION = "tasks"

class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val tasksCollection = firestore.collection(TASKS_COLLECTION)

    /**
     * Create a new task
     */
    suspend fun createTask(task: Task): Result<String> = try {
        // Validate task has required fields
        if (task.title.isBlank() || task.description.isBlank()) {
            Result.failure(Exception("Title and description are required"))
        } else if (task.userId.isBlank()) {
            Result.failure(Exception("User ID is required. Please log in first."))
        } else {
            val documentRef = tasksCollection.add(task).await()
            Log.d(TAG, "Task created successfully: ${documentRef.id}")
            Result.success(documentRef.id)
        }
    } catch (e: Exception) {
        val errorMsg = when {
            e.message?.contains("Permission denied") == true ->
                "Permission denied. Please check your Firestore security rules."
            e.message?.contains("PERMISSION_DENIED") == true ->
                "You don't have permission to create tasks. Please contact admin."
            else -> e.message ?: "Failed to create task"
        }
        Log.e(TAG, "Error creating task: $errorMsg", e)
        Result.failure(Exception(errorMsg))
    }

    /**
     * Get all active tasks with real-time updates
     */
    fun getActiveTasks(): Flow<List<Task>> = callbackFlow {
        Log.d(TAG, "Setting up real-time listener for active tasks")

        val listener = tasksCollection
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching tasks: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Task::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing task: ${e.message}", e)
                            null
                        }
                    }
                    Log.d(TAG, "Fetched ${tasks.size} active tasks")
                    trySend(tasks)
                }
            }

        awaitClose {
            Log.d(TAG, "Closing tasks listener")
            listener.remove()
        }
    }

    /**
     * Get tasks created by a specific user
     */
    fun getUserTasks(userId: String): Flow<List<Task>> = callbackFlow {
        Log.d(TAG, "Setting up listener for user tasks: $userId")

        val listener = tasksCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching user tasks: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    }
                    trySend(tasks)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Update a task
     */
    suspend fun updateTask(taskId: String, updates: Map<String, Any>): Result<Unit> = try {
        tasksCollection.document(taskId).update(updates).await()
        Log.d(TAG, "Task updated successfully: $taskId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating task: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Delete a task
     */
    suspend fun deleteTask(taskId: String): Result<Unit> = try {
        tasksCollection.document(taskId).delete().await()
        Log.d(TAG, "Task deleted successfully: $taskId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting task: ${e.message}", e)
        Result.failure(e)
    }
}

