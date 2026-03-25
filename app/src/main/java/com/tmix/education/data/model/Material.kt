package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Material data model for learning materials
 */
data class Material(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val category: String = "other",
    val fileUrl: String = "",
    val fileType: String = "other",
    val originalFileName: String? = null,
    val fileSize: Long = 0,
    val classId: String = "",
    val uploadedById: String = "",
    val createdAt: String = ""
)

/**
 * Paginated materials response
 */
data class MaterialsResponse(
    val meta: PaginationMeta? = null,
    val result: List<Material> = emptyList()
)


