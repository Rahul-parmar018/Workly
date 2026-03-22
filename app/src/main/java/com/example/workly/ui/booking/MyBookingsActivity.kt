package com.example.workly.ui.booking

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Booking
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvBookings: RecyclerView
    private lateinit var layoutEmptyBookings: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: MyBookingsAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        initViews()
        setupBookingsListener()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvBookings = findViewById(R.id.rvBookings)
        layoutEmptyBookings = findViewById(R.id.layoutEmptyBookings)
        progressBar = findViewById(R.id.progressBar)

        adapter = MyBookingsAdapter()
        rvBookings.layoutManager = LinearLayoutManager(this)
        rvBookings.adapter = adapter
    }

    private fun setupBookingsListener() {
        val userId = auth.currentUser?.uid ?: return
        
        progressBar.visibility = View.VISIBLE
        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires index, use timestamp or simple sort for now
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                if (error != null) {
                    Log.e("MyBookings", "Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }
                
                val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                // Sort manually for now to avoid index requirements in dev
                val sortedBookings = bookings.sortedByDescending { it.id } 
                
                adapter.submitList(sortedBookings) {
                    if (sortedBookings.isNotEmpty()) {
                        layoutEmptyBookings.visibility = View.GONE
                    } else {
                        layoutEmptyBookings.visibility = View.VISIBLE
                    }
                }
            }
    }
}
