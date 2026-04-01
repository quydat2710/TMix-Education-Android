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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel
import com.tmix.education.ui.viewmodel.NotificationViewModel
import com.tmix.education.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Student Dashboard — Premium Redesign
 * Clean & minimalist with depth, soft shadows, gradient accents
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val avatarUrl = state.user?.avatar ?: state.student?.avatar
                        val userName = state.user?.name ?: state.student?.name ?: "Học sinh"
                        // Avatar with gradient ring
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(TMixNavy, TMixNavySoft))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(CardSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!avatarUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        Modifier.fillMaxSize().background(TMixNavy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            userName.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("TMIX", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = TMixRed,
                                letterSpacing = 2.sp)
                            Text(
                                "Xin chào, $userName!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
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
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { ShimmerBox(width = 300.dp, height = 120.dp, cornerRadius = 20.dp) }
                item { SkeletonStatsRow() }
                item { repeat(3) { SkeletonCard(Modifier.padding(bottom = 12.dp)) } }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Error
                state.error?.let { error ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorLight),
                            shape = TMixShapes.Card
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, null, Modifier.size(20.dp), tint = Error)
                                Spacer(Modifier.width(10.dp))
                                Text(error, color = Error, style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium)
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

                // Stats Row — gradient icon circle + pastel tint background
                item {
                    SlideInFromBottom(index = 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val classCount = state.classes.size.toString()
                            val attendanceRate = state.attendanceStats?.let {
                                if (it.total > 0) "${(it.present * 100 / it.total)}%" else "0%"
                            } ?: "N/A"

                            StatCard(Modifier.weight(1f), "Số lớp", classCount,
                                Icons.Filled.MenuBook, TMixNavy, NavyTint)
                            StatCard(Modifier.weight(1f), "Kiểm tra", state.upcomingTests.toString(),
                                Icons.Filled.Quiz, TMixRed, RedTint)
                            StatCard(Modifier.weight(1f), "Điểm danh", attendanceRate,
                                Icons.Filled.CheckCircle, Success, SuccessTint)
                        }
                    }
                }

                // My Classes Section
                item {
                    SlideInFromBottom(index = 2) {
                        SectionHeader(
                            icon = Icons.Filled.School,
                            title = "Lớp học của tôi",
                            isLoading = state.isLoading
                        )
                    }
                }

                // Class Cards
                item {
                    SlideInFromBottom(index = 3) {
                        if (state.classes.isEmpty() && !state.isLoading) {
                            EmptyCard(
                                icon = Icons.Filled.School,
                                message = "Chưa có lớp học nào",
                                actionLabel = "Đăng ký khóa học",
                                onAction = onCourseClick
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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

                // Schedule Section
                item {
                    SlideInFromBottom(index = 4) {
                        SectionHeader(
                            icon = Icons.Filled.CalendarToday,
                            title = "Lịch học hôm nay"
                        )
                    }
                }

                // Schedule Cards
                if (state.schedule.isEmpty() && !state.isLoading) {
                    item {
                        SlideInFromBottom(index = 5) {
                            EmptyCard(
                                icon = Icons.Filled.EventBusy,
                                message = "Hôm nay không có lịch học 🎉"
                            )
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

                // Quick Actions
                item {
                    SlideInFromBottom(index = 8) {
                        SectionHeader(
                            icon = Icons.Filled.Bolt,
                            title = "Thao tác nhanh"
                        )
                    }
                }

                item {
                    SlideInFromBottom(index = 9) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            QuickActionCard(Modifier.weight(1f), Icons.Filled.School,
                                "Khóa học", "Đăng ký mới",
                                Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)),
                                onClick = onCourseClick)
                            QuickActionCard(Modifier.weight(1f), Icons.Filled.MenuBook,
                                "Tài liệu", "Ôn tập",
                                Brush.linearGradient(listOf(TMixRed, TMixRedSoft)),
                                onClick = onMaterialsClick)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// PREMIUM COMPONENTS
// =====================================================

/** Section header with icon */
@Composable
fun SectionHeader(icon: ImageVector, title: String, isLoading: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
        }
    }
}

/** Empty state card */
@Composable
fun EmptyCard(
    icon: ImageVector, message: String,
    actionLabel: String? = null, onAction: (() -> Unit)? = null
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = TMixShapes.CardLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = TMixRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Stat card with tinted background + gradient icon circle */
@Composable
fun StatCard(
    modifier: Modifier, label: String, value: String,
    icon: ImageVector, color: Color, tintBg: Color
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = if (isDark) color.copy(alpha = 0.1f) else tintBg
    Card(
        modifier = modifier,
        shape = TMixShapes.Card,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient icon circle
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = color)
            }
            Spacer(Modifier.height(8.dp))
            AnimatedCounterText(
                valueString = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Class card with gradient + depth */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassCard(name: String, teacher: String, progress: Float, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = TMixShapes.CardLarge,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)))
                .padding(18.dp)
        ) {
            Column {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MenuBook, null, Modifier.size(18.dp), tint = Color.White)
                }
                Spacer(Modifier.height(14.dp))
                Text(name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(teacher, style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f), maxLines = 1)
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp)
                        .clip(TMixShapes.Badge),
                    color = TMixRed,
                    trackColor = Color.White.copy(0.2f)
                )
                Spacer(Modifier.height(6.dp))
                Text("${(progress * 100).toInt()}% Hoàn thành",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.8f))
            }
        }
    }
}

/** Schedule card with timeline */
@Composable
fun ScheduleCard(
    time: String, className: String, room: String,
    isFirst: Boolean = false, isLast: Boolean = false
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // Timeline
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
            if (!isFirst) Box(Modifier.width(2.dp).height(8.dp).background(TMixRed.copy(0.2f)))
            else Spacer(Modifier.height(8.dp))
            // Gradient dot
            Box(
                Modifier.size(12.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(TMixRed, TMixRedDark))),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White))
            }
            if (!isLast) Box(Modifier.width(2.dp).height(56.dp).background(TMixRed.copy(0.2f)))
        }

        Spacer(Modifier.width(12.dp))

        Card(
            Modifier.weight(1f).padding(bottom = 8.dp),
            shape = TMixShapes.Card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(className, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(room, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Quick Action card with gradient brush */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector, label: String, subtitle: String,
    gradient: Brush, onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        shape = TMixShapes.CardLarge,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier.fillMaxSize().background(gradient).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(22.dp), tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(label, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.75f))
            }
        }
    }
}

/** Animated counter text */
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
            style = style, color = color, fontWeight = fontWeight,
            modifier = modifier
        )
    } else {
        Text(text = valueString, style = style, color = color,
            fontWeight = fontWeight, modifier = modifier)
    }
}
