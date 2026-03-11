package com.tmix.education.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.tmix.education.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tmix_auth")

/**
 * Token Manager using DataStore
 * Handles JWT access token and user data persistence
 */
class TokenManager(private val context: Context) {
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }
    
    private val gson = Gson()
    
    // ============ ACCESS TOKEN ============
    
    /**
     * Get access token as Flow
     */
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }
    
    /**
     * Get access token synchronously (blocking - use carefully)
     */
    fun getAccessTokenSync(): String? = runBlocking {
        context.dataStore.data.first()[ACCESS_TOKEN_KEY]
    }
    
    /**
     * Save access token
     */
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }
    
    // ============ USER DATA ============
    
    /**
     * Get current user as Flow
     */
    val currentUser: Flow<User?> = context.dataStore.data.map { preferences ->
        preferences[USER_DATA_KEY]?.let { json ->
            try {
                gson.fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Get current user synchronously
     */
    fun getCurrentUserSync(): User? = runBlocking {
        context.dataStore.data.first()[USER_DATA_KEY]?.let { json ->
            try {
                gson.fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Save user data
     */
    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = gson.toJson(user)
            preferences[USER_ID_KEY] = user.id
            preferences[USER_ROLE_KEY] = user.role?.id?.toString() ?: ""
        }
    }
    
    /**
     * Get user ID
     */
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    fun getUserIdSync(): String? = runBlocking {
        context.dataStore.data.first()[USER_ID_KEY]
    }
    
    /**
     * Get user role
     */
    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }
    
    /**
     * Check if user is logged in
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[ACCESS_TOKEN_KEY].isNullOrEmpty()
    }
    
    fun isLoggedInSync(): Boolean = runBlocking {
        !context.dataStore.data.first()[ACCESS_TOKEN_KEY].isNullOrEmpty()
    }
    
    /**
     * Check if current user is student
     */
    fun isStudentSync(): Boolean {
        val user = getCurrentUserSync()
        return user?.role?.id == 4 // STUDENT role ID
    }
    
    /**
     * Check if current user is parent
     */
    fun isParentSync(): Boolean {
        val user = getCurrentUserSync()
        return user?.role?.id == 3 // PARENT role ID
    }
    
    // ============ LOGOUT ============
    
    /**
     * Clear all stored data (logout)
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Save login data (token + user) in one call
     */
    suspend fun saveLoginData(accessToken: String, user: User) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[USER_DATA_KEY] = gson.toJson(user)
            preferences[USER_ID_KEY] = user.id
            preferences[USER_ROLE_KEY] = user.role?.id?.toString() ?: ""
        }
    }
}
