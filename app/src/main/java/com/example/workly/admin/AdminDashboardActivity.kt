package com.example.workly.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.Provider
import kotlinx.coroutines.launch

/**
 * Premium Admin Dashboard - Monitoring Hub.
 * Strictly Read-Only as per Security Requirements.
 */
class AdminDashboardActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()
    
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvTodayRevenue: TextView
    private lateinit var tvActiveProviders: TextView
    private lateinit var tvNoPending: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var rvPendingProviders: RecyclerView
    private lateinit var pendingAdapter: PendingProviderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        initViews()
        setupListeners()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue)
        tvActiveProviders = findViewById(R.id.tvActiveProviders)
        tvNoPending = findViewById(R.id.tvNoPending)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnAdminProfile).setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }

        findViewById<View>(R.id.btnManageServices).setOnClickListener {
            startActivity(Intent(this, ManageServicesActivity::class.java))
        }

        findViewById<View>(R.id.btnAllBookings).setOnClickListener {
            startActivity(Intent(this, AllBookingsActivity::class.java))
        }

        findViewById<View>(R.id.btnUserDirectory).setOnClickListener {
            startActivity(Intent(this, UserDirectoryActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        rvPendingProviders = findViewById(R.id.rvPendingProviders)
        rvPendingProviders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        pendingAdapter = PendingProviderAdapter(emptyList()) { pendingProvider ->
            // Open Provider Details for Read-Only Review
            val intent = Intent(this, ProviderDetailsActivity::class.java).apply {
                putExtra("PROVIDER_ID", pendingProvider.providerId)
                putExtra("PROVIDER_NAME", pendingProvider.name)
            }
            startActivity(intent)
        }
        rvPendingProviders.adapter = pendingAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Revenue
                launch {
                    viewModel.totalRevenue.collect { total ->
                        tvTotalRevenue.text = "₹${"%,.0f".format(total)}"
                    }
                }
                
                launch {
                    viewModel.todayRevenue.collect { today ->
                        tvTodayRevenue.text = "+₹${"%,.0f".format(today)} Today"
                    }
                }

                // Observe User Counts
                launch {
                    viewModel.userState.collect { state ->
                        if (state is AdminUIState.Success) {
                            tvTotalUsers.text = state.data.size.toString()
                        }
                    }
                }

                // Observe Provider Counts
                launch {
                    viewModel.providerState.collect { state ->
                        if (state is AdminUIState.Success) {
                            tvActiveProviders.text = state.data.count { it.isActive }.toString()
                            
                            // Map real providers to pending UI items
                            val pending = state.data.filter { !it.isActive }.map {
                                // In a real app, we'd fetch the user name from userState or join data
                                PendingProviderData(it.id, it.name.ifEmpty { "New Professional" }, "")
                            }
                            
                            updatePendingList(pending)
                        }
                    }
                }

                launch {
                    viewModel.bookingState.collect { state ->
                        progressBar.visibility = if (state is AdminUIState.Loading) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun updatePendingList(pending: List<PendingProviderData>) {
        pendingAdapter.updateProviders(pending)
        if (pending.isEmpty()) {
            rvPendingProviders.visibility = View.GONE
            tvNoPending.visibility = View.VISIBLE
        } else {
            rvPendingProviders.visibility = View.VISIBLE
            tvNoPending.visibility = View.GONE
        }
    }
}
