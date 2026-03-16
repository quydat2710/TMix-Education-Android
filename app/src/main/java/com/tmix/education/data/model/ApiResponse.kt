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
    @SerializedName("result")
    val data: List<T>,
    
    @SerializedName("meta")
    val meta: PaginationMeta? = null
)

data class PaginationMeta(
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("limit")
    val limit: Int = 10,
    
    @SerializedName("totalPages")
    val totalPages: Int = 1,
    
    @SerializedName("totalItems")
    val totalItems: Int = 0
)
