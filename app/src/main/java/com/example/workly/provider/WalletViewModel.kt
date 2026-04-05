package com.example.workly.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workly.network.ApiClient
import com.example.workly.network.TransactionData
import com.example.workly.network.WalletOverviewResponse
import com.example.workly.network.WithdrawalRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.util.UUID

sealed class WalletState {
    object Idle : WalletState()
    object Loading : WalletState()
    data class Success(val data: WalletOverviewResponse) : WalletState()
    data class Error(val message: String) : WalletState()
}

class WalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<WalletState>(WalletState.Idle)
    val uiState: StateFlow<WalletState> = _uiState

    private val _withdrawalStatus = MutableStateFlow<String?>(null)
    val withdrawalStatus: StateFlow<String?> = _withdrawalStatus

    /**
     * Fetch the wallet overview from the backend
     */
    fun fetchWalletOverview(token: String) {
        viewModelScope.launch {
            _uiState.value = WalletState.Loading
            try {
                val response = ApiClient.authService.getWalletOverview("Bearer $token").awaitResponse()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = WalletState.Success(response.body()!!)
                } else {
                    _uiState.value = WalletState.Error("Failed to fetch wallet: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = WalletState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Request a withdrawal
     */
    fun requestWithdrawal(token: String, amount: Double) {
        viewModelScope.launch {
            _withdrawalStatus.value = "Processing..."
            try {
                // Generate a unique requestId for idempotency
                val requestId = UUID.randomUUID().toString()
                
                val request = WithdrawalRequest(
                    amount = amount,
                    requestId = requestId,
                    payoutMethod = mapOf("type" to "upi", "details" to "provider@upi") // Mock data
                )

                val response = ApiClient.authService.requestWithdrawal("Bearer $token", request).awaitResponse()
                if (response.isSuccessful) {
                    _withdrawalStatus.value = "Success! Your withdrawal is being processed."
                    fetchWalletOverview(token) // Refresh wallet
                } else {
                    _withdrawalStatus.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _withdrawalStatus.value = "Error: ${e.message}"
            }
        }
    }
    
    fun clearWithdrawalStatus() {
        _withdrawalStatus.value = null
    }
}
