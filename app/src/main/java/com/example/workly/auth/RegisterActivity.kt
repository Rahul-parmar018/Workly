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
import androidx.core.content.ContextCompat
import com.example.workly.R
import com.example.workly.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private var selectedRole = "user"

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
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)
        
        val etName: TextInputEditText = findViewById(R.id.etName)
        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnRegister: MaterialButton = findViewById(R.id.btnRegister)
        val btnGoogleSignIn: MaterialButton = findViewById(R.id.btnGoogleSignIn)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)
        val toggleRole: MaterialButtonToggleGroup = findViewById(R.id.toggleRole)

        toggleRole.addOnButtonCheckedListener { group: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                selectedRole = if (checkedId == R.id.btnRoleProvider) "provider" else "user"
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

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
                                completeRegistration(userId, name, email)
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
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnGoogleSignIn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }


    private fun completeRegistration(uid: String, name: String, email: String) {
        val userMap = hashMapOf(
            "id" to uid,
            "name" to name,
            "email" to email,
            "role" to selectedRole
        )

        // Always save to main users directory
        db.collection("users").document(uid).set(userMap)
            .addOnSuccessListener {
                if (selectedRole == "provider") {
                    // Create pending profile for providers
                    val providerMap = hashMapOf(
                        "id" to uid,
                        "isApproved" to false,
                        "status" to "pending"
                    )
                    db.collection("providers").document(uid).set(providerMap)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Professional Profile Pending Approval", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finishAffinity()
                        }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Welcome to Workly!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
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
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    // User already exists, redirect
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finishAffinity()
                                } else {
                                    completeRegistration(user.uid, user.displayName ?: "User", user.email ?: "")
                                }
                            }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

