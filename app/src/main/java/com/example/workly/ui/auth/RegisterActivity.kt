package com.example.workly.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.workly.R
import com.example.workly.ui.home.HomeActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private var selectedRole = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)
        
        val etName: TextInputEditText = findViewById(R.id.etName)
        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnRegister: MaterialButton = findViewById(R.id.btnRegister)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        // Role is now fixed for this activity
        selectedRole = intent.getStringExtra("role") ?: "user"

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
<<<<<<< HEAD:app/src/main/java/com/example/workly/auth/RegisterActivity.kt
            val password = etPassword.text.toString()
=======
            val password = etPassword.text.toString().trim()
>>>>>>> Xml_files:app/src/main/java/com/example/workly/ui/auth/RegisterActivity.kt

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnRegister.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val userMap = hashMapOf(
                                    "id" to userId,
                                    "name" to name,
                                    "email" to email,
                                    "role" to selectedRole
                                )
                                
                                db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(this@RegisterActivity, "Logged in as $selectedRole", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                                        finishAffinity()
                                    }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }

            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
