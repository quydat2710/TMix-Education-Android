package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Role entity matching Backend
 */
data class Role(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("description")
    val description: String? = null
) {
    companion object {
        const val ADMIN = 1
        const val TEACHER = 2
        const val PARENT = 3
        const val STUDENT = 4
    }
}

/**
 * User entity matching Backend User domain
 */
data class User(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("dayOfBirth")
    val dayOfBirth: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("avatar")
    val avatar: String? = null,
    
    @SerializedName("publicId")
    val publicId: String? = null,
    
    @SerializedName("role")
    val role: Role? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val isStudent: Boolean
        get() = role?.id == Role.STUDENT
    
    val isParent: Boolean
        get() = role?.id == Role.PARENT
}
