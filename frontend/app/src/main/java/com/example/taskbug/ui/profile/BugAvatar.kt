package com.example.taskbug.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a selectable bug avatar.
 *
 * @param id A unique identifier for the avatar.
 * @param image The vector graphic for the avatar icon.
 * @param contentDescription A description for accessibility.
 * @param backgroundColor The background color for the avatar circle.
 */
data class BugAvatar(
    val id: Int,
    val image: ImageVector,
    val contentDescription: String,
    val backgroundColor: Color
)
