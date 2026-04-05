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
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_selection)

        progressBar = findViewById(R.id.progressBar)
        val cardUser: MaterialCardView = findViewById(R.id.cardUser)
        val cardProvider: MaterialCardView = findViewById(R.id.cardProvider)
        val btnContinue: MaterialButton = findViewById(R.id.btnContinue)
        val tvAdminPortal: TextView = findViewById(R.id.tvAdminPortal)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        // Reset state
        updateUI()

        cardUser.setOnClickListener {
            selectedRole = "user"
            animateSelection(cardUser)
            updateUI()
        }

        cardProvider.setOnClickListener {
            selectedRole = "provider"
            animateSelection(cardProvider)
            updateUI()
        }

        btnContinue.setOnClickListener {
            if (selectedRole == "user") {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("role", "user")
                startActivity(intent)
            } else if (selectedRole == "provider") {
                val intent = Intent(this, ProviderRegisterActivity::class.java)
                startActivity(intent)
            }
        }

        tvAdminPortal.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
    }

    private fun updateUI() {
        val cardUser: MaterialCardView = findViewById(R.id.cardUser)
        val cardProvider: MaterialCardView = findViewById(R.id.cardProvider)
        val btnContinue: MaterialButton = findViewById(R.id.btnContinue)

        // Reset both
        cardUser.strokeColor = ContextCompat.getColor(this, android.R.color.transparent)
        cardUser.strokeWidth = 0
        cardUser.cardElevation = 0f
        cardUser.setCardBackgroundColor(android.graphics.Color.parseColor("#2A2A2A"))

        cardProvider.strokeColor = ContextCompat.getColor(this, android.R.color.transparent)
        cardProvider.strokeWidth = 0
        cardProvider.cardElevation = 0f
        cardProvider.setCardBackgroundColor(android.graphics.Color.parseColor("#2A2A2A"))

        // Highlight selected
        if (selectedRole == "user") {
            cardUser.strokeColor = android.graphics.Color.WHITE
            cardUser.strokeWidth = 6
            cardUser.setCardBackgroundColor(android.graphics.Color.parseColor("#3A3A3A"))
            btnContinue.visibility = View.VISIBLE
        } else if (selectedRole == "provider") {
            cardProvider.strokeColor = android.graphics.Color.WHITE
            cardProvider.strokeWidth = 6
            cardProvider.setCardBackgroundColor(android.graphics.Color.parseColor("#3A3A3A"))
            btnContinue.visibility = View.VISIBLE
        } else {
            btnContinue.visibility = View.GONE
        }
    }

    private fun animateSelection(view: View) {
        view.animate()
            .scaleX(0.97f)
            .scaleY(0.97f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}
