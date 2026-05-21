package com.tmix.education.ui.components

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tmix.education.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.*

/**
 * TTS (Text-to-Speech) Button
 * 
 * Uses server-side Piper TTS (VITS neural model) with automatic
 * fallback to Android built-in TTS when server is unavailable.
 * 
 * Architecture:
 *   Text -> Backend API -> Piper TTS (VITS/ONNX) -> Audio WAV -> MediaPlayer
 *   Fallback: Text -> Android TextToSpeech Engine
 */
@Composable
fun TTSButton(
    text: String,
    modifier: Modifier = Modifier,
    language: Locale = Locale.US,
    tint: Color = Color(0xFFEA580C)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSpeaking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var localTts by remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize fallback Android TTS
    LaunchedEffect(Unit) {
        localTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                localTts?.language = language
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            localTts?.stop()
            localTts?.shutdown()
        }
    }

    IconButton(
        onClick = {
            if (isSpeaking) {
                // Stop playback
                mediaPlayer?.release()
                mediaPlayer = null
                localTts?.stop()
                isSpeaking = false
                isLoading = false
            } else if (!isLoading) {
                isLoading = true
                scope.launch {
                    try {
                        // Try server-side TTS (Piper/VITS)
                        val audioFile = downloadTTSAudio(context, text)
                        if (audioFile != null) {
                            withContext(Dispatchers.Main) {
                                val player = MediaPlayer()
                                player.setDataSource(audioFile.absolutePath)
                                player.prepare()
                                player.setOnCompletionListener {
                                    isSpeaking = false
                                    it.release()
                                    mediaPlayer = null
                                }
                                player.start()
                                mediaPlayer = player
                                isSpeaking = true
                                isLoading = false
                            }
                        } else {
                            throw Exception("Server TTS unavailable")
                        }
                    } catch (e: Exception) {
                        // Fallback to Android built-in TTS
                        Log.w("TTSButton", "Server TTS failed, using fallback: ${e.message}")
                        withContext(Dispatchers.Main) {
                            localTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
                            isSpeaking = true
                            isLoading = false
                            // Auto-reset after estimated speaking duration
                            val words = text.split(Regex("\\s+")).size
                            val durationMs = (words * 400L).coerceAtLeast(1000L)
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isSpeaking = false
                            }, durationMs)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = tint,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                contentDescription = if (isSpeaking) "Stop" else "Listen (Piper TTS)",
                tint = if (isSpeaking) Color(0xFFDC2626) else tint
            )
        }
    }
}

/**
 * Download TTS audio from the backend server.
 * Caches audio locally to avoid redundant API calls.
 */
private suspend fun downloadTTSAudio(context: android.content.Context, text: String): File? {
    return withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val cacheKey = MessageDigest.getInstance("MD5")
                .digest("$text|1.0".toByteArray())
                .joinToString("") { "%02x".format(it) }
            val cacheFile = File(context.cacheDir, "tts_$cacheKey.wav")
            
            if (cacheFile.exists() && cacheFile.length() > 100) {
                return@withContext cacheFile
            }

            // Build TTS API URL
            val baseUrl = ApiConfig.BASE_URL.removeSuffix("/")
            val url = URL("$baseUrl/tts/synthesize")
            
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 10000
            conn.readTimeout = 30000
            conn.doOutput = true

            // Send request
            val body = """{"text":"${text.replace("\"", "\\\"").replace("\n", " ")}","speed":1.0}"""
            conn.outputStream.use { it.write(body.toByteArray()) }

            if (conn.responseCode == 200) {
                // Save audio to cache
                val audioBytes = conn.inputStream.use { it.readBytes() }
                FileOutputStream(cacheFile).use { it.write(audioBytes) }
                return@withContext cacheFile
            } else {
                Log.w("TTSButton", "Server returned ${conn.responseCode}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.w("TTSButton", "TTS download failed: ${e.message}")
            return@withContext null
        }
    }
}

/**
 * Standalone function to speak text using Piper TTS.
 * Called from ChatbotScreen for auto-read functionality.
 * Falls back to Android TTS if server is unavailable.
 */
suspend fun speakWithTTS(context: android.content.Context, text: String) {
    try {
        val audioFile = downloadTTSAudio(context, text)
        if (audioFile != null) {
            withContext(Dispatchers.Main) {
                val player = MediaPlayer()
                player.setDataSource(audioFile.absolutePath)
                player.prepare()
                player.setOnCompletionListener { it.release() }
                player.start()
            }
        } else {
            // Fallback to Android TTS
            withContext(Dispatchers.Main) {
                val tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        // Will be set up in the callback
                    }
                }
                // Small delay for TTS engine init
                kotlinx.coroutines.delay(500)
                tts.language = Locale.US
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "auto_${System.currentTimeMillis()}")
            }
        }
    } catch (e: Exception) {
        Log.w("TTSButton", "speakWithTTS failed: ${e.message}")
    }
}
