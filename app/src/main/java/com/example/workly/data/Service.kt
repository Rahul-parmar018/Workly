package com.example.workly.data

data class Service(
    val id: String = "",
    val name: String = "",
    val category: String = "", // Home, Wellness, Tech, Auto, etc.
    val basePrice: Double = 0.0,
    val iconRes: Int = 0,
    val description: String = ""
)
