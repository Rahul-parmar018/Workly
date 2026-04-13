package com.example.workly.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UpiPaymentHandler {

    /**
     * Creates a UPI intent that opens installed UPI apps on the device.
     * 
     * @param vpa Virtual Payment Address of the payee (e.g. your-business@upi)
     * @param name Name of the payee
     * @param transactionNote A short description of the transaction
     * @param amount The amount to be paid
     */
    fun getUpiIntent(
        vpa: String,
        name: String,
        transactionNote: String,
        amount: String
    ): Intent {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", vpa)          // Payee VPA
            .appendQueryParameter("pn", name)         // Payee Name
            .appendQueryParameter("tn", transactionNote) // Transaction Note
            .appendQueryParameter("am", amount)       // Amount
            .appendQueryParameter("cu", "INR")        // Currency
            .build()
        
        return Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }
    }

    /**
     * Verifies the intent response from the UPI app.
     * Note: This is client-side only. In production, use a status-check API.
     */
    fun isPaymentSuccessful(data: Intent?): Boolean {
        val response = data?.getStringExtra("response") ?: return false
        // Response format usually looks like:
        // txnId=...&responseCode=00&Status=SUCCESS&txnRef=...
        return response.contains("Status=SUCCESS", ignoreCase = true) || 
               response.contains("Status=00", ignoreCase = true)
    }
}
