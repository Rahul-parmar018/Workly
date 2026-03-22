package com.example.workly.ui.booking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.ui.home.HomeActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class BookingSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"
        val bookingId = intent.getStringExtra("BOOKING_ID") ?: "Unknown"
        val date = intent.getStringExtra("DATE") ?: ""
        val time = intent.getStringExtra("TIME") ?: ""
        val address = intent.getStringExtra("ADDRESS") ?: ""
        val price = intent.getDoubleExtra("PRICE", 0.0)

        findViewById<Chip>(R.id.chipBookingId).text = "Booking ID: #${bookingId.take(8).uppercase()}"
        findViewById<TextView>(R.id.tvFinalPrice).text = "₹${price.toInt()}"

        val layoutDetails = findViewById<LinearLayout>(R.id.layoutDetails)
        addDetailRow(layoutDetails, "Service", serviceName)
        addDetailRow(layoutDetails, "Professional", providerName)
        if (date.isNotEmpty()) addDetailRow(layoutDetails, "Date", date)
        if (time.isNotEmpty()) addDetailRow(layoutDetails, "Time", time)
        if (address.isNotEmpty()) addDetailRow(layoutDetails, "Address", address)

        findViewById<MaterialButton>(R.id.btnGoHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        findViewById<MaterialButton>(R.id.btnChatPro).setOnClickListener {
            // Future implementation
        }
    }

    private fun addDetailRow(container: LinearLayout, label: String, value: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_detail_row, container, false)
        view.findViewById<TextView>(R.id.tvLabel).text = label
        view.findViewById<TextView>(R.id.tvValue).text = value
        container.addView(view)
    }
}
