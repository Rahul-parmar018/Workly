package com.example.workly.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Booking
import com.example.workly.ui.booking.MyBookingsAdapter
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// --- OVERVIEW FRAGMENT ---
class AdminOverviewFragment : Fragment() {
    private lateinit var rvRecentBookings: RecyclerView
    private lateinit var adapter: MyBookingsAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_admin_overview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvRecentBookings = view.findViewById(R.id.rvRecentBookings)
        adapter = MyBookingsAdapter()
        rvRecentBookings.layoutManager = LinearLayoutManager(requireContext())
        rvRecentBookings.adapter = adapter

        setupListeners(view)
    }

    private fun setupListeners(view: View) {
        firestore.collection("bookings")
            .orderBy("id", Query.Direction.DESCENDING) // Fallback for sorting
            .limit(5)
            .addSnapshotListener { snapshot, _ ->
                val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                adapter.submitList(bookings)
                updateStats(view, bookings)
            }
    }

    private fun updateStats(view: View, bookings: List<Booking>) {
        // Find stat cards by position and update (Simplified)
        // In real app, IDs would be unique or data binding used
    }
}

// --- BOOKINGS FRAGMENT ---
class AdminBookingsFragment : Fragment() {
    private lateinit var rvAdminBookings: RecyclerView
    private lateinit var tabLayoutFilters: TabLayout
    private lateinit var adapter: MyBookingsAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var allBookings = listOf<Booking>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_admin_bookings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvAdminBookings = view.findViewById(R.id.rvAdminBookings)
        tabLayoutFilters = view.findViewById(R.id.tabLayoutFilters)
        adapter = MyBookingsAdapter()
        rvAdminBookings.layoutManager = LinearLayoutManager(requireContext())
        rvAdminBookings.adapter = adapter

        val filters = listOf("All", "Pending", "Confirmed", "InProgress", "Completed", "Cancelled")
        filters.forEach { tabLayoutFilters.addTab(tabLayoutFilters.newTab().setText(it)) }

        tabLayoutFilters.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { filterBookings(tab?.text.toString()) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadBookings()
    }

    private fun loadBookings() {
        firestore.collection("bookings").addSnapshotListener { snapshot, _ ->
            allBookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
            filterBookings(tabLayoutFilters.getTabAt(tabLayoutFilters.selectedTabPosition)?.text.toString())
        }
    }

    private fun filterBookings(status: String) {
        val filtered = if (status == "All") allBookings else allBookings.filter { it.status == status }
        adapter.submitList(filtered)
    }
}

// --- PROVIDERS FRAGMENT ---
class AdminProvidersFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_admin_providers, container, false)
}

// --- ANALYTICS FRAGMENT ---
class AdminAnalyticsFragment : Fragment() {
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvCompletedJobs: TextView
    private lateinit var tvAvgOrder: TextView
    private lateinit var layoutStatusBreakdown: LinearLayout
    private lateinit var layoutCategoryRevenue: LinearLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_admin_analytics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue)
        tvCompletedJobs = view.findViewById(R.id.tvCompletedJobs)
        tvAvgOrder = view.findViewById(R.id.tvAvgOrder)
        layoutStatusBreakdown = view.findViewById(R.id.layoutStatusBreakdown)
        layoutCategoryRevenue = view.findViewById(R.id.layoutCategoryRevenue)

        loadAnalytics()
    }

    private fun loadAnalytics() {
        firestore.collection("bookings").addSnapshotListener { snapshot, _ ->
            val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
            updateUI(bookings)
        }
    }

    private fun updateUI(bookings: List<Booking>) {
        val completed = bookings.filter { it.status == "Completed" }
        val revenue = completed.sumOf { it.finalPrice }
        
        tvTotalRevenue.text = "₹${revenue.toInt()}"
        tvCompletedJobs.text = completed.size.toString()
        tvAvgOrder.text = "₹${if (completed.isNotEmpty()) (revenue / completed.size).toInt() else 0}"

        // Update breakdowns dynamically
        updateStatusBreakdown(bookings)
    }

    private fun updateStatusBreakdown(bookings: List<Booking>) {
        layoutStatusBreakdown.removeAllViews()
        val statuses = listOf("Pending", "Confirmed", "InProgress", "Completed", "Cancelled")
        val total = bookings.size.coerceAtLeast(1)

        statuses.forEach { status ->
            val count = bookings.count { it.status == status }
            val pct = (count.toFloat() / total * 100).toInt()
            
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_analytics_item, layoutStatusBreakdown, false)
            itemView.findViewById<TextView>(R.id.tvItemLabel).text = status
            itemView.findViewById<TextView>(R.id.tvItemValue).text = "$count ($pct%)"
            itemView.findViewById<LinearProgressIndicator>(R.id.progressIndicator).progress = pct
            
            layoutStatusBreakdown.addView(itemView)
        }
    }
}
