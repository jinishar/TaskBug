package com.example.taskbug.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppTeal = Color(0xFFC1603A)
private val AppBackground = Color(0xFFFAF6F1)
private val AppSurface = Color.White
private val TextPrimary = Color(0xFF1E1712)
private val TextSecondary = Color(0xFFA08878)
private val AppBorder = Color(0xFFEAE0D8)

data class ChatThread(
    val id: Int,
    val otherUserName: String,
    val taskTitle: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val avatarColor: Color
)

data class Message(
    val id: Int,
    val text: String,
    val isMine: Boolean,
    val time: String
)

val mockThreads = listOf(
    ChatThread(1, "Arjun S.", "Help me move a sofa", "Sounds good! I'll be there at 5.", "2m ago", 2, Color(0xFFC1603A)),
    ChatThread(2, "Priya M.", "Grocery run from Dmart", "Can you share the list?", "15m ago", 0, Color(0xFF7C3AED)),
    ChatThread(3, "Ramesh K.", "Tutor for Math – 10th grade", "What topics does your son need?", "1h ago", 1, Color(0xFFD97706)),
    ChatThread(4, "Sneha P.", "Fix leaky kitchen tap", "I've fixed it! Please confirm.", "3h ago", 0, Color(0xFFDC2626)),
    ChatThread(5, "Vikram N.", "Deliver documents to Whitefield", "Package received, thanks!", "Yesterday", 0, Color(0xFF0369A1)),
)

val mockMessages = listOf(
    Message(1, "Hi! I saw your task about moving the sofa.", false, "4:45 PM"),
    Message(2, "Hey! Yes, I need help moving it from ground to 2nd floor.", true, "4:46 PM"),
    Message(3, "I can do it. I'm nearby. Does 5 PM work?", false, "4:47 PM"),
    Message(4, "Perfect! The address is 12B, 5th Cross, Koramangala.", true, "4:48 PM"),
    Message(5, "Got it. I'll bring a friend too so it's quicker.", false, "4:49 PM"),
    Message(6, "Great, thank you! The payment is already in escrow.", true, "4:50 PM"),
    Message(7, "Sounds good! I'll be there at 5.", false, "4:52 PM"),
)

@Composable
fun ChatScreen() {
    var openThread by remember { mutableStateOf<ChatThread?>(null) }

    if (openThread == null) {
        ChatListScreen(threads = mockThreads, onThreadClick = { openThread = it })
    } else {
        ChatConversationScreen(thread = openThread!!, onBack = { openThread = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(threads: List<ChatThread>, onThreadClick: (ChatThread) -> Unit) {
    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppSurface)
            )
        }
    ) { padding ->
        if (threads.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ChatBubbleOutline, null, Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Text("No messages yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Text("Accept or post a task to start chatting", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(threads) { thread ->
                    ChatThreadItem(thread, onClick = { onThreadClick(thread) })
                }
            }
        }
    }
}

@Composable
fun ChatThreadItem(thread: ChatThread, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(AppSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(thread.avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(thread.otherUserName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(thread.otherUserName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(thread.time, fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(2.dp))
            Text(thread.taskTitle, fontSize = 12.sp, color = AppTeal, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(thread.lastMessage, fontSize = 13.sp, color = TextSecondary, maxLines = 1, modifier = Modifier.weight(1f))
                if (thread.unreadCount > 0) {
                    Box(
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(AppTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(thread.unreadCount.toString(), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = AppBorder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(thread: ChatThread, onBack: () -> Unit) {
    val messages = remember { mockMessages.toMutableStateList() }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(thread.avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(thread.otherUserName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(thread.otherUserName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(thread.taskTitle, fontSize = 11.sp, color = AppTeal)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppSurface)
            )
        },
        bottomBar = {
            Surface(color = AppSurface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message…") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTeal, unfocusedBorderColor = AppBorder)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                messages.add(Message(messages.size + 1, inputText.trim(), true, "Now"))
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(AppTeal)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (message.isMine) 16.dp else 4.dp,
                            bottomEnd = if (message.isMine) 4.dp else 16.dp
                        )
                    )
                    .background(if (message.isMine) AppTeal else AppSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    message.text,
                    color = if (message.isMine) Color.White else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(message.time, fontSize = 11.sp, color = TextSecondary)
        }
    }
}