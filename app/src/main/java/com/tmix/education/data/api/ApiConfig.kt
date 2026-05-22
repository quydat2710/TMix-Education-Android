package com.tmix.education.data.api

import android.content.Context
import android.util.Log
import com.tmix.education.data.local.PersistentCookieJar
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

    private const val TAG = "ApiConfig"
    
    // For Android Emulator connecting to localhost
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/api/v1/"
    private const val BASE_URL_LOCALHOST = "http://localhost:8080/api/v1/"
    
    // For physical device - use your computer's local IP
    private const val BASE_URL_PHYSICAL_DEVICE = "http://192.168.1.145:8080/api/v1/"
    
    // Production URL
    private const val BASE_URL_PRODUCTION = "https://tmixeducation.id.vn/api/v1/"
    
    // Current active URL
    var BASE_URL = BASE_URL_PRODUCTION
        private set
    
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    private var tokenManager: TokenManager? = null
    private var cookieJar: PersistentCookieJar? = null

    // Flag to prevent infinite refresh loop
    @Volatile
    private var isRefreshing = false
    
    /**
     * Initialize API with context for TokenManager
     */
    fun init(context: Context, isProduction: Boolean = false) {
        tokenManager = TokenManager(context)
        cookieJar = PersistentCookieJar(context)
        if (isProduction) {
            BASE_URL = BASE_URL_PRODUCTION
        }
        retrofit = null
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
     * Auth Interceptor - Adds JWT token to requests.
     * On 401, tries to refresh the token automatically using the refresh_token cookie.
     * If refresh succeeds, retries the original request with the new token.
     * If refresh fails, returns the 401 response as-is (caller decides what to do).
     */
    private fun createAuthInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenManager?.getAccessTokenSync()
        val hasContentType = original.header("Content-Type") != null
        
        val request = if (token != null) {
            val builder = original.newBuilder()
                .header("Authorization", "Bearer $token")
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
        
        val response = chain.proceed(request)
        
        // On 401, try to refresh the token (only for non-auth endpoints)
        if (response.code == 401) {
            val path = original.url.encodedPath
            val isAuthEndpoint = path.contains("auth/login") || path.contains("auth/refresh")
            
            if (!isAuthEndpoint && !isRefreshing) {
                Log.d(TAG, "Got 401 on $path, attempting token refresh...")
                val newToken = tryRefreshToken()
                
                if (newToken != null) {
                    Log.d(TAG, "Token refresh succeeded, retrying original request")
                    response.close()
                    
                    val retryRequest = original.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    return@Interceptor chain.proceed(retryRequest)
                } else {
                    Log.w(TAG, "Token refresh failed — returning 401 to caller")
                    // DON'T clear data here. Let the caller (SplashScreen) decide.
                    // Only emit session expired so active screens can react.
                    AuthEventBus.emitSessionExpired()
                }
            }
        }
        
        response
    }

    /**
     * Attempt to refresh the access token using the refresh_token cookie.
     * Returns the new access token on success, null on failure.
     */
    private fun tryRefreshToken(): String? {
        synchronized(this) {
            if (isRefreshing) return null
            isRefreshing = true
        }

        try {
            val jar = cookieJar ?: return null

            // Build a minimal OkHttp client with just the cookie jar (no auth interceptor)
            val refreshClient = OkHttpClient.Builder()
                .cookieJar(jar)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(createLoggingInterceptor())
                .build()

            val refreshUrl = BASE_URL + "auth/refresh"
            Log.d(TAG, "Refreshing token via: $refreshUrl")

            val request = okhttp3.Request.Builder()
                .url(refreshUrl)
                .get()
                .build()

            val response = refreshClient.newCall(request).execute()
            Log.d(TAG, "Refresh response code: ${response.code}")

            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    Log.d(TAG, "Refresh response body length: ${body.length}")
                    try {
                        val json = com.google.gson.JsonParser.parseString(body).asJsonObject
                        val data = json.getAsJsonObject("data")
                        val newAccessToken = data?.get("access_token")?.asString

                        if (newAccessToken != null) {
                            kotlinx.coroutines.runBlocking {
                                tokenManager?.saveAccessToken(newAccessToken)

                                val userJson = data.getAsJsonObject("user")
                                if (userJson != null) {
                                    val gson = com.google.gson.Gson()
                                    val user = gson.fromJson(userJson, com.tmix.education.data.model.User::class.java)
                                    tokenManager?.saveUser(user)
                                }
                            }

                            Log.d(TAG, "Token refreshed successfully ✓")
                            return newAccessToken
                        } else {
                            Log.w(TAG, "access_token not found in response. Body: ${body.take(200)}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse refresh response: ${e.message}. Body: ${body.take(200)}")
                    }
                }
            } else {
                Log.w(TAG, "Refresh request failed with code: ${response.code}")
            }

            return null
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh error", e)
            return null
        } finally {
            isRefreshing = false
        }
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
     * Create OkHttpClient with interceptors, cookie jar, and cache
     */
    private fun createOkHttpClient(): OkHttpClient {
        val cacheDir = java.io.File(
            android.os.Environment.getDownloadCacheDirectory(), "tmix_http_cache"
        )
        val cache = okhttp3.Cache(cacheDir, 10L * 1024 * 1024)
        
        return OkHttpClient.Builder()
            .cache(cache)
            .cookieJar(cookieJar ?: okhttp3.CookieJar.NO_COOKIES)
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
     * Get CookieJar instance
     */
    fun getCookieJar(): PersistentCookieJar? = cookieJar
    
    /**
     * Clear cached instances and cookies (for logout)
     */
    fun reset() {
        retrofit = null
        apiService = null
        cookieJar?.clear()
    }

    /**
     * Perform a full logout — clear tokens, cookies, and cached instances
     */
    fun logout() {
        kotlinx.coroutines.runBlocking { tokenManager?.clearAll() }
        cookieJar?.clear()
        retrofit = null
        apiService = null
    }
}
