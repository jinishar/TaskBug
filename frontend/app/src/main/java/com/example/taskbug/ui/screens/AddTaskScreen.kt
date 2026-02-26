package com.example.taskbug.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskbug.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.navigation.NavController
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable

private val AppTeal = Color(0xFF0F766E)
private val AppBackground = Color(0xFFF9FAFB)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val AppBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    onTaskCreated: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf("") }
    var pay by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Observe result from MapPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedLocation = savedStateHandle?.getLiveData<String>("selected_location")?.observeAsState()
    
    LaunchedEffect(selectedLocation?.value) {
        val loc = selectedLocation?.value as? String
        if (!loc.isNullOrBlank()) {
            location = loc
            savedStateHandle?.remove<String>("selected_location")
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // Clear form
            title = ""
            description = ""
            category = ""
            deadline = ""
            pay = ""
            location = ""
            onTaskCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Post a New Task",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        // Photo Upload Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppBorder.copy(alpha = 0.3f))
                .clickable { /* Photo Picker Logic */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = AppTeal,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Upload Task Photo", fontSize = 12.sp, color = TextSecondary)
            }
        }

        // Task Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTeal,
                unfocusedBorderColor = AppBorder
            )
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTeal,
                unfocusedBorderColor = AppBorder
            )
        )

        // Category
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTeal,
                unfocusedBorderColor = AppBorder
            )
        )

        // Pay (₹)
        OutlinedTextField(
            value = pay,
            onValueChange = { pay = it },
            label = { Text("Pay (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTeal,
                unfocusedBorderColor = AppBorder
            )
        )

        // Date Picker with Calendar Icon
        OutlinedTextField(
            value = deadline,
            onValueChange = { },
            label = { Text("Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showDatePicker = true },
                    tint = AppTeal
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTeal,
                unfocusedBorderColor = AppBorder
            ),
            readOnly = true
        )

        // Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(
                    Icons.Default.Place,
                    contentDescription = "Pick Location",
                    modifier = Modifier.size(24.dp).clickable { 
                        navController.navigate("map_picker") 
                    },
                    tint = AppTeal
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTeal,
                unfocusedBorderColor = AppBorder
            )
        )

        // Error Message
        if (uiState.error != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Success Message
        if (uiState.successMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    uiState.successMessage!!,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Post Task Button
        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    return@Button
                }
                viewModel.createTask(
                    title = title,
                    description = description,
                    category = category,
                    deadline = deadline,
                    pay = pay.toDoubleOrNull() ?: 0.0,
                    location = location
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
            enabled = !uiState.isLoading && title.isNotBlank() && description.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    "Post Task",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { selectedDate ->
                deadline = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        val formattedDate = dateFormat.format(calendar.time)
                        onDateSelected(formattedDate)
                    }
                }
            ) {
                Text("OK", color = Color(0xFF0F766E))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF0F766E))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

