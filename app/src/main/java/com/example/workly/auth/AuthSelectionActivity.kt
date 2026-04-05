package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.workly.theme.ThemeDataStore
import com.example.workly.theme.WorklyTheme

class AuthSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
                AuthSelectionScreen(
                    onRoleSelect = { /* handled internally */ },
                    onContinue = { role ->
                        if (role == "user") {
                            val intent = Intent(this, RegisterActivity::class.java)
                            intent.putExtra("role", "user")
                            startActivity(intent)
                        } else if (role == "provider") {
                            val intent = Intent(this, ProviderRegisterActivity::class.java)
                            startActivity(intent)
                        }
                    },
                    onSignIn = {
                        startActivity(Intent(this, LoginActivity::class.java))
                    },
                    onAdminPortal = {
                        startActivity(Intent(this, AdminLoginActivity::class.java))
                    }
                )
            }
        }
    }
}
