package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Advertisement/Banner entity matching Backend Advertisement domain
 */
data class Advertisement(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("linkUrl")
    val linkUrl: String? = null,
    
    @SerializedName("type")
    val type: String? = null, // banner, popup
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("startDate")
    val startDate: String? = null,
    
    @SerializedName("endDate")
    val endDate: String? = null
)
