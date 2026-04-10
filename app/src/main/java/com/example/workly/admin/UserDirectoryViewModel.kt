package com.example.workly.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workly.admin.data.AdminRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserDirectoryViewModel(private val repository: AdminRepository = AdminRepository()) : ViewModel() {

    private val _userState = MutableStateFlow<AdminUIState<List<AdminUserData>>>(AdminUIState.Loading)
    val userState: StateFlow<AdminUIState<List<AdminUserData>>> = _userState

    private val _searchQuery = MutableStateFlow("")
    private val _roleFilter = MutableStateFlow("All")

    val filteredUsers = combine(_userState, _searchQuery, _roleFilter) { state, query, role ->
        if (state is AdminUIState.Success) {
            val users = state.data
            users.filter { user ->
                val matchesSearch = user.name.contains(query, ignoreCase = true) || 
                                   user.email.contains(query, ignoreCase = true)
                val matchesRole = if (role == "All") true else user.role.equals(role, ignoreCase = true)
                matchesSearch && matchesRole
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Mock data initially
        _userState.value = AdminUIState.Success(List(5) { 
            AdminUserData(it.toString(), "Loading User...", "...", "user") 
        })
        
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _userState.value = AdminUIState.Success(users)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateRoleFilter(role: String) {
        _roleFilter.value = role
    }
}
