package com.example.workly.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.theme.*
import com.example.workly.data.Service
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun ExploreScreenContent(
    innerPadding: PaddingValues,
    onServiceClick: (Service) -> Unit
) {
    var firestoreServices by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("services")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    firestoreServices = snapshot.documents.mapNotNull { doc ->
                        try { doc.toObject(Service::class.java) } catch (e: Exception) { null }
                    }
                }
                isLoading = false
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 180.dp) // HUD Safe Zone
    ) {
        // --- 1. INDUSTRIAL HEADER ---
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("MARKETPLACE", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Elite Gallery", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Silver Search HUD
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clickable { },
                    shape = RoundedCornerShape(16.dp),
                    color = PremiumBlackSurface,
                    border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.15f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = PremiumSilver, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Search for premium services...", color = Color.White.copy(alpha = 0.3f), fontSize = 15.sp)
                    }
                }
            }
        }

        // --- 2. ELITE CHIPS (Platinum Theme) ---
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filters = listOf("All", "Top Rated", "Trending", "Budget", "Luxury")
                items(filters.size) { idx ->
                    val isAll = idx == 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isAll) PremiumSilver else PremiumBlackSurface,
                        border = BorderStroke(1.dp, if (isAll) Color.Transparent else PremiumSilver.copy(alpha = 0.1f))
                    ) {
                        Text(
                            filters[idx],
                            color = if (isAll) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // --- 3. BRUSHED SILVER CATEGORIES ---
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                val categories = listOf(
                    Triple("Cleaning", Icons.Default.CleaningServices, "Home"),
                    Triple("Electric", Icons.Default.ElectricalServices, "Power"),
                    Triple("Plumbing", Icons.Default.Plumbing, "Water"),
                    Triple("Kitchen", Icons.Default.Countertops, "Chef")
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    categories.forEach { (label, icon, _) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                color = PremiumBlackSurface,
                                border = BorderStroke(1.5.dp, PremiumSilver.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // --- 4. DATA FEED / EMPTY STATE ---
        item {
            Spacer(modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("EXCLUSIVE OFFERS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("See All", color = PremiumSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumSilver)
                }
            } else if (firestoreServices.isEmpty()) {
                // PREMIUM EMPTY STATE
                Column(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = PremiumSilver.copy(alpha = 0.05f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FilterList, null, tint = PremiumSilver.copy(0.2f), modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Elite Services Nearby", color = Color.White.copy(0.4f), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("Expand your radius in settings", color = Color.White.copy(0.2f), fontSize = 11.sp)
                }
            }

        }

        items(firestoreServices.size) { idx ->
            RealMarketplaceCard(firestoreServices[idx], onServiceClick)
        }
    }
}

@Composable
fun RealMarketplaceCard(service: Service, onClick: (Service) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).clickable { onClick(service) },
        shape = RoundedCornerShape(24.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
    ) {
        Column {
            AsyncImage(
                model = service.imageUrl.ifEmpty { getPremiumImageForCategory(service.category) },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(service.category.uppercase(), color = PremiumSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Text("₹${service.price.toInt()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
