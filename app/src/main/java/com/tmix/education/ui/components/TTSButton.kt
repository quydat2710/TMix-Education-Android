package com.tmix.education.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.*

/**
 * TTS (Text-to-Speech) Button using Android built-in TTS engine
 */
@Composable
fun TTSButton(
    text: String,
    modifier: Modifier = Modifier,
    language: Locale = Locale.US,
    tint: Color = Color(0xFFEA580C)
) {
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = language
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    IconButton(
        onClick = {
            if (isSpeaking) {
                tts?.stop()
                isSpeaking = false
            } else {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
                isSpeaking = true
                // Auto-reset after speaking (estimate ~100ms per word)
                val words = text.split(Regex("\\s+")).size
                val durationMs = (words * 400L).coerceAtLeast(1000L)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isSpeaking = false
                }, durationMs)
            }
        },
        modifier = modifier
    ) {
        Icon(
            if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
            contentDescription = if (isSpeaking) "Dừng" else "Nghe",
            tint = if (isSpeaking) Color(0xFFDC2626) else tint
        )
    }
}
