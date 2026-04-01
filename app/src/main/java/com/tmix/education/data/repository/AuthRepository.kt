package com.tmix.education.data.repository

import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.api.ApiErrorParser
import com.tmix.education.data.api.ApiService
import com.tmix.education.data.local.TokenManager
import com.tmix.education.data.model.LoginRequest
import com.tmix.education.data.model.LoginResponse
import com.tmix.education.data.model.User
import com.tmix.education.data.model.ChangePasswordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Authentication operations
 */
class AuthRepository(
    private val apiService: ApiService = ApiConfig.getApiService(),
    private val tokenManager: TokenManager? = ApiConfig.getTokenManager()
) {
    
    /**
     * Login with email and password
     * @return Result with User on success
     */
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body()?.data != null) {
                val loginResponse = response.body()!!.data!!
                
                // Save token and user data
                tokenManager?.saveLoginData(
                    accessToken = loginResponse.accessToken,
                    user = loginResponse.user
                )
                
                Result.success(loginResponse.user)
            } else {
                Result.failure(Exception(ApiErrorParser.parse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorParser.parseException(e)))
        }
    }
    
    /**
     * Refresh access token using refresh token cookie
     */
    suspend fun refreshToken(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.refreshToken()
            
            if (response.isSuccessful && response.body()?.data != null) {
                val tokenResponse = response.body()!!.data!!
                
                // Update stored token and user
                tokenManager?.saveLoginData(
                    accessToken = tokenResponse.accessToken,
                    user = tokenResponse.user
                )
                
                Result.success(tokenResponse.user)
            } else {
                Result.failure(Exception(ApiErrorParser.parse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorParser.parseException(e)))
        }
    }
    
    /**
     * Change password
     */
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changePassword(
                ChangePasswordRequest(oldPassword, newPassword, confirmPassword)
            )
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(ApiErrorParser.parse(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorParser.parseException(e)))
        }
    }
    
    /**
     * Logout - clear local data
     */
    suspend fun logout() {
        tokenManager?.clearAll()
        ApiConfig.reset()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = tokenManager?.isLoggedInSync() ?: false
    
    /**
     * Get current user from local storage
     */
    fun getCurrentUser(): User? = tokenManager?.getCurrentUserSync()
    
    /**
     * Observe current user as a Flow
     */
    val currentUserFlow: kotlinx.coroutines.flow.Flow<User?> = tokenManager?.currentUser ?: kotlinx.coroutines.flow.flowOf(null)
    
    /**
     * Update cached user in local storage
     */
    suspend fun updateCachedUser(user: User) {
        tokenManager?.saveUser(user)
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = tokenManager?.getUserIdSync()
    
    /**
     * Is current user a student?
     */
    fun isStudent(): Boolean = tokenManager?.isStudentSync() ?: false
    
    /**
     * Is current user a parent?
     */
    fun isParent(): Boolean = tokenManager?.isParentSync() ?: false
}
