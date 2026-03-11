package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Student entity extending User, matching Backend Student domain
 */
data class Student(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String? = null,
    
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
    
    @SerializedName("role")
    val role: Role? = null,
    
    @SerializedName("classes")
    val classes: List<StudentClassInfo>? = null
)

/**
 * Parent entity extending User, matching Backend Parent domain
 */
data class Parent(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String? = null,
    
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
    
    @SerializedName("role")
    val role: Role? = null,
    
    @SerializedName("students")
    val students: List<Student>? = null
)
