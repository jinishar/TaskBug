package com.example.taskbug.ui.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskbug.data.repository.TaskRepository
import com.example.taskbug.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "TaskViewModel"

data class TaskUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val tasks: List<Task> = emptyList(),
    val userTasks: List<Task> = emptyList(),
    val enrolledTasks: List<Task> = emptyList(),
    val allTasks: List<Task> = emptyList(),  // Store unfiltered tasks for filtering
    val selectedCategory: String? = null,
    val selectedPriceRange: ClosedFloatingPointRange<Float> = 0f..10000f
)

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()
    private val auth = FirebaseAuth.getInstance()

    // Initialize Firebase Realtime Database for fetching user name
    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://taskbugcu-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Database: ${e.message}", e)
            FirebaseDatabase.getInstance()
        }
    }
    private val usersRef by lazy { database.getReference("users") }

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d(TAG, "TaskViewModel initialized")
        loadActiveTasks()
        loadUserTasks()
        loadEnrolledTasks()
    }

    /**
     * Create a new task
     */
    fun createTask(
        title: String,
        description: String,
        category: String,
        deadline: String,
        pay: Double,
        location: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not logged in"
                )
                return@launch
            }

            try {
                // Fetch user name from Realtime Database
                val userSnapshot = usersRef.child(currentUser.uid).get().await()
                val userName = if (userSnapshot.exists()) {
                    userSnapshot.child("name").getValue(String::class.java) ?: "Anonymous"
                } else {
                    "Anonymous"
                }

                Log.d(TAG, "Fetched userName from database: $userName")

                val task = Task(
                    title = title,
                    description = description,
                    category = category,
                    deadline = deadline,
                    pay = pay,
                    location = location,
                    userId = currentUser.uid,
                    userName = userName,
                    status = "active"
                )

                Log.d(TAG, "Creating task with userId: ${task.userId}, userName: ${task.userName}")

                val result = repository.createTask(task)
                result.onSuccess {
                    Log.d(TAG, "Task created successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Task posted successfully!"
                    )
                    clearSuccessMessage()
                }.onFailure { exception ->
                    Log.e(TAG, "Error creating task: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create task"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in createTask: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Load all active tasks
     */
    private fun loadActiveTasks() {
        viewModelScope.launch {
            try {
                repository.getActiveTasks().collect { tasks ->
                    Log.d(TAG, "Tasks updated: ${tasks.size} tasks")
                    // Store all unfiltered tasks
                    _uiState.value = _uiState.value.copy(allTasks = tasks)

                    // Check if filters are active
                    val hasActiveFilters = _uiState.value.selectedCategory != null ||
                        _uiState.value.selectedPriceRange != (0f..10000f)

                    if (hasActiveFilters) {
                        // If filters are active, reapply them to the new tasks
                        Log.d(TAG, "Reapplying filters to updated tasks")
                        applyFilters()
                    } else {
                        // If no filters, show all tasks
                        _uiState.value = _uiState.value.copy(tasks = tasks)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading tasks: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load tasks"
                )
            }
        }
    }

    fun loadUserTasks() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = _uiState.value.copy(error = "User not logged in")
                return@launch
            }

            try {
                repository.getUserTasks(userId).collect { tasks ->
                    _uiState.value = _uiState.value.copy(userTasks = tasks)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user tasks: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Failed to load your tasks")
            }
        }
    }

    /**
     * Load tasks the current user is enrolled in
     */
    fun loadEnrolledTasks() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                repository.getEnrolledTasks(userId).collect { tasks ->
                    _uiState.value = _uiState.value.copy(enrolledTasks = tasks)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading enrolled tasks: ${e.message}", e)
            }
        }
    }

    /**
     * Enroll in a task
     */
    fun enrollTask(taskId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                var userName = currentUser.displayName ?: "Anonymous"
                try {
                    val userSnapshot = usersRef.child(currentUser.uid).get().await()
                    if (userSnapshot.exists()) {
                        val dbName = userSnapshot.child("name").getValue(String::class.java)
                        if (!dbName.isNullOrBlank()) userName = dbName
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Non-critical: couldn't fetch name from RTDB, using displayName: ${e.message}")
                }

                val updates = mapOf(
                    "status" to "assigned",
                    "enrolledUserId" to currentUser.uid,
                    "enrolledUserName" to userName
                )
                
                val result = repository.updateTask(taskId, updates)
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Successfully enrolled in task!"
                    )
                    clearSuccessMessage()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to enroll: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to enroll: ${e.message}"
                )
            }
        }
    }

    /**
     * Mark an assigned task as completed by the owner
     */
    fun markTaskCompleted(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val updates = mapOf("status" to "completed")
            val result = repository.updateTask(taskId, updates)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Task marked as completed!"
                )
                clearSuccessMessage()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to mark as completed: ${exception.message}"
                )
            }
        }
    }

    /**
     * Delete a task (only if current user is owner)
     */
    fun deleteTask(taskId: String, ownerId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != ownerId) {
            _uiState.value = _uiState.value.copy(error = "You can only delete your own tasks")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteTask(taskId)
            result.onSuccess {
                Log.d(TAG, "Task deleted successfully")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Task deleted"
                )
                clearSuccessMessage()
            }.onFailure { exception ->
                Log.e(TAG, "Error deleting task: ${exception.message}", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to delete task"
                )
            }
        }
    }

    /**
     * Update task status
     */
    fun updateTaskStatus(taskId: String, newStatus: String, ownerId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != ownerId) {
            _uiState.value = _uiState.value.copy(error = "You can only update your own tasks")
            return
        }

        viewModelScope.launch {
            val result = repository.updateTask(taskId, mapOf("status" to newStatus))
            result.onSuccess {
                Log.d(TAG, "Task status updated to: $newStatus")
            }.onFailure { exception ->
                Log.e(TAG, "Error updating status: ${exception.message}", exception)
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Failed to update task"
                )
            }
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Filter tasks by category
     */
    fun filterByCategory(category: String?) {
        Log.d(TAG, "Filtering tasks by category: $category")
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    /**
     * Filter tasks by price range
     */
    fun filterByPriceRange(priceRange: ClosedFloatingPointRange<Float>) {
        Log.d(TAG, "Filtering tasks by price range: ${priceRange.start} - ${priceRange.endInclusive}")
        _uiState.value = _uiState.value.copy(selectedPriceRange = priceRange)
        applyFilters()
    }

    /**
     * Apply all active filters to tasks
     */
    private fun applyFilters() {
        try {
            val allTasks = _uiState.value.allTasks
            var filtered = allTasks

            Log.d(TAG, "Applying filters - total tasks: ${allTasks.size}")

            // Filter by category
            val selectedCategory = _uiState.value.selectedCategory
            if (!selectedCategory.isNullOrEmpty() && selectedCategory != "All") {
                filtered = filtered.filter { task ->
                    task.category.equals(selectedCategory, ignoreCase = true)
                }
                Log.d(TAG, "After category filter '$selectedCategory': ${filtered.size} tasks")
            }

            // Filter by price range
            val priceRange = _uiState.value.selectedPriceRange
            filtered = filtered.filter { task ->
                task.pay in priceRange
            }
            Log.d(TAG, "After price filter (${priceRange.start}-${priceRange.endInclusive}): ${filtered.size} tasks")

            // Update UI with filtered tasks
            _uiState.value = _uiState.value.copy(tasks = filtered)
            Log.d(TAG, "Final filtered tasks: ${filtered.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying filters: ${e.message}", e)
            _uiState.value = _uiState.value.copy(error = "Failed to apply filters")
        }
    }

    /**
     * Clear all filters and reload all tasks
     */
    fun clearAllFilters() {
        Log.d(TAG, "Clearing all filters")
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            selectedPriceRange = 0f..10000f,
            tasks = _uiState.value.allTasks  // Show all unfiltered tasks
        )
    }

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

