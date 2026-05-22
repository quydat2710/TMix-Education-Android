package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.ApiErrorParser
import com.tmix.education.data.api.ApiService
import com.tmix.education.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Student operations
 */
class StudentRepository(
    private val apiService: ApiService = ApiConfig.getApiService()
) {
    
    /**
     * Get student profile by ID
     */
    suspend fun getStudent(studentId: String): Result<Student> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStudent(studentId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(ApiErrorParser.parse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorParser.parseException(e)))
        }
    }
    
    /**
     * Get student schedule
     */
    suspend fun getSchedule(studentId: String): Result<List<ScheduleItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStudentSchedule(studentId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.success(emptyList()) // Return empty list if no schedule
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get student's enrolled classes
     */
    suspend fun getClasses(studentId: String): Result<List<StudentClassInfo>> = withContext(Dispatchers.IO) {
        try {
            val studentResult = getStudent(studentId)
            studentResult.map { student ->
                student.classes ?: emptyList()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get student attendance from backend
     * Backend returns pre-calculated stats, not raw session list
     */
    suspend fun getAttendanceStats(studentId: String): Result<AttendanceStats> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStudentAttendance(studentId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                val body = response.body()!!.data!!
                val stats = body.attendanceStats?.toAttendanceStats()
                    ?: AttendanceStats()
                Result.success(stats)
            } else {
                Result.success(AttendanceStats())
            }
        } catch (e: Exception) {
            Result.success(AttendanceStats()) // Fallback to empty stats
        }
    }

    /**
     * Get full attendance data: stats + detailed per-session records
     */
    suspend fun getFullAttendance(studentId: String): Result<StudentAttendanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStudentAttendance(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.success(StudentAttendanceResponse())
            }
        } catch (e: Exception) {
            Result.success(StudentAttendanceResponse())
        }
    }
    
    /**
     * Get student payments
     */
    suspend fun getPayments(
        studentId: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<List<Payment>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStudentPayments(studentId, page, limit)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.data)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update student profile
     */
    suspend fun updateProfile(
        studentId: String,
        updates: Map<String, Any>
    ): Result<Student> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateStudent(studentId, updates)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(ApiErrorParser.parse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorParser.parseException(e)))
        }
    }
}
