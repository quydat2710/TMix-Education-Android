package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.StudentClassInfo
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel

/**
 * Student Classes Screen
 * List all enrolled classes with progress - connected to real backend data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClassesScreen(
    viewModel: StudentDashboardViewModel = viewModel(),
    onClassClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val classes = state.classes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lớp học của tôi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            state.error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                        Spacer(Modifier.height(16.dp))
                        Text(state.error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            classes.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có lớp học nào", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text("Liên hệ trung tâm để đăng ký lớp", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary
                    item {
                        Card(
                            shape = TMixShapes.Card,
                            colors = CardDefaults.cardColors(containerColor = TMixNavy)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${classes.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Tổng lớp", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val activeCount = classes.count { it.isActive }
                                    Text("$activeCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TMixRed)
                                    Text("Đang học", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                                }
                            }
                        }
                    }

                    items(classes) { classItem ->
                        StudentClassDetailCard(classItem, onClick = { onClassClick(classItem.classInfo.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClassDetailCard(classItem: StudentClassInfo, onClick: () -> Unit = {}) {
    val classInfo = classItem.classInfo
    val teacherName = classInfo.teacher?.name ?: "Chưa phân công"
    val schedule = classInfo.schedule?.let { sched ->
        val days = sched.daysOfWeek?.joinToString(", ") ?: ""
        val time = sched.timeSlots?.let { "${it.startTime}" } ?: ""
        if (days.isNotEmpty() && time.isNotEmpty()) "$days - $time" else days.ifEmpty { time }
    } ?: "Chưa có lịch"
    val room = classInfo.room ?: "Chưa có phòng"

    Card(
        onClick = onClick,
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(classInfo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(teacherName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Surface(
                    color = if (classItem.isActive) SuccessLight else ErrorLight,
                    shape = TMixShapes.Chip
                ) {
                    Text(
                        if (classItem.isActive) "Đang học" else "Kết thúc",
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (classItem.isActive) Success else Error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = TMixNavy)
                    Spacer(Modifier.width(4.dp))
                    Text(schedule, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Room, null, Modifier.size(14.dp), tint = TMixRed)
                    Spacer(Modifier.width(4.dp))
                    Text(room, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (classItem.discountPercent > 0) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = WarningLight,
                    shape = TMixShapes.Chip
                ) {
                    Text(
                        "Giảm ${classItem.discountPercent.toInt()}%",
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Warning
                    )
                }
            }
        }
    }
}
