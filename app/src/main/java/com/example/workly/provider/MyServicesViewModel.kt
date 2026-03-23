package com.example.workly.provider

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.workly.data.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyServicesViewModel(private val repository: AddServiceRepository) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchServices()
    }

    private fun fetchServices() {
        val user = auth.currentUser ?: return
        db.collection("services")
            .whereEqualTo("providerId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Service::class.java)
                        } catch (e: Exception) {
                            Log.e("MyServicesVM", "Error parsing service: ${e.message}")
                            null
                        }
                    }
                    _services.value = list.sortedByDescending { it.createdAt }
                }
                _isLoading.value = false
            }
    }
}
