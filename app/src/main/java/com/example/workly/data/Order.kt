package com.example.workly.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Order(
    var id: String = "",
    var serviceId: String = "",
    var serviceName: String = "",
    var serviceTitle: String = "",
    var serviceCategory: String = "",
    var price: Double = 0.0,
    var basePrice: Double = 0.0,
    var finalPrice: Double = 0.0,
    var userId: String = "",
    var userName: String = "",
    var providerId: String = "",
    var providerName: String = "",
    var providerPhone: String = "",
    var address: String = "",
    var status: String = "pending", // pending | accepted | completed | cancelled
    var date: String = "",
    var time: String = "",
    var createdAt: Any? = null, // Handle both Long and Timestamp
    var acceptedAt: Any? = null,
    var completedAt: Any? = null
) {
    // Helper to get safe date for UI
    fun getSafeDate(): Date {
        return when (val time = createdAt) {
            is Timestamp -> time.toDate()
            is Long -> Date(time)
            is Double -> Date(time.toLong())
            else -> Date()
        }
    }

    // Helper to get safe price
    fun getSafePrice(): Int {
        val p = if (finalPrice > 0.0) finalPrice else (if (price > 0.0) price else basePrice)
        return p.toInt()
    }
}
