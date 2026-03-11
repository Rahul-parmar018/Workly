package com.example.workly.booking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.R
import com.example.workly.data.AIMatcher
import com.example.workly.data.Provider
import com.example.workly.theme.*

class ProviderListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val userLat = intent.getDoubleExtra("USER_LAT", 0.0)
        val userLon = intent.getDoubleExtra("USER_LON", 0.0)

        setContent {
            WorklyTheme {
                ProviderListScreen(
                    serviceName = serviceName,
                    userLat = userLat,
                    userLon = userLon,
                    onBackClick = { finish() },
                    onProviderSelected = { provider ->
                        // Pass selected provider back to Booking or proceed to Payment
                        val resultIntent = Intent()
                        resultIntent.putExtra("SELECTED_PROVIDER_NAME", provider.name)
                        resultIntent.putExtra("SELECTED_PROVIDER_RATE", provider.hourlyRate)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(
    serviceName: String,
    userLat: Double,
    userLon: Double,
    onBackClick: () -> Unit,
    onProviderSelected: (Provider) -> Unit
) {
    // Mock Providers
    val rawProviders = listOf(
        Provider("1", "Alex Johnson", 4.9, 45.0, listOf("Cleaning"), 0.05, 0.05, R.drawable.img_service_cleaner, 120),
        Provider("2", "Maria Garcia", 4.7, 35.0, listOf("Cleaning"), 0.1, 0.1, R.drawable.img_service_electrician, 85),
        Provider("3", "David Smith", 4.8, 55.0, listOf("Cleaning"), 0.02, 0.02, R.drawable.img_service_plumber, 200)
    )

    // AI Matching Logic
    val rankedProviders = remember(userLat, userLon) {
        rawProviders.map { provider ->
            provider to AIMatcher.calculateScore(provider, userLat, userLon)
        }.sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Select Professional", fontWeight = FontWeight.Bold)
                        Text("AI Matched for $serviceName", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rankedProviders) { (provider, score) ->
                ProviderCard(provider, score, onClick = { onProviderSelected(provider) })
            }
        }
    }
}

@Composable
fun ProviderCard(provider: Provider, score: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(BackgroundGray)
            ) {
                // Simplified Image for now
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).align(Alignment.Center),
                    tint = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // AI Score Badge
                    Surface(
                        color = ElectricTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AI: ${score.toInt()}%",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = EnergyOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = " ${provider.rating} (${provider.reviewsCount} reviews)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$${provider.hourlyRate}/hr",
                    style = MaterialTheme.typography.titleMedium,
                    color = ProfessionalBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
