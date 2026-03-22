package com.example.workly.data.model

data class Service(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val basePrice: Double = 0.0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val providerId: String = ""
)
