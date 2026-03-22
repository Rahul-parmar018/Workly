package com.example.workly.ui.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.google.android.material.card.MaterialCardView

class TimeSlotAdapter(
    private val slots: List<String>,
    private var selectedSlot: String?,
    private val onSlotClick: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    inner class SlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardTimeSlot)
        val tvSlot: TextView = view.findViewById(R.id.tvTimeSlot)

        fun bind(slot: String) {
            tvSlot.text = slot
            val isSelected = slot == selectedSlot

            if (isSelected) {
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.nav_item_color))
                tvSlot.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                card.strokeWidth = 0
            } else {
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                tvSlot.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                card.strokeWidth = 1
            }

            itemView.setOnClickListener {
                onSlotClick(slot)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(slots[position])
    }

    override fun getItemCount() = slots.size

    fun updateSelected(slot: String) {
        selectedSlot = slot
        notifyDataSetChanged()
    }
}
