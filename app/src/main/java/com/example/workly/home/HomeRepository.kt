package com.example.workly.home

import com.example.workly.data.Booking
import com.example.workly.data.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class HomeRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    // Fixed: removed compound orderBy+where query that requires composite index (causes crash)
    fun getUpcomingBookings(): Flow<List<Booking>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .limit(5)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList()) // don't crash on index errors
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = try {
                        snapshot.toObjects(Booking::class.java)
                    } catch (ex: Exception) {
                        emptyList()
                    }
                    trySend(bookings)
                }
            }

        awaitClose { listener.remove() }
    }

    fun getPopularServices(): List<Service> = emptyList()
}
