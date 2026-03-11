package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Schedule time slots
 */
data class TimeSlots(
    @SerializedName("start_time")
    val startTime: String,
    
    @SerializedName("end_time")
    val endTime: String
)

/**
 * Class schedule
 */
data class Schedule(
    @SerializedName("start_date")
    val startDate: String?,
    
    @SerializedName("end_date")
    val endDate: String?,
    
    @SerializedName("days_of_week")
    val daysOfWeek: List<String>?,
    
    @SerializedName("time_slots")
    val timeSlots: TimeSlots?
)

/**
 * Teacher info (simplified)
 */
data class Teacher(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("avatar")
    val avatar: String? = null
)

/**
 * Class/Course entity matching Backend Class domain
 */
data class ClassInfo(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("grade")
    val grade: Int? = null,
    
    @SerializedName("section")
    val section: Int? = null,
    
    @SerializedName("year")
    val year: Int? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("feePerLesson")
    val feePerLesson: Double? = null,
    
    @SerializedName("status")
    val status: String = "active", // active, upcoming, closed
    
    @SerializedName("max_student")
    val maxStudent: Int? = null,
    
    @SerializedName("room")
    val room: String? = null,
    
    @SerializedName("schedule")
    val schedule: Schedule? = null,
    
    @SerializedName("teacher")
    val teacher: Teacher? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

/**
 * Student's class enrollment info
 */
data class StudentClassInfo(
    @SerializedName("discountPercent")
    val discountPercent: Double = 0.0,
    
    @SerializedName("class")
    val classInfo: ClassInfo,
    
    @SerializedName("isActive")
    val isActive: Boolean = true
)
