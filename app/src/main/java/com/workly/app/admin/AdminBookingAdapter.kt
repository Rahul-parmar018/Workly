package com.workly.app.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workly.app.R
import com.workly.app.data.Booking
import com.workly.app.data.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

class AdminBookingAdapter(
    private var bookings: List<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookingId: TextView = view.findViewById(R.id.tvBookingId)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        
        holder.tvBookingId.text = "ID: #${booking.id.takeLast(6).uppercase()}"
        holder.tvServiceName.text = booking.serviceName
        holder.tvUserName.text = "Customer: ${booking.userName}"
        holder.tvDateTime.text = "${booking.date}, ${booking.time}"
        holder.tvAmount.text = "₹${"%.2f".format(booking.finalPrice)}"
        
        // Status Styling
        holder.tvStatus.text = booking.status.uppercase()
        val bgRes = when (booking.status) {
            OrderStatus.PENDING -> R.drawable.bg_status_pending
            OrderStatus.ACCEPTED -> R.drawable.bg_status_accepted
            OrderStatus.COMPLETED -> R.drawable.bg_status_completed
            OrderStatus.CANCELLED -> R.drawable.bg_status_cancelled
            else -> R.drawable.bg_status_pending
        }
        holder.tvStatus.setBackgroundResource(bgRes)
        
        holder.itemView.setOnClickListener { onItemClick(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateBookings(newBookings: List<Booking>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
}
