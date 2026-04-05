package com.workly.app.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.workly.app.R
import com.workly.app.data.Service

class AdminServiceAdapter(
    private var services: List<Service>,
    private val onApproveClick: (Service) -> Unit,
    private val onRejectClick: (Service) -> Unit
) : RecyclerView.Adapter<AdminServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivService: ImageView = view.findViewById(R.id.ivService)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.tvTitle.text = service.title
        holder.tvCategory.text = service.category.uppercase()
        holder.tvPrice.text = "₹${"%,.2f".format(service.price)}"
        
        holder.ivService.load(service.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.img_service_cleaner)
        }

        holder.btnApprove.setOnClickListener { onApproveClick(service) }
        holder.btnReject.setOnClickListener { onRejectClick(service) }
    }

    override fun getItemCount() = services.size

    fun updateServices(newServices: List<Service>) {
        this.services = newServices
        notifyDataSetChanged()
    }
}

