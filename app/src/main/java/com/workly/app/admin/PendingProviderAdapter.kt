package com.workly.app.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workly.app.R
import com.google.android.material.button.MaterialButton

data class PendingProviderData(
    val providerId: String = "",
    val name: String = "",
    val email: String = ""
)

class PendingProviderAdapter(
    private var providers: List<PendingProviderData>,
    private val onApproveClick: (PendingProviderData) -> Unit,
    private val onRejectClick: (PendingProviderData) -> Unit
) : RecyclerView.Adapter<PendingProviderAdapter.ProviderViewHolder>() {

    class ProviderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProviderName: TextView = view.findViewById(R.id.tvProviderName)
        val tvProviderEmail: TextView = view.findViewById(R.id.tvProviderEmail)
        val btnApprove: MaterialButton = view.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_provider, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.tvProviderName.text = provider.name
        holder.tvProviderEmail.text = provider.email

        holder.btnApprove.setOnClickListener {
            onApproveClick(provider)
        }
        
        holder.btnReject.setOnClickListener {
            onRejectClick(provider)
        }
    }

    override fun getItemCount() = providers.size

    fun updateProviders(newProviders: List<PendingProviderData>) {
        this.providers = newProviders
        notifyDataSetChanged()
    }
}
