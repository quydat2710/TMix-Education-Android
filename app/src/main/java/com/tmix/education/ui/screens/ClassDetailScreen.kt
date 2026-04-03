package com.tmix.education.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tmix.education.data.model.AttendanceStatus
import com.tmix.education.data.model.ClassInfo
import com.tmix.education.data.model.Session
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.data.repository.ClassRepository
import com.tmix.education.ui.components.ShimmerBox
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Class Detail Screen — Premium Redesign
 * Features: Gradient hero header, animated progress ring, glassmorphic cards,
 *           timeline session list, staggered animations, full dark mode support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    classId: String = "",
    onBack: () -> Unit = {}
) {
    val classRepository = remember { ClassRepository() }
    val authRepository = remember { AuthRepository() }
    val currentUserId = remember { authRepository.getCurrentUserId() ?: "" }
    var classInfo by remember { mutableStateOf<ClassInfo?>(null) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    // Load data from API
    LaunchedEffect(classId) {
        if (classId.isNotEmpty()) {
            isLoading = true
            error = null
            try {
                val classResult = classRepository.getClass(classId)
                classResult.onSuccess { info -> classInfo = info }.onFailure { e -> error = e.message }
                val sessionsResult = classRepository.getClassSessions(classId, limit = 50)
                sessionsResult.onSuccess { sessionList -> sessions = sessionList.sortedBy { it.date } }
            } catch (e: Exception) {
                error = "Không thể tải thông tin lớp: ${e.message}"
            }
            isLoading = false
        }
    }

    val className = classInfo?.name ?: "Lớp học"
    val teacher = classInfo?.teacher?.name ?: "Chưa phân công"
    val schedule = classInfo?.schedule?.let { sched ->
        val days = sched.daysOfWeek?.joinToString(", ") { dayNum ->
            when (dayNum.trim()) {
                "0" -> "CN"; "1" -> "T2"; "2" -> "T3"; "3" -> "T4"
                "4" -> "T5"; "5" -> "T6"; "6" -> "T7"; else -> dayNum
            }
        } ?: ""
        val time = sched.timeSlots?.let { "${it.startTime} - ${it.endTime}" } ?: ""
        if (days.isNotEmpty() && time.isNotEmpty()) "$days · $time" else days.ifEmpty { time }
    } ?: "Chưa có lịch"
    val room = classInfo?.room ?: "Chưa có phòng"

    val progress = classInfo?.schedule?.let { sched ->
        if (sched.startDate != null && sched.endDate != null) {
            try {
                val dateFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                val now = System.currentTimeMillis()
                val startMs = dateFormat.parse(sched.startDate.take(10))?.time ?: now
                val endMs = dateFormat.parse(sched.endDate.take(10))?.time ?: now
                val totalDuration = endMs - startMs
                if (totalDuration > 0) {
                    ((now - startMs).coerceIn(0, totalDuration).toFloat() / totalDuration).coerceIn(0f, 1f)
                } else 1f
            } catch (_: Exception) { 0f }
        } else 0f
    } ?: 0f
    val completedSessions = sessions.size

    // Adaptive colors
    val headerGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F1E33), Color(0xFF1A2840)))
    } else {
        Brush.verticalGradient(listOf(TMixNavy, TMixNavySoft))
    }
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBgAlt = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            // Enhanced TopAppBar with gradient and status
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(headerGradient)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                className,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (classInfo != null) {
                                Text(
                                    "GV: $teacher",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(0.7f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại",
                                tint = Color.White)
                        }
                    },
                    actions = {
                        // Status badge
                        val statusLabel = when (classInfo?.status) {
                            "active" -> "Đang học"
                            "completed" -> "Hoàn thành"
                            "cancelled" -> "Đã hủy"
                            else -> null
                        }
                        val statusBadgeColor = when (classInfo?.status) {
                            "active" -> Success
                            "completed" -> Info
                            "cancelled" -> Error
                            else -> Color.Transparent
                        }
                        if (statusLabel != null) {
                            Surface(
                                color = Color.White.copy(0.15f),
                                shape = TMixShapes.Badge,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(7.dp)
                                            .background(statusBadgeColor, CircleShape)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(0.9f)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            isLoading -> SkeletonLoadingState(Modifier.padding(padding))
            error != null -> ErrorState(
                modifier = Modifier.padding(padding),
                errorMessage = error ?: "Có lỗi xảy ra",
                onRetry = {
                    scope.launch {
                        isLoading = true
                        error = null
                        classRepository.getClass(classId).onSuccess { classInfo = it }.onFailure { error = it.message }
                        classRepository.getClassSessions(classId, limit = 50).onSuccess { sessions = it.sortedBy { s -> s.date } }
                        isLoading = false
                    }
                }
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ===== HERO HEADER CARD =====
                    item {
                        HeroHeaderCard(
                            className = className,
                            teacher = teacher,
                            schedule = schedule,
                            room = room,
                            status = classInfo?.status,
                            progress = progress,
                            completedSessions = completedSessions,
                            gradient = headerGradient,
                            isDark = isDark
                        )
                    }

                    // ===== INFO CARD =====
                    item {
                        InfoCard(
                            classInfo = classInfo,
                            cardBg = cardBg,
                            cardBgAlt = cardBgAlt,
                            textPrimary = textPrimary,
                            textSecondary = textSecondaryColor,
                            isDark = isDark
                        )
                    }

                    // ===== SESSION TIMELINE =====
                    if (sessions.isNotEmpty()) {
                        item {
                            StaggeredItem(index = 0) {
                                Text(
                                    "Danh sách buổi học (${sessions.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }

                        itemsIndexed(
                            items = sessions,
                            key = { _, session -> session.id }
                        ) { index, session ->
                            TimelineSessionCard(
                                index = index,
                                number = index + 1,
                                session = session,
                                isLast = index == sessions.lastIndex,
                                isDark = isDark,
                                currentStudentId = currentUserId
                            )
                        }
                    } else {
                        item {
                            EmptySessionsState(textSecondaryColor)
                        }
                    }

                    // Bottom spacer
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ====================================================================
// HERO HEADER CARD
// ====================================================================
@Composable
private fun HeroHeaderCard(
    className: String,
    teacher: String,
    schedule: String,
    room: String,
    status: String?,
    progress: Float,
    completedSessions: Int,
    gradient: Brush,
    isDark: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(500), label = "hero_alpha"
    )
    val scale by animateFloatAsState(
        if (visible) 1f else 0.95f,
        spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "hero_scale"
    )
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            },
        shape = TMixShapes.CardLarge,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 8.dp
        )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(gradient)
        ) {
            // Subtle decorative circles
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = 120.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.02f),
                    radius = 80.dp.toPx(),
                    center = Offset(size.width * 0.1f, size.height * 0.9f)
                )
            }

            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Animated progress ring as class icon
                        HeroProgressRing(
                            progress = progress,
                            className = className
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                className,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                teacher,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(0.8f)
                            )
                        }
                    }

                    // Status badge with glow
                    StatusBadge(status = status)
                }

                Spacer(Modifier.height(16.dp))

                // Schedule & Room info chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        icon = Icons.Default.Schedule,
                        text = schedule,
                        isDark = isDark
                    )
                    InfoChip(
                        icon = Icons.Default.Room,
                        text = room,
                        isDark = isDark
                    )
                }

                if (completedSessions > 0 || progress > 0) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tiến độ: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "$completedSessions buổi đã học",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.7f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Animated progress bar
                    AnimatedProgressBar(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroProgressRing(
    progress: Float,
    className: String
) {
    val animatedProgress by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(1200, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    Box(
        Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Track
            drawArc(
                color = Color.White.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = Color(0xFFFF6B7A), // TMixRedSoft for visibility on dark
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        // Book icon in center
        Icon(
            Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val statusText = when (status) {
        "active" -> "Đang học"
        "upcoming" -> "Sắp mở"
        "closed" -> "Đã kết thúc"
        else -> status ?: ""
    }
    val statusColor = when (status) {
        "active" -> Success
        "upcoming" -> Warning
        "closed" -> Error
        else -> TextSecondary
    }

    if (statusText.isNotEmpty()) {
        Surface(
            color = statusColor.copy(0.2f),
            shape = TMixShapes.Badge,
            shadowElevation = 2.dp
        ) {
            Text(
                statusText,
                Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    isDark: Boolean
) {
    Surface(
        color = Color.White.copy(alpha = if (isDark) 0.08f else 0.12f),
        shape = TMixShapes.Chip
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = Color.White.copy(0.8f))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.85f))
        }
    }
}

@Composable
private fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(1200, easing = FastOutSlowInEasing),
        label = "bar_progress"
    )

    // Shimmer effect on the bar
    val infiniteTransition = rememberInfiniteTransition(label = "bar_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Canvas(modifier.height(6.dp).clip(RoundedCornerShape(3.dp))) {
        val width = size.width
        val height = size.height
        // Track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            cornerRadius = CornerRadius(height / 2),
            size = Size(width, height)
        )
        // Progress fill
        val progressWidth = width * animatedProgress
        if (progressWidth > 0) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE31837), Color(0xFFFF6B7A)),
                    startX = 0f,
                    endX = progressWidth
                ),
                cornerRadius = CornerRadius(height / 2),
                size = Size(progressWidth, height)
            )
            // Shimmer highlight on progress
            val shimmerX = shimmerOffset * progressWidth
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0f)
                    ),
                    startX = shimmerX - 40.dp.toPx(),
                    endX = shimmerX + 40.dp.toPx()
                ),
                cornerRadius = CornerRadius(height / 2),
                size = Size(progressWidth, height)
            )
        }
    }
}

// ====================================================================
// INFO CARD — Glassmorphic Design
// ====================================================================
@Composable
private fun InfoCard(
    classInfo: ClassInfo?,
    cardBg: Color,
    cardBgAlt: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(400, delayMillis = 150), label = "info_alpha"
    )
    val offsetY by animateFloatAsState(
        if (visible) 0f else 30f, tween(400, delayMillis = 150), label = "info_offset"
    )
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            },
        shape = TMixShapes.Card,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 4.dp
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            // Section title with accent
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(listOf(TMixRed, TMixRedSoft))
                        )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Thông tin lớp học",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Detail rows with staggered animation
            var rowIndex = 0

            classInfo?.schedule?.startDate?.let { startDate ->
                PremiumDetailRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Ngày bắt đầu",
                    value = formatDate(startDate),
                    index = rowIndex++,
                    isAlternate = rowIndex % 2 == 0,
                    cardBgAlt = cardBgAlt,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }

            classInfo?.schedule?.endDate?.let { endDate ->
                PremiumDetailRow(
                    icon = Icons.Default.EventAvailable,
                    label = "Ngày kết thúc",
                    value = formatDate(endDate),
                    index = rowIndex++,
                    isAlternate = rowIndex % 2 == 0,
                    cardBgAlt = cardBgAlt,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }

            classInfo?.grade?.let { grade ->
                PremiumDetailRow(
                    icon = Icons.Default.School,
                    label = "Khối lớp",
                    value = "Lớp $grade",
                    index = rowIndex++,
                    isAlternate = rowIndex % 2 == 0,
                    cardBgAlt = cardBgAlt,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }

            classInfo?.year?.let { year ->
                PremiumDetailRow(
                    icon = Icons.Default.DateRange,
                    label = "Năm học",
                    value = "$year",
                    index = rowIndex++,
                    isAlternate = rowIndex % 2 == 0,
                    cardBgAlt = cardBgAlt,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }

            classInfo?.feePerLesson?.let { fee ->
                PremiumDetailRow(
                    icon = Icons.Default.Payments,
                    label = "Học phí / buổi",
                    value = "${java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(fee.toLong())}đ",
                    index = rowIndex++,
                    isAlternate = rowIndex % 2 == 0,
                    cardBgAlt = cardBgAlt,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }

            classInfo?.maxStudent?.let { max ->
                PremiumDetailRow(
                    icon = Icons.Default.Groups,
                    label = "Sĩ số tối đa",
                    value = "$max học sinh",
                    index = rowIndex++,
                    isAlternate = rowIndex % 2 == 0,
                    cardBgAlt = cardBgAlt,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }

            // Description section
            classInfo?.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            if (isDark) TMixNavyLight else TMixNavy,
                                            TMixNavySoft
                                        )
                                    )
                                )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Mô tả",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Description with accent left bar
                    Row {
                        Box(
                            Modifier
                                .width(3.dp)
                                .heightIn(min = 20.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (isDark) TMixNavyLight.copy(0.4f)
                                    else TMixNavy.copy(0.15f)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    index: Int,
    isAlternate: Boolean,
    cardBgAlt: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(300, delayMillis = index * 60),
        label = "row_alpha_$index"
    )
    val offsetX by animateFloatAsState(
        if (visible) 0f else 20f,
        tween(300, delayMillis = index * 60, easing = FastOutSlowInEasing),
        label = "row_offsetX_$index"
    )
    LaunchedEffect(Unit) { visible = true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationX = offsetX
            }
            .let { base ->
                if (isAlternate) {
                    base
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardBgAlt)
                } else base
            }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon in tinted circle
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.08f)
                    else NavyTint
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                Modifier.size(18.dp),
                tint = if (isDark) TMixNavyLight else TMixNavy
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )
    }
}

// ====================================================================
// TIMELINE SESSION CARD
// ====================================================================
@Composable
private fun TimelineSessionCard(
    index: Int,
    number: Int,
    session: Session,
    isLast: Boolean,
    isDark: Boolean,
    currentStudentId: String = ""
) {
    // Determine per-student attendance status
    val studentAttendance = if (!session.isActive && currentStudentId.isNotEmpty()) {
        session.attendances?.find { it.student?.id == currentStudentId }
    } else null

    val sessionStatus = when {
        session.isActive -> SessionAttendanceStatus.UPCOMING
        studentAttendance == null -> SessionAttendanceStatus.NOT_RECORDED
        studentAttendance.status == AttendanceStatus.PRESENT -> SessionAttendanceStatus.PRESENT
        studentAttendance.status == AttendanceStatus.ABSENT -> SessionAttendanceStatus.ABSENT
        studentAttendance.status == AttendanceStatus.LATE -> SessionAttendanceStatus.LATE
        studentAttendance.status == AttendanceStatus.EXCUSED -> SessionAttendanceStatus.EXCUSED
        else -> SessionAttendanceStatus.NOT_RECORDED
    }

    val statusText = sessionStatus.label
    val statusColor = sessionStatus.color
    val statusIcon = sessionStatus.icon
    val accentColor = sessionStatus.accentColor

    // Lightweight entrance animation — no stagger delay for smooth scrolling
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(200),
        label = "session_alpha"
    )
    val offsetY by animateFloatAsState(
        if (visible) 0f else 16f,
        tween(200, easing = FastOutSlowInEasing),
        label = "session_offset"
    )
    LaunchedEffect(Unit) { visible = true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            }
    ) {
        // Timeline column (dot + line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Timeline dot
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                when (sessionStatus) {
                    SessionAttendanceStatus.UPCOMING -> PulsatingTimelineDot(color = TMixRed)
                    SessionAttendanceStatus.NOT_RECORDED -> Icon(
                        Icons.Default.HourglassEmpty, null,
                        Modifier.size(14.dp), tint = TextSecondary
                    )
                    else -> Icon(statusIcon, null, Modifier.size(16.dp), tint = statusColor)
                }
            }

            // Connecting line
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(72.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f)
                            else Color(0xFFE2E8F0)
                        )
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Session card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 8.dp else 0.dp),
            shape = TMixShapes.Card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 2.dp
            )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Left accent border — fixed with IntrinsicSize instead of fillMaxHeight
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.3f))
                            )
                        )
                )

                Row(
                    Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Buổi $number",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            session.date.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = statusColor.copy(0.1f),
                            shape = TMixShapes.Badge
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    statusIcon, null,
                                    Modifier.size(12.dp),
                                    tint = statusColor
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = statusColor
                                )
                            }
                        }
                        val attendanceCount = session.attendances?.size ?: 0
                        if (attendanceCount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.People, null,
                                    Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "$attendanceCount HS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsatingTimelineDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_timeline")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(Modifier.size(12.dp)) {
        drawCircle(
            color = color.copy(alpha = alpha * 0.3f),
            radius = 6.dp.toPx() * scale
        )
        drawCircle(
            color = color,
            radius = 4.dp.toPx()
        )
    }
}

// ====================================================================
// STAGGERED ANIMATION WRAPPER
// ====================================================================
@Composable
private fun StaggeredItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(350, delayMillis = index * 60),
        label = "stagger_alpha_$index"
    )
    val offsetY by animateFloatAsState(
        if (visible) 0f else 20f,
        tween(350, delayMillis = index * 60, easing = FastOutSlowInEasing),
        label = "stagger_offset_$index"
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
        }
    ) {
        content()
    }
}

// ====================================================================
// SKELETON LOADING STATE
// ====================================================================
@Composable
private fun SkeletonLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header skeleton
        Card(
            shape = TMixShapes.CardLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerBox(width = 56.dp, height = 56.dp, cornerRadius = 28.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        ShimmerBox(width = 120.dp, height = 20.dp)
                        Spacer(Modifier.height(8.dp))
                        ShimmerBox(width = 160.dp, height = 14.dp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                ShimmerBox(width = 200.dp, height = 12.dp)
                Spacer(Modifier.height(12.dp))
                ShimmerBox(width = 280.dp, height = 6.dp)
            }
        }

        // Info skeleton
        Card(
            shape = TMixShapes.Card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                ShimmerBox(width = 140.dp, height = 18.dp)
                Spacer(Modifier.height(16.dp))
                repeat(5) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(width = 36.dp, height = 36.dp, cornerRadius = 18.dp)
                        Spacer(Modifier.width(12.dp))
                        ShimmerBox(width = 100.dp, height = 14.dp)
                        Spacer(Modifier.weight(1f))
                        ShimmerBox(width = 80.dp, height = 14.dp)
                    }
                }
            }
        }

        // Sessions skeleton
        ShimmerBox(width = 180.dp, height = 18.dp)
        Spacer(Modifier.height(4.dp))
        repeat(3) {
            Row(Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    ShimmerBox(width = 28.dp, height = 28.dp, cornerRadius = 14.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(width = 2.dp, height = 50.dp, cornerRadius = 1.dp)
                }
                Spacer(Modifier.width(12.dp))
                Card(
                    Modifier.weight(1f),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(Modifier.padding(14.dp)) {
                        Column(Modifier.weight(1f)) {
                            ShimmerBox(width = 80.dp, height = 14.dp)
                            Spacer(Modifier.height(6.dp))
                            ShimmerBox(width = 100.dp, height = 12.dp)
                        }
                        ShimmerBox(width = 70.dp, height = 22.dp, cornerRadius = 12.dp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ====================================================================
// ERROR STATE
// ====================================================================
@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated error icon
            val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")
            val scale by infiniteTransition.animateFloat(
                1f, 1.05f,
                infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                label = "error_scale"
            )

            Icon(
                Icons.Default.ErrorOutline, null,
                Modifier
                    .size(72.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale },
                tint = Error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                errorMessage,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = TMixShapes.Button,
                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Thử lại")
            }
        }
    }
}

// ====================================================================
// EMPTY STATE
// ====================================================================
@Composable
private fun EmptySessionsState(textColor: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.EventNote,
                null,
                Modifier.size(56.dp),
                tint = textColor.copy(0.4f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Chưa có buổi học nào",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

// ====================================================================
// UTILITIES
// ====================================================================
private fun formatDate(isoDate: String): String {
    return try {
        val datePart = isoDate.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else datePart
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

// ====================================================================
// SESSION ATTENDANCE STATUS ENUM
// ====================================================================
private enum class SessionAttendanceStatus(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val accentColor: Color
) {
    PRESENT("Có mặt", Success, Icons.Default.Check, Success),
    ABSENT("Vắng", Error, Icons.Default.Close, Error),
    LATE("Đi muộn", Warning, Icons.Default.Schedule, Warning),
    EXCUSED("Có phép", Info, Icons.Default.Description, Info),
    NOT_RECORDED("Chưa điểm danh", TextSecondary, Icons.Default.HourglassEmpty, TextSecondary),
    UPCOMING("Sắp tới", Warning, Icons.Default.CalendarToday, TMixRed)
}
