package com.example.workly.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Service
import com.example.workly.ui.admin.AdminDashboardActivity
import com.example.workly.ui.auth.AuthSelectionActivity
import com.example.workly.ui.booking.MyBookingsActivity
import com.example.workly.ui.services.ServicesAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

// --- HOME FRAGMENT ---
class HomeFragment : Fragment() {
    private lateinit var rvServices: RecyclerView
    private lateinit var adapter: ServicesAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvUserName).text = auth.currentUser?.displayName ?: "User"
        
        rvServices = view.findViewById(R.id.rvServices)
        adapter = ServicesAdapter()
        rvServices.layoutManager = LinearLayoutManager(requireContext())
        rvServices.adapter = adapter
        
        // Mock data or load from Firestore
        adapter.submitList(listOf(
            Service("1", "Cleaning Service", "Home", 500.0, "https://images.unsplash.com/photo-1581578731548-c64695cc6958?auto=format&fit=crop&w=800&q=80"),
            Service("2", "Electricians", "Repair", 300.0, "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=800&q=80")
        ))
    }
}

// --- EXPLORE FRAGMENT ---
class ExploreFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }
}

// --- INBOX FRAGMENT ---
class InboxFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }
}

// --- PROFILE FRAGMENT ---
class ProfileFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<TextView>(R.id.tvProfileName).text = auth.currentUser?.displayName ?: "User"
        view.findViewById<TextView>(R.id.tvProfileEmail).text = auth.currentUser?.email ?: "Email"

        view.findViewById<MaterialButton>(R.id.btnMyBookings).setOnClickListener {
            startActivity(Intent(requireContext(), MyBookingsActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnAdminPanel).setOnClickListener {
            startActivity(Intent(requireContext(), AdminDashboardActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), AuthSelectionActivity::class.java))
            requireActivity().finishAffinity()
        }
    }
}
