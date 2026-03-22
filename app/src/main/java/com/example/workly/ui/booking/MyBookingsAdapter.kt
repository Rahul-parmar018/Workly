package com.example.workly.ui.booking

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Booking
import com.google.android.material.card.MaterialCardView

class MyBookingsAdapter : ListAdapter<Booking, MyBookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    inner class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvBookingServiceName)
        val tvServiceCategory: TextView = view.findViewById(R.id.tvBookingServiceCategory)
        val tvStatus: TextView = view.findViewById(R.id.tvBookingStatus)
        val cardStatus: MaterialCardView = view.findViewById(R.id.cardStatus)
        val tvDateTime: TextView = view.findViewById(R.id.tvBookingDateTime)
        val tvPrice: TextView = view.findViewById(R.id.tvBookingPrice)
        val tvProName: TextView = view.findViewById(R.id.tvBookingProName)
        val layoutProInfo: View = view.findViewById(R.id.layoutProInfo)
        val tvAddress: TextView = view.findViewById(R.id.tvBookingAddress)

        fun bind(booking: Booking) {
            tvServiceName.text = booking.serviceName
            tvServiceCategory.text = booking.serviceCategory
            tvStatus.text = booking.status
            tvDateTime.text = "${booking.date} · ${booking.time}"
            tvPrice.text = "₹${booking.finalPrice.toInt()}"
            tvAddress.text = booking.address

            if (booking.providerName.isNotEmpty()) {
                layoutProInfo.visibility = View.VISIBLE
                tvProName.text = "Pro: ${booking.providerName}"
            } else {
                layoutProInfo.visibility = View.GONE
            }

            val statusColorRes = when (booking.status) {
                "Pending" -> android.R.color.holo_orange_dark
                "Confirmed" -> R.color.nav_item_color
                "InProgress" -> android.R.color.holo_blue_dark // Or a teal color
                "Completed" -> android.R.color.holo_green_dark
                "Cancelled" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            
            val color = ContextCompat.getColor(itemView.context, statusColorRes)
            tvStatus.setTextColor(color)
            cardStatus.setCardBackgroundColor(ColorStateList.valueOf(color).withAlpha(30))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking_history_card, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking) = oldItem == newItem
    }
}
