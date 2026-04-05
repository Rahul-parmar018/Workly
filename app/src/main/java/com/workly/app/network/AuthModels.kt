package com.workly.app.network

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
