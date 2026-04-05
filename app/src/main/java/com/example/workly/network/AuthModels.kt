package com.example.workly.network

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String?,
    val user: UserData?,
    val error: String?
)

data class WalletOverviewResponse(
    val availableForPayout: Double,
    val pendingEscrow: Double,
    val totalEarnings: Double,
    val currency: String,
    val transactions: List<TransactionData>
)

data class TransactionData(
    val type: String,
    val amount: Double,
    val status: String,
    val createdAt: String,
    val referenceId: String
)

data class WithdrawalRequest(
    val amount: Double,
    val requestId: String,
    val payoutMethod: Map<String, String>
)
