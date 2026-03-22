package com.example.workly.ui.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private var categories: List<String>,
    private var selectedCategory: String,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardCategory)
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)

        fun bind(category: String) {
            tvName.text = category
            val isSelected = category == selectedCategory

            if (isSelected) {
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.nav_item_color)) // Or define a primary blue
                tvName.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                card.cardElevation = 8f
            } else {
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                tvName.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                card.cardElevation = 2f
            }

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    fun updateSelected(category: String) {
        selectedCategory = category
        notifyDataSetChanged()
    }
}
