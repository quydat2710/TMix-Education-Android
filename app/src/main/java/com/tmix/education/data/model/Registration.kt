package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request to create a new course registration
 * Matches Backend CreateRegistrationDto
 */
data class CreateRegistrationRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("gender")
    val gender: String, // "male" or "female"

    @SerializedName("address")
    val address: String,

    @SerializedName("note")
    val note: String,

    @SerializedName("classId")
    val classId: String,

    @SerializedName("processed")
    val processed: Boolean = false
)

/**
 * Registration entity returned from backend
 */
data class Registration(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("classId")
    val classId: String? = null,

    @SerializedName("processed")
    val processed: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null
)
