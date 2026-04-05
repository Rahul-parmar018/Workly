package com.workly.app.provider

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workly.app.data.Service
import com.google.firebase.auth.FirebaseAuth
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
                // 1. Save locally
                val localPath = repository.saveImageToInternalStorage(uri)
                if (localPath == null) {
                    _uiState.value = AddServiceState.Error("Failed to save image locally")
                    return@launch
                }

                // 2. Save directly to Firestore (Simplified for college project)
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

    fun resetState() {
        _uiState.value = AddServiceState.Idle
    }
}
