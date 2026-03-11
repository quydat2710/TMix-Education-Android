package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
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
                val errorMsg = response.body()?.message ?: "Failed to get student"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
     * Get student attendance history
     */
    suspend fun getAttendanceHistory(studentId: String): Result<List<Session>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStudentAttendance(studentId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate attendance statistics from history
     */
    suspend fun getAttendanceStats(studentId: String): Result<AttendanceStats> = withContext(Dispatchers.IO) {
        try {
            val historyResult = getAttendanceHistory(studentId)
            
            historyResult.map { sessions ->
                var total = 0
                var present = 0
                var absent = 0
                var late = 0
                
                sessions.forEach { session ->
                    session.attendances?.forEach { attendance ->
                        total++
                        when (attendance.status) {
                            AttendanceStatus.PRESENT -> present++
                            AttendanceStatus.ABSENT -> absent++
                            AttendanceStatus.LATE -> late++
                        }
                    }
                }
                
                AttendanceStats(total, present, absent, late)
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                val errorMsg = response.body()?.message ?: "Update failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
