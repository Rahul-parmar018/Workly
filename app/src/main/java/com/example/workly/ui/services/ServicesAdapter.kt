package com.example.workly.ui.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.workly.R
import com.example.workly.data.model.Service

class ServicesAdapter(
    private val onServiceClick: (Service) -> Unit
) : ListAdapter<Service, ServicesAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    inner class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivServiceImage)
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvPrice: TextView = view.findViewById(R.id.tvServicePrice)
        val tvCategoryBadge: TextView = view.findViewById(R.id.tvCategoryBadge)

        fun bind(service: Service) {
            tvName.text = service.name
            tvPrice.text = "₹${service.basePrice.toInt()}+"
            tvCategoryBadge.text = service.category

            val imageUrl = getServiceCardImageUrl(service.name, service.category)
            Glide.with(itemView.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.img_service_cleaner) // Default placeholder
                .into(ivImage)

            itemView.setOnClickListener { onServiceClick(service) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_card, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Service, newItem: Service) = oldItem == newItem
    }

    // Helper from the original Compose code
    private fun getServiceCardImageUrl(name: String, category: String): String {
        return when {
            name.contains("Cleaning", true) || name.contains("Clean", true) ->
                "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=400&fit=crop"
            name.contains("Kitchen", true) ->
                "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&fit=crop"
            name.contains("Sofa", true) || name.contains("Carpet", true) ->
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&fit=crop"
            name.contains("AC", true) || name.contains("Air", true) ->
                "https://images.unsplash.com/photo-1558002038-1055907df827?w=400&fit=crop"
            name.contains("Refrigerator", true) || name.contains("Washing", true) ->
                "https://images.unsplash.com/photo-1584771145729-0bd779f33c40?w=400&fit=crop"
            else -> "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=400&fit=crop"
        }
    }
}
