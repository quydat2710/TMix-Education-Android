package com.tmix.education.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.components.TTSButton
import com.tmix.education.ui.components.speakWithTTS
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onBack: () -> Unit = {}
) {
    val apiService = remember { ApiConfig.getApiService() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    role = "assistant",
                    content = "Xin chào! 👋 Mình là trợ lý AI của TMix Education.\n\n• 📖 Giải thích ngữ pháp\n• ✍️ Sửa lỗi câu\n• 🗣️ Luyện hội thoại\n• 🔄 Dịch Anh ↔ Việt\n• 💡 Mẹo thi IELTS, TOEIC\n\nHãy hỏi mình bất cứ điều gì nhé! 😊"
                )
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Auto-read toggle with SharedPreferences persistence
    val context = LocalContext.current
    var autoRead by remember {
        mutableStateOf(
            context.getSharedPreferences("tmix_prefs", 0)
                .getBoolean("chatbot_autoread", false)
        )
    }

    // Persist auto-read preference
    LaunchedEffect(autoRead) {
        context.getSharedPreferences("tmix_prefs", 0)
            .edit().putBoolean("chatbot_autoread", autoRead).apply()
    }

    // Auto-read: speak latest AI message when it arrives
    var prevMessageCount by remember { mutableIntStateOf(messages.size) }
    LaunchedEffect(messages.size) {
        if (autoRead && messages.size > prevMessageCount) {
            val lastMsg = messages.last()
            if (lastMsg.role == "assistant" && !lastMsg.content.startsWith("\u274c")) {
                delay(300)
                speakWithTTS(context, lastMsg.content)
            }
        }
        prevMessageCount = messages.size
    }

    // Auto scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, null, Modifier.size(20.dp), tint = Color.White)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("TMix AI Assistant", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("• Online", style = MaterialTheme.typography.bodySmall, color = Color(0xFF16A34A))
                        }
                    }
                },
                actions = {
                    // Auto-read toggle button
                    Surface(
                        onClick = { autoRead = !autoRead },
                        shape = RoundedCornerShape(16.dp),
                        color = if (autoRead) Color(0xFF7c3aed).copy(alpha = 0.15f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, if (autoRead) Color(0xFF7c3aed) else Color(0xFFe2e8f0)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (autoRead) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
                                contentDescription = "Auto-read",
                                modifier = Modifier.size(16.dp),
                                tint = if (autoRead) Color(0xFF7c3aed) else Color(0xFF9ca3af)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (autoRead) "🔊 Auto" else "🔇 Auto",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (autoRead) Color(0xFF7c3aed) else Color(0xFF9ca3af)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập câu hỏi tiếng Anh...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7c3aed),
                            unfocusedBorderColor = Color(0xFFe2e8f0)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val text = input.trim()
                            if (text.isNotEmpty() && !isLoading) {
                                val userMsg = ChatMessage(role = "user", content = text)
                                messages = messages + userMsg
                                input = ""
                                isLoading = true

                                coroutineScope.launch {
                                    try {
                                        val history = messages
                                            .filter { it.role == "user" || it.role == "assistant" }
                                            .takeLast(20)
                                            .map { mapOf("role" to it.role, "content" to it.content) }

                                        val body = mapOf(
                                            "message" to text,
                                            "history" to history
                                        )
                                        val response = apiService.sendChatMessage(body)
                                        if (response.isSuccessful) {
                                            val reply = (response.body() as? Map<*, *>)
                                                ?.let { (it["data"] as? Map<*, *>)?.get("reply") as? String }
                                            messages = messages + ChatMessage(
                                                role = "assistant",
                                                content = reply ?: "❌ Không nhận được phản hồi"
                                            )
                                        } else {
                                            messages = messages + ChatMessage(
                                                role = "assistant",
                                                content = "❌ Lỗi: ${response.code()}"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        messages = messages + ChatMessage(
                                            role = "assistant",
                                            content = "❌ Lỗi kết nối: ${e.message}"
                                        )
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = input.isNotBlank() && !isLoading,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF7c3aed)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Gửi", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(Color(0xFF7c3aed)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, null, Modifier.size(18.dp), tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) TMixNavy else Color(0xFFF3F4F6)
                        )
                    ) {
                        Text(
                            msg.content,
                            Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = if (!isUser) 4.dp else 12.dp),
                            color = if (isUser) Color.White else Color(0xFF1F2937),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                        // TTS button for AI messages
                        if (!isUser) {
                            Row(
                                Modifier.padding(start = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TTSButton(
                                    text = msg.content,
                                    tint = Color(0xFF7c3aed),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    if (isUser) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(TMixNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = Color.White)
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                item {
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(Color(0xFF7c3aed)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp), tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(16.dp), color = Color(0xFF7c3aed), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Đang suy nghĩ...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
