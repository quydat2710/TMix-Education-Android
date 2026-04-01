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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
                    // Summary Header
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)))
                                .padding(vertical = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${classes.size}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Tổng số lớp", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                                }
                                
                                // Vách ngăn mờ (Translucent Divider)
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(44.dp)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val activeCount = classes.count { 
                                        val prog = state.classProgress[it.classInfo.id] ?: 0f
                                        it.isActive && prog < 1f && it.classInfo.status != "closed"
                                    }
                                    Text("$activeCount", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TMixRed)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Đang học", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                                }
                            }
                        }
                    }

                    items(classes) { classItem ->
                        val progress = state.classProgress[classItem.classInfo.id] ?: 0f
                        val isFinished = progress >= 1f || classItem.classInfo.status == "closed"
                        StudentClassDetailCard(
                            classItem = classItem, 
                            isFinished = isFinished, 
                            onClick = { onClassClick(classItem.classInfo.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClassDetailCard(classItem: StudentClassInfo, isFinished: Boolean = false, onClick: () -> Unit = {}) {
    val classInfo = classItem.classInfo
    val teacherName = classInfo.teacher?.name ?: "Chưa phân công"
    val schedule = classInfo.schedule?.let { sched ->
        val days = sched.daysOfWeek?.map { dayStr ->
            val d = dayStr.trim()
            if (d == "0" || d == "CN") "CN" else "T$d"
        }?.joinToString(", ") ?: ""
        val time = sched.timeSlots?.let { "${it.startTime}" } ?: ""
        if (days.isNotEmpty() && time.isNotEmpty()) "$days - $time" else days.ifEmpty { time }
    } ?: "Chưa có lịch"
    val room = classInfo.room ?: "Chưa có phòng"
    
    val isCurrentlyLearning = classItem.isActive && !isFinished

    Card(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Thanh Accent bên trái giống thẻ Lịch Học
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (isCurrentlyLearning) TMixNavy else MaterialTheme.colorScheme.surfaceVariant)
            )
            
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                // Header Row: Tiêu đề lớp & Trạng thái
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = classInfo.name, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Surface(
                        color = if (isCurrentlyLearning) SuccessLight else ErrorLight,
                        shape = TMixShapes.Chip
                    ) {
                        Text(
                            if (isCurrentlyLearning) "Đang học" else "Kết thúc",
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentlyLearning) Success else Error
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Dòng 2: Thời gian học (Pill) và Phòng (Pill) - Lấy cảm hứng từ Lịch Học
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lịch Học (Pill)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            schedule,
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    // Phòng
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Room, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(room, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Dòng 3: Giáo viên & Giảm giá / Mũi tên
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    // Giảng viên
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(teacherName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    // Cột bên phải chứa Giảm giá & Nút đi tiếp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (classItem.discountPercent > 0) {
                            Surface(
                                color = WarningLight,
                                shape = TMixShapes.Chip
                            ) {
                                Text(
                                    "Giảm ${classItem.discountPercent.toInt()}%",
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Warning
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Xem chi tiết", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
