package com.example.workly.network

import retrofit2.Call
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @GET("payments/provider/wallet")
    fun getWalletOverview(@Header("Authorization") token: String): Call<WalletOverviewResponse>

    @GET("payments/provider/transactions")
    fun getTransactionHistory(@Header("Authorization") token: String): Call<List<TransactionData>>

    @POST("payments/provider/withdraw")
    fun requestWithdrawal(@Header("Authorization") token: String, @Body request: WithdrawalRequest): Call<TransactionData> // Assuming it returns the created payout as TransactionData or similar
}
