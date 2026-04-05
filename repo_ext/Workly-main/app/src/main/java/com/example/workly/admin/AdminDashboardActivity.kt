package com.example.workly.admin

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import android.content.Intent
import com.example.workly.data.Booking
import com.example.workly.data.OrderStatus
import com.google.android.material.button.MaterialButton

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvServicePendingCount: TextView
    private lateinit var tvNoPending: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAdminProfile: View
    
    private lateinit var rvPendingProviders: RecyclerView
    private lateinit var providerAdapter: PendingProviderAdapter
    
    private var allUsersList = mutableListOf<AdminUserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()

        // Initialize UI
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvServicePendingCount = findViewById(R.id.tvServicePendingCount)
        tvNoPending = findViewById(R.id.tvNoPending)
        progressBar = findViewById(R.id.progressBar)
        btnAdminProfile = findViewById(R.id.btnAdminProfile)
        
        // Navigation Buttons
        val btnManageServices: View = findViewById(R.id.btnManageServices)
        val btnAllBookings: View = findViewById(R.id.btnAllBookings)
        val btnUserDirectory: View = findViewById(R.id.btnUserDirectory)

        btnAdminProfile.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }

        btnManageServices.setOnClickListener {
            startActivity(Intent(this, ManageServicesActivity::class.java))
        }

        btnAllBookings.setOnClickListener {
            startActivity(Intent(this, AllBookingsActivity::class.java))
        }

        btnUserDirectory.setOnClickListener {
             Toast.makeText(this, "User Management coming soon in next update!", Toast.LENGTH_SHORT).show()
        }
        
        rvPendingProviders = findViewById(R.id.rvPendingProviders)

        // Setup RecyclerView for Providers
        rvPendingProviders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        providerAdapter = PendingProviderAdapter(
            emptyList(),
            onApproveClick = { provider -> approveProvider(provider) },
            onRejectClick = { provider -> rejectProvider(provider) }
        )
        rvPendingProviders.adapter = providerAdapter

        loadDashboardData()
        loadAnalytics()
    }

    private fun loadDashboardData() {
        progressBar.visibility = View.VISIBLE
        // Load users to get general stats and helper data for providers
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                val newUsers = it.toObjects(AdminUserData::class.java)
                allUsersList.clear()
                allUsersList.addAll(newUsers)
                tvTotalUsers.text = allUsersList.size.toString()
                
                checkPendingProviders() 
            }
        }
    }

    private fun loadAnalytics() {
        // 1. Revenue Aggregation
        db.collection("orders")
            .whereEqualTo("status", OrderStatus.COMPLETED)
            .addSnapshotListener { snapshot, _ ->
                var total = 0.0
                snapshot?.documents?.forEach { total += it.getDouble("finalPrice") ?: 0.0 }
                tvTotalRevenue.text = "₹${"%,.2f".format(total)}"
            }

        // 2. Pending Service Requests (The Rapido-style approval system)
        db.collection("services")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                tvServicePendingCount.text = if (count > 0) "$count Pending Requests" else "No Pending Requests"
            }
    }

    private fun checkPendingProviders() {
        db.collection("providers")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                if (error != null || snapshot == null) return@addSnapshotListener

                val pendingList = mutableListOf<PendingProviderData>()
                for (doc in snapshot.documents) {
                    val pid = doc.id
                    val user = allUsersList.find { it.id == pid }
                    pendingList.add(PendingProviderData(pid, user?.name ?: "Professional", user?.email ?: ""))
                }
                
                providerAdapter.updateProviders(pendingList)
                
                if (pendingList.isEmpty()) {
                    rvPendingProviders.visibility = View.GONE
                    tvNoPending.visibility = View.VISIBLE
                } else {
                    rvPendingProviders.visibility = View.VISIBLE
                    tvNoPending.visibility = View.GONE
                }
            }
    }

    private fun approveProvider(provider: PendingProviderData) {
        db.collection("providers").document(provider.providerId).update("isApproved", true)
            .addOnSuccessListener { Toast.makeText(this, "Provider Approved!", Toast.LENGTH_SHORT).show() }
    }

    private fun rejectProvider(provider: PendingProviderData) {
        db.collection("providers").document(provider.providerId).delete()
            .addOnSuccessListener {
                db.collection("users").document(provider.providerId).update("role", "user")
                Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show()
            }
    }
}



