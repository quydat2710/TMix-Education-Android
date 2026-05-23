package com.tmix.education.ui.screens

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.model.*
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private data class LevelOption(
    val value: String,
    val label: String,
    val desc: String,
    val color: Color,
    val icon: ImageVector
)

private val LEVELS = listOf(
    LevelOption("easy", "Cơ bản", "Câu ngắn, từ vựng đơn giản", Color(0xFF16A34A), Icons.Filled.SignalCellularAlt1Bar),
    LevelOption("medium", "Trung bình", "Câu dài hơn, từ vựng phong phú", Color(0xFFD97706), Icons.Filled.SignalCellularAlt2Bar),
    LevelOption("hard", "Nâng cao", "Câu phức tạp, chủ đề chuyên sâu", Color(0xFFDC2626), Icons.Filled.SignalCellularAlt),
)

private const val MAX_ATTEMPTS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictationPracticeScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ApiConfig.getApiService() }

    // State
    var selectedLevel by remember { mutableStateOf("") }
    var sentenceId by remember { mutableStateOf("") }
    var sentenceCategory by remember { mutableStateOf("") }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var audioLoading by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var userAnswer by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<DictationResult?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var stats by remember { mutableStateOf(0 to 0) } // total, correct
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }

    // Fetch random sentence + audio
    fun fetchSentence(level: String) {
        scope.launch {
            selectedLevel = level
            sentenceId = ""
            result = null
            showResult = false
            userAnswer = ""
            error = ""
            attempts = 0
            audioLoading = true

            try {
                val metaRes = withContext(Dispatchers.IO) { api.getDictationRandom(level) }
                val sentence = metaRes.body()?.data
                if (sentence == null) { error = "Không thể tải bài nghe."; audioLoading = false; return@launch }

                sentenceId = sentence.id
                sentenceCategory = sentence.category

                // Fetch audio
                val audioRes = withContext(Dispatchers.IO) {
                    api.getDictationAudio(DictationAudioRequest(sentence.id, speed.toDouble()))
                }
                if (audioRes.isSuccessful) {
                    val bytes = withContext(Dispatchers.IO) { audioRes.body()?.bytes() }
                    if (bytes != null) {
                        val file = File(context.cacheDir, "dictation_${sentence.id}.wav")
                        withContext(Dispatchers.IO) { FileOutputStream(file).use { it.write(bytes) } }
                        audioFile = file
                    }
                } else {
                    error = "Không thể tạo audio. TTS server có thể chưa khởi động."
                }
            } catch (e: Exception) {
                error = "Lỗi: ${e.message}"
            } finally {
                audioLoading = false
            }
        }
    }

    // Play audio
    fun playAudio() {
        val file = audioFile ?: return
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
            mediaPlayer.setOnCompletionListener { isPlaying = false }
        } catch (e: Exception) {
            error = "Không thể phát audio."
            isPlaying = false
        }
    }

    // Change speed and re-fetch audio
    fun changeSpeed(newSpeed: Float) {
        speed = newSpeed
        if (sentenceId.isNotEmpty()) {
            scope.launch {
                audioLoading = true
                try {
                    val audioRes = withContext(Dispatchers.IO) {
                        api.getDictationAudio(DictationAudioRequest(sentenceId, newSpeed.toDouble()))
                    }
                    if (audioRes.isSuccessful) {
                        val bytes = withContext(Dispatchers.IO) { audioRes.body()?.bytes() }
                        if (bytes != null) {
                            val file = File(context.cacheDir, "dictation_${sentenceId}_${newSpeed}.wav")
                            withContext(Dispatchers.IO) { FileOutputStream(file).use { it.write(bytes) } }
                            audioFile = file
                        }
                    }
                } catch (_: Exception) {}
                finally { audioLoading = false }
            }
        }
    }

    // Check answer
    fun checkAnswer() {
        if (userAnswer.isBlank() || sentenceId.isEmpty()) return
        scope.launch {
            checking = true
            error = ""
            try {
                val nextAttempt = attempts + 1
                val isLast = nextAttempt >= MAX_ATTEMPTS
                val res = withContext(Dispatchers.IO) {
                    api.checkDictation(DictationCheckRequest(sentenceId, userAnswer.trim(), isLast))
                }
                val data = res.body()?.data
                if (data != null) {
                    result = data
                    attempts = nextAttempt
                    showResult = true
                    stats = (stats.first + 1) to (stats.second + if (data.isCorrect) 1 else 0)
                } else {
                    error = "Kiểm tra thất bại."
                }
            } catch (e: Exception) {
                error = "Lỗi: ${e.message}"
            } finally {
                checking = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Hearing, null, tint = Info, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Luyện Chính Tả", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    if (stats.first > 0) {
                        Surface(
                            color = Success.copy(0.12f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "${stats.second}/${stats.first} đúng",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ═══ Header Banner ═══
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Info, Color(0xFF2563EB), Color(0xFF7C3AED))))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "🎧 Nghe và viết lại",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Rèn luyện kỹ năng nghe hiểu tiếng Anh",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.85f)
                        )
                    }
                }
            }

            // ═══ Level Selection ═══
            if (sentenceId.isEmpty() && !audioLoading) {
                Text(
                    "Chọn mức độ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LEVELS.forEach { lv ->
                    Card(
                        onClick = { fetchSentence(lv.value) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(lv.color.copy(0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(lv.icon, null, tint = lv.color, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(lv.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(lv.desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
                        }
                    }
                }
            }

            // ═══ Loading ═══
            if (audioLoading) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Info)
                        Spacer(Modifier.height(12.dp))
                        Text("Đang chuẩn bị bài nghe...", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ═══ Dictation Area ═══
            if (sentenceId.isNotEmpty() && !audioLoading) {
                // Level + Category badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val lv = LEVELS.find { it.value == selectedLevel }
                    if (lv != null) {
                        Surface(color = lv.color.copy(0.12f), shape = RoundedCornerShape(8.dp)) {
                            Text(lv.label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = lv.color)
                        }
                    }
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                        Text(sentenceCategory, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    if (attempts > 0) {
                        Spacer(Modifier.weight(1f))
                        Surface(color = Warning.copy(0.12f), shape = RoundedCornerShape(8.dp)) {
                            Text("Lần ${attempts}/$MAX_ATTEMPTS", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning)
                        }
                    }
                }

                // Audio Player Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = InfoTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Nhấn nút để nghe bài",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = Info)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { playAudio() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Info,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(if (isPlaying) Icons.Filled.VolumeUp else Icons.Filled.PlayArrow,
                                    null, Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isPlaying) "Đang phát..." else "Nghe", fontWeight = FontWeight.Bold)
                            }

                            OutlinedIconButton(
                                onClick = { playAudio() },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Filled.Replay, "Nghe lại", tint = Info)
                            }
                        }

                        // Speed slider
                        Column(Modifier.fillMaxWidth()) {
                            Text("Tốc độ: ${String.format("%.1f", speed)}x",
                                style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Slider(
                                value = speed,
                                onValueChange = { speed = it },
                                onValueChangeFinished = { changeSpeed(speed) },
                                valueRange = 0.5f..1.5f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = Info,
                                    activeTrackColor = Info
                                )
                            )
                        }
                    }
                }

                // Answer input (only when no result showing)
                if (!showResult) {
                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        label = { Text("Viết lại những gì bạn nghe được") },
                        placeholder = { Text("Type what you hear...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                sentenceId = ""
                                result = null
                                userAnswer = ""
                                attempts = 0
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.ArrowBack, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Quay lại")
                        }

                        Button(
                            onClick = { checkAnswer() },
                            enabled = userAnswer.isNotBlank() && !checking,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Info)
                        ) {
                            if (checking) {
                                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Send, null, Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(if (checking) "Đang kiểm tra..." else "Kiểm tra")
                        }
                    }
                }
            }

            // ═══ Error ═══
            if (error.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = Error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(error, color = Error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = { error = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, null, tint = Error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ═══ Result Card (inline, not dialog) ═══
            if (showResult && result != null) {
                val r = result!!
                val finished = r.isCorrect || attempts >= MAX_ATTEMPTS

                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Score header
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (r.isCorrect) SuccessTint else RedTint)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    if (r.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    null,
                                    tint = if (r.isCorrect) Success else Error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${r.score}%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (r.isCorrect) Success else Error
                                )
                                Text(
                                    "${r.correctWords}/${r.totalWords} từ đúng",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { r.score / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = if (r.isCorrect) Success else Error,
                                    trackColor = if (r.isCorrect) Success.copy(0.15f) else Error.copy(0.15f)
                                )
                            }
                        }

                        // Word-by-word
                        Text("Bài làm của bạn:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                        @Composable
                        fun WordChips() {
                            val rows = mutableListOf<MutableList<WordResult>>()
                            var currentRow = mutableListOf<WordResult>()
                            r.wordResults.forEach { wr ->
                                currentRow.add(wr)
                                if (currentRow.size >= 5) {
                                    rows.add(currentRow)
                                    currentRow = mutableListOf()
                                }
                            }
                            if (currentRow.isNotEmpty()) rows.add(currentRow)

                            rows.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    row.forEach { wr ->
                                        Surface(
                                            color = if (wr.correct) Success.copy(0.12f) else Error.copy(0.12f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp, if (wr.correct) Success.copy(0.3f) else Error.copy(0.3f)
                                            )
                                        ) {
                                            Column(
                                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    wr.word,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (wr.correct) Success else Error,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                if (!wr.correct && finished && wr.expected != null) {
                                                    Text(
                                                        "→ ${wr.expected}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Info,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        WordChips()

                        // Reveal or retry
                        if (finished) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SuccessTint),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = Success, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (r.isCorrect) "Hoàn hảo! Đây là câu gốc:" else "Hết số lần thử. Đáp án:",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF064E3B),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "\"${r.originalSentence ?: ""}\"",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF064E3B),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }

                            Button(
                                onClick = { showResult = false; fetchSentence(selectedLevel) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Icon(Icons.Filled.NavigateNext, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Câu tiếp theo", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarningTint),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, null, tint = Warning, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Chưa đúng. Còn ${MAX_ATTEMPTS - attempts} lần thử. Nghe kỹ lại nhé!",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Button(
                                onClick = { showResult = false; result = null },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Info)
                            ) {
                                Icon(Icons.Filled.Replay, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Đóng & Nghe lại", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
