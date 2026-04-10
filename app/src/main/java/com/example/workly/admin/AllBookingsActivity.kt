package com.example.workly.admin

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.Booking
import com.example.workly.data.OrderStatus
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class AllBookingsActivity : AppCompatActivity() {

    private val viewModel: BookingsViewModel by viewModels()
    private lateinit var rvBookings: RecyclerView
    private lateinit var adapter: AdminBookingAdapter
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_bookings)

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvBookings = findViewById(R.id.rvBookings)
        chipGroupStatus = findViewById(R.id.chipGroupStatus)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        rvBookings.layoutManager = LinearLayoutManager(this)
        adapter = AdminBookingAdapter(emptyList()) { booking ->
            // Optionally: Details View
            Toast.makeText(this, "Order for ${booking.userName}", Toast.LENGTH_SHORT).show()
        }
        rvBookings.adapter = adapter
    }

    private fun setupListeners() {
        chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            val status = when (checkedId) {
                R.id.chipPending -> OrderStatus.PENDING
                R.id.chipAccepted -> OrderStatus.ACCEPTED
                R.id.chipCompleted -> OrderStatus.COMPLETED
                R.id.chipCancelled -> OrderStatus.CANCELLED
                else -> "All"
            }
            viewModel.updateStatusFilter(status)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredBookings.collect { bookings ->
                    adapter.updateBookings(bookings)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookingState.collect { state ->
                    progressBar.visibility = if (state is AdminUIState.Loading) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
