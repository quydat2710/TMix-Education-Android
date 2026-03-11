package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.ApiService
import com.tmix.education.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Class operations
 */
class ClassRepository(
    private val apiService: ApiService = ApiConfig.getApiService()
) {
    
    /**
     * Get all classes (paginated)
     */
    suspend fun getClasses(
        page: Int = 1,
        limit: Int = 10
    ): Result<List<ClassInfo>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClasses(page, limit)
            
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
     * Get public classes
     */
    suspend fun getPublicClasses(
        page: Int = 1,
        limit: Int = 10
    ): Result<List<ClassInfo>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPublicClasses(page, limit)
            
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
     * Get class by ID
     */
    suspend fun getClass(classId: String): Result<ClassInfo> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClass(classId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: "Class not found"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get class banner info
     */
    suspend fun getClassBannerInfo(classId: String): Result<ClassInfo> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClassBannerInfo(classId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: "Class not found"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get class sessions (attendance records)
     */
    suspend fun getClassSessions(
        classId: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<List<Session>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClassSessions(classId, page, limit)
            
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
     * Get today's session for a class
     */
    suspend fun getTodaySession(classId: String): Result<Session?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTodaySession(classId)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.success(null) // No session today
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
