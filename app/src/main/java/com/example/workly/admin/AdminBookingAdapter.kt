package com.example.workly.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.Booking
import com.example.workly.data.OrderStatus
import com.google.android.material.card.MaterialCardView

class AdminBookingAdapter(
    private var bookings: List<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookingId: TextView = view.findViewById(R.id.tvBookingId)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cardStatus: MaterialCardView = view.findViewById(R.id.cardStatus)
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
        
        holder.tvBookingId.text = "#${booking.id.takeLast(8).uppercase()}"
        holder.tvServiceName.text = booking.serviceName
        holder.tvUserName.text = booking.userName
        holder.tvDateTime.text = "${booking.date}, ${booking.time}"
        holder.tvAmount.text = "₹${"%,.0f".format(booking.finalPrice)}"
        
        // Professional Status Styling
        holder.tvStatus.text = booking.status.uppercase()
        
        val (bgColor, textColor) = when (booking.status) {
            OrderStatus.PENDING -> "#FEF3C7" to "#D97706"    // Amber
            OrderStatus.ACCEPTED -> "#DBEAFE" to "#2563EB"   // Blue
            OrderStatus.COMPLETED -> "#D1FAE5" to "#059669"  // Green
            OrderStatus.CANCELLED -> "#FEE2E2" to "#DC2626"  // Red
            else -> "#F1F5F9" to "#64748B"                   // Slate
        }
        
        holder.cardStatus.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(bgColor)))
        holder.tvStatus.setTextColor(Color.parseColor(textColor))
        
        holder.itemView.setOnClickListener { onItemClick(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateBookings(newBookings: List<Booking>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
}
