package com.tmix.education.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.SubmitState
import com.tmix.education.ui.viewmodel.TestDetailState
import com.tmix.education.ui.viewmodel.TestViewModel

/**
 * Test Taking Screen - loads real questions from Backend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestTakingScreen(
    testId: String = "",
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    testViewModel: TestViewModel = viewModel()
) {
    var currentQuestion by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableIntStateOf(0) }
    var timerStarted by remember { mutableStateOf(false) }
    
    val testDetailState by testViewModel.testDetailState.collectAsState()
    val submitState by testViewModel.submitState.collectAsState()
    
    // Load test on composition
    LaunchedEffect(testId) {
        testViewModel.loadTestForTaking(testId)
    }
    
    when (val state = testDetailState) {
        is TestDetailState.Loading -> {
            Scaffold { padding ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TMixRed)
                        Spacer(Modifier.height(16.dp))
                        Text("Đang tải đề thi...", color = TextSecondary)
                    }
                }
            }
        }
        is TestDetailState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Lỗi") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                        Spacer(Modifier.height(16.dp))
                        Text(state.message, color = TextSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { testViewModel.loadTestForTaking(testId) },
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
        }
        is TestDetailState.Success -> {
            val test = state.test
            val questions = test.questions ?: emptyList()
            
            // Start timer when test loads
            if (!timerStarted) {
                remainingTime = test.duration * 60
                timerStarted = true
            }
            
            val answeredCount = answers.size
            val minutes = remainingTime / 60
            val seconds = remainingTime % 60
            
            // Countdown timer
            LaunchedEffect(timerStarted) {
                while (remainingTime > 0) {
                    kotlinx.coroutines.delay(1000)
                    remainingTime--
                }
                // Auto-submit when time runs out
                if (remainingTime <= 0 && questions.isNotEmpty()) {
                    val answerList = (0 until questions.size).map { answers[it] ?: -1 }
                    testViewModel.submitTest(testId, answerList)
                }
            }
            
            // Handle submit result
            when (val sState = submitState) {
                is SubmitState.Success -> {
                    // Show result screen
                    Scaffold { padding ->
                        Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                            Card(
                                shape = TMixShapes.Card,
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val attempt = sState.attempt
                                    Icon(
                                        if (attempt.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        null, Modifier.size(80.dp),
                                        tint = if (attempt.passed) Success else Error
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        if (attempt.passed) "Đạt!" else "Chưa đạt",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (attempt.passed) Success else Error
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Điểm: ${attempt.score.toInt()}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${attempt.percentage.toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = TMixNavy
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    
                                    // Feedback
                                    if (attempt.feedback.isNotEmpty()) {
                                        Text(
                                            "Nhận xét:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        attempt.feedback.forEachIndexed { i, fb ->
                                            Text(
                                                "Câu ${i + 1}: $fb",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        Spacer(Modifier.height(16.dp))
                                    }
                                    
                                    Button(
                                        onClick = {
                                            testViewModel.resetSubmitState()
                                            onSubmit()
                                        },
                                        Modifier.fillMaxWidth(),
                                        shape = TMixShapes.Button,
                                        colors = ButtonDefaults.buttonColors(containerColor = TMixNavy)
                                    ) {
                                        Text("Quay lại")
                                    }
                                }
                            }
                        }
                    }
                    return
                }
                is SubmitState.Submitting -> {
                    Scaffold { padding ->
                        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = TMixRed)
                                Spacer(Modifier.height(16.dp))
                                Text("Đang chấm bài...", color = TextSecondary)
                            }
                        }
                    }
                    return
                }
                is SubmitState.Error -> {
                    // Continue showing test, show error snackbar
                }
                else -> {}
            }
            
            if (questions.isEmpty()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(test.title) },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Đề thi chưa có câu hỏi", color = TextSecondary)
                    }
                }
                return
            }
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(test.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Còn lại: ${String.format("%02d:%02d", minutes, seconds)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (remainingTime < 300) Error else TextSecondary
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                            }
                        },
                        actions = {
                            TextButton(onClick = { showSubmitDialog = true }) {
                                Text("Nộp bài", color = TMixRed)
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(shadowElevation = 8.dp) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { if (currentQuestion > 0) currentQuestion-- },
                                enabled = currentQuestion > 0,
                                shape = TMixShapes.Button
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Trước")
                            }
                            
                            Text(
                                "${currentQuestion + 1}/${questions.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = {
                                    if (currentQuestion < questions.size - 1) currentQuestion++
                                    else showSubmitDialog = true
                                },
                                shape = TMixShapes.Button,
                                colors = ButtonDefaults.buttonColors(containerColor = TMixNavy)
                            ) {
                                Text(if (currentQuestion < questions.size - 1) "Tiếp" else "Nộp")
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                            }
                        }
                    }
                }
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(16.dp)
                ) {
                    // Progress
                    LinearProgressIndicator(
                        progress = { answeredCount.toFloat() / questions.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = TMixRed,
                        trackColor = SurfaceVariant
                    )
                    Text(
                        "Đã trả lời: $answeredCount/${questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Question
                    val question = questions[currentQuestion]
                    
                    Text(
                        "Câu ${currentQuestion + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TMixNavy,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        question.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Options
                    question.options.forEachIndexed { index, option ->
                        val isSelected = answers[currentQuestion] == index
                        
                        Card(
                            onClick = { answers[currentQuestion] = index },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = TMixShapes.Card,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) TMixNavy.copy(0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) BorderStroke(2.dp, TMixNavy) else null
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { answers[currentQuestion] = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = TMixNavy)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${('A' + index)}. $option",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            // Submit dialog
            if (showSubmitDialog) {
                AlertDialog(
                    onDismissRequest = { showSubmitDialog = false },
                    title = { Text("Nộp bài?") },
                    text = { Text("Bạn đã trả lời $answeredCount/${questions.size} câu.\nBạn có chắc muốn nộp bài?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSubmitDialog = false
                                val answerList = (0 until questions.size).map { answers[it] ?: -1 }
                                testViewModel.submitTest(testId, answerList)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) {
                            Text("Nộp bài")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubmitDialog = false }) {
                            Text("Tiếp tục làm")
                        }
                    }
                )
            }
        }
    }
}
