package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.ApiService
import com.tmix.education.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Parent operations
 */
class ParentRepository(
    private val apiService: ApiService = ApiConfig.getApiService(),
    private val studentRepository: StudentRepository = StudentRepository()
) {
    
    /**
     * Get parent profile by ID
     */
    suspend fun getParent(parentId: String): Result<Parent> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getParent(parentId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to get parent"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get parent's children (students)
     */
    suspend fun getChildren(parentId: String): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val parentResult = getParent(parentId)
            parentResult.map { parent ->
                parent.students ?: emptyList()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get child details with classes
     */
    suspend fun getChildDetails(childId: String): Result<Student> = 
        studentRepository.getStudent(childId)
    
    /**
     * Get child's schedule
     */
    suspend fun getChildSchedule(childId: String): Result<List<ScheduleItem>> =
        studentRepository.getSchedule(childId)
    
    /**
     * Get child's attendance history
     */
    suspend fun getChildAttendance(childId: String): Result<List<Session>> =
        studentRepository.getAttendanceHistory(childId)
    
    /**
     * Get child's attendance statistics
     */
    suspend fun getChildAttendanceStats(childId: String): Result<AttendanceStats> =
        studentRepository.getAttendanceStats(childId)
    
    /**
     * Get payments for a child
     */
    suspend fun getChildPayments(
        childId: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<List<Payment>> = studentRepository.getPayments(childId, page, limit)
    
    /**
     * Get all payments for all children
     */
    suspend fun getAllChildrenPayments(
        parentId: String
    ): Result<List<Payment>> = withContext(Dispatchers.IO) {
        try {
            val childrenResult = getChildren(parentId)
            
            childrenResult.map { children ->
                val allPayments = mutableListOf<Payment>()
                
                children.forEach { child ->
                    val paymentsResult = getChildPayments(child.id)
                    paymentsResult.getOrNull()?.let { payments ->
                        allPayments.addAll(payments)
                    }
                }
                
                // Sort by year and month descending
                allPayments.sortedByDescending { it.year * 12 + it.month }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get payment QR code
     */
    suspend fun getPaymentQRCode(
        paymentId: String,
        amount: Double
    ): Result<QRCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPaymentQRCode(paymentId, amount)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to get QR code"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update parent profile
     */
    suspend fun updateProfile(
        parentId: String,
        updates: Map<String, Any>
    ): Result<Parent> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateParent(parentId, updates)
            
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
