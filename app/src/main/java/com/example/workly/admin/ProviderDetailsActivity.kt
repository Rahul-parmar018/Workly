package com.example.workly.admin

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.Provider
import com.example.workly.data.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProviderDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var providerId: String? = null
    
    private lateinit var tvName: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvEarnings: TextView
    private lateinit var tvJobsCount: TextView
    private lateinit var tvBio: TextView
    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: AdminServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_details)

        providerId = intent.getStringExtra("PROVIDER_ID")
        val initialName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"

        initViews()
        tvName.text = initialName

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadProviderData()
    }

    private fun initViews() {
        tvName = findViewById(R.id.tvDetailName)
        tvRating = findViewById(R.id.tvDetailRating)
        tvEarnings = findViewById(R.id.tvDetailEarnings)
        tvJobsCount = findViewById(R.id.tvDetailJobsCount)
        tvBio = findViewById(R.id.tvDetailBio)
        rvServices = findViewById(R.id.rvDetailServices)
    }

    private fun setupRecyclerView() {
        rvServices.layoutManager = LinearLayoutManager(this)
        // Strictly read-only: no approve/reject lambdas provided (or empty)
        serviceAdapter = AdminServiceAdapter(emptyList(), {}, {})
        rvServices.adapter = serviceAdapter
    }

    private fun loadProviderData() {
        val pid = providerId ?: return
        
        lifecycleScope.launch {
            try {
                // 1. Fetch Provider Bio/Stats
                val providerDoc = db.collection("providers").document(pid).get().await()
                val provider = providerDoc.toObject(Provider::class.java)
                
                provider?.let {
                    tvRating.text = "%.1f".format(it.rating)
                    tvEarnings.text = "₹${"%,.0f".format(it.hourlyRate * 10)}" // Placeholder for earnings UI using valid property
                    tvBio.text = it.bio.ifEmpty { "No biography provided by the professional." }
                }

                // 2. Fetch Assigned Services
                val servicesSnapshot = db.collection("services")
                    .whereEqualTo("providerId", pid)
                    .get().await()
                val services = servicesSnapshot.toObjects(Service::class.java)
                serviceAdapter.updateServices(services)
                tvJobsCount.text = services.size.toString()
                
            } catch (e: Exception) {
                // Silent fail for monitoring console
            }
        }
    }
}
