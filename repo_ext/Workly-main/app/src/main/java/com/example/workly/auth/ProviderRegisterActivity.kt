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

class ProviderRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)
        
        val etName: TextInputEditText = findViewById(R.id.etName)
        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnRegister: MaterialButton = findViewById(R.id.btnRegister)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnRegister.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val batch = db.batch()
                                
                                val userRef = db.collection("users").document(userId)
                                val providerRef = db.collection("providers").document(userId)

                                val identityMap = hashMapOf(
                                    "id" to userId,
                                    "name" to name,
                                    "email" to email,
                                    "role" to "provider"
                                )

                                val businessMap = hashMapOf(
                                    "id" to userId,
                                    "isApproved" to true, // Auto-approved for development
                                    "status" to "approved",
                                    "earnings" to 0,
                                    "createdAt" to System.currentTimeMillis()
                                )

                                batch.set(userRef, identityMap)
                                batch.set(providerRef, businessMap)

                                batch.commit()
                                    .addOnSuccessListener {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(this, "Provider Account Created Successfully!", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this, HomeActivity::class.java))
                                        finishAffinity()
                                    }
                                    .addOnFailureListener { e ->
                                        progressBar.visibility = View.GONE
                                        btnRegister.isEnabled = true
                                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnRegister.isEnabled = true
                            Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, ProviderLoginActivity::class.java))
            finish()
        }
    }
}
