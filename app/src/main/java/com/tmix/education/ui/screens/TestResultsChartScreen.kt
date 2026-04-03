package com.tmix.education.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.tmix.education.data.model.TestAttempt
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.AttemptHistoryState
import com.tmix.education.ui.viewmodel.TestViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Test Results Chart Screen - Biểu đồ kết quả học tập
 * Full dark mode support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsChartScreen(
    onBack: () -> Unit = {},
    onAttemptClick: (String) -> Unit = {},
    testViewModel: TestViewModel = viewModel()
) {
    val historyState by testViewModel.attemptHistoryState.collectAsState()
    var selectedSkillFilter by remember { mutableStateOf("all") }
    val isDark = isSystemInDarkTheme()

    val skillFilters = listOf(
        "all" to "Tất cả",
        "reading" to "📖 Đọc",
        "listening" to "🎧 Nghe",
        "writing" to "✍️ Viết",
        "speaking" to "🎤 Nói"
    )

    LaunchedEffect(Unit) {
        testViewModel.loadAttemptHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả học tập", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF0F1E33) else TMixNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when (val state = historyState) {
            is AttemptHistoryState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }

            is AttemptHistoryState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                        Spacer(Modifier.height(16.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { testViewModel.loadAttemptHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) { Text("Thử lại") }
                    }
                }
            }

            is AttemptHistoryState.Success -> {
                val allAttempts = state.attempts
                val filteredAttempts = if (selectedSkillFilter == "all") allAttempts
                else allAttempts.filter { it.test?.skillType == selectedSkillFilter }

                // Sort by date ascending for chart
                val sortedAttempts = filteredAttempts.sortedBy { it.submittedAt ?: "" }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Summary card
                    item {
                        SummaryCard(filteredAttempts, isDark)
                    }

                    // Skill filter chips
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(skillFilters) { (key, label) ->
                                val isSelected = selectedSkillFilter == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSkillFilter = key },
                                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (isDark) Color(0xFF1A2B45) else TMixNavy,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Chart section
                    item {
                        if (sortedAttempts.size >= 2) {
                            ChartSection(sortedAttempts, isDark)
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                shape = TMixShapes.Card,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Info.copy(0.1f) else InfoLight
                                )
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, null, tint = Info, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Cần ít nhất 2 bài thi để hiển thị biểu đồ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Skill distribution
                    item {
                        if (filteredAttempts.isNotEmpty()) {
                            SkillDistributionCard(allAttempts, isDark)
                        }
                    }

                    // Recent results header
                    item {
                        Text(
                            "Kết quả gần đây",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Recent results list
                    if (filteredAttempts.isEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Assignment, null,
                                        Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("Chưa có kết quả nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(filteredAttempts.sortedByDescending { it.submittedAt }.take(15)) { attempt ->
                        AttemptResultCard(attempt, isDark = isDark, onClick = { onAttemptClick(attempt.id) })
                    }
                }
            }
        }
    }
}

/**
 * Summary statistics card
 */
@Composable
private fun SummaryCard(attempts: List<TestAttempt>, isDark: Boolean) {
    val totalTests = attempts.size
    val averageScore = if (attempts.isNotEmpty()) attempts.map { it.percentage }.average() else 0.0
    val passRate = if (attempts.isNotEmpty())
        (attempts.count { it.passed }.toFloat() / totalTests * 100).toInt() else 0
    val highestScore = attempts.maxOfOrNull { it.percentage } ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF0D1B2E), Color(0xFF1A2B45))
                        else listOf(TMixNavy, TMixNavyLight)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "📊 Tổng quan kết quả",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Đã làm", "$totalTests", "bài", Color.White)
                    StatItem("Điểm TB", "${averageScore.toInt()}%", "",
                        if (averageScore >= 70) Success else TMixRedLight)
                    StatItem("Tỷ lệ đạt", "$passRate%", "",
                        if (passRate >= 50) Success else TMixRedLight)
                    StatItem("Cao nhất", "${highestScore.toInt()}%", "", Success)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        if (unit.isNotEmpty()) {
            Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.8f))
    }
}

/**
 * Line chart section using Vico — adaptive dark mode colors
 */
@Composable
private fun ChartSection(attempts: List<TestAttempt>, isDark: Boolean) {
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    val parseFormat = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }

    // Prepare chart data
    val dateLabels = remember(attempts) {
        attempts.mapIndexed { index, attempt ->
            val date = try {
                parseFormat.parse(attempt.submittedAt ?: "")
            } catch (e: Exception) { null }
            index to (date?.let { dateFormat.format(it) } ?: "Bài ${index + 1}")
        }.toMap()
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(attempts) {
        modelProducer.runTransaction {
            lineSeries {
                series(attempts.map { it.percentage })
            }
        }
    }

    val bottomAxisValueFormatter = remember(dateLabels) {
        CartesianValueFormatter { _, value, _ ->
            dateLabels[value.toInt()] ?: " "
        }
    }

    // Adaptive chart colors
    val lineColor = if (isDark) Color(0xFF60A5FA) else TMixNavy  // Brighter blue for dark mode
    val axisLabelColor = if (isDark) Color.White.copy(0.6f) else TextSecondary
    val guidelineColor = if (isDark) Color.White.copy(0.08f) else SurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "📈 Tiến trình điểm số",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Phần trăm điểm theo thời gian",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                                areaFill = LineCartesianLayer.AreaFill.single(
                                    fill(lineColor.copy(alpha = 0.2f))
                                ),
                            )
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        label = rememberTextComponent(
                            color = axisLabelColor,
                            textSize = 11.sp,
                        ),
                        guideline = rememberLineComponent(
                            fill = fill(guidelineColor),
                            thickness = 1.dp,
                        ),
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = rememberTextComponent(
                            color = axisLabelColor,
                            textSize = 10.sp,
                        ),
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null,
                    ),
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )

            // Passing score reference line note
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(12.dp, 3.dp)
                        .background(Success, shape = MaterialTheme.shapes.extraSmall)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Đường điểm đạt: 70%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Skill distribution breakdown card — dark mode adaptive
 */
@Composable
private fun SkillDistributionCard(attempts: List<TestAttempt>, isDark: Boolean) {
    val skillGroups = attempts.groupBy { it.test?.skillType ?: "reading" }
    val skillNames = mapOf(
        "reading" to "📖 Đọc hiểu",
        "listening" to "🎧 Nghe",
        "writing" to "✍️ Viết",
        "speaking" to "🎤 Nói"
    )
    val skillColors = mapOf(
        "reading" to Info,
        "listening" to Color(0xFF8B5CF6),
        "writing" to Warning,
        "speaking" to Success
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "🎯 Phân bổ theo kỹ năng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            skillGroups.forEach { (skill, skillAttempts) ->
                val avgScore = skillAttempts.map { it.percentage }.average()
                val targetFraction = (avgScore / 100f).toFloat().coerceIn(0f, 1f)
                val animatedFraction by animateFloatAsState(
                    targetValue = targetFraction,
                    animationSpec = tween(800),
                    label = "progress_$skill"
                )
                val color = skillColors[skill] ?: Info

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        skillNames[skill] ?: skill,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(90.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LinearProgressIndicator(
                        progress = { animatedFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = color,
                        trackColor = color.copy(alpha = if (isDark) 0.2f else 0.15f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${avgScore.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        "(${skillAttempts.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual attempt result card — dark mode adaptive
 */
@Composable
private fun AttemptResultCard(attempt: TestAttempt, isDark: Boolean = false, onClick: () -> Unit = {}) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val parseFormat = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }

    val formattedDate = remember(attempt.submittedAt) {
        try {
            val date = parseFormat.parse(attempt.submittedAt ?: "")
            date?.let { dateFormat.format(it) } ?: "—"
        } catch (e: Exception) { "—" }
    }

    val skillEmoji = when (attempt.test?.skillType) {
        "listening" -> "🎧"
        "writing" -> "✍️"
        "speaking" -> "🎤"
        else -> "📖"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 1.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (attempt.passed)
                            (if (isDark) Success.copy(0.15f) else SuccessLight)
                        else
                            (if (isDark) Error.copy(0.15f) else ErrorLight)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${attempt.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (attempt.passed) Success else Error
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(skillEmoji, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        attempt.test?.title ?: "Bài kiểm tra",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pass/Fail badge
            Surface(
                color = if (attempt.passed)
                    (if (isDark) Success.copy(0.15f) else SuccessLight)
                else
                    (if (isDark) Error.copy(0.15f) else ErrorLight),
                shape = TMixShapes.Chip
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (attempt.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null, Modifier.size(14.dp),
                        tint = if (attempt.passed) Success else Error
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (attempt.passed) "Đạt" else "Chưa đạt",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (attempt.passed) Success else Error
                    )
                }
            }
        }
    }
}
