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

    /** Effective rate: present + late count as "attended" */
    val effectiveAttendanceRate: Float
        get() = if (total > 0) (present + late).toFloat() / total else 0f
}

/**
 * Backend response for GET /sessions/student/:studentId
 * The backend returns pre-calculated attendance stats + detailed per-session records
 */
data class StudentAttendanceResponse(
    @SerializedName("student")
    val student: Map<String, Any>? = null,

    @SerializedName("attendanceStats")
    val attendanceStats: AttendanceStatsRaw? = null,

    @SerializedName("detailedAttendance")
    val detailedAttendance: List<AttendanceDetail>? = null,

    @SerializedName("totalRecord")
    val totalRecord: Int = 0
)

/**
 * Per-session attendance detail
 */
data class AttendanceDetail(
    @SerializedName("date")
    val date: String,

    @SerializedName("class")
    val classInfo: AttendanceClassInfo? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("note")
    val note: String? = null
)

data class AttendanceClassInfo(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("grade")
    val grade: String? = null
)

data class AttendanceStatsRaw(
    @SerializedName("totalSessions")
    val totalSessions: Int = 0,

    @SerializedName("presentSessions")
    val presentSessions: Int = 0,

    @SerializedName("absentSessions")
    val absentSessions: Int = 0,

    @SerializedName("lateSessions")
    val lateSessions: Int = 0
) {
    fun toAttendanceStats() = AttendanceStats(
        total = totalSessions,
        present = presentSessions,
        absent = absentSessions,
        late = lateSessions
    )
}

