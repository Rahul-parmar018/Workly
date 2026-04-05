package com.workly.app.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workly.app.R

data class AdminUserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user"
)

class AdminUserAdapter(private var users: List<AdminUserData>) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvUserRole: TextView = view.findViewById(R.id.tvUserRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserName.text = user.name
        holder.tvUserEmail.text = user.email
        holder.tvUserRole.text = user.role.replaceFirstChar { it.uppercase() }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<AdminUserData>) {
        this.users = newUsers
        notifyDataSetChanged()
    }
}
