package com.workly.app.home

import com.workly.app.data.Order
import com.workly.app.data.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HomeRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun getUpcomingBookings(): Flow<List<Order>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Needs composite index if whereEqualTo is used
            .limit(5)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = try {
                        snapshot.toObjects(Order::class.java)
                    } catch (ex: Exception) {
                        emptyList()
                    }
                    trySend(orders)
                }
            }

        awaitClose { listener.remove() }
    }

    fun getPopularServices(): Flow<List<Service>> = callbackFlow {
        val listener = firestore.collection("services")
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(6)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val services = try {
                        snapshot.toObjects(Service::class.java)
                    } catch (ex: Exception) {
                        emptyList()
                    }
                    trySend(services)
                }
            }

        awaitClose { listener.remove() }
    }
}
