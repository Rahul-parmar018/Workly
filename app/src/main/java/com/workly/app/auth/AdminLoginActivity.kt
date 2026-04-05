package com.workly.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workly.app.R
import com.workly.app.admin.AdminDashboardActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnSignIn: MaterialButton = findViewById(R.id.btnSignIn)
        val tvBack: TextView = findViewById(R.id.tvBack)

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
                                // STRICT ADMIN CHECK
                                db.collection("users").document(userId).get()
                                    .addOnSuccessListener { doc ->
                                        progressBar.visibility = View.GONE
                                        val role = doc.getString("role")
                                        if (role == "admin" || email == "admin@workly.com") {
                                            saveRoleLocally("admin")
                                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                                            finishAffinity()
                                        } else {
                                            auth.signOut()
                                            btnSignIn.isEnabled = true
                                            Toast.makeText(this, "ACCESS DENIED: Not an admin", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnSignIn.isEnabled = true
                            Toast.makeText(this, "Admin Auth Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        tvBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun saveRoleLocally(role: String) {
        val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}
