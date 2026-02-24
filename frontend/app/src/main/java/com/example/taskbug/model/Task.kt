package com.example.taskbug.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val deadline: String = "",
    val pay: Double = 0.0,
    val location: String = "",
    val userId: String = "",
    val userName: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val status: String = "active",
    val imageUrl: String = ""
)


