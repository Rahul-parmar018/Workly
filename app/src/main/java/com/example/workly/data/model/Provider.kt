package com.example.workly.data.model

data class Provider(
    val id: String = "",
    val name: String = "",
    val rating: Float = 0f,
    val hourlyRate: Double = 0.0,
    val specialties: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val experience: Int = 0,
    val reviewsCount: Int = 0,
    val profileImageUrl: String = "",
    val bio: String = ""
)
