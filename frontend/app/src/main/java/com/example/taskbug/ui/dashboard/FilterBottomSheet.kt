package com.example.taskbug.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val AppTeal = Color(0xFF0F766E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    isTaskFilter: Boolean,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTaskFilter) "Filter Tasks" else "Filter Events",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY SECTION (Renamed/Updated based on domain)
            FilterSection(title = "Category", icon = Icons.Default.Category) {
                val categories = if (isTaskFilter) 
                    listOf("Shopping", "Home", "Pets", "Delivery", "Others")
                else 
                    listOf("Tech", "Volunteer", "Networking", "Social", "Others")
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTeal.copy(alpha = 0.1f),
                                selectedLabelColor = AppTeal
                            )
                        )
                    }
                }
            }

            // PRICE RANGE (0 to 10000)
            FilterSection(title = if (isTaskFilter) "Price / Reward" else "Entry Fee", icon = Icons.Default.Payments) {
                var sliderPosition by remember { mutableStateOf(0f..10000f) }
                Column {
                    RangeSlider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..10000f,
                        colors = SliderDefaults.colors(
                            thumbColor = AppTeal,
                            activeTrackColor = AppTeal
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("₹${sliderPosition.start.roundToInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTeal)
                        Text("₹${sliderPosition.endInclusive.roundToInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTeal)
                    }
                }
            }

            // DATE / DEADLINE SECTION
            FilterSection(
                title = if (isTaskFilter) "Deadline" else "Event Date",
                icon = Icons.Default.DateRange
            ) {
                val dates = listOf("Today", "Tomorrow", "This Week", "Custom")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dates.forEach { date ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(date) }
                        )
                    }
                }
            }

            // LOCATION SECTION
            FilterSection(title = "Location", icon = Icons.Default.Place) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Option 1: Current Location
                    OutlinedButton(
                        onClick = { /* Map current location logic */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTeal),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use My Current Location", color = AppTeal)
                    }

                    // Option 2: Search Area
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search city, area or street") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal)
                    )
                }
            }

            // MEDIA TOGGLE
            FilterSection(title = "Media", icon = Icons.Default.Image) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show only with Image / Video", fontSize = 14.sp)
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(checkedThumbColor = AppTeal, checkedTrackColor = AppTeal.copy(alpha = 0.3f))
                    )
                }
            }

            // SORT BY
            FilterSection(title = "Sort By", icon = Icons.AutoMirrored.Filled.Sort) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Newest First") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Oldest First") })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All", color = Color.Gray)
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = AppTeal)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFF3F4F6))
    }
}
