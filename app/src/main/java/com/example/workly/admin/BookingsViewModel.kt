package com.example.workly.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workly.admin.data.AdminRepository
import com.example.workly.data.Booking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookingsViewModel(private val repository: AdminRepository = AdminRepository()) : ViewModel() {

    private val _bookingState = MutableStateFlow<AdminUIState<List<Booking>>>(AdminUIState.Loading)
    val bookingState: StateFlow<AdminUIState<List<Booking>>> = _bookingState

    private val _statusFilter = MutableStateFlow("All")

    val filteredBookings = combine(_bookingState, _statusFilter) { state, status ->
        if (state is AdminUIState.Success) {
            val bookings = state.data
            if (status == "All") bookings else bookings.filter { it.status.equals(status, ignoreCase = true) }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Mock data initially
        _bookingState.value = AdminUIState.Success(List(3) { 
            Booking(
                serviceName = "Loading...",
                userName = "...",
                status = "pending",
                finalPrice = 0.0
            )
        })
        
        fetchBookings()
    }

    private fun fetchBookings() {
        viewModelScope.launch {
            repository.getAllBookings().collect { bookings ->
                _bookingState.value = AdminUIState.Success(bookings)
            }
        }
    }

    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
    }
}
