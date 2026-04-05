package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.workly.admin.AdminDashboardActivity
import com.example.workly.theme.ThemeDataStore
import com.example.workly.theme.WorklyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoginActivity : ComponentActivity() {

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
                AdminLoginScreen(
                    onSignIn = { email, password ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            db.collection("users").document(userId).get()
                                                .addOnSuccessListener { doc ->
                                                    isLoading = false
                                                    val role = doc.getString("role")
                                                    if (role == "admin" || email == "admin@workly.com") {
                                                        saveRoleLocally("admin")
                                                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                                                        finishAffinity()
                                                    } else {
                                                        auth.signOut()
                                                        Toast.makeText(this, "ACCESS DENIED: Not an admin", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(this, "Admin Auth Failed", Toast.LENGTH_SHORT).show()
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

    private fun saveRoleLocally(role: String) {
        val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_role", role).apply()
    }
}
