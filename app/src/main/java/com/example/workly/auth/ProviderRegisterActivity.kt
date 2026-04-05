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

class ProviderRegisterActivity : ComponentActivity() {

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
                ProviderRegisterScreen(
                    onSignUp = { name, email, password ->
                        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: ""
                                        saveProviderToFirestore(userId, name, email)
                                    } else {
                                        isLoading = false
                                        Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSignIn = {
                        startActivity(Intent(this, ProviderLoginActivity::class.java))
                        finish()
                    },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun saveProviderToFirestore(userId: String, name: String, email: String) {
        val batch = db.batch()
        val userRef = db.collection("users").document(userId)
        val providerRef = db.collection("providers").document(userId)

        val identityMap = hashMapOf(
            "id" to userId,
            "name" to name,
            "email" to email,
            "role" to "provider"
        )

        val businessMap = hashMapOf(
            "id" to userId,
            "isApproved" to true,
            "status" to "approved",
            "earnings" to 0,
            "createdAt" to System.currentTimeMillis()
        )

        batch.set(userRef, identityMap)
        batch.set(providerRef, businessMap)

        batch.commit()
            .addOnSuccessListener {
                isLoading = false
                val sharedPrefs = getSharedPreferences("WorklyPrefs", MODE_PRIVATE)
                sharedPrefs.edit().putString("user_role", "provider").apply()
                
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
