package com.example.workly.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore

class UserDirectoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var chipGroupRoles: ChipGroup

    private var allUsersList = mutableListOf<AdminUserData>()
    private var filteredUsersList = mutableListOf<AdminUserData>()

    private var currentSearchQuery: String = ""
    private var currentRoleFilter: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_directory)

        db = FirebaseFirestore.getInstance()

        // Initialize UI
        rvUsers = findViewById(R.id.rvUsers)
        progressBar = findViewById(R.id.progressBar)
        etSearch = findViewById(R.id.etSearch)
        chipGroupRoles = findViewById(R.id.chipGroupRoles)

        findViewById<View>(R.id.toolbar).findViewById<View>(R.id.toolbar).rootView.let {
            // Setup Toolbar navigation if needed, but toolbar title should suffice
        }
        
        // Manual toolbar setup since I'm using a CoordinatorLayout
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        // Setup RecyclerView
        rvUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = AdminUserAdapter(emptyList())
        rvUsers.adapter = userAdapter

        // Search logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.lowercase() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter logic
        chipGroupRoles.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            currentRoleFilter = when (checkedId) {
                R.id.chipUsers -> "user"
                R.id.chipProviders -> "provider"
                R.id.chipAdmins -> "admin"
                else -> "All"
            }
            applyFilters()
        }

        loadUsers()
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        db.collection("users").addSnapshotListener { snapshot, error ->
            progressBar.visibility = View.GONE
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                allUsersList = it.toObjects(AdminUserData::class.java).toMutableList()
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        filteredUsersList = allUsersList.filter { user ->
            val matchesSearch = user.name.lowercase().contains(currentSearchQuery) || 
                               user.email.lowercase().contains(currentSearchQuery)
            
            val matchesRole = if (currentRoleFilter == "All") {
                true
            } else {
                user.role.equals(currentRoleFilter, ignoreCase = true)
            }
            
            matchesSearch && matchesRole
        }.toMutableList()
        
        userAdapter.updateUsers(filteredUsersList)
    }
}
