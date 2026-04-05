package com.example.workly.data

import kotlin.math.*

object AIMatcher {
    /**
     * Scores a provider based on rating, distance, and price.
     * Higher score is better.
     */
    fun calculateScore(
        provider: Provider,
        userLat: Double,
        userLon: Double,
        targetPrice: Double = 40.0
    ): Double {
        // 1. Rating Weight (40%)
        val ratingScore = (provider.rating / 5.0) * 40.0
        
        // 2. Distance Weight (40%)
        val distance = calculateDistance(userLat, userLon, provider.latitude, provider.longitude)
        // Max distance 10km for full points, diminishes after that
        val distanceScore = max(0.0, (1.0 - (distance / 50.0))) * 40.0
        
        // 3. Price Weight (20%)
        // Prefer prices closer to targetPrice
        val priceDiff = abs(provider.hourlyRate - targetPrice)
        val priceScore = max(0.0, (1.0 - (priceDiff / targetPrice))) * 20.0
        
        return ratingScore + distanceScore + priceScore
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
