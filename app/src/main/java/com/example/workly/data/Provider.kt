package com.example.workly.data

/**
 * Provider model for Workly.
 * Firestore path: /providers/{providerId}
 */
data class Provider(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val hourlyRate: Double = 0.0,
    // specialties: list of service categories this provider serves (e.g. ["Cleaning", "Repair"])
    val specialties: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageRes: Int = 0,
    val reviewsCount: Int = 0,
    val bio: String = "",
    val phone: String = "",
    val isActive: Boolean = true
)
