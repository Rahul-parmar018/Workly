package com.example.workly.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvAdminName: TextView
    private lateinit var tvAdminEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvAdminName = findViewById(R.id.tvAdminName)
        tvAdminEmail = findViewById(R.id.tvAdminEmail)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnSignOut: MaterialButton = findViewById(R.id.btnSignOut)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnSignOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Signed out securely", Toast.LENGTH_SHORT).show()
            
            // Clear activity stack and launch LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        loadAdminProfile()
    }

    private fun loadAdminProfile() {
        val user = auth.currentUser
        if (user != null) {
            // First display auth details immediately
            tvAdminEmail.text = user.email ?: "No Email Linked"
            
            // Then override with Firestore details if available
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        tvAdminName.text = doc.getString("name") ?: "Administrator"
                        val emailField = doc.getString("email")
                        if (!emailField.isNullOrEmpty()) {
                            tvAdminEmail.text = emailField
                        }
                    } else {
                        tvAdminName.text = "Administrator"
                    }
                }
                .addOnFailureListener {
                    tvAdminName.text = "Administrator"
                }
        } else {
            Toast.makeText(this, "No active session found", Toast.LENGTH_SHORT).show()
        }
    }
}
