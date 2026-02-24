# Task System - Code Examples & Snippets

## 📝 Common Use Cases

### 1. Using TaskViewModel in a Composable

```kotlin
@Composable
fun MyTaskScreen() {
    val viewModel: TaskViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Your UI code here
}
```

### 2. Creating a Task Programmatically

```kotlin
val viewModel: TaskViewModel = viewModel()

viewModel.createTask(
    title = "Buy Groceries",
    description = "Milk, bread, eggs",
    category = "Shopping",
    deadline = "2024-10-25",
    pay = 500.0,
    location = "Brooklyn, NY"
)
```

### 3. Observing Task Feed in Real-time

```kotlin
val uiState by viewModel.uiState.collectAsState()

LazyColumn {
    items(uiState.tasks) { task ->
        TaskCard(task = task)
    }
}
```

### 4. Deleting a Task with Owner Check

```kotlin
fun deleteMyTask(task: Task) {
    viewModel.deleteTask(
        taskId = task.id,
        ownerId = task.userId
    )
}
```

### 5. Showing Loading State

```kotlin
val uiState by viewModel.uiState.collectAsState()

if (uiState.isLoading) {
    CircularProgressIndicator()
} else {
    TaskFeedScreen()
}
```

### 6. Handling Errors

```kotlin
val uiState by viewModel.uiState.collectAsState()

if (uiState.error != null) {
    Snackbar(modifier = Modifier.padding(16.dp)) {
        Text(uiState.error!!)
    }
}
```

### 7. Showing Success Message

```kotlin
val uiState by viewModel.uiState.collectAsState()

LaunchedEffect(uiState.successMessage) {
    if (uiState.successMessage != null) {
        // Task was created successfully
        // Navigate or close dialog
    }
}
```

### 8. Loading Only User's Tasks

```kotlin
val viewModel: TaskViewModel = viewModel()

// In ViewModel init or button click
viewModel.loadUserTasks()

val uiState by viewModel.uiState.collectAsState()
val myTasks = uiState.userTasks
```

## 🔧 Extending TaskViewModel

### Add Custom Filtering

```kotlin
fun filterTasksByCategory(category: String) {
    viewModelScope.launch {
        repository.getActiveTasks().collect { allTasks ->
            val filtered = allTasks.filter { it.category == category }
            _uiState.value = _uiState.value.copy(tasks = filtered)
        }
    }
}
```

### Add Task Search

```kotlin
fun searchTasks(query: String) {
    viewModelScope.launch {
        repository.getActiveTasks().collect { allTasks ->
            val results = allTasks.filter { 
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
            _uiState.value = _uiState.value.copy(tasks = results)
        }
    }
}
```

### Add Price Range Filter

```kotlin
fun getTasksByPriceRange(minPay: Double, maxPay: Double) {
    viewModelScope.launch {
        repository.getActiveTasks().collect { allTasks ->
            val filtered = allTasks.filter { 
                it.pay >= minPay && it.pay <= maxPay
            }
            _uiState.value = _uiState.value.copy(tasks = filtered)
        }
    }
}
```

## 🎨 Customizing UI

### Change Theme Colors

In `AddTaskScreen.kt` and `TaskFeedScreen.kt`, modify:

```kotlin
// Change this color
private val AppTeal = Color(0xFF0F766E)

// To any other color
private val AppTeal = Color(0xFF2196F3)  // Blue
// OR
private val AppTeal = Color(0xFF4CAF50)  // Green
```

### Add Custom Task Card Styling

```kotlin
@Composable
fun CustomTaskCard(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp),  // More shadow
        shape = RoundedCornerShape(20.dp),  // More rounded
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        // Your custom card content
    }
}
```

### Add Animation to Task Feed

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(
        items = uiState.tasks,
        key = { it.id }  // Enables animations
    ) { task ->
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically() + fadeIn()
        ) {
            TaskCard(task = task)
        }
    }
}
```

## 📊 Adding Features

### Add Task Approval/Rejection

```kotlin
// In TaskViewModel
fun approveTask(taskId: String) {
    viewModelScope.launch {
        repository.updateTask(taskId, mapOf("status" to "approved"))
    }
}

fun rejectTask(taskId: String) {
    viewModelScope.launch {
        repository.updateTask(taskId, mapOf("status" to "rejected"))
    }
}
```

### Add Task Comments

```kotlin
// Extend Task data class
data class Task(
    // ... existing fields ...
    val comments: List<String> = emptyList()
)

// In TaskViewModel
fun addComment(taskId: String, comment: String) {
    viewModelScope.launch {
        val currentComments = repository.getTaskById(taskId).getOrNull()?.comments ?: emptyList()
        repository.updateTask(
            taskId, 
            mapOf("comments" to currentComments + comment)
        )
    }
}
```

### Add Task Favoriting

```kotlin
// Add to Task
data class Task(
    // ... existing ...
    val favoritedBy: List<String> = emptyList()
)

// In TaskViewModel
fun toggleFavorite(taskId: String, userId: String) {
    viewModelScope.launch {
        val task = repository.getTaskById(taskId).getOrNull() ?: return@launch
        val updated = if (task.favoritedBy.contains(userId)) {
            task.favoritedBy - userId
        } else {
            task.favoritedBy + userId
        }
        repository.updateTask(taskId, mapOf("favoritedBy" to updated))
    }
}
```

## 🔍 Querying Examples

### Query by Multiple Criteria

```kotlin
// In TaskRepository - Add new function
fun getTasksByCategory(category: String): Flow<List<Task>> = callbackFlow {
    val listener = tasksCollection
        .whereEqualTo("status", "active")
        .whereEqualTo("category", category)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tasks = snapshot.documents.mapNotNull { 
                    it.toObject(Task::class.java)?.copy(id = it.id) 
                }
                trySend(tasks)
            }
        }
    awaitClose { listener.remove() }
}
```

### Query with Pagination

```kotlin
// In TaskRepository
var lastDocument: DocumentSnapshot? = null

fun getTasksWithPagination(pageSize: Int): Flow<List<Task>> = callbackFlow {
    var query: Query = tasksCollection
        .whereEqualTo("status", "active")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(pageSize.toLong())
    
    if (lastDocument != null) {
        query = query.startAfter(lastDocument)
    }
    
    query.get().addOnSuccessListener { snapshot ->
        if (snapshot.documents.isNotEmpty()) {
            lastDocument = snapshot.documents.last()
            val tasks = snapshot.toObjects(Task::class.java)
            trySend(tasks)
        }
    }
    
    awaitClose {}
}
```

## 🧪 Testing Examples

### Unit Test - Create Task

```kotlin
@Test
fun testCreateTask() {
    val viewModel = TaskViewModel()
    
    viewModel.createTask(
        title = "Test Task",
        description = "Test Description",
        category = "Test",
        deadline = "2024-12-31",
        pay = 100.0,
        location = "Test Location"
    )
    
    // Verify state updated
    assert(viewModel.uiState.value.successMessage != null)
}
```

### UI Test - Task Feed

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testTaskFeedDisplaysTaskCard() {
    composeTestRule.setContent {
        TaskFeedScreen()
    }
    
    composeTestRule.onNodeWithText("Active Tasks").assertIsDisplayed()
    composeTestRule.onNodeWithTag("task_card").assertIsDisplayed()
}
```

## 🔐 Security Examples

### Verify User Ownership

```kotlin
private fun isTaskOwner(task: Task, currentUserId: String): Boolean {
    return task.userId == currentUserId
}

// Usage
if (isTaskOwner(task, viewModel.getCurrentUserId()!!)) {
    // Show edit/delete buttons
}
```

### Validate Task Data Before Submit

```kotlin
fun isValidTask(task: Task): Boolean {
    return task.title.isNotBlank() &&
           task.description.isNotBlank() &&
           task.userId.isNotBlank() &&
           task.pay >= 0
}
```

## 🎯 Example: Complete Task Creation Flow

```kotlin
@Composable
fun CompleteTaskCreationExample() {
    val viewModel: TaskViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    Column {
        // Show success message
        if (uiState.successMessage != null) {
            Surface(color = Color.Green) {
                Text(uiState.successMessage!!)
            }
        }
        
        // Show error message
        if (uiState.error != null) {
            Surface(color = Color.Red) {
                Text(uiState.error!!)
            }
        }
        
        // Input fields
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") }
        )
        
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )
        
        // Submit button
        Button(
            onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    viewModel.createTask(
                        title = title,
                        description = description,
                        category = "",
                        deadline = "",
                        pay = 0.0,
                        location = ""
                    )
                }
            },
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Create Task")
            }
        }
    }
}
```

## 🔄 Example: Real-time Task Update

```kotlin
@Composable
fun RealTimeTaskFeedExample() {
    val viewModel: TaskViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Automatically updates when tasks change
    LazyColumn {
        items(uiState.tasks) { task ->
            TaskCard(
                task = task,
                isOwner = task.userId == viewModel.getCurrentUserId(),
                onDelete = { 
                    viewModel.deleteTask(task.id, task.userId) 
                }
            )
        }
    }
}
```

## 📱 Example: Full Task Screen

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullTaskScreenExample() {
    val viewModel: TaskViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) {
        TaskFeedScreen(
            onEditTask = { taskId -> 
                // Handle edit
            }
        )
    }
    
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            AddTaskScreen(
                onTaskCreated = { showAddDialog = false },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}
```

---

**Last Updated**: February 20, 2026  
**Version**: 1.0  
**Status**: Complete ✅

