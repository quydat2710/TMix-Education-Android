package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * MC Question from Backend
 */
data class MCQuestion(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("question")
    val question: String,
    
    @SerializedName("options")
    val options: List<String>,
    
    @SerializedName("correctAnswer")
    val correctAnswer: Int? = null, // null when fetched by student (hidden)
    
    @SerializedName("explanation")
    val explanation: String? = null,
    
    @SerializedName("points")
    val points: Int = 1
)

/**
 * Test entity from Backend
 */
data class Test(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("classId")
    val classId: String? = null,
    
    @SerializedName("className")
    val className: String? = null,
    
    @SerializedName("teacherName")
    val teacherName: String? = null,
    
    @SerializedName("duration")
    val duration: Int = 30,
    
    @SerializedName("totalPoints")
    val totalPoints: Int = 0,
    
    @SerializedName("passingScore")
    val passingScore: Int = 70,
    
    @SerializedName("questionCount")
    val questionCount: Int = 0,
    
    @SerializedName("questions")
    val questions: List<MCQuestion>? = null,
    
    @SerializedName("status")
    val status: String = "published",
    
    @SerializedName("hasAttempted")
    val hasAttempted: Boolean = false,
    
    @SerializedName("lastAttempt")
    val lastAttempt: LastAttemptInfo? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * Short attempt info (returned with test list)
 */
data class LastAttemptInfo(
    @SerializedName("score")
    val score: Double = 0.0,
    
    @SerializedName("percentage")
    val percentage: Double = 0.0,
    
    @SerializedName("passed")
    val passed: Boolean = false,
    
    @SerializedName("submittedAt")
    val submittedAt: String? = null
)

/**
 * Test attempt result from Backend
 */
data class TestAttempt(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("testId")
    val testId: String,
    
    @SerializedName("studentId")
    val studentId: String? = null,
    
    @SerializedName("answers")
    val answers: List<Int> = emptyList(),
    
    @SerializedName("score")
    val score: Double = 0.0,
    
    @SerializedName("percentage")
    val percentage: Double = 0.0,
    
    @SerializedName("passed")
    val passed: Boolean = false,
    
    @SerializedName("feedback")
    val feedback: List<String> = emptyList(),
    
    @SerializedName("test")
    val test: Test? = null,
    
    @SerializedName("submittedAt")
    val submittedAt: String? = null,
    
    @SerializedName("gradedAt")
    val gradedAt: String? = null
)

/**
 * Submit test request body
 */
data class SubmitTestRequest(
    @SerializedName("answers")
    val answers: List<Int>
)
