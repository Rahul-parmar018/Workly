package com.example.workly.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.workly.R
import com.example.workly.ui.admin.AdminDashboardActivity
import com.example.workly.ui.home.HomeActivity
import com.example.workly.ui.provider.AddServiceActivity
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

        val ivUserAvatar: ImageView = findViewById(R.id.ivUserAvatar)
        val currentUser = auth.currentUser
        if (currentUser?.photoUrl != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .circleCrop()
                .into(ivUserAvatar)
        }

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

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
            val password = etPassword.text.toString().trim()

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
                            Toast.makeText(this, "Login failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
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
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val role = userDoc.getString("role") ?: "user"
                                    saveRoleLocally(role)
                                    checkRoleAndRedirect(user.uid, role)
                                } else {
                                    val userMap = hashMapOf(
                                        "id" to user.uid,
                                        "name" to (user.displayName ?: "Google User"),
                                        "email" to (user.email ?: ""),
                                        "role" to "user"
                                    )
                                    db.collection("users").document(user.uid).set(userMap)
                                        .addOnCompleteListener {
                                            saveRoleLocally("user")
                                            checkRoleAndRedirect(user.uid, "user")
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

    private fun fetchRoleAndRedirect(uid: String?) {
        if (uid == null) return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "user"
                    saveRoleLocally(role)
                    
                    checkRoleAndRedirect(uid, role)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Authentication Error", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkRoleAndRedirect(uid: String, role: String) {
        when (role) {
            "admin" -> {
                progressBar.visibility = View.GONE
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finishAffinity()
            }
            "provider" -> {
                checkProviderApproval(uid)
            }
            else -> { // user
                progressBar.visibility = View.GONE
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun checkProviderApproval(uid: String) {
        db.collection("providers").document(uid).get()
            .addOnSuccessListener { provDoc ->
                progressBar.visibility = View.GONE
                val approved = provDoc.getBoolean("isApproved") ?: false
                if (approved) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    auth.signOut()
                    Toast.makeText(this, "Waiting for Approval", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                auth.signOut()
                Toast.makeText(this, "Error checking approval", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRoleLocally(role: String) {
        val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}
