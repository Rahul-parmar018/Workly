package com.example.workly.provider

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class AddServiceState {
    object Idle : AddServiceState()
    object Loading : AddServiceState()
    data class Success(val message: String) : AddServiceState()
    data class Error(val message: String) : AddServiceState()
}

class AddServiceViewModel(private val repo: AddServiceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AddServiceState>(AddServiceState.Idle)
    val uiState: StateFlow<AddServiceState> = _uiState.asStateFlow()

    var title = MutableStateFlow("")
    var category = MutableStateFlow("")
    var location = MutableStateFlow("")
    var duration = MutableStateFlow("")
    var price = MutableStateFlow("")
    var imageUri = MutableStateFlow<Uri?>(null)

    fun publishService() {
        val t = title.value.trim()
        val c = category.value.trim()
        val l = location.value.trim()
        val d = duration.value.trim()
        val p = price.value.toIntOrNull()
        val img = imageUri.value

        if (img == null) { _uiState.value = AddServiceState.Error("Select an image first"); return }
        if (t.isBlank() || c.isBlank() || l.isBlank() || d.isBlank()) {
            _uiState.value = AddServiceState.Error("Fill all fields"); return
        }
        if (p == null || p <= 0) { _uiState.value = AddServiceState.Error("Enter a valid price"); return }
        if (p > 100000) { _uiState.value = AddServiceState.Error("Price too high (max ₹1,00,000)"); return }

        _uiState.value = AddServiceState.Loading

        viewModelScope.launch {
            var compressedUri: Uri? = null
            try {
                // 1. Compress
                compressedUri = repo.compressImage(img)
                    ?: throw Exception("Image compression failed — try a different photo")

                // 2. Upload (NO RETRIES — shows exact error)
                val (url, ref) = repo.uploadImage(compressedUri)

                // 3. Save to Firestore
                val name = repo.getProviderName().takeIf { it != "Provider" } ?: repo.getProviderNameRemote()
                val ok = repo.saveService(t, c, l, d, p, url, ref, name)

                if (ok) {
                    _uiState.value = AddServiceState.Success("Service published!")
                } else {
                    _uiState.value = AddServiceState.Error("Failed to save service data")
                }
            } catch (e: Exception) {
                Log.e("AddServiceVM", "Publish FAIL: ${e.message}", e)
                _uiState.value = AddServiceState.Error(e.message ?: "Unknown error occurred")
            } finally {
                // Cleanup cache
                try { compressedUri?.path?.let { File(it).delete() } } catch (_: Exception) {}
            }
        }
    }

    fun resetState() { _uiState.value = AddServiceState.Idle }
}
