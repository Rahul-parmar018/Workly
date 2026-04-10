package com.example.workly.admin

/**
 * Generic UI State for the Admin Dashboard.
 * T is the data type for the success state.
 */
sealed class AdminUIState<out T : Any> {
    object Loading : AdminUIState<Nothing>()
    data class Success<out T : Any>(val data: T) : AdminUIState<T>()
    data class Error(val message: String) : AdminUIState<Nothing>()
}
