package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.home.HomeActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progressBar)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val etName: TextInputEditText = findViewById(R.id.etName)
        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnRegister: MaterialButton = findViewById(R.id.btnRegister)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnRegister.isEnabled = false
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }
                            user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                progressBar.visibility = View.GONE
                                btnRegister.isEnabled = true
                                startActivity(Intent(this, HomeActivity::class.java))
                                finishAffinity()
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnRegister.isEnabled = true
                            Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
