package com.example.workly.data

import com.google.firebase.Timestamp

data class Booking(
    val id: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val date: String = "",
    val time: String = "",
    val address: String = "",
    val status: String = "Pending", // Pending, Confirmed, Completed, Cancelled
    val price: Double = 0.0,
    val serviceIcon: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
) {
    // Helper to convert to map for Firestore if needed, 
    // although Firestore can handle data classes with default constructors.
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "serviceName" to serviceName,
            "date" to date,
            "time" to time,
            "address" to address,
            "status" to status,
            "price" to price,
            "serviceIcon" to serviceIcon,
            "createdAt" to createdAt
        )
    }
}
