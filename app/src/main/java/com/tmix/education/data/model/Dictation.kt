package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Dictation Practice - Data Models
 * Used for the AI Dictation feature where students listen to TTS audio
 * and type what they hear.
 */

/** Metadata for a random dictation sentence (text is hidden) */
data class DictationSentence(
    @SerializedName("id")
    val id: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("level")
    val level: String
)

/** Request body for checking a dictation answer */
data class DictationCheckRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("answer")
    val answer: String,

    @SerializedName("forceReveal")
    val forceReveal: Boolean = false
)

/** Request body for generating dictation audio */
data class DictationAudioRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("speed")
    val speed: Double = 1.0
)

/** Result of checking a dictation answer */
data class DictationResult(
    @SerializedName("isCorrect")
    val isCorrect: Boolean,

    @SerializedName("score")
    val score: Int,

    @SerializedName("totalWords")
    val totalWords: Int,

    @SerializedName("correctWords")
    val correctWords: Int,

    @SerializedName("wordResults")
    val wordResults: List<WordResult>,

    @SerializedName("originalSentence")
    val originalSentence: String? = null
)

/** Individual word result in a dictation check */
data class WordResult(
    @SerializedName("word")
    val word: String,

    @SerializedName("correct")
    val correct: Boolean,

    @SerializedName("expected")
    val expected: String? = null
)
