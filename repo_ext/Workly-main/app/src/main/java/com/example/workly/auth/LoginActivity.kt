package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
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

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
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
                            fetchRoleAndRedirect(auth.currentUser?.uid)
                        } else {
                            progressBar.visibility = View.GONE
                            btnSignIn.isEnabled = true
                            val error = task.exception?.message ?: "Login failed"
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
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
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    fetchRoleAndRedirect(auth.currentUser?.uid)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchRoleAndRedirect(uid: String?) {
        if (uid == null) {
            progressBar.visibility = View.GONE
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "user"
                    saveRoleLocally(role)
                    handleRoleRedirection(uid, role)
                } else {
                    // Try to recover by creating a default user entry if it was missing
                    val userMap = hashMapOf(
                        "id" to uid,
                        "email" to (auth.currentUser?.email ?: ""),
                        "role" to "user"
                    )
                    db.collection("users").document(uid).set(userMap).addOnSuccessListener {
                        saveRoleLocally("user")
                        handleRoleRedirection(uid, "user")
                    }
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleRoleRedirection(uid: String, role: String) {
        progressBar.visibility = View.GONE
        when (role) {
            "admin" -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finishAffinity()
            }
            "provider" -> {
                checkProviderApproval(uid)
            }
            else -> { // Standard user
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun checkProviderApproval(uid: String) {
        db.collection("providers").document(uid).get()
            .addOnSuccessListener { provDoc ->
                val approved = provDoc.getBoolean("isApproved") ?: false
                if (approved) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    auth.signOut()
                    Toast.makeText(this, "Your professional profile is pending Admin approval.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                auth.signOut()
                Toast.makeText(this, "Error verifying provider status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRoleLocally(role: String) {
        val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}

