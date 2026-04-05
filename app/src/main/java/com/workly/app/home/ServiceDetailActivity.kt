package com.workly.app.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.workly.app.R
import com.workly.app.booking.BookingActivity

class ServiceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)

        val title = intent.getStringExtra("SERVICE_TITLE") ?: "Service"
        val desc = intent.getStringExtra("SERVICE_DESC") ?: "No description provided."
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val duration = intent.getStringExtra("SERVICE_DURATION") ?: "1 hr"
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Unknown Provider"
        val providerId = intent.getStringExtra("PROVIDER_ID") ?: ""
        val category = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        val serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        val imgUrl = intent.getStringExtra("SERVICE_IMG")

        findViewById<TextView>(R.id.tv_title).text = title
        findViewById<TextView>(R.id.tv_description).text = desc
        findViewById<TextView>(R.id.tv_price).text = "₹${price.toInt()}"
        findViewById<TextView>(R.id.tv_duration).text = "Duration: $duration"
        findViewById<TextView>(R.id.tv_provider).text = "Provider: $providerName"

        val ivService = findViewById<ImageView>(R.id.iv_service)
        if (!imgUrl.isNullOrEmpty()) {
            ivService.load(imgUrl)
        }

        findViewById<Button>(R.id.btn_book).setOnClickListener {
            // Navigate to BookingActivity (or a refactored simplified version of it)
            val bookIntent = Intent(this, BookingActivity::class.java).apply {
                putExtra("SERVICE_TITLE", title)
                putExtra("SERVICE_PRICE", price)
                putExtra("SERVICE_CATEGORY", category)
                putExtra("SERVICE_ID", serviceId)
                putExtra("PROVIDER_NAME", providerName)
                putExtra("PROVIDER_ID", providerId)
            }
            startActivity(bookIntent)
            finish()
        }
        
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}
