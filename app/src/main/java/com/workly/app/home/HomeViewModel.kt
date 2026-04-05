package com.workly.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workly.app.data.Order
import com.workly.app.data.Service
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository = HomeRepository()) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _upcomingBookings = MutableStateFlow<List<Order>>(emptyList())
    val upcomingBookings: StateFlow<List<Order>> = _upcomingBookings.asStateFlow()

    private val _popularServices = MutableStateFlow<List<Service>>(emptyList())
    val popularServices: StateFlow<List<Service>> = _popularServices.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUpcomingBookings()
        fetchPopularServices()
    }

    private fun fetchUpcomingBookings() {
        viewModelScope.launch {
            repository.getUpcomingBookings().collect { orders ->
                _upcomingBookings.value = orders
                _isLoading.value = false
            }
        }
    }

    private fun fetchPopularServices() {
        viewModelScope.launch {
            repository.getPopularServices().collect { services ->
                _popularServices.value = services
            }
        }
    }

    fun refreshUser() {
        _currentUser.value = repository.getCurrentUser()
    }
}
