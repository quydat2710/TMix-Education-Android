package com.tmix.education.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel
import com.tmix.education.ui.viewmodel.NotificationViewModel
import com.tmix.education.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Student Dashboard Screen — Polished Edition
 * Brand colors preserved: Navy + Red
 * Added: smooth entrance animations, better card design, shimmer loading, timeline schedule
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

    LaunchedEffect(showSnackbar, latestNotification) {
        if (showSnackbar && latestNotification != null) {
            snackbarHostState.showSnackbar(
                "${latestNotification!!.title}: ${latestNotification!!.message}",
                duration = SnackbarDuration.Short
            )
            notificationViewModel?.dismissSnackbar()
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { isRefreshing = state.isLoading }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TMIX", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = TMixRed)
                        Text(
                            "Xin chào, ${state.user?.name ?: state.student?.name ?: "Học sinh"}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onNotificationClick()
                        notificationViewModel?.refreshUnreadCount()
                    }) {
                        BadgedBox(badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = TMixRed) {
                                    Text(if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Notifications, "Thông báo")
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (state.isLoading && state.classes.isEmpty()) {
            // ========== SHIMMER SKELETON LOADING ==========
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fixed ambiguity: using 'item { repeat(3) { ... } }' instead of 'items(3)'
                item { ShimmerBox(width = 300.dp, height = 120.dp, cornerRadius = 16.dp) }
                item { SkeletonStatsRow() }
                item { repeat(3) { SkeletonCard(Modifier.padding(bottom = 12.dp)) } }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error message
                state.error?.let { error ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = ErrorLight)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, null, tint = Error)
                                Spacer(Modifier.width(8.dp))
                                Text(error, color = Error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Banner
                item {
                    SlideInFromBottom(index = 0) {
                        BannerCarousel(Modifier.fillMaxWidth())
                    }
                }

                // Stats Row — improved with icons + elevation
                item {
                    SlideInFromBottom(index = 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val classCount = state.classes.size.toString()
                            val attendanceRate = state.attendanceStats?.let {
                                if (it.total > 0) "${(it.present * 100 / it.total)}%" else "0%"
                            } ?: "N/A"

                            StatCard(Modifier.weight(1f), "Số lớp", classCount, Icons.Filled.MenuBook, TMixNavy)
                            StatCard(Modifier.weight(1f), "Kiểm tra", state.upcomingTests.toString(), Icons.Filled.Quiz, TMixRed)
                            StatCard(Modifier.weight(1f), "Điểm danh", attendanceRate, Icons.Filled.CheckCircle, Success)
                        }
                    }
                }

                // My Classes Section Header
                item {
                    SlideInFromBottom(index = 2) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.School, null, Modifier.size(20.dp), tint = TMixNavy)
                                Spacer(Modifier.width(8.dp))
                                Text("Lớp học của tôi", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                            }
                            if (state.isLoading) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }

                // Class Cards — gradient + badge icon
                item {
                    SlideInFromBottom(index = 3) {
                        if (state.classes.isEmpty() && !state.isLoading) {
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Filled.School, null, Modifier.size(48.dp), tint = TextSecondary)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Chưa có lớp học nào", color = TextSecondary)
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = onCourseClick) {
                                        Text("Đăng ký khóa học", color = TMixRed, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                itemsIndexed(state.classes) { index, classInfo ->
                                    val classId = classInfo.classInfo?.id ?: ""
                                    val progress = state.classProgress[classId] ?: 0f
                                    FadeInScale(delay = index * 80) {
                                        ClassCard(
                                            name = classInfo.classInfo?.name ?: "Lớp học",
                                            teacher = classInfo.classInfo?.teacher?.name ?: "Giáo viên",
                                            progress = progress,
                                            onClick = { classInfo.classInfo?.id?.let { onClassClick(it) } }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Schedule Section Header
                item {
                    SlideInFromBottom(index = 4) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, null, Modifier.size(20.dp), tint = TMixNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Lịch học hôm nay", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Schedule Cards — with timeline
                if (state.schedule.isEmpty() && !state.isLoading) {
                    item {
                        SlideInFromBottom(index = 5) {
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Filled.EventBusy, null, Modifier.size(48.dp), tint = TextSecondary)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Hôm nay không có lịch học 🎉", color = TextSecondary)
                                }
                            }
                        }
                    }
                } else {
                    val filteredSchedule = state.schedule.filter { it.className != null }
                    itemsIndexed(filteredSchedule) { index, scheduleItem ->
                        SlideInFromBottom(index = 5 + index) {
                            ScheduleCard(
                                time = "${scheduleItem.startTime ?: "N/A"} - ${scheduleItem.endTime ?: "N/A"}",
                                className = scheduleItem.className ?: "Lớp học",
                                room = scheduleItem.room ?: "Chưa xác định",
                                isFirst = index == 0,
                                isLast = index == filteredSchedule.size - 1
                            )
                        }
                    }
                }

                // Quick Actions — improved with filled cards
                item {
                    SlideInFromBottom(index = 8) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, null, Modifier.size(20.dp), tint = TMixNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Thao tác nhanh", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    SlideInFromBottom(index = 9) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(Modifier.weight(1f), Icons.Filled.School,
                                "Khóa học", "Đăng ký mới", TMixNavy, onClick = onCourseClick)
                            QuickActionCard(Modifier.weight(1f), Icons.Filled.MenuBook,
                                "Tài liệu", "Ôn tập", TMixRed, onClick = onMaterialsClick)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// POLISHED COMPONENTS (brand-consistent)
// =====================================================

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = color.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            AnimatedCounterText(
                valueString = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)))
                .padding(16.dp)
        ) {
            Column {
                // Icon badge
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MenuBook, null, Modifier.size(18.dp), tint = Color.White)
                }

                Spacer(Modifier.height(10.dp))

                Text(name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(teacher, style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.75f), maxLines = 1)

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(TMixShapes.Chip),
                    color = TMixRed,
                    trackColor = Color.White.copy(0.25f)
                )
                Spacer(Modifier.height(4.dp))
                Text("${(progress * 100).toInt()}% Hoàn thành",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
fun ScheduleCard(
    time: String, className: String, room: String,
    isFirst: Boolean = false, isLast: Boolean = false
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // Timeline indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            if (!isFirst) Box(Modifier.width(2.dp).height(8.dp).background(TMixRed.copy(0.3f)))
            else Spacer(Modifier.height(8.dp))

            Box(Modifier.size(10.dp).clip(CircleShape).background(TMixRed),
                contentAlignment = Alignment.Center) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White))
            }

            if (!isLast) Box(Modifier.width(2.dp).height(52.dp).background(TMixRed.copy(0.3f)))
        }

        Spacer(Modifier.width(12.dp))

        // Card content
        Card(
            Modifier.weight(1f).padding(bottom = 8.dp),
            shape = TMixShapes.Card,
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(className, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(time, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(room, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector, label: String, subtitle: String,
    color: Color, onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            Modifier.fillMaxSize().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(22.dp), tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.75f))
            }
        }
    }
}

@Composable
fun AnimatedCounterText(
    valueString: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight? = null
) {
    val numberRegex = Regex("(\\d+)(.*)")
    val matchResult = numberRegex.matchEntire(valueString.trim())
    
    if (matchResult != null) {
        val targetNumber = matchResult.groupValues[1].toIntOrNull() ?: 0
        val suffix = matchResult.groupValues[2]
        
        var isTriggered by remember { mutableStateOf(false) }
        LaunchedEffect(targetNumber) {
            // Tiny delay to wait for the slide-in entrance animation to settle
            kotlinx.coroutines.delay(250)
            isTriggered = true
        }
        
        val animatedValue by animateIntAsState(
            targetValue = if (isTriggered) targetNumber else 0,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            label = "counter"
        )
        
        Text(
            text = "$animatedValue$suffix",
            style = style,
            color = color,
            fontWeight = fontWeight,
            modifier = modifier
        )
    } else {
        // Fallback for non-numeric (e.g. "N/A")
        Text(
            text = valueString,
            style = style,
            color = color,
            fontWeight = fontWeight,
            modifier = modifier
        )
    }
}
