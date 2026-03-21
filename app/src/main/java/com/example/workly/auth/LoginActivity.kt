package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.admin.AdminDashboardActivity
import com.example.workly.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Google Auth failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnSignIn: MaterialButton = findViewById(R.id.btnSignIn)
        val btnGoogleSignIn: MaterialButton = findViewById(R.id.btnGoogleSignIn)
        val tvSignUp: TextView = findViewById(R.id.tvSignUp)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnSignIn.isEnabled = false

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkRoleAndRedirect(auth.currentUser?.uid, email)
                        } else {
                            progressBar.visibility = View.GONE
                            btnSignIn.isEnabled = true
                            Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill fields", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogleSignIn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    checkRoleAndRedirect(user.uid, user.email)
                                } else {
                                    val userMap = hashMapOf(
                                        "id" to user.uid,
                                        "name" to (user.displayName ?: "Google User"),
                                        "email" to (user.email ?: ""),
                                        "role" to "user",
                                        "isApproved" to false
                                    )
                                    db.collection("users").document(user.uid).set(userMap)
                                        .addOnCompleteListener {
                                            checkRoleAndRedirect(user.uid, user.email)
                                        }
                                }
                            }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Auth Denied", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkRoleAndRedirect(uid: String?, email: String?) {
        if (uid == null) return

        // 4. ADMIN LOGIN (SPECIAL FLOW) hardcoded protection logic
        if (email == "admin@workly.com") {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Logged in as admin", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finishAffinity()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "user"
                    Toast.makeText(this, "Logged in as $role", Toast.LENGTH_SHORT).show()
                    when (role) {
                        "admin" -> startActivity(Intent(this, AdminDashboardActivity::class.java))
                        "provider" -> startActivity(Intent(this, HomeActivity::class.java))
                        else -> startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finishAffinity()
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed role extraction", Toast.LENGTH_LONG).show()
            }
    }
}
