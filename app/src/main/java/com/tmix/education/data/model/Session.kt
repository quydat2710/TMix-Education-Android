package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Attendance status enum
 */
object AttendanceStatus {
    const val PRESENT = "present"
    const val ABSENT = "absent"
    const val LATE = "late"
    const val EXCUSED = "excused"
}

/**
 * Attendance record for a student in a session
 */
data class AttendanceRecord(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("student")
    val student: Student? = null,
    
    @SerializedName("note")
    val note: String? = null,
    
    @SerializedName("isModified")
    val isModified: Boolean = false
)

/**
 * Session/Attendance entity matching Backend Session domain
 */
data class Session(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("class")
    val classInfo: ClassInfo? = null,
    
    @SerializedName("attendances")
    val attendances: List<AttendanceRecord>? = null
)

/**
 * Student schedule item (for daily schedule display)
 */
data class ScheduleItem(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("className")
    val className: String? = null,
    
    @SerializedName("teacher")
    val teacherName: String? = null,
    
    @SerializedName("room")
    val room: String? = null,
    
    @SerializedName("startTime")
    val startTime: String? = null,
    
    @SerializedName("endTime")
    val endTime: String? = null,
    
    @SerializedName("date")
    val date: String? = null
)

/**
 * Attendance statistics
 */
data class AttendanceStats(
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0
) {
    val attendanceRate: Float
        get() = if (total > 0) present.toFloat() / total else 0f
}
