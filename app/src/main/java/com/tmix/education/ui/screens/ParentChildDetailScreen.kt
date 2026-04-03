package com.tmix.education.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmix.education.data.model.*
import com.tmix.education.data.repository.StudentRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Parent Child Detail Screen — Premium Redesign
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildDetailScreen(
    childId: String,
    onBack: () -> Unit = {}
) {
    val studentRepository = remember { StudentRepository() }
    val scope = rememberCoroutineScope()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var student by remember { mutableStateOf<Student?>(null) }
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var attendanceStats by remember { mutableStateOf<AttendanceStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(childId) {
        scope.launch {
            isLoading = true
            studentRepository.getStudent(childId).onSuccess { s ->
                student = s
            }.onFailure { error = it.message }

            studentRepository.getPayments(childId).onSuccess { p ->
                payments = p
            }

            studentRepository.getAttendanceStats(childId).onSuccess { a ->
                attendanceStats = a
            }

            isLoading = false
        }
    }

    val surfaceColor = if (isDark) Color(0xFF1A2030) else Color.White
    val tileBg = if (isDark) Color(0xFF232A3A) else Color(0xFFF8FAFC)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondaryColor = if (isDark) Color.White.copy(0.6f) else Color(0xFF64748B)

    Scaffold(
        containerColor = if (isDark) Color(0xFF111827) else Color(0xFFF1F5F9),
        topBar = {
            student?.let { s ->
                val initials = s.name.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?"
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(TMixNavy, Color(0xFF2A4D7A), TMixNavySoft)
                            )
                        )
                        .drawBehind {
                            drawCircle(
                                color = Color.White,
                                radius = size.height * 0.7f,
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f),
                                alpha = 0.05f
                            )
                            drawCircle(
                                color = Color.White,
                                radius = size.height * 0.5f,
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.9f),
                                alpha = 0.03f
                            )
                        }
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 8.dp, bottom = 24.dp)
                ) {
                    Column {
                        // Back button
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, "Quay lại",
                                tint = Color.White
                            )
                        }

                        // Profile row
                        Row(
                            Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, Color.White.copy(0.3f), CircleShape)
                                    .background(TMixRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    initials,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    s.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (s.email != null) {
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, null, Modifier.size(13.dp), tint = Color.White.copy(0.6f))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            s.email!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (s.phone != null) {
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, null, Modifier.size(13.dp), tint = Color.White.copy(0.6f))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            s.phone!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixNavy)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2A1520) else ErrorLight)
                    ) {
                        Column(
                            Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(56.dp), tint = Error)
                            Spacer(Modifier.height(16.dp))
                            Text(error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            student != null -> {
                val s = student!!
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ═══════════════════════════════════════
                    // 2. ATTENDANCE STATS
                    // ═══════════════════════════════════════
                    attendanceStats?.let { stats ->
                        item {
                            DetailAnimatedItem(index = 0) {
                                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    DetailSectionHeader(
                                        icon = Icons.Default.FactCheck,
                                        title = "Điểm danh",
                                        isDark = isDark
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PremiumStatTile("Tổng", "${stats.total}", TMixNavy, Icons.Default.CalendarMonth, isDark, tileBg, Modifier.weight(1f))
                                        PremiumStatTile("Có mặt", "${stats.present}", Success, Icons.Default.CheckCircle, isDark, tileBg, Modifier.weight(1f))
                                        PremiumStatTile("Vắng", "${stats.absent}", Error, Icons.Default.Cancel, isDark, tileBg, Modifier.weight(1f))
                                        PremiumStatTile("Trễ", "${stats.late}", Warning, Icons.Default.AccessTime, isDark, tileBg, Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // ═══════════════════════════════════════
                    // 3. CLASSES
                    // ═══════════════════════════════════════
                    val classes = s.classes
                    if (!classes.isNullOrEmpty()) {
                        item {
                            DetailAnimatedItem(index = 1) {
                                DetailSectionHeader(
                                    icon = Icons.Default.MenuBook,
                                    title = "Lớp học (${classes.size})",
                                    isDark = isDark,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        itemsIndexed(
                            classes,
                            contentType = { _, _ -> "class_card" }
                        ) { index, enrollment ->
                            DetailAnimatedItem(index = index + 2) {
                                PremiumClassCard(
                                    enrollment = enrollment,
                                    isDark = isDark,
                                    surfaceColor = surfaceColor,
                                    textPrimary = textPrimary,
                                    textSecondaryColor = textSecondaryColor
                                )
                            }
                        }
                    }

                    // ═══════════════════════════════════════
                    // 4. PAYMENTS
                    // ═══════════════════════════════════════
                    if (payments.isNotEmpty()) {
                        item {
                            DetailAnimatedItem(index = (classes?.size ?: 0) + 3) {
                                DetailSectionHeader(
                                    icon = Icons.Default.Payment,
                                    title = "Học phí",
                                    isDark = isDark,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        item {
                            DetailAnimatedItem(index = (classes?.size ?: 0) + 4) {
                                Card(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                    border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null
                                ) {
                                    Column {
                                        payments.take(5).forEachIndexed { idx, payment ->
                                            PremiumPaymentRow(
                                                payment = payment,
                                                isDark = isDark,
                                                textPrimary = textPrimary,
                                                textSecondaryColor = textSecondaryColor
                                            )
                                            if (idx < payments.take(5).size - 1) {
                                                HorizontalDivider(
                                                    Modifier.padding(horizontal = 16.dp),
                                                    color = if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// HELPER COMPOSABLES
// ================================================================

@Composable
private fun DetailAnimatedItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 60L).coerceAtMost(400))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(350, easing = EaseOutCubic)
        )
    ) {
        content()
    }
}

@Composable
private fun DetailSectionHeader(
    icon: ImageVector,
    title: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(
                    Brush.linearGradient(listOf(TMixNavy, TMixNavySoft))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(15.dp), tint = Color.White)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF1E293B)
        )
    }
}

@Composable
private fun PremiumStatTile(
    label: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    isDark: Boolean,
    tileBg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tileBg)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(accentColor.copy(if (isDark) 0.2f else 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = accentColor)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun PremiumClassCard(
    enrollment: StudentClassInfo,
    isDark: Boolean,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondaryColor: Color
) {
    val classInfo = enrollment.classInfo
    val statusColor = when (classInfo.status) {
        "active" -> Success
        "upcoming" -> Info
        else -> if (isDark) Color(0xFF6B7280) else Color(0xFF94A3B8)
    }
    val statusText = when (classInfo.status) {
        "active" -> "Đang học"
        "upcoming" -> "Sắp mở"
        else -> "Đã kết thúc"
    }

    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null
    ) {
        Row {
            // Status accent strip
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(Modifier.padding(16.dp).weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        classInfo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = statusColor.copy(if (isDark) 0.2f else 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            statusText,
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Schedule info
                classInfo.schedule?.let { schedule ->
                    val dayNames = schedule.daysOfWeek?.joinToString(", ") { dayNum ->
                        when (dayNum) {
                            "0" -> "CN"; "1" -> "T2"; "2" -> "T3"; "3" -> "T4"
                            "4" -> "T5"; "5" -> "T6"; "6" -> "T7"
                            else -> dayNum
                        }
                    } ?: ""

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = TMixNavy)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "$dayNames · ${schedule.timeSlots?.startTime ?: ""} - ${schedule.timeSlots?.endTime ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (classInfo.teacher != null) {
                        Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = Info)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            classInfo.teacher!!.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (classInfo.teacher != null && classInfo.room != null) {
                        Spacer(Modifier.width(12.dp))
                    }
                    if (classInfo.room != null) {
                        Icon(Icons.Default.Room, null, Modifier.size(14.dp), tint = Warning)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            classInfo.room!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPaymentRow(
    payment: Payment,
    isDark: Boolean,
    textPrimary: Color,
    textSecondaryColor: Color
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (payment.isPaid) Success.copy(if (isDark) 0.15f else 0.1f)
                        else Warning.copy(if (isDark) 0.15f else 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (payment.isPaid) Icons.Default.CheckCircle else Icons.Default.AccessTime,
                    null, Modifier.size(18.dp),
                    tint = if (payment.isPaid) Success else Warning
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Tháng ${payment.month}/${payment.year}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                val amountText = if (payment.isPaid) {
                    "Đã đóng: ${formatter.format(payment.paidAmount)}"
                } else {
                    "Còn lại: ${formatter.format(payment.remainingAmount)}"
                }
                Text(
                    amountText,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondaryColor
                )
            }
        }
        Surface(
            color = if (payment.isPaid) Success.copy(if (isDark) 0.2f else 0.1f)
                    else Warning.copy(if (isDark) 0.2f else 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                if (payment.isPaid) "Đã thanh toán" else "Chưa thanh toán",
                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (payment.isPaid) Success else Warning
            )
        }
    }
}
