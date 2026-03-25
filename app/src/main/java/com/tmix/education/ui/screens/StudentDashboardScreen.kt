package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel
import com.tmix.education.ui.viewmodel.NotificationViewModel
import com.tmix.education.ui.components.BannerCarousel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Student Dashboard Screen
 * Connected to StudentDashboardViewModel for real data from Backend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: StudentDashboardViewModel = viewModel(),
    notificationViewModel: NotificationViewModel? = null,
    onNotificationClick: () -> Unit = {},
    onClassClick: (String) -> Unit = {},
    onCourseClick: () -> Unit = {},
    onMaterialsClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val unreadCount by (notificationViewModel?.unreadCount ?: MutableStateFlow(0)).collectAsState()
    val showSnackbar by (notificationViewModel?.showSnackbar ?: MutableStateFlow(false)).collectAsState()
    val latestNotification by (notificationViewModel?.latestNotification ?: MutableStateFlow(null)).collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show Snackbar when new notification arrives
    LaunchedEffect(showSnackbar, latestNotification) {
        if (showSnackbar && latestNotification != null) {
            snackbarHostState.showSnackbar(
                message = "${latestNotification!!.title}: ${latestNotification!!.message}",
                duration = SnackbarDuration.Short
            )
            notificationViewModel?.dismissSnackbar()
        }
    }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.isLoading) {
        isRefreshing = state.isLoading
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TMIX", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TMixRed)
                        Text(
                            text = "Xin chào, ${state.user?.name ?: state.student?.name ?: "Học sinh"}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onNotificationClick()
                        notificationViewModel?.refreshUnreadCount()
                    }) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = TMixRed) {
                                        Text(
                                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, "Thông báo")
                        }
                    }
                }
            )
        }
    ) { padding ->
        
        if (state.isLoading && state.classes.isEmpty()) {
            // Show loading indicator
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TMixRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error message
                state.error?.let { error ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = Error)
                                Spacer(Modifier.width(8.dp))
                                Text(error, color = Error)
                            }
                        }
                    }
                }
                
                // Banner carousel
                item {
                    BannerCarousel(Modifier.fillMaxWidth())
                }
                
                // Stats Row
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val classCount = state.classes.size.toString()
                        val attendanceRate = state.attendanceStats?.let {
                            if (it.total > 0) "${(it.present * 100 / it.total)}%" else "0%"
                        } ?: "N/A"
                        
                        StatCard(Modifier.weight(1f), "Số lớp", classCount, Icons.Filled.MenuBook, TMixNavy)
                        StatCard(Modifier.weight(1f), "Kiểm tra sắp tới", state.upcomingTests.toString(), Icons.Filled.Quiz, TMixRed)
                        StatCard(Modifier.weight(1f), "Điểm danh", attendanceRate, Icons.Filled.CheckCircle, Success)
                    }
                }
                
                // My Classes Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lớp học của tôi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                
                item {
                    if (state.classes.isEmpty() && !state.isLoading) {
                        // Empty state
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.School, null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Text("Chưa có lớp học nào", color = TextSecondary)
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.classes) { classInfo ->
                                val classId = classInfo.classInfo?.id ?: ""
                                val progress = state.classProgress[classId] ?: 0f
                                ClassCard(
                                    name = classInfo.classInfo?.name ?: "Lớp học",
                                    teacher = classInfo.classInfo?.teacher?.name ?: "Giáo viên",
                                    progress = progress,
                                    onClick = { 
                                        classInfo.classInfo?.id?.let { onClassClick(it) }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Today's Schedule Section
                item {
                    Text("Lịch học hôm nay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                
                if (state.schedule.isEmpty() && !state.isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.EventBusy, null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Text("Hôm nay không có lịch học", color = TextSecondary)
                            }
                        }
                    }
                } else {
                    items(state.schedule.filter { it.className != null }) { scheduleItem ->
                        ScheduleCard(
                            time = "${scheduleItem.startTime ?: "N/A"} - ${scheduleItem.endTime ?: "N/A"}",
                            className = scheduleItem.className ?: "Lớp học",
                            room = scheduleItem.room ?: "Chưa xác định"
                        )
                    }
                }
                
                // Quick actions
                item {
                    Text("Thao tác nhanh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCourseClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TMixNavy),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = TMixShapes.Button,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Filled.School, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Khóa học", fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = onMaterialsClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TMixNavy),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = TMixShapes.Button,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Filled.MenuBook, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tài liệu", fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, shape = TMixShapes.Card) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Icon(icon, null, Modifier.size(14.dp).padding(start = 4.dp), tint = color)
            }
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassCard(name: String, teacher: String, progress: Float, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = TMixShapes.Card,
        colors = CardDefaults.cardColors(containerColor = TMixNavy)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(teacher, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = TMixRed,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Text("${(progress * 100).toInt()}% Hoàn thành", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ScheduleCard(time: String, className: String, room: String) {
    Card(shape = TMixShapes.Card) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val times = time.split(" - ")
                Text(times[0], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(times.getOrElse(1) { "" }, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.width(16.dp))
            Box(Modifier.width(3.dp).height(40.dp).background(TMixRed, TMixShapes.Chip))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(className, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("📍 $room", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
