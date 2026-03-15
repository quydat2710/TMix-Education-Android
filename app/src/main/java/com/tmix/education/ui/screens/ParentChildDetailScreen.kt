package com.tmix.education.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tmix.education.data.model.*
import com.tmix.education.data.repository.StudentRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.DayOfWeek
import java.util.Locale

/**
 * Parent Child Detail Screen
 * Shows detailed information about a specific child: classes, schedule, attendance, payments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildDetailScreen(
    childId: String,
    onBack: () -> Unit = {}
) {
    val studentRepository = remember { StudentRepository() }
    val scope = rememberCoroutineScope()
    
    var student by remember { mutableStateOf<Student?>(null) }
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var attendanceStats by remember { mutableStateOf<AttendanceStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(childId) {
        scope.launch {
            isLoading = true
            // Load student info
            studentRepository.getStudent(childId).onSuccess { s ->
                student = s
            }.onFailure { error = it.message }
            
            // Load payments
            studentRepository.getPayments(childId).onSuccess { p ->
                payments = p
            }
            
            // Load attendance
            studentRepository.getAttendanceStats(childId).onSuccess { a ->
                attendanceStats = a
            }
            
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student?.name ?: "Chi tiết học sinh") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
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
                        Text(error ?: "Có lỗi xảy ra", color = TextSecondary)
                    }
                }
            }
            student != null -> {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Student Info Card
                    item {
                        Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = TMixNavy.copy(0.1f), modifier = Modifier.size(56.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = TMixNavy)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(student!!.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(student!!.email ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        if (student!!.phone != null) {
                                            Text(student!!.phone!!, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Attendance Stats
                    attendanceStats?.let { stats ->
                        item {
                            Text("Điểm danh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatMiniCard("Tổng", "${stats.total}", TMixNavy, Modifier.weight(1f))
                                StatMiniCard("Có mặt", "${stats.present}", Success, Modifier.weight(1f))
                                StatMiniCard("Vắng", "${stats.absent}", Error, Modifier.weight(1f))
                                StatMiniCard("Trễ", "${stats.late}", Warning, Modifier.weight(1f))
                            }
                        }
                    }
                    
                    // Classes
                    val classes = student!!.classes
                    if (!classes.isNullOrEmpty()) {
                        item {
                            Text("Lớp học (${classes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(classes) { enrollment ->
                            val classInfo = enrollment.classInfo
                            Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(1.dp)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(classInfo.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = when (classInfo.status) {
                                                "active" -> Success.copy(0.1f)
                                                "upcoming" -> Info.copy(0.1f)
                                                else -> TextSecondary.copy(0.1f)
                                            },
                                            shape = TMixShapes.Chip
                                        ) {
                                            Text(
                                                when (classInfo.status) {
                                                    "active" -> "Đang học"
                                                    "upcoming" -> "Sắp mở"
                                                    else -> "Đã kết thúc"
                                                },
                                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = when (classInfo.status) {
                                                    "active" -> Success
                                                    "upcoming" -> Info
                                                    else -> TextSecondary
                                                }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    
                                    // Schedule info
                                    classInfo.schedule?.let { schedule ->
                                        val dayNames = schedule.daysOfWeek?.joinToString(", ") { dayNum ->
                                            when (dayNum) {
                                                "0" -> "CN"
                                                "1" -> "T2"
                                                "2" -> "T3"
                                                "3" -> "T4"
                                                "4" -> "T5"
                                                "5" -> "T6"
                                                "6" -> "T7"
                                                else -> dayNum
                                            }
                                        } ?: ""
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = TextSecondary)
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "$dayNames · ${schedule.timeSlots?.startTime ?: ""} - ${schedule.timeSlots?.endTime ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (classInfo.teacher != null) {
                                            Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = TextSecondary)
                                            Spacer(Modifier.width(4.dp))
                                            Text(classInfo.teacher!!.name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                            Spacer(Modifier.width(16.dp))
                                        }
                                        if (classInfo.room != null) {
                                            Icon(Icons.Default.Room, null, Modifier.size(14.dp), tint = TextSecondary)
                                            Spacer(Modifier.width(4.dp))
                                            Text(classInfo.room!!, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Payments
                    if (payments.isNotEmpty()) {
                        item {
                            Text("Học phí", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(payments.take(5)) { payment ->
                            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(1.dp)) {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Tháng ${payment.month}/${payment.year}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            formatter.format(payment.totalAmount),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Surface(
                                        color = if (payment.isPaid) Success.copy(0.1f) else Warning.copy(0.1f),
                                        shape = TMixShapes.Chip
                                    ) {
                                        Text(
                                            if (payment.isPaid) "Đã thanh toán" else "Chưa thanh toán",
                                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (payment.isPaid) Success else Warning
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(shape = TMixShapes.Card, modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(0.08f))) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(0.8f))
        }
    }
}
