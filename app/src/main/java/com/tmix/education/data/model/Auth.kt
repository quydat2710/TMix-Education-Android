package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Login Request DTO
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * Login Response matching Backend auth.service.ts login() response
 */
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("user")
    val user: User
)

/**
 * Token Refresh Response
 */
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("user")
    val user: User
)

/**
 * Change Password Request
 */
data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,
    
    @SerializedName("newPassword")
    val newPassword: String,
    
    @SerializedName("confirmPassword")
    val confirmPassword: String
)
