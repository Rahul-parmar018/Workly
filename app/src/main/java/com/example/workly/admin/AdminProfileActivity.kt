package com.example.workly.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var profileListener: ListenerRegistration? = null

    private lateinit var tvAdminName: TextView
    private lateinit var tvAdminEmail: TextView
    private lateinit var ivAdminAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI
        tvAdminName = findViewById(R.id.tvAdminName)
        tvAdminEmail = findViewById(R.id.tvAdminEmail)
        ivAdminAvatar = findViewById(R.id.ivAdminAvatar)
        
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val btnSignOut: MaterialButton = findViewById(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            showSignOutConfirmation()
        }

        // Refined click logic
        findViewById<View>(R.id.btnAccountSettings).setOnClickListener {
            Toast.makeText(this, "Profile Configuration", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<View>(R.id.btnNotifications).setOnClickListener {
             Toast.makeText(this, "System Alert Settings", Toast.LENGTH_SHORT).show()
        }

        startProfileListener()
    }

    private fun startProfileListener() {
        val user = auth.currentUser
        if (user == null) {
            finish()
            return
        }

        // Real-time listener for premium UX
        profileListener = db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    tvAdminName.text = "System Admin"
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val name = snapshot.getString("name") ?: "Administrator"
                    val email = snapshot.getString("email") ?: user.email ?: "No Email"
                    
                    tvAdminName.text = name
                    tvAdminEmail.text = email
                    
                    // Direct Role Check
                    val role = snapshot.getString("role")
                    if (role != "admin") {
                        Toast.makeText(this, "Session Terminated: Admin role required", Toast.LENGTH_LONG).show()
                        logout()
                    }
                } else {
                     tvAdminName.text = "Administrator"
                }
            }
    }

    private fun showSignOutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out of your account?")
            .setNegativeButton("Stay Locked", null)
            .setPositiveButton("Sign Out") { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        profileListener?.remove()
        super.onDestroy()
    }
}
