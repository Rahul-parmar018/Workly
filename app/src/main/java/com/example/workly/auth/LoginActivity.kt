package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.workly.R
import com.example.workly.admin.AdminDashboardActivity
import com.example.workly.home.HomeActivity
import com.example.workly.theme.ThemeDataStore
import com.example.workly.theme.WorklyTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isLoading by mutableStateOf(false)

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isLoading = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Commented out to fix build error with new google-services.json
        /*
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        */

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
                LoginScreen(
                    onSignIn = { email, password ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        fetchRoleAndRedirect(auth.currentUser?.uid)
                                    } else {
                                        isLoading = false
                                        val error = task.exception?.message ?: "Login failed"
                                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onGoogleSignIn = {
                        /*
                        isLoading = true
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                        */
                        Toast.makeText(this, "Google Sign-In is temporarily disabled", Toast.LENGTH_SHORT).show()
                    },
                    onSignUp = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    fetchRoleAndRedirect(auth.currentUser?.uid)
                } else {
                    isLoading = false
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchRoleAndRedirect(uid: String?) {
        if (uid == null) {
            isLoading = false
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "user"
                    saveRoleLocally(role)
                    handleRoleRedirection(uid, role)
                } else {
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
                isLoading = false
                Toast.makeText(this, "Error fetching profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleRoleRedirection(uid: String, role: String) {
        isLoading = false
        when (role) {
            "admin" -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finishAffinity()
            }
            "provider" -> {
                checkProviderApproval(uid)
            }
            else -> {
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
