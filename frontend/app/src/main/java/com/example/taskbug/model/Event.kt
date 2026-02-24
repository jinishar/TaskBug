package com.example.taskbug.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    /** Date in yyyy-MM-dd format — used for 10-day edit lock calculation */
    val eventDate: String = "",
    /** Display-friendly time string, e.g. "06:00 PM" */
    val eventTime: String = "",
    val venue: String = "",
    val category: String = "",
    /** Ticket price in ₹; 0.0 means Free */
    val ticketPrice: Double = 0.0,
    /** Maximum number of attendees; 0 means Unlimited */
    val maxAttendees: Int = 0,
    /** Firebase Storage download URL for the event banner image */
    val imageUrl: String = "",
    val userId: String = "",
    val userName: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
