package com.example.workly.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

object ValidationEngine {
    private val db = FirebaseFirestore.getInstance()

    suspend fun canUserBookService(userId: String, serviceId: String): Pair<Boolean, String?> {
        // 1. DUPLICATE SERVICE LOCK
        try {
            val activeBookings = db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("serviceId", serviceId)
                .whereIn("status", listOf("PENDING", "ACCEPTED", "IN_PROGRESS"))
                .get()
                .await()

            if (!activeBookings.isEmpty) {
                return false to "You already have an active booking for this service."
            }
        } catch (e: Exception) {
            Log.e("ValidationEngine", "Error checking duplicates", e)
        }

        return true to null
    }

    suspend fun isTimeSlotAvailable(userId: String, date: String, time: String): Pair<Boolean, String?> {
        // 2. TIME-SLOT CONCURRENCY
        try {
            val clashes = db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .whereEqualTo("time", time)
                .whereIn("status", listOf("PENDING", "ACCEPTED", "IN_PROGRESS"))
                .get()
                .await()

            if (!clashes.isEmpty) {
                return false to "You already have another service booked at this time."
            }
        } catch (e: Exception) {
            Log.e("ValidationEngine", "Error checking time clash", e)
        }
        return true to null
    }

    suspend fun checkCancellationPenalty(userId: String): Pair<Boolean, String?> {
        // 6. CANCELLATION RATE PENALTY
        try {
            val history = db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            
            val cancelledCount = history.documents.count { it.getString("status") == "CANCELLED" }
            if (cancelledCount >= 3) {
                return false to "Account Restricted: Too many recent cancellations. Please contact support."
            }
        } catch (e: Exception) {
            Log.e("ValidationEngine", "Error checking penalty", e)
        }
        return true to null
    }

    fun isWithinOperationalRadius(userLat: Double, userLon: Double, proLat: Double, proLon: Double, radiusKm: Double = 15.0): Pair<Boolean, String?> {
        // 4. GEO-FENCE RADIUS CHECK
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(proLat - userLat)
        val dLon = Math.toRadians(proLon - userLon)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(proLat)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c

        if (distance > radiusKm) {
            return false to "Location Out of Range: Professional is %.1f km away (Limit is 15km).".format(distance)
        }
        return true to null
    }

    fun isProfileComplete(userName: String, phone: String, address: String): Pair<Boolean, String?> {
        // 7. INCOMPLETE PROFILE BLOCK
        if (userName.isBlank()) return false to "Please set your name in profile."
        // Phone verification removed per user request:
        // if (phone.isBlank() || phone.length < 10) return false to "Please verify your phone number first."
        if (address.isBlank() || address.length < 10) return false to "Please provide a complete address with landmark."
        return true to null
    }
}
