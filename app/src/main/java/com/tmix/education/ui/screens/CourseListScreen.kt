package com.tmix.education.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmix.education.data.model.ClassInfo
import com.tmix.education.data.repository.ClassRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Course List Screen — Premium Redesign
 * Browse public courses available for registration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    onBack: () -> Unit = {},
    onRegister: (String) -> Unit = {}
) {
    val classRepository = remember { ClassRepository() }

    var classes by remember { mutableStateOf<List<ClassInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    // Load public classes
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val result = classRepository.getPublicClasses(page = 1, limit = 100)
            result.onSuccess { data ->
                classes = data.filter { it.status.lowercase() != "closed" }
            }.onFailure { e ->
                error = e.message ?: "Không thể tải danh sách khóa học"
            }
            isLoading = false
        }
    }

    // Derive filtered list — only recalculates when inputs change (no coroutine overhead)
    val filteredClasses by remember(classes, searchQuery, selectedFilter) {
        androidx.compose.runtime.derivedStateOf {
            classes.filter { classInfo ->
                val matchesSearch = searchQuery.isBlank() ||
                    classInfo.name.contains(searchQuery, ignoreCase = true) ||
                    classInfo.teacher?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    classInfo.description?.contains(searchQuery, ignoreCase = true) == true

                val matchesFilter = when (selectedFilter) {
                    "active" -> classInfo.status.lowercase() in listOf("active", "ongoing")
                    "upcoming" -> classInfo.status.lowercase() == "upcoming"
                    else -> true
                }

                matchesSearch && matchesFilter
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Khóa học",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar — elevated style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(
                        elevation = if (isDark) 0.dp else 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = TMixNavy.copy(0.08f),
                        spotColor = TMixNavy.copy(0.05f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF252D3F) else Color.White)
                    .then(
                        if (isDark) Modifier.border(1.dp, Color(0xFF3A4560), RoundedCornerShape(16.dp))
                        else Modifier
                    )
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Tìm kiếm khóa học, giáo viên...",
                            color = if (isDark) Color.White.copy(0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, "Tìm kiếm",
                            tint = if (isDark) Color.White.copy(0.6f) else TMixNavy
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear, "Xóa",
                                    tint = if (isDark) Color.White.copy(0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                        cursorColor = if (isDark) Color.White else TMixNavy
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Filter chips row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val filters = listOf(
                    "all" to "Tất cả",
                    "active" to "Đang học",
                    "upcoming" to "Sắp khai giảng"
                )
                items(filters.size) { index ->
                    val (key, label) = filters[index]
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TMixNavy,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Results count
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.School,
                    null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${filteredClasses.size} khóa học",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            when {
                isLoading -> {
                    // Shimmer loading skeleton
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(3) {
                            ShimmerCourseCard(isDark)
                        }
                    }
                }
                error != null -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDark) Color(0xFF2A1A1A) else Color(0xFFFEE2E2)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudOff, null,
                                    Modifier.size(40.dp),
                                    tint = Error
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                "Không thể tải dữ liệu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                error ?: "Có lỗi xảy ra",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        error = null
                                        val result = classRepository.getPublicClasses(page = 1, limit = 100)
                                        result.onSuccess { data ->
                                            classes = data.filter { it.status.lowercase() != "closed" }
                                        }.onFailure { e ->
                                            error = e.message
                                        }
                                        isLoading = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixNavy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Thử lại", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                filteredClasses.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDark) Color(0xFF1A2433) else NavyTint
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.School, null,
                                    Modifier.size(40.dp),
                                    tint = TMixNavy.copy(0.6f)
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                if (searchQuery.isBlank()) "Chưa có khóa học nào"
                                else "Không tìm thấy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isBlank()) "Các khóa học sẽ xuất hiện tại đây"
                                else "Thử tìm kiếm với từ khóa khác",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            filteredClasses,
                            key = { _, it -> it.id },
                            contentType = { _, _ -> "course_card" }
                        ) { index, classInfo ->
                            // Animate each card entrance
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 60L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(400)) + slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = tween(400, easing = EaseOutCubic)
                                )
                            ) {
                                PremiumCourseCard(
                                    classInfo = classInfo,
                                    onRegister = { onRegister(classInfo.id) },
                                    isDark = isDark
                                )
                            }
                        }
                        // Bottom spacing
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

// ================================================================
// Premium Course Card
// ================================================================
@Composable
private fun PremiumCourseCard(
    classInfo: ClassInfo,
    onRegister: () -> Unit,
    isDark: Boolean
) {
    val cardBg = if (isDark) Color(0xFF1A2030) else Color.White
    val infoBg = if (isDark) Color(0xFF232A3A) else Color(0xFFF8FAFC)

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 8.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = if (isDark) BorderStroke(1.dp, Color(0xFF2A3347)) else null
    ) {
        Column {
            // ─── Header: Gradient + Class Name + Badges ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(TMixNavy, Color(0xFF2A4D7A), TMixNavySoft)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    // Status badge row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Class name
                        Text(
                            "Lớp ${classInfo.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            letterSpacing = (-0.3).sp
                        )

                        // Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            classInfo.grade?.let { grade ->
                                BadgePill(
                                    text = "Khối $grade",
                                    bgColor = Color.White.copy(0.15f),
                                    textColor = Color.White
                                )
                            }
                            StatusBadge(classInfo.status)
                        }
                    }

                    // Quick info row under title
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        classInfo.schedule?.let { schedule ->
                            val daysMap = mapOf(
                                "0" to "CN", "1" to "T2", "2" to "T3", "3" to "T4",
                                "4" to "T5", "5" to "T6", "6" to "T7"
                            )
                            val daysText = schedule.daysOfWeek?.joinToString(", ") { daysMap[it] ?: it } ?: ""
                            val timeText = schedule.timeSlots?.let { "${it.startTime} - ${it.endTime}" } ?: ""

                            if (daysText.isNotBlank()) {
                                QuickInfoChip(Icons.Default.CalendarMonth, daysText)
                            }
                            if (timeText.isNotBlank()) {
                                QuickInfoChip(Icons.Default.AccessTime, timeText)
                            }
                        }
                    }
                }
            }

            // ─── Body: Info Grid + Description ───
            Column(Modifier.padding(16.dp)) {
                // 2-Column info grid
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left column
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        classInfo.room?.let { room ->
                            InfoTile(
                                icon = Icons.Outlined.MeetingRoom,
                                label = "Phòng",
                                value = room,
                                isDark = isDark,
                                accentColor = Info
                            )
                        }
                        InfoTile(
                            icon = Icons.Outlined.Groups,
                            label = "Sĩ số",
                            value = "${classInfo.maxStudent ?: "N/A"} học sinh",
                            isDark = isDark,
                            accentColor = Success
                        )
                    }
                    // Right column
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        classInfo.teacher?.let { teacher ->
                            InfoTile(
                                icon = Icons.Outlined.Person,
                                label = "Giáo viên",
                                value = teacher.name,
                                isDark = isDark,
                                accentColor = TMixNavy
                            )
                        }
                        InfoTile(
                            icon = Icons.Outlined.Payments,
                            label = "Học phí",
                            value = classInfo.feePerLesson?.let {
                                formatCurrencyVND(it) + "/buổi"
                            } ?: "Liên hệ",
                            isDark = isDark,
                            accentColor = Warning
                        )
                    }
                }

                // Description
                classInfo.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF252B3B) else Color(0xFFF1F5F9))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                null,
                                Modifier
                                    .size(16.dp)
                                    .offset(y = 2.dp),
                                tint = if (isDark) Color.White.copy(0.4f) else Color(0xFF94A3B8)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(0.7f) else Color(0xFF64748B),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // ─── Register Button — Gradient with animation ───
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isDark) 0.dp else 4.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(TMixRed, Color(0xFFE84357), TMixRedSoft)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AppRegistration, null,
                                Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Đăng ký ngay",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowForward, null,
                                Modifier.size(16.dp),
                                tint = Color.White.copy(0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Info Tile — mini card for each info item
// ================================================================
@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    isDark: Boolean,
    accentColor: Color
) {
    val tileBg = if (isDark) Color(0xFF232A3A) else Color(0xFFF8FAFC)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(tileBg)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accentColor.copy(if (isDark) 0.15f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    Modifier.size(14.dp),
                    tint = accentColor
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(0.6f) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White else Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ================================================================
// Quick Info Chip — in the header
// ================================================================
@Composable
private fun QuickInfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, null, Modifier.size(13.dp), tint = Color.White.copy(0.8f))
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.85f),
            fontSize = 11.sp
        )
    }
}

// ================================================================
// Badge Pill
// ================================================================
@Composable
private fun BadgePill(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}

// ================================================================
// Status Badge
// ================================================================
@Composable
private fun StatusBadge(status: String) {
    val (label, bgColor, textColor) = when (status.lowercase()) {
        "active", "ongoing" -> Triple("Đang học", Color(0xFF22C55E), Color.White)
        "upcoming" -> Triple("Sắp khai giảng", Color(0xFFF59E0B), Color.White)
        else -> Triple(status, Color.White.copy(0.2f), Color.White)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

// ================================================================
// Shimmer Loading Skeleton
// ================================================================
@Composable
private fun ShimmerCourseCard(isDark: Boolean) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by shimmerTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    val shimmerColor = if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0)

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1A2030) else Color.White
        )
    ) {
        Column {
            // Header shimmer
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(shimmerColor.copy(shimmerAlpha))
            )
            Column(Modifier.padding(16.dp)) {
                // Info tiles shimmer
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerColor.copy(shimmerAlpha * 0.5f))
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerColor.copy(shimmerAlpha * 0.5f))
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerColor.copy(shimmerAlpha * 0.5f))
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerColor.copy(shimmerAlpha * 0.5f))
                    )
                }
                Spacer(Modifier.height(14.dp))
                // Button shimmer
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(shimmerColor.copy(shimmerAlpha * 0.4f))
                )
            }
        }
    }
}

private fun formatCurrencyVND(amount: Double): String {
    return java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")).format(amount)
}
