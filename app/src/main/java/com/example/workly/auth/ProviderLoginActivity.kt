package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.workly.home.HomeActivity
import com.example.workly.theme.ThemeDataStore
import com.example.workly.theme.WorklyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProviderLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
                ProviderLoginScreen(
                    onSignIn = { email, password ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            db.collection("users").document(userId).get()
                                                .addOnSuccessListener { userDoc ->
                                                    if (userDoc.exists()) {
                                                        val role = userDoc.getString("role") ?: "user"
                                                        saveRoleLocally(role)

                                                        if (role == "provider") {
                                                            checkProviderApproval(userId)
                                                        } else {
                                                            isLoading = false
                                                            auth.signOut()
                                                            Toast.makeText(this, "Not a provider account", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        isLoading = false
                                                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    },
                    onBack = { finish() },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun checkProviderApproval(uid: String) {
        db.collection("providers").document(uid).get()
            .addOnSuccessListener { provDoc ->
                isLoading = false
                val approved = provDoc.getBoolean("isApproved") ?: false
                if (approved) {
                    Toast.makeText(this, "Logged in as Provider", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    auth.signOut()
                    Toast.makeText(this, "Waiting for Approval", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                isLoading = false
                auth.signOut()
                Toast.makeText(this, "Error checking approval", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRoleLocally(role: String) {
        val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}
