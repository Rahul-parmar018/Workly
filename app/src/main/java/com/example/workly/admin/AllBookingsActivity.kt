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

class AllBookingsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminBookingAdapter
    private var allBookingsList = mutableListOf<Booking>()
    
    private lateinit var rvBookings: RecyclerView
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_bookings)

        db = FirebaseFirestore.getInstance()

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvBookings = findViewById(R.id.rvBookings)
        chipGroupStatus = findViewById(R.id.chipGroupStatus)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        setupListeners()
        loadBookings()
    }

    private fun setupRecyclerView() {
        rvBookings.layoutManager = LinearLayoutManager(this)
        adapter = AdminBookingAdapter(emptyList()) { booking ->
            // Optionally: Details View
            Toast.makeText(this, "Booking for ${booking.userName}", Toast.LENGTH_SHORT).show()
        }
        rvBookings.adapter = adapter
    }

    private fun setupListeners() {
        chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.chipPending -> OrderStatus.PENDING
                R.id.chipAccepted -> OrderStatus.ACCEPTED
                R.id.chipCompleted -> OrderStatus.COMPLETED
                R.id.chipCancelled -> OrderStatus.CANCELLED
                else -> "all"
            }
            filterBookings(status)
        }
    }

    private fun loadBookings() {
        progressBar.visibility = View.VISIBLE
        db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val bookings = it.toObjects(Booking::class.java)
                    allBookingsList.clear()
                    allBookingsList.addAll(bookings)
                    
                    // Maintain current filter
                    val checkedChipId = chipGroupStatus.checkedChipId
                    val status = when (checkedChipId) {
                        R.id.chipPending -> OrderStatus.PENDING
                        R.id.chipAccepted -> OrderStatus.ACCEPTED
                        R.id.chipCompleted -> OrderStatus.COMPLETED
                        R.id.chipCancelled -> OrderStatus.CANCELLED
                        else -> "all"
                    }
                    filterBookings(status)
                }
            }
    }

    private fun filterBookings(status: String) {
        val filtered = if (status == "all") {
            allBookingsList
        } else {
            allBookingsList.filter { it.status == status }
        }
        adapter.updateBookings(filtered)
    }
}
