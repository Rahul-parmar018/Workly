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
import android.content.Intent
import com.example.workly.data.OrderStatus
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvTodayRevenue: TextView
    private lateinit var tvNewUsers: TextView
    private lateinit var tvActiveProviders: TextView
    private lateinit var tvServicePendingCount: TextView
    private lateinit var tvNoPending: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAdminProfile: View

    private lateinit var rvPendingProviders: RecyclerView
    private lateinit var providerAdapter: PendingProviderAdapter

    // FIX #8: Store all listeners for cleanup to prevent memory leaks
    private val listeners = mutableListOf<ListenerRegistration>()

    // FIX #1 & #4: Single source of truth — users map keyed by document ID
    private val usersMap = mutableMapOf<String, AdminUserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()

        // Initialize UI
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue)
        tvNewUsers = findViewById(R.id.tvNewUsers)
        tvActiveProviders = findViewById(R.id.tvActiveProviders)
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
            startActivity(Intent(this, UserDirectoryActivity::class.java))
        }

        rvPendingProviders = findViewById(R.id.rvPendingProviders)
        rvPendingProviders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        providerAdapter = PendingProviderAdapter(
            emptyList(),
            onApproveClick = { provider -> approveProvider(provider) },
            onRejectClick = { provider -> rejectProvider(provider) }
        )
        rvPendingProviders.adapter = providerAdapter

        loadDashboardData()
        loadAnalytics()
        loadProviderStats()
    }

    private fun loadDashboardData() {
        progressBar.visibility = View.VISIBLE
        // FIX #1 & #4: Populate id using doc.id, then call checkPendingProviders INSIDE this listener
        // so usersMap is always ready before providers are looked up — fixes the race condition.
        val reg = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                usersMap.clear()
                // FIX #1: Correctly populate id from Firestore document ID
                it.documents.forEach { doc ->
                    val user = doc.toObject(AdminUserData::class.java)
                    if (user != null) {
                        usersMap[doc.id] = user.copy(id = doc.id)
                    }
                }

                val allUsers = usersMap.values.toList()
                tvTotalUsers.text = allUsers.size.toString()
                tvNewUsers.text = "${allUsers.count { u -> u.role != "admin" }} Active Base"

                // FIX #4: Called AFTER usersMap is populated — no more race condition
                checkPendingProviders()
            }
        }
        listeners.add(reg)
    }

    private fun loadAnalytics() {
        val startOfDay = getStartOfDayTimestamp()

        val revenueReg = db.collection("orders")
            .whereEqualTo("status", OrderStatus.COMPLETED)
            .addSnapshotListener { snapshot, _ ->
                var total = 0.0
                var todayTotal = 0.0

                snapshot?.documents?.forEach { doc ->
                    val price = doc.getDouble("finalPrice") ?: 0.0
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    total += price
                    if (createdAt >= startOfDay) todayTotal += price
                }
                tvTotalRevenue.text = "₹${"%,.0f".format(total)}"
                tvTodayRevenue.text = "+ ₹${"%,.0f".format(todayTotal)} Today"
            }
        listeners.add(revenueReg)

        val servicesReg = db.collection("services")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                tvServicePendingCount.text = if (count > 0) "$count Requests" else "0 Requests"
            }
        listeners.add(servicesReg)
    }

    private fun loadProviderStats() {
        val reg = db.collection("providers")
            .whereEqualTo("isApproved", true)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                tvActiveProviders.text = count.toString()
            }
        listeners.add(reg)
    }

    private fun checkPendingProviders() {
        // FIX #4: This is now called from inside the users listener, so usersMap is ready.
        // We use a one-time get() here instead of another snapshot listener to avoid double-listener complexity.
        db.collection("providers")
            .whereEqualTo("isApproved", false)
            .get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                val pendingList = snapshot.documents.mapNotNull { doc ->
                    val pid = doc.id
                    // FIX #1: usersMap is now populated with correct IDs — lookup works correctly
                    val user = usersMap[pid]
                    PendingProviderData(pid, user?.name ?: "Professional", user?.email ?: "")
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
            .addOnFailureListener {
                progressBar.visibility = View.GONE
            }
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun approveProvider(provider: PendingProviderData) {
        val batch = db.batch()

        // Update /providers document
        val providerRef = db.collection("providers").document(provider.providerId)
        batch.update(providerRef, "isApproved", true)

        // FIX #9: Also update /users document so backend auth middleware recognizes approval
        val userRef = db.collection("users").document(provider.providerId)
        batch.update(userRef, "isApproved", true)

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Provider Approved!", Toast.LENGTH_SHORT).show()
                // Refresh pending list
                checkPendingProviders()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Approval failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectProvider(provider: PendingProviderData) {
        // FIX #12: Delete provider's services + provider doc + reset user role — all in one batch
        db.collection("services")
            .whereEqualTo("providerId", provider.providerId)
            .get()
            .addOnSuccessListener { serviceSnapshot ->
                val batch = db.batch()

                // Delete all services submitted by this provider
                serviceSnapshot.documents.forEach { serviceDoc ->
                    batch.delete(db.collection("services").document(serviceDoc.id))
                }

                // Delete the provider document
                batch.delete(db.collection("providers").document(provider.providerId))

                // Reset user role back to "user"
                batch.update(db.collection("users").document(provider.providerId), "role", "user")

                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request Rejected & Cleaned Up", Toast.LENGTH_SHORT).show()
                        checkPendingProviders()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Rejection failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    // FIX #8: Remove all listeners to prevent memory leaks
    override fun onDestroy() {
        listeners.forEach { it.remove() }
        super.onDestroy()
    }
}
