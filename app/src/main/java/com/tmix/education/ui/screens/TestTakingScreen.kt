package com.tmix.education.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.SubmitState
import com.tmix.education.ui.viewmodel.TestDetailState
import com.tmix.education.ui.viewmodel.TestViewModel
import kotlinx.coroutines.delay
import java.io.File
import com.tmix.education.ui.components.TTSButton

/**
 * Test Taking Screen - supports MC, Listening, Writing, Speaking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestTakingScreen(
    testId: String = "",
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    testViewModel: TestViewModel = viewModel()
) {
    val testDetailState by testViewModel.testDetailState.collectAsState()
    val submitState by testViewModel.submitState.collectAsState()
    
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
            val skillType = test.skillType ?: "reading"
            
            // Handle submit state results
            when (val sState = submitState) {
                is SubmitState.Success -> {
                    ResultScreen(attempt = sState.attempt, onBack = {
                        testViewModel.resetSubmitState()
                        onSubmit()
                    })
                    return
                }
                is SubmitState.Submitting -> {
                    Scaffold { padding ->
                        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = TMixRed)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    if (skillType == "writing") "AI đang chấm bài viết..."
                                    else if (skillType == "speaking") "AI đang chấm bài nói..."
                                    else "Đang chấm bài...",
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    return
                }
                else -> {}
            }
            
            // Route to appropriate screen based on skillType
            when (skillType) {
                "writing" -> WritingTestContent(test, testId, testViewModel, onBack)
                "speaking" -> SpeakingTestContent(test, testId, testViewModel, onBack)
                else -> MCTestContent(test, testId, testViewModel, onBack, onSubmit)
            }
        }
    }
}

// ============================================================
// MC Test (Reading / Listening)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MCTestContent(
    test: com.tmix.education.data.model.Test,
    testId: String,
    testViewModel: TestViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val questions = test.questions ?: emptyList()
    var currentQuestion by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableIntStateOf(test.duration * 60) }
    var timerStarted by remember { mutableStateOf(false) }
    val isListening = test.skillType == "listening"
    
    // Audio player for listening
    val context = LocalContext.current
    var isAudioPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }
    var audioPrepared by remember { mutableStateOf(false) }
    
    // Setup audio for listening tests
    if (isListening && test.audioUrl != null && !audioPrepared) {
        LaunchedEffect(test.audioUrl) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(test.audioUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { audioPrepared = true }
                mediaPlayer.setOnCompletionListener { isAudioPlaying = false }
            } catch (_: Exception) {}
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }
    
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
            delay(1000)
            remainingTime--
        }
        if (remainingTime <= 0 && questions.isNotEmpty()) {
            val answerList = (0 until questions.size).map { answers[it] ?: -1 }
            testViewModel.submitTest(testId, answerList)
        }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isListening) {
                                Icon(Icons.Default.Headphones, null, Modifier.size(16.dp), tint = TMixRed)
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(test.title, style = MaterialTheme.typography.titleSmall)
                        }
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
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            // Listening: Audio player
            if (isListening && test.audioUrl != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🎧 Nghe đoạn audio", fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = {
                                    if (audioPrepared) {
                                        if (isAudioPlaying) {
                                            mediaPlayer.pause()
                                            isAudioPlaying = false
                                        } else {
                                            mediaPlayer.start()
                                            isAudioPlaying = true
                                        }
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFEA580C))
                            ) {
                                Icon(
                                    if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    "Play/Pause", tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (!audioPrepared) "Đang tải audio..."
                                else if (isAudioPlaying) "Đang phát..."
                                else "Nhấn để nghe",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
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
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    question.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TTSButton(
                    text = "${question.question}. ${question.options.mapIndexed { i, opt -> "${('A' + i)}: $opt" }.joinToString(". ")}"
                )
            }
            
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

// ============================================================
// Writing Test
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingTestContent(
    test: com.tmix.education.data.model.Test,
    testId: String,
    testViewModel: TestViewModel,
    onBack: () -> Unit
) {
    var writingText by remember { mutableStateOf("") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableIntStateOf(test.duration * 60) }
    
    val wordCount = if (writingText.isBlank()) 0 else writingText.trim().split(Regex("\\s+")).size
    val question = test.questions?.firstOrNull()
    val minWords = question?.minWords ?: 0
    val minutes = remainingTime / 60
    val seconds = remainingTime % 60
    
    // Countdown
    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
        if (writingText.isNotBlank()) {
            testViewModel.submitWriting(testId, writingText)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = Color(0xFF16A34A))
                            Spacer(Modifier.width(4.dp))
                            Text(test.title, style = MaterialTheme.typography.titleSmall)
                        }
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
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Số từ: $wordCount${if (minWords > 0) " / $minWords" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (wordCount < minWords) Error else Success
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showSubmitDialog = true },
                        Modifier.fillMaxWidth(),
                        enabled = wordCount >= 5,
                        shape = TMixShapes.Button,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Nộp bài viết")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            // Prompt
            if (question?.prompt != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📝 Đề bài:", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        Spacer(Modifier.height(4.dp))
                        Text(question.prompt, color = Color(0xFF166534))
                        if (question.minWords != null || question.maxWords != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                buildString {
                                    append("Yêu cầu: ")
                                    question.minWords?.let { append("tối thiểu $it từ") }
                                    if (question.minWords != null && question.maxWords != null) append(" — ")
                                    question.maxWords?.let { append("tối đa $it từ") }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Reading passage
            if (test.passage != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📖 Đoạn văn tham khảo:", fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        Spacer(Modifier.height(4.dp))
                        Text(test.passage, color = Color(0xFF1E3A5A), lineHeight = 22.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Text editor
            OutlinedTextField(
                value = writingText,
                onValueChange = { writingText = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                placeholder = { Text("Nhập bài viết của bạn tại đây...") },
                shape = TMixShapes.Card
            )
        }
    }
    
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Nộp bài viết?") },
            text = { Text("Bài viết $wordCount từ. AI sẽ chấm điểm tự động.\nBạn có chắc muốn nộp?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        testViewModel.submitWriting(testId, writingText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) { Text("Nộp bài") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Tiếp tục viết") }
            }
        )
    }
}

// ============================================================
// Speaking Test
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingTestContent(
    test: com.tmix.education.data.model.Test,
    testId: String,
    testViewModel: TestViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordingDone by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableIntStateOf(0) }
    var audioFilePath by remember { mutableStateOf("") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val recorder = remember { mutableStateOf<MediaRecorder?>(null) }
    val player = remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    
    val question = test.questions?.firstOrNull()
    val maxTime = test.maxRecordingTime ?: 60
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }
    
    // Recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingTime++
                if (recordingTime >= maxTime) {
                    // Stop recording when max time reached
                    isRecording = false
                    try {
                        recorder.value?.stop()
                        recorder.value?.release()
                        recorder.value = null
                        recordingDone = true
                    } catch (_: Exception) {}
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try { recorder.value?.release() } catch (_: Exception) {}
            try { player.value?.release() } catch (_: Exception) {}
        }
    }
    
    // Pulsing animation for recording indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val buttonColor by animateColorAsState(
        targetValue = if (isRecording) Color(0xFFDC2626) else Color(0xFFEA580C),
        label = "buttonColor"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, null, Modifier.size(16.dp), tint = Color(0xFFEA580C))
                        Spacer(Modifier.width(4.dp))
                        Text(test.title, style = MaterialTheme.typography.titleSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Prompt
            if (question?.prompt != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📝 Đề bài:", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        Spacer(Modifier.height(4.dp))
                        Text(question.prompt, color = Color(0xFF166534))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            
            // Reference text
            if (question?.referenceText != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📖 Đoạn văn tham khảo:", fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        Spacer(Modifier.height(4.dp))
                        Text(question.referenceText, color = Color(0xFF1E3A5A), lineHeight = 22.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            
            Text(
                "⏱ Thời gian ghi âm tối đa: ${maxTime} giây",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9A3412)
            )
            
            Spacer(Modifier.height(32.dp))
            
            if (!hasPermission) {
                // Request permission
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                ) {
                    Icon(Icons.Default.Mic, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cho phép ghi âm")
                }
            } else if (!recordingDone) {
                // Record button
                FilledIconButton(
                    onClick = {
                        if (isRecording) {
                            // Stop
                            isRecording = false
                            try {
                                recorder.value?.stop()
                                recorder.value?.release()
                                recorder.value = null
                                recordingDone = true
                            } catch (_: Exception) {}
                        } else {
                            // Start
                            val file = File(context.cacheDir, "speaking_${System.currentTimeMillis()}.m4a")
                            audioFilePath = file.absolutePath
                            recordingTime = 0
                            try {
                                val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    MediaRecorder(context)
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaRecorder()
                                }
                                mr.setAudioSource(MediaRecorder.AudioSource.MIC)
                                mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                mr.setAudioSamplingRate(44100)
                                mr.setAudioEncodingBitRate(128000)
                                mr.setOutputFile(file.absolutePath)
                                mr.prepare()
                                mr.start()
                                recorder.value = mr
                                isRecording = true
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.size(80.dp).then(
                        if (isRecording) Modifier.scale(pulseScale) else Modifier
                    ),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = buttonColor)
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Dừng" else "Ghi âm",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                if (isRecording) {
                    Text(
                        "🔴 Đang ghi âm... ${recordingTime}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    Text("Nhấn để bắt đầu ghi âm", color = TextSecondary)
                }
            } else {
                // Recording done - playback + submit
                Card(
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅ Đã ghi âm xong!", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        Text("Thời lượng: ${recordingTime}s", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Play
                            OutlinedButton(onClick = {
                                if (isPlaying) {
                                    player.value?.pause()
                                    isPlaying = false
                                } else {
                                    try {
                                        player.value?.release()
                                        val mp = MediaPlayer()
                                        mp.setDataSource(audioFilePath)
                                        mp.prepare()
                                        mp.start()
                                        mp.setOnCompletionListener { isPlaying = false }
                                        player.value = mp
                                        isPlaying = true
                                    } catch (_: Exception) {}
                                }
                            }) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    null, Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (isPlaying) "Tạm dừng" else "Nghe lại")
                            }
                            
                            // Re-record
                            OutlinedButton(onClick = {
                                recordingDone = false
                                recordingTime = 0
                                player.value?.release()
                                player.value = null
                                isPlaying = false
                            }) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ghi âm lại")
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Submit button
                Button(
                    onClick = { showSubmitDialog = true },
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nộp bài nói")
                }
            }
        }
    }
    
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Nộp bài nói?") },
            text = { Text("Thời lượng: ${recordingTime}s.\nAI sẽ chấm phát âm, trôi chảy và chính xác.\nBạn có chắc muốn nộp?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        val file = File(audioFilePath)
                        testViewModel.submitSpeaking(testId, file)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) { Text("Nộp bài") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Ghi âm lại") }
            }
        )
    }
}

// ============================================================
// Result Screen (shared)
// ============================================================
@Composable
fun ResultScreen(
    attempt: com.tmix.education.data.model.TestAttempt,
    onBack: () -> Unit
) {
    val skillType = attempt.test?.skillType
    
    Scaffold { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = TMixShapes.Card,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        "Điểm: ${attempt.score}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${attempt.percentage.toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TMixNavy
                    )
                    
                    // AI grading details
                    if (attempt.aiGrading != null) {
                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "🤖 Nhận xét AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        val grading = attempt.aiGrading
                        val scoreKeys = listOf(
                            "overallScore" to "Tổng",
                            "grammar" to "Ngữ pháp",
                            "vocabulary" to "Từ vựng",
                            "coherence" to "Liên kết",
                            "pronunciation" to "Phát âm",
                            "fluency" to "Trôi chảy",
                            "accuracy" to "Chính xác"
                        )
                        
                        scoreKeys.forEach { (key, label) ->
                            val value = grading[key]
                            if (value != null) {
                                val score = when (value) {
                                    is Number -> value.toDouble()
                                    is Map<*, *> -> (value["score"] as? Number)?.toDouble()
                                    else -> null
                                }
                                if (score != null) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                        Text("${score}/10", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        // Detailed feedback
                        val feedback = grading["detailedFeedback"]
                        if (feedback is String && feedback.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                            ) {
                                Text(
                                    feedback,
                                    Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                    
                    // Transcription for speaking
                    if (skillType == "speaking" && attempt.transcription != null) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF))
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("📝 Phiêm âm:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7C3AED))
                                Text(attempt.transcription, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    // MC feedback
                    if (skillType != "writing" && skillType != "speaking" && attempt.feedback.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
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
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = onBack,
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
}
