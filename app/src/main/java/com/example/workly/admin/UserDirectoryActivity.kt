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
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class UserDirectoryActivity : AppCompatActivity() {

    private val viewModel: UserDirectoryViewModel by viewModels()
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var chipGroupRoles: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_directory)

        // Initialize UI
        rvUsers = findViewById(R.id.rvUsers)
        progressBar = findViewById(R.id.progressBar)
        etSearch = findViewById(R.id.etSearch)
        chipGroupRoles = findViewById(R.id.chipGroupRoles)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        rvUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = AdminUserAdapter(emptyList())
        rvUsers.adapter = userAdapter
    }

    private fun setupListeners() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString()?.lowercase() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        chipGroupRoles.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            val role = when (checkedId) {
                R.id.chipUsers -> "user"
                R.id.chipProviders -> "provider"
                R.id.chipAdmins -> "admin"
                else -> "All"
            }
            viewModel.updateRoleFilter(role)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredUsers.collect { users ->
                    userAdapter.updateUsers(users)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect { state ->
                    progressBar.visibility = if (state is AdminUIState.Loading) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
