package com.example.workly.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.workly.theme.ThemeDataStore
import com.example.workly.theme.WorklyTheme

class ServiceDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val title = intent.getStringExtra("SERVICE_TITLE") ?: "Service"
        val desc = intent.getStringExtra("SERVICE_DESC") ?: "No description provided."
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val duration = intent.getStringExtra("SERVICE_DURATION") ?: "1 hr"
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Unknown"
        val providerId = intent.getStringExtra("PROVIDER_ID") ?: ""
        val category = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        val serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        val imgUrl = intent.getStringExtra("SERVICE_IMG")

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
                ServiceDetailScreen(
                    title = title,
                    description = desc,
                    price = price,
                    duration = duration,
                    providerName = providerName,
                    providerId = providerId,
                    category = category,
                    serviceId = serviceId,
                    imgUrl = imgUrl,
                    onBack = { finish() }
                )
            }
        }
    }
}
