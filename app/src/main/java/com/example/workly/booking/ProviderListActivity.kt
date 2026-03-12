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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.data.AIMatcher
import com.example.workly.data.Provider
import com.example.workly.theme.*
import com.google.firebase.firestore.FirebaseFirestore

class ProviderListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        val userLat = intent.getDoubleExtra("USER_LAT", 0.0)
        val userLon = intent.getDoubleExtra("USER_LON", 0.0)

        setContent {
            WorklyTheme {
                ProviderListScreen(
                    serviceName = serviceName,
                    serviceCategory = serviceCategory,
                    userLat = userLat,
                    userLon = userLon,
                    onBackClick = { finish() },
                    onProviderSelected = { provider ->
                        val result = Intent().apply {
                            putExtra("SELECTED_PROVIDER_NAME", provider.name)
                            putExtra("SELECTED_PROVIDER_ID", provider.id)
                            putExtra("SELECTED_PROVIDER_RATE", provider.hourlyRate)
                        }
                        setResult(RESULT_OK, result)
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
    serviceCategory: String,
    userLat: Double,
    userLon: Double,
    onBackClick: () -> Unit,
    onProviderSelected: (Provider) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var providers by remember { mutableStateOf<List<Pair<Provider, Double>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Seed fallback providers if Firestore is empty
    val fallbackProviders = listOf(
        Provider("pro_1", "Alex Johnson", 4.9, 45.0, listOf("Cleaning", "Repair"), 28.6, 77.2, 0, 127),
        Provider("pro_2", "Maria Garcia", 4.8, 38.0, listOf("Cleaning", "Wellness"), 28.61, 77.21, 0, 85),
        Provider("pro_3", "David Smith", 4.7, 55.0, listOf("Repair", "Electric"), 28.62, 77.19, 0, 200),
        Provider("pro_4", "Priya Sharma", 4.9, 42.0, listOf("Plumbing", "Cleaning"), 28.63, 77.22, 0, 156),
        Provider("pro_5", "Raj Kumar", 4.6, 35.0, listOf("Electric", "Tech"), 28.64, 77.20, 0, 98),
        Provider("pro_6", "Sunita Devi", 4.8, 50.0, listOf("Wellness", "Cleaning"), 28.65, 77.18, 0, 210),
        Provider("pro_7", "Mohammed Ali", 4.7, 40.0, listOf("Auto", "Repair"), 28.66, 77.23, 0, 143),
        Provider("pro_8", "Anita Patel", 4.9, 60.0, listOf("Events", "Wellness"), 28.67, 77.21, 0, 312),
    )

    LaunchedEffect(serviceCategory) {
        // Try Firestore first, fallback to mock if empty
        var query = firestore.collection("providers").limit(20)
        if (serviceCategory.isNotEmpty()) {
            query = firestore.collection("providers")
                .whereArrayContains("specialties", serviceCategory)
                .limit(20)
        }
        query.get()
            .addOnSuccessListener { snapshot ->
                val firestoreProviders = snapshot.toObjects(Provider::class.java)
                val source = if (firestoreProviders.isEmpty()) fallbackProviders else firestoreProviders
                providers = source
                    .map { it to AIMatcher.calculateScore(it, userLat, userLon) }
                    .sortedByDescending { it.second }
                isLoading = false
            }
            .addOnFailureListener {
                providers = fallbackProviders
                    .map { it to AIMatcher.calculateScore(it, userLat, userLon) }
                    .sortedByDescending { it.second }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Choose Professional", fontWeight = FontWeight.Bold)
                        Text("AI Matched for $serviceName", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ProfessionalBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Finding best professionals...", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ElectricTeal.copy(0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, null, tint = ElectricTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI ranked ${providers.size} professionals by rating, proximity & experience", fontSize = 12.sp, color = ElectricTeal)
                        }
                    }
                }
                items(providers) { (provider, score) ->
                    ProviderCard(provider = provider, score = score, onClick = { onProviderSelected(provider) })
                }
            }
        }
    }
}

@Composable
fun ProviderCard(provider: Provider, score: Double, onClick: () -> Unit) {
    val avatarUrl = "https://ui-avatars.com/api/?name=${provider.name.replace(" ", "+")}&background=1565C0&color=fff&size=200&bold=true&rounded=true"

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(22.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar from ui-avatars.com
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = provider.name,
                    modifier = Modifier.size(68.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(provider.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        // AI Score badge
                        Surface(shape = RoundedCornerShape(8.dp), color = ElectricTeal.copy(0.12f)) {
                            Text(
                                "AI ${score.toInt()}%",
                                color = ElectricTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { idx ->
                            Icon(
                                if (idx < provider.rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                null,
                                tint = EnergyOrange,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(" ${provider.rating} (${provider.reviewsCount} reviews)", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        provider.specialties.take(2).forEach { specialty ->
                            Surface(shape = RoundedCornerShape(6.dp), color = ProfessionalBlue.copy(0.08f)) {
                                Text(specialty, fontSize = 10.sp, color = ProfessionalBlue, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.4f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("₹${provider.hourlyRate.toInt()}/hr", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ProfessionalBlue)
                    Text("${provider.reviewsCount}+ jobs done · Verified", fontSize = 11.sp, color = TextSecondary)
                }
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue)
                ) {
                    Text("Select", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
