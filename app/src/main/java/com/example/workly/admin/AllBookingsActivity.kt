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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AllBookingsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminBookingAdapter
    private var allBookingsList = mutableListOf<Booking>()

    private lateinit var rvBookings: RecyclerView
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var progressBar: ProgressBar

    // FIX #8: Store listener for cleanup
    private var bookingsListener: ListenerRegistration? = null

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
            // Show detail dialog with admin force-update options
            showBookingActionDialog(booking)
        }
        rvBookings.adapter = adapter
    }

    private fun setupListeners() {
        chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.chipPending   -> OrderStatus.PENDING
                R.id.chipAccepted  -> OrderStatus.ACCEPTED
                // FIX #7: Added arriving and started to filter correctly
                R.id.chipArriving  -> OrderStatus.ARRIVING
                R.id.chipStarted   -> OrderStatus.STARTED
                R.id.chipCompleted -> OrderStatus.COMPLETED
                R.id.chipCancelled -> OrderStatus.CANCELLED
                else -> "all"
            }
            filterBookings(status)
        }
    }

    private fun loadBookings() {
        progressBar.visibility = View.VISIBLE
        // FIX #8: Store listener reference for onDestroy cleanup
        bookingsListener = db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    // FIX #2: Use mapNotNull + .copy(id = doc.id) to correctly populate id field
                    val bookings = it.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(id = doc.id)
                    }
                    allBookingsList.clear()
                    allBookingsList.addAll(bookings)

                    // Maintain current filter
                    val checkedChipId = chipGroupStatus.checkedChipId
                    val status = when (checkedChipId) {
                        R.id.chipPending   -> OrderStatus.PENDING
                        R.id.chipAccepted  -> OrderStatus.ACCEPTED
                        R.id.chipArriving  -> OrderStatus.ARRIVING
                        R.id.chipStarted   -> OrderStatus.STARTED
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

    // FIX #5: Admin can now force-update booking status from the detail dialog
    private fun showBookingActionDialog(booking: Booking) {
        val statusOptions = arrayOf("Mark Completed", "Mark Cancelled", "Reset to Pending")
        android.app.AlertDialog.Builder(this)
            .setTitle("Booking #${booking.id.takeLast(8).uppercase()}\n${booking.userName} • ${booking.serviceName}")
            .setItems(statusOptions) { _, which ->
                val newStatus = when (which) {
                    0 -> OrderStatus.COMPLETED
                    1 -> OrderStatus.CANCELLED
                    2 -> OrderStatus.PENDING
                    else -> return@setItems
                }
                forceUpdateBookingStatus(booking.id, newStatus)
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    private fun forceUpdateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("orders").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // FIX #8: Cleanup listener on destroy
    override fun onDestroy() {
        bookingsListener?.remove()
        super.onDestroy()
    }
}
