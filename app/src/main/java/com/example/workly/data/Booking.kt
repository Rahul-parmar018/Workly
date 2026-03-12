package com.example.workly.data

import com.google.firebase.Timestamp

/**
 * Booking schema for Workly.
 * Firestore path: /bookings/{bookingId}
 * NOTE: No nullable Timestamp — Firestore cannot serialize null Timestamp fields.
 */
data class Booking(
    val id: String = "",
    val userId: String = "",
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
    // Status: Pending → Confirmed → InProgress → Completed | Cancelled
    val status: String = "Pending",
    // Payment
    val paymentStatus: String = "Unpaid",
    val paymentMethod: String = "",
    val userNotes: String = "",
    val providerNotes: String = "",
    val rating: Float = 0f,
    val review: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
    // completedAt REMOVED — nullable Timestamp breaks Firestore serialization
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
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
        "updatedAt" to updatedAt
    )
}
