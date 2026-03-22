package com.example.workly.data

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val price: Double = 0.0,
    val userId: String = "",
    val userName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val status: String = "pending", // pending | accepted | completed
    val createdAt: Timestamp = Timestamp.now(),
    val acceptedAt: Timestamp? = null,
    val completedAt: Timestamp? = null
)
