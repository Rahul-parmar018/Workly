package com.example.workly.provider

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workly.data.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddServiceState {
    object Idle : AddServiceState()
    object Loading : AddServiceState()
    data class Success(val message: String) : AddServiceState()
    data class Error(val message: String) : AddServiceState()
}

class AddServiceViewModel(private val repository: AddServiceRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val title = MutableStateFlow("")
    val category = MutableStateFlow("")
    val location = MutableStateFlow("")
    val duration = MutableStateFlow("")
    val price = MutableStateFlow("")
    val imageUri = MutableStateFlow<Uri?>(null)
    val imageUrl = MutableStateFlow("") 

    private val _uiState = MutableStateFlow<AddServiceState>(AddServiceState.Idle)
    val uiState: StateFlow<AddServiceState> = _uiState.asStateFlow()

    fun publishService() {
        val uri = imageUri.value
        if (uri == null) {
            _uiState.value = AddServiceState.Error("Please select an image")
            return
        }

        _uiState.value = AddServiceState.Loading

        viewModelScope.launch {
            try {
                val localPath = repository.saveImageToInternalStorage(uri)
                if (localPath == null) {
                    _uiState.value = AddServiceState.Error("Failed to save image locally")
                    return@launch
                }

                val providerName = auth.currentUser?.displayName ?: "Professional Provider"
                
                repository.saveService(
                    title = title.value,
                    category = category.value,
                    location = location.value,
                    duration = duration.value,
                    price = price.value.toIntOrNull() ?: 0,
                    providerName = providerName,
                    localImagePath = localPath,
                    onComplete = { success ->
                        if (success) {
                            _uiState.value = AddServiceState.Success("Service published successfully!")
                        } else {
                            _uiState.value = AddServiceState.Error("Failed to save to database")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AddServiceState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadService(serviceId: String) {
        _uiState.value = AddServiceState.Loading
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("services").document(serviceId).get()
                .addOnSuccessListener { doc ->
                    title.value = doc.getString("title") ?: ""
                    category.value = doc.getString("category") ?: ""
                    location.value = doc.getString("location") ?: ""
                    duration.value = doc.getString("duration") ?: ""
                    price.value = (doc.getDouble("price")?.toInt() ?: 0).toString()
                    imageUrl.value = doc.getString("imageUrl") ?: ""
                    _uiState.value = AddServiceState.Idle
                }
                .addOnFailureListener {
                    _uiState.value = AddServiceState.Error("Failed to load service")
                }
        }
    }

    fun updateService(serviceId: String) {
        _uiState.value = AddServiceState.Loading
        repository.updateService(
            id = serviceId,
            title = title.value,
            category = category.value,
            location = location.value,
            duration = duration.value,
            price = price.value.toIntOrNull() ?: 0
        ) { success ->
            if (success) _uiState.value = AddServiceState.Success("Service updated successfully!")
            else _uiState.value = AddServiceState.Error("Update failed")
        }
    }

    fun resetState() {
        _uiState.value = AddServiceState.Idle
    }
}
