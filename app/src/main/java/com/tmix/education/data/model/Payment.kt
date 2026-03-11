package com.tmix.education.data.model

import com.google.gson.annotations.SerializedName

/**
 * Payment status enum
 */
object PaymentStatus {
    const val PENDING = "pending"
    const val PARTIAL = "partial"
    const val PAID = "paid"
    const val OVERDUE = "overdue"
}

/**
 * Payment history record
 */
data class PaymentHistory(
    @SerializedName("method")
    val method: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("note")
    val note: String? = null,
    
    @SerializedName("date")
    val date: String
)

/**
 * Class info for payment
 */
data class PaymentClassInfo(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("lessons")
    val lessons: Int? = null
)

/**
 * Payment entity matching Backend Payment domain
 */
data class Payment(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("month")
    val month: Int,
    
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("totalLessons")
    val totalLessons: Int,
    
    @SerializedName("paidAmount")
    val paidAmount: Double,
    
    @SerializedName("totalAmount")
    val totalAmount: Double,
    
    @SerializedName("discountAmount")
    val discountAmount: Double = 0.0,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("student")
    val student: Student? = null,
    
    @SerializedName("class")
    val classInfo: PaymentClassInfo? = null,
    
    @SerializedName("histories")
    val histories: List<PaymentHistory>? = null
) {
    val remainingAmount: Double
        get() = totalAmount - discountAmount - paidAmount
    
    val isPaid: Boolean
        get() = status == PaymentStatus.PAID
}

/**
 * QR Code response for payment
 */
data class QRCodeResponse(
    @SerializedName("qrDataURL")
    val qrDataUrl: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("description")
    val description: String? = null
)
