package com.example.workly.ui.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R

data class CleaningService(
    val name: String,
    val description: String,
    val price: String,
    val rating: String,
    val iconRes: Int
)

class CleaningServiceAdapter(
    private val services: List<CleaningService>,
    private val onServiceClick: (String) -> Unit
) : RecyclerView.Adapter<CleaningServiceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivServiceIcon)
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvDesc: TextView = view.findViewById(R.id.tvServiceDesc)
        val tvPrice: TextView = view.findViewById(R.id.tvServicePrice)
        val tvRating: TextView = view.findViewById(R.id.tvServiceRating)

        fun bind(service: CleaningService) {
            tvName.text = service.name
            tvDesc.text = service.description
            tvPrice.text = service.price
            tvRating.text = service.rating
            ivIcon.setImageResource(service.iconRes)

            itemView.setOnClickListener { onServiceClick(service.name) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cleaning_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount() = services.size
}
