package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper matching Backend response format
 */
data class ApiResponse<T>(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?
)

/**
 * Paginated response wrapper
 */
data class PaginatedResponse<T>(
    @SerializedName("data")
    val data: List<T>,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int
)
