package com.example.workly.data.model

data class Booking(
    val id: String = "",
    val serviceId: String = "",
    val userId: String = "",
    val providerId: String = "",
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis(),
    val scheduledDate: String = "",
    val price: Double = 0.0
)
