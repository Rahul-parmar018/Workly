package com.example.workly.ui.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.workly.R
import com.example.workly.data.model.Provider
import com.google.android.material.button.MaterialButton

class ProviderAdapter(
    private val onProviderSelected: (Provider) -> Unit
) : ListAdapter<Pair<Provider, Double>, ProviderAdapter.ProviderViewHolder>(ProviderDiffCallback()) {

    inner class ProviderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivProviderAvatar)
        val tvName: TextView = view.findViewById(R.id.tvProviderName)
        val tvAiScore: TextView = view.findViewById(R.id.tvAiScore)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvRatingReviews: TextView = view.findViewById(R.id.tvRatingReviews)
        val tvHourlyRate: TextView = view.findViewById(R.id.tvHourlyRate)
        val tvJobsDone: TextView = view.findViewById(R.id.tvJobsDone)
        val btnSelect: MaterialButton = view.findViewById(R.id.btnSelectProvider)

        fun bind(item: Pair<Provider, Double>) {
            val (provider, score) = item
            tvName.text = provider.name
            tvAiScore.text = "AI ${score.toInt()}%"
            ratingBar.rating = provider.rating
            tvRatingReviews.text = "${provider.rating} (${provider.reviewsCount} reviews)"
            tvHourlyRate.text = "₹${provider.hourlyRate.toInt()}/hr"
            tvJobsDone.text = "${provider.reviewsCount}+ jobs done · Verified"

            val avatarUrl = "https://ui-avatars.com/api/?name=${provider.name.replace(" ", "+")}&background=1565C0&color=fff&size=200&bold=true&rounded=true"
            Glide.with(itemView.context)
                .load(avatarUrl)
                .circleCrop()
                .into(ivAvatar)

            btnSelect.setOnClickListener { onProviderSelected(provider) }
            itemView.setOnClickListener { onProviderSelected(provider) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider_card, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProviderDiffCallback : DiffUtil.ItemCallback<Pair<Provider, Double>>() {
        override fun areItemsTheSame(oldItem: Pair<Provider, Double>, newItem: Pair<Provider, Double>) = 
            oldItem.first.id == newItem.first.id
        override fun areContentsTheSame(oldItem: Pair<Provider, Double>, newItem: Pair<Provider, Double>) = 
            oldItem == newItem
    }
}
