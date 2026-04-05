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

class RegisterActivity : ComponentActivity() {

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
                RegisterScreen(
                    onSignUp = { name, email, password ->
                        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: ""
                                        saveUserToFirestore(userId, name, email)
                                    } else {
                                        isLoading = false
                                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSignIn = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        val userMap = hashMapOf(
            "id" to uid,
            "name" to name,
            "email" to email,
            "role" to "user"
        )

        db.collection("users").document(uid).set(userMap)
            .addOnSuccessListener {
                isLoading = false
                val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
                sharedPrefs.edit().putString("user_role", "user").apply()
                
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
