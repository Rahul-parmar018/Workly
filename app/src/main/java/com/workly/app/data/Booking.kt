package com.workly.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

object OrderStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

/**
 * Booking/Order schema for Workly.
 * Firestore path: /orders/{orderId}
 */
data class Booking(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val serviceName: String = "",
    val serviceCategory: String = "",
    val serviceId: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val providerPhone: String = "",
    val date: String = "",
    val time: String = "",
    val durationHours: Int = 1,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val basePrice: Double = 0.0,
    val finalPrice: Double = 0.0,
    val discount: Double = 0.0,
    
    // Status: Use OrderStatus constants
    val status: String = OrderStatus.PENDING,
    
    // Payment
    val paymentStatus: String = "Unpaid",
    val paymentMethod: String = "",
    val userNotes: String = "",
    val providerNotes: String = "",
    val rating: Float = 0f,
    val review: String = "",
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    @ServerTimestamp
    val createdAtServer: Timestamp? = null,
    val acceptedAt: Long? = null,
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "userName" to userName,
        "serviceName" to serviceName,
        "serviceCategory" to serviceCategory,
        "serviceId" to serviceId,
        "providerId" to providerId,
        "providerName" to providerName,
        "providerPhone" to providerPhone,
        "date" to date,
        "time" to time,
        "durationHours" to durationHours,
        "address" to address,
        "latitude" to latitude,
        "longitude" to longitude,
        "basePrice" to basePrice,
        "finalPrice" to finalPrice,
        "discount" to discount,
        "status" to status,
        "paymentStatus" to paymentStatus,
        "paymentMethod" to paymentMethod,
        "userNotes" to userNotes,
        "providerNotes" to providerNotes,
        "rating" to rating,
        "review" to review,
        "createdAt" to createdAt,
        "createdAtServer" to createdAtServer,
        "acceptedAt" to acceptedAt,
        "completedAt" to completedAt,
        "updatedAt" to updatedAt
    )
}
