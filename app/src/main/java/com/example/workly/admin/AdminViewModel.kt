package com.example.workly.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workly.admin.data.AdminRepository
import com.example.workly.data.Booking
import com.example.workly.data.OrderStatus
import com.example.workly.data.Provider
import com.example.workly.data.Service
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * AdminViewModel - Manages dashboard data and analytics.
 * Supports realistic mock data and smooth transitions to real data.
 */
class AdminViewModel(private val repository: AdminRepository = AdminRepository()) : ViewModel() {

    // UI States
    private val _userState = MutableStateFlow<AdminUIState<List<AdminUserData>>>(AdminUIState.Loading)
    val userState: StateFlow<AdminUIState<List<AdminUserData>>> = _userState

    private val _bookingState = MutableStateFlow<AdminUIState<List<Booking>>>(AdminUIState.Loading)
    val bookingState: StateFlow<AdminUIState<List<Booking>>> = _bookingState

    private val _providerState = MutableStateFlow<AdminUIState<List<Provider>>>(AdminUIState.Loading)
    val providerState: StateFlow<AdminUIState<List<Provider>>> = _providerState

    // Analytics state
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue

    private val _todayRevenue = MutableStateFlow(0.0)
    val todayRevenue: StateFlow<Double> = _todayRevenue

    private val _pendingRequestsCount = MutableStateFlow(0)
    val pendingRequestsCount: StateFlow<Int> = _pendingRequestsCount

    init {
        // Initially populate with placeholders for a "real" feel
        _userState.value = AdminUIState.Success(listOf(
            AdminUserData("1", "Calculating...", "...", "user"),
            AdminUserData("2", "Synchronizing...", "...", "provider")
        ))
        
        collectRealData()
    }

    private fun collectRealData() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _userState.value = AdminUIState.Success(users)
            }
        }

        viewModelScope.launch {
            repository.getAllProviders().collect { providers ->
                _providerState.value = AdminUIState.Success(providers)
            }
        }

        viewModelScope.launch {
            repository.getAllBookings().collect { bookings ->
                _bookingState.value = AdminUIState.Success(bookings)
                
                // Analytics
                var total = 0.0
                var today = 0.0
                val startOfDay = getStartOfDay()
                
                bookings.filter { it.status == OrderStatus.COMPLETED }.forEach { booking ->
                    total += booking.finalPrice
                    if (booking.createdAt >= startOfDay) {
                        today += booking.finalPrice
                    }
                }
                _totalRevenue.value = total
                _todayRevenue.value = today
            }
        }

        viewModelScope.launch {
            repository.getAllServices().collect { services ->
                _pendingRequestsCount.value = services.count { !it.isApproved }
            }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
