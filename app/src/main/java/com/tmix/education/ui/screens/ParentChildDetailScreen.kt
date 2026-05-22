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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.tmix.education.data.repository.ParentRepository
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
    val parentRepository = remember { ParentRepository() }
    val scope = rememberCoroutineScope()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var student by remember { mutableStateOf<Student?>(null) }
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var attendanceStats by remember { mutableStateOf<AttendanceStats?>(null) }
    var attendanceDetails by remember { mutableStateOf<List<AttendanceDetail>>(emptyList()) }
    var testResults by remember { mutableStateOf<ChildTestResults?>(null) }
    var testResultsError by remember { mutableStateOf<String?>(null) }
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

            studentRepository.getFullAttendance(childId).onSuccess { resp ->
                attendanceStats = resp.attendanceStats?.toAttendanceStats() ?: AttendanceStats()
                attendanceDetails = resp.detailedAttendance ?: emptyList()
            }

            parentRepository.getChildTestResults(childId).onSuccess { r ->
                testResults = r
            }.onFailure { e -> testResultsError = e.message }

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
                val tabTitles = listOf("Tổng quan", "Lớp học", "Kết quả", "Học phí")
                val tabIcons = listOf(Icons.Default.FactCheck, Icons.Default.MenuBook, Icons.Default.Assignment, Icons.Default.Payment)
                val pagerState = rememberPagerState(pageCount = { tabTitles.size })
                Column(Modifier.fillMaxSize().padding(padding)) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = if (isDark) Color(0xFF1A2030) else Color.White,
                        contentColor = TMixNavy,
                        divider = { HorizontalDivider(color = if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0)) }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, maxLines = 1) },
                                icon = { Icon(tabIcons[index], null, Modifier.size(18.dp)) },
                                selectedContentColor = TMixRed,
                                unselectedContentColor = if (isDark) Color.White.copy(0.5f) else Color(0xFF94A3B8)
                            )
                        }
                    }
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp, top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (page) {
                                0 -> { // Tab Tổng quan — Attendance Timeline
                                    attendanceStats?.let { stats ->
                                        // Attendance Rate Card
                                        item {
                                            Card(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 3.dp),
                                                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                                border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null
                                            ) {
                                                Column(Modifier.padding(16.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.FactCheck, null, Modifier.size(20.dp), tint = TMixNavy)
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Tỷ lệ chuyên cần", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textPrimary)
                                                        Spacer(Modifier.weight(1f))
                                                        val ratePercent = if (stats.total > 0) ((stats.present + stats.late) * 100 / stats.total) else 0
                                                        Text("${ratePercent}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (ratePercent >= 80) Success else if (ratePercent >= 60) Warning else Error)
                                                    }
                                                    Spacer(Modifier.height(12.dp))
                                                    // Progress bar
                                                    Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0))) {
                                                        val fraction = if (stats.total > 0) (stats.present + stats.late).toFloat() / stats.total else 0f
                                                        val barColor = if (fraction >= 0.8f) Success else if (fraction >= 0.6f) Warning else Error
                                                        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).clip(RoundedCornerShape(5.dp)).background(Brush.horizontalGradient(listOf(barColor.copy(0.7f), barColor))))
                                                    }
                                                    Spacer(Modifier.height(14.dp))
                                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("${stats.total}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TMixNavy)
                                                            Text("Tổng", style = MaterialTheme.typography.labelSmall, color = textSecondaryColor)
                                                        }
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("${stats.present}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Success)
                                                            Text("Có mặt", style = MaterialTheme.typography.labelSmall, color = textSecondaryColor)
                                                        }
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("${stats.absent}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Error)
                                                            Text("Vắng", style = MaterialTheme.typography.labelSmall, color = textSecondaryColor)
                                                        }
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("${stats.late}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Warning)
                                                            Text("Trễ", style = MaterialTheme.typography.labelSmall, color = textSecondaryColor)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } ?: item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Chưa có dữ liệu điểm danh", color = textSecondaryColor) } }

                                    // Timeline header
                                    if (attendanceDetails.isNotEmpty()) {
                                        item {
                                            Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.History, null, Modifier.size(16.dp), tint = if (isDark) Color.White.copy(0.4f) else Color(0xFF94A3B8))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Lịch sử điểm danh", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(0.4f) else Color(0xFF94A3B8))
                                            }
                                        }
                                        // Timeline items
                                        val sorted = attendanceDetails.sortedByDescending { it.date }
                                        val displayList = sorted.take(30)
                                        itemsIndexed(displayList, contentType = { _, _ -> "timeline" }) { idx, detail ->
                                            val statusColor = when(detail.status) { "present" -> Success; "late" -> Warning; else -> Error }
                                            val statusLabel = when(detail.status) { "present" -> "Có mặt"; "late" -> "Trễ"; "absent" -> "Vắng"; else -> detail.status }
                                            val statusIcon = when(detail.status) { "present" -> Icons.Default.CheckCircle; "late" -> Icons.Default.AccessTime; else -> Icons.Default.Cancel }
                                            val isLast = idx == displayList.size - 1
                                            val dateFormatted = try {
                                                val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                                val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                                formatter.format(parser.parse(detail.date)!!)
                                            } catch (_: Exception) { detail.date.take(10) }

                                            // Timeline row with dot + line
                                            Row(Modifier.padding(start = 16.dp, end = 16.dp).fillMaxWidth()) {
                                                // Timeline column (dot + line)
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
                                                    Box(Modifier.size(14.dp).clip(CircleShape).background(statusColor).border(2.dp, statusColor.copy(0.3f), CircleShape))
                                                    if (!isLast) {
                                                        Box(Modifier.width(2.dp).height(52.dp).background(if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0)))
                                                    }
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                // Content card
                                                Card(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 1.dp),
                                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E2A3A) else Color(0xFFF8FAFC)),
                                                    border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(0.15f))
                                                ) {
                                                    Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                        Column(Modifier.weight(1f)) {
                                                            Text(detail.classInfo?.name ?: "Lớp học", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            Text(dateFormatted, style = MaterialTheme.typography.bodySmall, color = textSecondaryColor)
                                                        }
                                                        Surface(color = statusColor.copy(if (isDark) 0.2f else 0.1f), shape = RoundedCornerShape(8.dp)) {
                                                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(statusIcon, null, Modifier.size(12.dp), tint = statusColor)
                                                                Spacer(Modifier.width(4.dp))
                                                                Text(statusLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = statusColor)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                1 -> { // Tab Lớp học
                                    val classes = s.classes
                                    if (!classes.isNullOrEmpty()) {
                                        item { DetailSectionHeader(icon = Icons.Default.MenuBook, title = "Lớp học (${classes.size})", isDark = isDark, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                                        itemsIndexed(classes, contentType = { _, _ -> "class_card" }) { index, enrollment ->
                                            DetailAnimatedItem(index = index) { PremiumClassCard(enrollment = enrollment, isDark = isDark, surfaceColor = surfaceColor, textPrimary = textPrimary, textSecondaryColor = textSecondaryColor) }
                                        }
                                    } else {
                                        item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.MenuBook, null, Modifier.size(48.dp), tint = textSecondaryColor.copy(0.4f)); Spacer(Modifier.height(8.dp)); Text("Chưa tham gia lớp học nào", color = textSecondaryColor) } } }
                                    }
                                }
                                2 -> { // Tab Kết quả
                                    testResults?.let { results ->
                                        if (results.summary.totalAttempts > 0) {
                                            item {
                                                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                                    DetailSectionHeader(icon = Icons.Default.Assignment, title = "Kết quả học tập", isDark = isDark)
                                                    Spacer(Modifier.height(10.dp))
                                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        PremiumStatTile("Bài test", "${results.summary.totalAttempts}", TMixNavy, Icons.Default.Assignment, isDark, tileBg, Modifier.weight(1f))
                                                        PremiumStatTile("Điểm TB", "${results.summary.averageScore}%", Info, Icons.Default.TrendingUp, isDark, tileBg, Modifier.weight(1f))
                                                        PremiumStatTile("Đạt", "${results.summary.passRate}%", Success, Icons.Default.EmojiEvents, isDark, tileBg, Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                            if (results.summary.bySkillType.isNotEmpty()) {
                                                item {
                                                    Column(Modifier.padding(horizontal = 16.dp)) {
                                                        results.summary.bySkillType.forEach { (skill, data) ->
                                                            val lbl = when(skill) { "reading"->"Reading"; "writing"->"Writing"; "speaking"->"Speaking"; "listening"->"Listening"; else->skill }
                                                            val clr = when(skill) { "reading"->Color(0xFF3B82F6); "writing"->Color(0xFF10B981); "speaking"->Color(0xFFF59E0B); "listening"->Color(0xFF8B5CF6); else->TMixNavy }
                                                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Text(lbl, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White.copy(0.8f) else Color(0xFF475569), modifier = Modifier.width(72.dp))
                                                                Box(Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(clr.copy(if (isDark) 0.15f else 0.1f))) { Box(Modifier.fillMaxHeight().fillMaxWidth((data.averageScore / 100f).toFloat().coerceIn(0f, 1f)).clip(RoundedCornerShape(6.dp)).background(clr)) }
                                                                Spacer(Modifier.width(8.dp))
                                                                Text("${data.averageScore.toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = clr, modifier = Modifier.width(36.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            val recent = results.attempts.take(10)
                                            if (recent.isNotEmpty()) {
                                                item { Text("Bài test gần đây", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(0.5f) else Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
                                                itemsIndexed(recent, contentType = { _, _ -> "attempt" }) { idx, att ->
                                                    val ac = when(att.testSkillType) { "reading"->Color(0xFF3B82F6); "writing"->Color(0xFF10B981); "speaking"->Color(0xFFF59E0B); "listening"->Color(0xFF8B5CF6); else->TMixNavy }
                                                    Card(modifier = Modifier.padding(horizontal = 16.dp), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 2.dp), colors = CardDefaults.cardColors(containerColor = surfaceColor), border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null) {
                                                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(ac.copy(if (isDark) 0.2f else 0.1f)), contentAlignment = Alignment.Center) { Icon(if (att.passed) Icons.Default.CheckCircle else Icons.Default.Assignment, null, Modifier.size(18.dp), tint = ac) }
                                                            Spacer(Modifier.width(12.dp))
                                                            Column(Modifier.weight(1f)) {
                                                                Text(att.testTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                Text("${att.className} · ${att.testSkillType.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodySmall, color = textSecondaryColor, maxLines = 1)
                                                            }
                                                            Surface(color = if (att.passed) Success.copy(if (isDark) 0.2f else 0.1f) else Error.copy(if (isDark) 0.2f else 0.1f), shape = RoundedCornerShape(8.dp)) {
                                                                Text("${att.percentage.toInt()}%", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (att.passed) Success else Error)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Assignment, null, Modifier.size(48.dp), tint = textSecondaryColor.copy(0.4f)); Spacer(Modifier.height(8.dp)); Text("Chưa có kết quả bài test", color = textSecondaryColor) } } }
                                        }
                                    } ?: item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Assignment, null, Modifier.size(48.dp), tint = textSecondaryColor.copy(0.4f)); Spacer(Modifier.height(8.dp)); Text(testResultsError ?: "Chưa có kết quả", color = textSecondaryColor) } } }
                                }
                                3 -> { // Tab Học phí
                                    if (payments.isNotEmpty()) {
                                        item { DetailSectionHeader(icon = Icons.Default.Payment, title = "Học phí", isDark = isDark, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                                        item {
                                            Card(modifier = Modifier.padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp), colors = CardDefaults.cardColors(containerColor = surfaceColor), border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null) {
                                                Column {
                                                    payments.forEachIndexed { idx, payment ->
                                                        PremiumPaymentRow(payment = payment, isDark = isDark, textPrimary = textPrimary, textSecondaryColor = textSecondaryColor)
                                                        if (idx < payments.size - 1) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0))
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Payment, null, Modifier.size(48.dp), tint = textSecondaryColor.copy(0.4f)); Spacer(Modifier.height(8.dp)); Text("Chưa có dữ liệu học phí", color = textSecondaryColor) } } }
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
