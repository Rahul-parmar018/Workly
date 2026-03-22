package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.workly.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AuthSelectionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var selectedRole = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_selection)

        val cardUser: MaterialCardView = findViewById(R.id.cardUser)
        val cardProvider: MaterialCardView = findViewById(R.id.cardProvider)
        val tvAdminPortal: TextView = findViewById(R.id.tvAdminPortal)
        val btnGoogleSignIn: MaterialButton = findViewById(R.id.btnGoogleSignIn)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        cardUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("role", "user")
            startActivity(intent)
        }

        cardProvider.setOnClickListener {
            // Specialized Provider Register
            val intent = Intent(this, ProviderRegisterActivity::class.java)
            startActivity(intent)
        }

        tvAdminPortal.setOnClickListener {
            // Specialized Admin Login
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }

        btnGoogleSignIn.setOnClickListener {
            // Standard User Google Sign In logic can stay in LoginActivity or be handled here
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("trigger_google", true)
            startActivity(intent)
        }

        tvSignIn.setOnClickListener {
            // Default back to standard login
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
