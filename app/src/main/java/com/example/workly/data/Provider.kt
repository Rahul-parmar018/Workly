package com.example.workly.data

data class Provider(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val skills: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageRes: Int = 0,
    val reviewsCount: Int = 0,
    val bio: String = ""
)
