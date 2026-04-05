package com.example.workly.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.Service
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class ManageServicesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminServiceAdapter
    private var allServicesList = mutableListOf<Service>()
    
    private lateinit var rvServices: RecyclerView
    private lateinit var etSearch: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_services)

        db = FirebaseFirestore.getInstance()

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = "Service Approvals"
        toolbar.setNavigationOnClickListener { finish() }

        rvServices = findViewById(R.id.rvServices)
        etSearch = findViewById(R.id.etSearch)
        
        // Hide FAB as Admins no longer add services manually
        findViewById<View>(R.id.fabAddService)?.visibility = View.GONE

        setupRecyclerView()
        setupListeners()
        loadPendingServices()
    }

    private fun setupRecyclerView() {
        rvServices.layoutManager = LinearLayoutManager(this)
        adapter = AdminServiceAdapter(
            emptyList(),
            onApproveClick = { service -> approveService(service) },
            onRejectClick = { service -> rejectService(service) }
        )
        rvServices.adapter = adapter
    }

    private fun setupListeners() {
        etSearch.addTextChangedListener { text ->
            filterServices(text.toString())
        }
    }

    private fun loadPendingServices() {
        db.collection("services")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val services = it.toObjects(Service::class.java)
                    allServicesList.clear()
                    allServicesList.addAll(services)
                    filterServices(etSearch.text.toString())
                }
            }
    }

    private fun filterServices(query: String) {
        val filtered = allServicesList.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true)
        }
        adapter.updateServices(filtered)
    }

    private fun approveService(service: Service) {
        db.collection("services").document(service.id)
            .update("isApproved", true, "status", "approved")
            .addOnSuccessListener {
                Toast.makeText(this, "Service Approved & Live!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Approval failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectService(service: Service) {
        // Option: Delete service entirely on rejection to keep DB clean
        db.collection("services").document(service.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Service Request Rejected", Toast.LENGTH_SHORT).show()
            }
    }
}

