package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.ApiService
import com.tmix.education.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Course Registration operations
 */
class RegistrationRepository(
    private val apiService: ApiService = ApiConfig.getApiService()
) {

    /**
     * Submit a course registration
     * This is a public endpoint - no authentication required
     */
    suspend fun submitRegistration(
        request: CreateRegistrationRequest
    ): Result<Registration> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.submitRegistration(request)

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: "Đăng ký thất bại"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
