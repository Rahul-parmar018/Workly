package com.workly.app.data

import com.google.firebase.Timestamp

data class Service(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val duration: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val rating: Double = 0.0,
    val totalOrders: Int = 0,
    val isApproved: Boolean = false,
    val status: String = "pending", // ["pending", "approved", "rejected"]
    val keywords: List<String> = emptyList(),
    val imagePath: String = "",
    val syncStatus: String = "synced",
    val createdAt: Timestamp = Timestamp.now()
)
