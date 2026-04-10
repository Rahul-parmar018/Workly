package com.example.workly.admin.data

import com.example.workly.admin.AdminUserData
import com.example.workly.data.Booking
import com.example.workly.data.Provider
import com.example.workly.data.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AdminRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAllUsers(): Flow<List<AdminUserData>> = callbackFlow {
        val listenerRegistration = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(AdminUserData::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getAllProviders(): Flow<List<Provider>> = callbackFlow {
        val listenerRegistration = db.collection("providers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Provider::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getAllBookings(): Flow<List<Booking>> = callbackFlow {
        val listenerRegistration = db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getAllServices(): Flow<List<Service>> = callbackFlow {
        val listenerRegistration = db.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Service::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listenerRegistration.remove() }
    }
}
