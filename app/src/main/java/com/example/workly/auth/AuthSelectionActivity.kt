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

        progressBar = findViewById(R.id.progressBar)

        val cardUser: MaterialCardView = findViewById(R.id.cardUser)
        val cardProvider: MaterialCardView = findViewById(R.id.cardProvider)
        val tvUserText: TextView = findViewById(R.id.tvUserText)
        val tvProviderText: TextView = findViewById(R.id.tvProviderText)

        val btnGoogleSignIn: MaterialButton = findViewById(R.id.btnGoogleSignIn)
        val btnCreateAccount: MaterialButton = findViewById(R.id.btnCreateAccount)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        fun highlight(selected: MaterialCardView, other: MaterialCardView, selectedText: TextView, otherText: TextView) {
            // Highlighting properties mapped precisely to UI balance
            selected.strokeWidth = 4
            selected.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            selectedText.setTextColor(android.graphics.Color.parseColor("#1A237E")) // Dark text bounds logic
            
            // Revert state for other card
            other.strokeWidth = 2
            other.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            otherText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }

        cardUser.setOnClickListener {
            selectedRole = "user"
            highlight(cardUser, cardProvider, tvUserText, tvProviderText)
        }

        cardProvider.setOnClickListener {
            selectedRole = "provider"
            highlight(cardProvider, cardUser, tvProviderText, tvUserText)
        }
        
        // Ensure default highlight is mapped correctly on initialization
        highlight(cardUser, cardProvider, tvUserText, tvProviderText)

        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Auth disconnected. Test with JWT backend Auth logic for correct DB handling.", Toast.LENGTH_SHORT).show()
        }

        btnCreateAccount.setOnClickListener {
            val regIntent = Intent(this, RegisterActivity::class.java)
            regIntent.putExtra("role", selectedRole)
            startActivity(regIntent)
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
