package com.example.taskbug.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AddPostScreen(type: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (type == "event") "Create New Event" else "Create New Task",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        // Add form fields here in the future
    }
}
