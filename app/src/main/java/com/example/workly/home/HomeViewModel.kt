package com.example.workly.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workly.data.Booking
import com.example.workly.data.Service
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository = HomeRepository()) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _upcomingBookings = MutableStateFlow<List<Booking>>(emptyList())
    val upcomingBookings: StateFlow<List<Booking>> = _upcomingBookings.asStateFlow()

    private val _popularServices = MutableStateFlow<List<Service>>(repository.getPopularServices())
    val popularServices: StateFlow<List<Service>> = _popularServices.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUpcomingBookings()
    }

    private fun fetchUpcomingBookings() {
        viewModelScope.launch {
            repository.getUpcomingBookings().collect { bookings ->
                _upcomingBookings.value = bookings
                _isLoading.value = false
            }
        }
    }

    fun refreshUser() {
        _currentUser.value = repository.getCurrentUser()
    }
}
