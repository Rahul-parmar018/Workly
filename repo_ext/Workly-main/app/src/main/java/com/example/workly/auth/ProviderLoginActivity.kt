package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.home.HomeActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProviderLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnSignIn: MaterialButton = findViewById(R.id.btnSignIn)
        val tvSignUp: TextView = findViewById(R.id.tvSignUp)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnSignIn.isEnabled = false

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                db.collection("users").document(userId).get()
                                    .addOnSuccessListener { userDoc ->
                                        if (userDoc.exists()) {
                                            val role = userDoc.getString("role") ?: "user"
                                            saveRoleLocally(role)

                                            if (role == "provider") {
                                                checkProviderApproval(userId)
                                            } else {
                                                progressBar.visibility = View.GONE
                                                auth.signOut()
                                                btnSignIn.isEnabled = true
                                                Toast.makeText(this, "Not a provider account", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            progressBar.visibility = View.GONE
                                            btnSignIn.isEnabled = true
                                            Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnSignIn.isEnabled = true
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, ProviderRegisterActivity::class.java))
            finish()
        }
    }

    private fun checkProviderApproval(uid: String) {
        db.collection("providers").document(uid).get()
            .addOnSuccessListener { provDoc ->
                progressBar.visibility = View.GONE
                val approved = provDoc.getBoolean("isApproved") ?: false
                if (approved) {
                    Toast.makeText(this, "Logged in as Provider", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    auth.signOut()
                    findViewById<MaterialButton>(R.id.btnSignIn).isEnabled = true
                    Toast.makeText(this, "Waiting for Approval", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                auth.signOut()
                findViewById<MaterialButton>(R.id.btnSignIn).isEnabled = true
                Toast.makeText(this, "Error checking approval", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRoleLocally(role: String) {
        val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}
