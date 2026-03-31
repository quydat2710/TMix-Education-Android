package com.tmix.education.data.api

import android.content.Context
import com.tmix.education.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API Configuration Object
 * Provides Retrofit instance configured for TMix Backend
 */
object ApiConfig {
    
    // For Android Emulator connecting to localhost
    // Use 10.0.2.2 for emulator, actual IP for physical device
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/api/v1/"
    private const val BASE_URL_LOCALHOST = "http://localhost:8080/api/v1/"
    
    // For physical device - use your computer's local IP
    // Find your IP: Windows -> ipconfig, Mac/Linux -> ifconfig
    private const val BASE_URL_PHYSICAL_DEVICE = "http://192.168.1.145:8080/api/v1/"
    
    // Production URL
    private const val BASE_URL_PRODUCTION = "https://tmixeducation.id.vn/api/v1/"
    
    // Current active URL - CHANGE THIS based on how you're testing
    // Use BASE_URL_EMULATOR for Android Emulator
    // Use BASE_URL_PHYSICAL_DEVICE for physical phone on same WiFi
    // Use BASE_URL_PRODUCTION for deployed server
    var BASE_URL = BASE_URL_PRODUCTION
        private set
    
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    private var tokenManager: TokenManager? = null
    
    /**
     * Initialize API with context for TokenManager
     */
    fun init(context: Context, isProduction: Boolean = false) {
        tokenManager = TokenManager(context)
        // Keep the current BASE_URL setting (set at declaration)
        // Only change to production URL if isProduction is true
        if (isProduction) {
            BASE_URL = BASE_URL_PRODUCTION
        }
        // For physical device testing, BASE_URL is already set to BASE_URL_PHYSICAL_DEVICE
        retrofit = null // Force rebuild
        apiService = null
    }
    
    /**
     * Set custom base URL (for testing or different environments)
     */
    fun setBaseUrl(url: String) {
        BASE_URL = url
        retrofit = null
        apiService = null
    }
    
    /**
     * Auth Interceptor - Adds JWT token to requests
     */
    private fun createAuthInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenManager?.getAccessTokenSync()
        val hasContentType = original.header("Content-Type") != null
        
        val request = if (token != null) {
            val builder = original.newBuilder()
                .header("Authorization", "Bearer $token")
            // Don't force Content-Type for multipart requests
            if (!hasContentType) {
                builder.header("Content-Type", "application/json")
            }
            builder.build()
        } else {
            val builder = original.newBuilder()
            if (!hasContentType) {
                builder.header("Content-Type", "application/json")
            }
            builder.build()
        }
        
        chain.proceed(request)
    }
    
    /**
     * Logging Interceptor - For debug purposes
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * Create OkHttpClient with interceptors
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .build()
    }
    
    /**
     * Get Retrofit instance
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
    
    /**
     * Get ApiService instance
     */
    fun getApiService(): ApiService {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService::class.java)
        }
        return apiService!!
    }
    
    /**
     * Get TokenManager instance
     */
    fun getTokenManager(): TokenManager? = tokenManager
    
    /**
     * Clear cached instances (for logout or config change)
     */
    fun reset() {
        retrofit = null
        apiService = null
    }
}
