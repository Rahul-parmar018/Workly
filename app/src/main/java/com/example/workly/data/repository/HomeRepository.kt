package com.example.workly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getCategories(): List<String> {
        // Mock or fetch from Firestore
        return listOf("Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events")
    }
}
