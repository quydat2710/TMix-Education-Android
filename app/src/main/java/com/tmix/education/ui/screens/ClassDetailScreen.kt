package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tmix.education.data.model.ClassInfo
import com.tmix.education.data.model.Session
import com.tmix.education.data.repository.ClassRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Class Detail Screen
 * Connected to real backend data via ClassRepository
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    classId: String = "",
    onBack: () -> Unit = {}
) {
    val classRepository = remember { ClassRepository() }
    var classInfo by remember { mutableStateOf<ClassInfo?>(null) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load data from API
    LaunchedEffect(classId) {
        if (classId.isNotEmpty()) {
            isLoading = true
            error = null

            // Load class info
            val classResult = classRepository.getClass(classId)
            classResult.onSuccess { info ->
                classInfo = info
            }.onFailure { e ->
                error = e.message
            }

            // Load class sessions
            val sessionsResult = classRepository.getClassSessions(classId, limit = 50)
            sessionsResult.onSuccess { sessionList ->
                sessions = sessionList.sortedBy { it.date }
            }

            isLoading = false
        }
    }

    val className = classInfo?.name ?: "Lớp học"
    val teacher = classInfo?.teacher?.name ?: "Chưa phân công"
    val schedule = classInfo?.schedule?.let { sched ->
        val days = sched.daysOfWeek?.joinToString(", ") ?: ""
        val time = sched.timeSlots?.let { "${it.startTime} - ${it.endTime}" } ?: ""
        if (days.isNotEmpty() && time.isNotEmpty()) "$days - $time" else days.ifEmpty { time }
    } ?: "Chưa có lịch"
    val room = classInfo?.room ?: "Chưa có phòng"

    // Calculate progress from sessions
    val totalSessions = sessions.size
    val completedSessions = sessions.count { !it.isActive }
    val progress = if (totalSessions > 0) completedSessions.toFloat() / totalSessions else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(className) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TMixNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                        Spacer(Modifier.height(16.dp))
                        Text(error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    error = null
                                    classRepository.getClass(classId).onSuccess { classInfo = it }.onFailure { error = it.message }
                                    classRepository.getClassSessions(classId, limit = 50).onSuccess { sessions = it.sortedBy { s -> s.date } }
                                    isLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Class info card
                    item {
                        Card(
                            shape = TMixShapes.Card,
                            colors = CardDefaults.cardColors(containerColor = TMixNavy)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(56.dp).clip(CircleShape).background(TMixRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MenuBook, null, Modifier.size(28.dp), tint = Color.White)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(className, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(teacher, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), tint = Color.White.copy(0.8f))
                                        Spacer(Modifier.width(4.dp))
                                        Text(schedule, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Room, null, Modifier.size(16.dp), tint = Color.White.copy(0.8f))
                                        Spacer(Modifier.width(4.dp))
                                        Text(room, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                                    }
                                }

                                if (totalSessions > 0) {
                                    Spacer(Modifier.height(16.dp))
                                    Text("Tiến độ: $completedSessions/$totalSessions buổi (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = TMixRed,
                                        trackColor = Color.White.copy(0.3f)
                                    )
                                }
                            }
                        }
                    }

                    // Sessions section header
                    if (sessions.isNotEmpty()) {
                        item {
                            Text("Danh sách buổi học (${sessions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }

                        itemsIndexed(sessions) { index, session ->
                            SessionCard(index + 1, session)
                        }
                    } else {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.EventNote, null, Modifier.size(48.dp), tint = TextSecondary.copy(0.5f))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Chưa có buổi học nào", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(number: Int, session: Session) {
    val statusText = if (session.isActive) "Sắp tới" else "Đã hoàn thành"
    val statusColor = if (session.isActive) Warning else Success

    Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Session number indicator
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(statusColor.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!session.isActive) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(24.dp), tint = statusColor)
                } else {
                    Text("$number", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text("Buổi $number", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(session.date.take(10), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(color = statusColor.copy(0.1f), shape = TMixShapes.Chip) {
                    Text(statusText, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
                // Show attendance count if available
                val attendanceCount = session.attendances?.size ?: 0
                if (attendanceCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("$attendanceCount HS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}
