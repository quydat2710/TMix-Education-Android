package com.tmix.education.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.MCQuestion
import com.tmix.education.data.model.TestAttempt
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.AttemptDetailState
import com.tmix.education.ui.viewmodel.TestViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Attempt Detail Screen - Xem chi tiết kết quả bài thi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptDetailScreen(
    attemptId: String,
    onBack: () -> Unit = {},
    onRetake: (testId: String) -> Unit = {},
    testViewModel: TestViewModel = viewModel()
) {
    val detailState by testViewModel.attemptDetailState.collectAsState()

    LaunchedEffect(attemptId) {
        testViewModel.loadAttemptDetail(attemptId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết kết quả", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TMixNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when (val state = detailState) {
            is AttemptDetailState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }

            is AttemptDetailState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                        Spacer(Modifier.height(16.dp))
                        Text(state.message, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { testViewModel.loadAttemptDetail(attemptId) },
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) { Text("Thử lại") }
                    }
                }
            }

            is AttemptDetailState.Success -> {
                val attempt = state.attempt
                val skillType = attempt.test?.skillType ?: "reading"

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Score Header Card
                    item { ScoreHeaderCard(attempt) }

                    // Content based on skill type
                    when (skillType) {
                        "writing" -> {
                            // Writing Response
                            item { WritingResponseSection(attempt) }
                            // AI Feedback
                            item { AiFeedbackSection(attempt) }
                        }
                        "speaking" -> {
                            // Transcription
                            item { SpeakingTranscriptionSection(attempt) }
                            // AI Feedback
                            item { AiFeedbackSection(attempt) }
                        }
                        else -> {
                            // MC Questions (Reading / Listening)
                            item {
                                Text(
                                    "📝 Chi tiết từng câu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                            val questions = attempt.test?.questions ?: emptyList()
                            val answers = attempt.answers
                            val feedbackList = attempt.feedback

                            itemsIndexed(questions) { index, question ->
                                MCQuestionResultCard(
                                    index = index,
                                    question = question,
                                    studentAnswer = answers.getOrNull(index),
                                    feedback = feedbackList.getOrNull(index)
                                )
                            }
                        }
                    }

                    // Retake Button
                    item {
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { attempt.testId.let { onRetake(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TMixNavy),
                            shape = TMixShapes.Button
                        ) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Làm lại bài này", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// Score Header
// =====================================================

@Composable
private fun ScoreHeaderCard(attempt: TestAttempt) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val parseFormat = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }

    val formattedDate = remember(attempt.submittedAt) {
        try {
            val date = parseFormat.parse(attempt.submittedAt ?: "")
            date?.let { dateFormat.format(it) } ?: "—"
        } catch (e: Exception) { "—" }
    }

    val skillLabel = when (attempt.test?.skillType) {
        "listening" -> "🎧 Nghe"
        "writing" -> "✍️ Viết"
        "speaking" -> "🎤 Nói"
        else -> "📖 Đọc"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (attempt.passed) listOf(Color(0xFF059669), Color(0xFF10B981))
                        else listOf(TMixNavy, TMixNavyLight)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Test title
                Text(
                    attempt.test?.title ?: "Bài kiểm tra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Skill + Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(skillLabel, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.8f))
                    Spacer(Modifier.width(12.dp))
                    Text("📅 $formattedDate", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.8f))
                }

                Spacer(Modifier.height(16.dp))

                // Score display
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score circle
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                            .background(Color.White.copy(0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${attempt.percentage.toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        // Pass/Fail badge
                        Surface(
                            color = if (attempt.passed) Color.White.copy(0.25f) else Color.White.copy(0.15f),
                            shape = TMixShapes.Chip
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (attempt.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    null, Modifier.size(18.dp), tint = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (attempt.passed) "Đạt ✨" else "Chưa đạt",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Điểm: ${attempt.score}/${attempt.test?.totalPoints ?: "?"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.8f)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// MC Question Result Card
// =====================================================

@Composable
private fun MCQuestionResultCard(
    index: Int,
    question: MCQuestion,
    studentAnswer: Int?,
    feedback: String?
) {
    val isCorrect = studentAnswer == question.correctAnswer
    val optionLabels = listOf("A", "B", "C", "D")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        shape = TMixShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) SuccessLight.copy(0.3f) else ErrorLight.copy(0.3f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Question header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                        .background(if (isCorrect) Success else Error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isCorrect) "✓" else "✗",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Câu ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Question text
            Text(
                question.question,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Spacer(Modifier.height(10.dp))

            // Options
            question.options.forEachIndexed { optIndex, option ->
                val isStudentChoice = studentAnswer == optIndex
                val isCorrectOption = question.correctAnswer == optIndex

                val bgColor = when {
                    isCorrectOption -> SuccessLight
                    isStudentChoice && !isCorrect -> ErrorLight
                    else -> Color.Transparent
                }
                val borderColor = when {
                    isCorrectOption -> Success
                    isStudentChoice && !isCorrect -> Error
                    else -> SurfaceVariant
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    shape = MaterialTheme.shapes.small,
                    color = bgColor,
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        width = if (isStudentChoice || isCorrectOption) 1.5.dp else 0.5.dp,
                        brush = Brush.linearGradient(listOf(borderColor, borderColor))
                    )
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${optionLabels.getOrElse(optIndex) { "?" }}.",
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCorrectOption -> Success
                                isStudentChoice && !isCorrect -> Error
                                else -> TextSecondary
                            },
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isCorrectOption) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = Success)
                        } else if (isStudentChoice && !isCorrect) {
                            Icon(Icons.Default.Cancel, null, Modifier.size(18.dp), tint = Error)
                        }
                    }
                }
            }

            // Explanation / Feedback
            if (!feedback.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = if (isCorrect) SuccessLight.copy(0.5f) else InfoLight,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(Modifier.padding(10.dp)) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            feedback.replaceFirst(Regex("^Câu \\d+: [✅❌] (Chính xác!|Sai\\.) ?"), ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// Writing Response Section
// =====================================================

@Composable
private fun WritingResponseSection(attempt: TestAttempt) {
    val writingText = attempt.writingResponse

    if (writingText.isNullOrBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "✍️ Bài viết của bạn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Surface(
                color = SurfaceVariant.copy(0.3f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    writingText,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// =====================================================
// Speaking Transcription Section
// =====================================================

@Composable
private fun SpeakingTranscriptionSection(attempt: TestAttempt) {
    val transcription = attempt.transcription

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "🎤 Phần nói của bạn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            if (!transcription.isNullOrBlank()) {
                Text(
                    "Nội dung AI nghe được:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = SurfaceVariant.copy(0.3f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        transcription,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 22.sp
                    )
                }
            } else {
                Text(
                    "Không có dữ liệu phiên âm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

// =====================================================
// AI Feedback Section (Writing / Speaking)
// =====================================================

@Composable
private fun AiFeedbackSection(attempt: TestAttempt) {
    val aiGrading = attempt.aiGrading
    val feedbackList = attempt.feedback

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Nhận xét từ AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // AI Grading scores (if available)
            if (aiGrading != null) {
                val overallScore = (aiGrading["overallScore"] as? Number)?.toDouble()
                if (overallScore != null) {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Điểm tổng: ${overallScore}/10",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (overallScore >= 7) Success else if (overallScore >= 5) Warning else Error
                        )
                    }
                }

                // Sub-scores
                val subScores = listOf(
                    "grammar" to "📝 Ngữ pháp",
                    "vocabulary" to "📚 Từ vựng",
                    "coherence" to "🔗 Mạch lạc",
                    "pronunciation" to "🗣️ Phát âm",
                    "fluency" to "💬 Trôi chảy",
                    "taskAchievement" to "🎯 Hoàn thành"
                )

                subScores.forEach { (key, label) ->
                    val score = (aiGrading[key] as? Number)?.toDouble()
                    if (score != null) {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(120.dp)
                            )
                            LinearProgressIndicator(
                                progress = { (score / 10f).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.weight(1f).height(8.dp)
                                    .clip(MaterialTheme.shapes.small),
                                color = when {
                                    score >= 7 -> Success
                                    score >= 5 -> Warning
                                    else -> Error
                                },
                                trackColor = SurfaceVariant,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${score.toInt()}/10",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(35.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // Detailed feedback text
            if (feedbackList.isNotEmpty()) {
                HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Nhận xét chi tiết:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))

                feedbackList.forEach { fb ->
                    Surface(
                        color = Color.White.copy(0.7f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Text(
                            fb,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
