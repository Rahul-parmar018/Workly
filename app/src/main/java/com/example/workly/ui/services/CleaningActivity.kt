package com.example.workly.ui.services

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.ui.booking.BookingActivity
import com.google.android.material.appbar.MaterialToolbar

class CleaningActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvCleaningServices: RecyclerView
    private lateinit var adapter: CleaningServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaning)

        initViews()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvCleaningServices = findViewById(R.id.rvCleaningServices)

        val services = listOf(
            CleaningService("Deep Cleaning", "Thorough cleaning for every corner.", "$80", "4.8", R.drawable.ic_home),
            CleaningService("Standard Cleaning", "Regular maintenance cleaning.", "$45", "4.6", R.drawable.ic_home),
            CleaningService("Move-In/Out", "Get your place ready for the big move.", "$120", "4.9", R.drawable.ic_home),
            CleaningService("Sofa Cleaning", "Shampoo and vacuum for sofas.", "$30", "4.7", R.drawable.ic_home)
        )

        adapter = CleaningServiceAdapter(services) { serviceName ->
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("SERVICE_NAME", serviceName)
                putExtra("SERVICE_CATEGORY", "Cleaning")
                putExtra("SERVICE_PRICE", parsePrice(serviceName))
            }
            startActivity(intent)
        }

        rvCleaningServices.layoutManager = LinearLayoutManager(this)
        rvCleaningServices.adapter = adapter
    }

    private fun parsePrice(name: String): Double {
        return when (name) {
            "Deep Cleaning" -> 80.0
            "Standard Cleaning" -> 45.0
            "Move-In/Out" -> 120.0
            "Sofa Cleaning" -> 30.0
            else -> 40.0
        }
    }
}
