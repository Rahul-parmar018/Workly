package com.example.workly.ui.booking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Provider
import com.example.workly.utils.AIMatcher
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore

class ProviderListActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvProviders: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var providerAdapter: ProviderAdapter

    private var serviceCategory = ""
    private var userLat = 0.0
    private var userLon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_list)

        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        userLat = intent.getDoubleExtra("USER_LAT", 0.0)
        userLon = intent.getDoubleExtra("USER_LON", 0.0)

        initViews()
        fetchProviders()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvProviders = findViewById(R.id.rvProviders)
        progressBar = findViewById(R.id.progressBar)

        providerAdapter = ProviderAdapter { provider ->
            val result = Intent().apply {
                putExtra("SELECTED_PROVIDER_NAME", provider.name)
                putExtra("SELECTED_PROVIDER_ID", provider.id)
                putExtra("SELECTED_PROVIDER_RATE", provider.hourlyRate)
            }
            setResult(RESULT_OK, result)
            finish()
        }
        rvProviders.layoutManager = LinearLayoutManager(this)
        rvProviders.adapter = providerAdapter
    }

    private fun fetchProviders() {
        progressBar.visibility = View.VISIBLE
        val firestore = FirebaseFirestore.getInstance()
        
        var query = firestore.collection("providers").limit(20)
        if (serviceCategory.isNotEmpty()) {
            query = firestore.collection("providers")
                .whereArrayContains("specialties", serviceCategory)
                .limit(20)
        }

        query.get().addOnSuccessListener { snapshot ->
            val firestoreProviders = snapshot.toObjects(Provider::class.java)
            val finalProviders = if (firestoreProviders.isEmpty()) getFallbackProviders() else firestoreProviders
            
            val scoredProviders = finalProviders
                .map { it to AIMatcher.calculateScore(it, userLat, userLon) }
                .sortedByDescending { it.second }
            
            providerAdapter.submitList(scoredProviders)
            progressBar.visibility = View.GONE
        }.addOnFailureListener {
            val scoredProviders = getFallbackProviders()
                .map { it to AIMatcher.calculateScore(it, userLat, userLon) }
                .sortedByDescending { it.second }
            providerAdapter.submitList(scoredProviders)
            progressBar.visibility = View.GONE
        }
    }

    private fun getFallbackProviders() = listOf(
        Provider("pro_1", "Alex Johnson", 4.9f, 45.0, listOf("Cleaning", "Repair"), 28.6, 77.2, 8, 127),
        Provider("pro_2", "Maria Garcia", 4.8f, 38.0, listOf("Cleaning", "Wellness"), 28.61, 77.21, 5, 85),
        Provider("pro_3", "David Smith", 4.7f, 55.0, listOf("Repair", "Electric"), 28.62, 77.19, 10, 200),
        Provider("pro_4", "Priya Sharma", 4.9f, 42.0, listOf("Plumbing", "Cleaning"), 28.63, 77.22, 6, 156)
    )
}
