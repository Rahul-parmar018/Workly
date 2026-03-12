package com.example.workly.auth

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
import coil.load
import coil.transform.CircleCropTransformation
import com.example.workly.R
import com.example.workly.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    progressBar.visibility = View.GONE
                    if (authTask.isSuccessful) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    } else {
                        Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: ApiException) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Google Sign-In Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progressBar)

        val ivUserAvatar: ImageView = findViewById(R.id.ivUserAvatar)
        val currentUser = auth.currentUser
        if (currentUser?.photoUrl != null) {
            ivUserAvatar.load(currentUser.photoUrl) {
                transformations(CircleCropTransformation())
            }
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
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnSignIn.isEnabled = false
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        btnSignIn.isEnabled = true
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finishAffinity()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
}
