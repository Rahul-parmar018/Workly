package com.example.workly.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.booking.BookingActivity
import com.example.workly.data.Service
import com.example.workly.theme.*

class ServicesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialCategory = intent.getStringExtra("CATEGORY") ?: "All"
        setContent {
            WorklyTheme {
                ServicesScreen(
                    initialCategory = initialCategory,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(initialCategory: String = "All", onBackClick: () -> Unit) {
    val allServices = remember { getAllServices() }
    val categories = listOf("All", "Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events")
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredServices = allServices.filter { service ->
        val matchesCategory = selectedCategory == "All" || service.category == selectedCategory
        val matchesSearch = searchQuery.isEmpty() ||
                service.name.contains(searchQuery, ignoreCase = true) ||
                service.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedCategory == "All") "All Services" else selectedCategory,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Search bar
            Surface(color = Color.White, shadowElevation = 2.dp) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    placeholder = { Text("Search services...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null, tint = TextSecondary) }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundGray,
                        unfocusedContainerColor = BackgroundGray,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = ProfessionalBlue
                    ),
                    singleLine = true
                )
            }

            // Category scroll
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { idx ->
                    val cat = categories[idx]
                    val isSelected = cat == selectedCategory
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(22.dp),
                        color = if (isSelected) ProfessionalBlue else Color.White,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(0.5f)) else null
                    ) {
                        Text(
                            cat,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Count row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredServices.size} services available",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                if (selectedCategory != "All") {
                    TextButton(onClick = { selectedCategory = "All"; searchQuery = "" }) {
                        Text("Clear", color = ProfessionalBlue, fontSize = 12.sp)
                    }
                }
            }

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredServices, key = { it.id }) { service ->
                    PremiumServiceCard(service)
                }
            }
        }
    }
}

@Composable
fun PremiumServiceCard(service: Service) {
    val context = LocalContext.current
    val imageUrl = getServiceCardImageUrl(service.name, service.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable {
                context.startActivity(Intent(context, BookingActivity::class.java).apply {
                    putExtra("SERVICE_NAME", service.name)
                    putExtra("SERVICE_PRICE", service.basePrice)
                    putExtra("SERVICE_CATEGORY", service.category)  // ← THE FIX
                    putExtra("SERVICE_ID", service.id)
                })
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Service image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = service.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Category badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = getCategoryColor(service.category).copy(alpha = 0.92f)
                ) {
                    Text(
                        service.category,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    service.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${service.basePrice.toInt()}+",
                        color = ProfessionalBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ProfessionalBlue
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp).size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Per-service Unsplash image URLs
fun getServiceCardImageUrl(name: String, category: String): String {
    return when {
        name.contains("Cleaning", true) || name.contains("Clean", true) ->
            "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=400&fit=crop"
        name.contains("Kitchen", true) ->
            "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&fit=crop"
        name.contains("Sofa", true) || name.contains("Carpet", true) ->
            "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&fit=crop"
        name.contains("AC", true) || name.contains("Air", true) ->
            "https://images.unsplash.com/photo-1558002038-1055907df827?w=400&fit=crop"
        name.contains("Refrigerator", true) || name.contains("Washing", true) ->
            "https://images.unsplash.com/photo-1584771145729-0bd779f33c40?w=400&fit=crop"
        name.contains("Plumb", true) || name.contains("Pipe", true) || name.contains("Tap", true) ->
            "https://images.unsplash.com/photo-1585704032915-c3400305e979?w=400&fit=crop"
        name.contains("Electric", true) || name.contains("Wiring", true) ->
            "https://images.unsplash.com/photo-1621905252507-b35492cc74b4?w=400&fit=crop"
        name.contains("CCTV", true) || name.contains("Smart", true) ->
            "https://images.unsplash.com/photo-1557318041-1ce374d55ebf?w=400&fit=crop"
        name.contains("Massage", true) ->
            "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=400&fit=crop"
        name.contains("Yoga", true) || name.contains("Trainer", true) ->
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&fit=crop"
        name.contains("Haircut", true) ->
            "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=400&fit=crop"
        name.contains("Laptop", true) || name.contains("Phone", true) ->
            "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&fit=crop"
        name.contains("WiFi", true) ->
            "https://images.unsplash.com/photo-1606904825846-647eb07f5be2?w=400&fit=crop"
        name.contains("Car", true) && name.contains("Wash", true) ->
            "https://images.unsplash.com/photo-1520340356584-f9917d1eea6f?w=400&fit=crop"
        name.contains("Bike", true) ->
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&fit=crop"
        name.contains("Photo", true) ->
            "https://images.unsplash.com/photo-1519741497674-611481863552?w=400&fit=crop"
        name.contains("Party", true) || name.contains("Decor", true) ->
            "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=400&fit=crop"
        name.contains("DJ", true) ->
            "https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?w=400&fit=crop"
        else -> when (category.lowercase()) {
            "cleaning" -> "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=400&fit=crop"
            "repair" -> "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=400&fit=crop"
            "plumbing" -> "https://images.unsplash.com/photo-1585704032915-c3400305e979?w=400&fit=crop"
            "electric" -> "https://images.unsplash.com/photo-1621905252507-b35492cc74b4?w=400&fit=crop"
            "wellness" -> "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=400&fit=crop"
            "tech" -> "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&fit=crop"
            "auto" -> "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=400&fit=crop"
            "events" -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&fit=crop"
            else -> "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&fit=crop"
        }
    }
}

fun getAllServices(): List<Service> = listOf(
    Service("1", "Full Home Cleaning", "Cleaning", 40.0),
    Service("2", "Deep Kitchen Clean", "Cleaning", 60.0),
    Service("3", "Bathroom Scrub", "Cleaning", 30.0),
    Service("4", "Sofa & Carpet Clean", "Cleaning", 80.0),
    Service("5", "Post-Construction Clean", "Cleaning", 150.0),
    Service("6", "Office Sanitization", "Cleaning", 200.0),
    Service("7", "AC Service & Repair", "Repair", 80.0),
    Service("8", "Refrigerator Repair", "Repair", 70.0),
    Service("9", "Washing Machine Fix", "Repair", 60.0),
    Service("10", "Door & Lock Repair", "Repair", 40.0),
    Service("11", "Furniture Assembly", "Repair", 50.0),
    Service("12", "Tap & Pipe Fixing", "Plumbing", 35.0),
    Service("13", "Drain Unclogging", "Plumbing", 50.0),
    Service("14", "Water Tank Cleaning", "Plumbing", 90.0),
    Service("15", "Bathroom Fitting", "Plumbing", 120.0),
    Service("16", "Fan & Light Fitting", "Electric", 45.0),
    Service("17", "Switch Board Repair", "Electric", 40.0),
    Service("18", "Inverter Installation", "Electric", 100.0),
    Service("19", "CCTV & Wiring Setup", "Electric", 130.0),
    Service("20", "Home Massage", "Wellness", 100.0),
    Service("21", "Yoga at Home", "Wellness", 60.0),
    Service("22", "Personal Trainer", "Wellness", 80.0),
    Service("23", "Haircut at Home", "Wellness", 40.0),
    Service("24", "Laptop Repair", "Tech", 50.0),
    Service("25", "Phone Screen Fix", "Tech", 40.0),
    Service("26", "WiFi Setup & Config", "Tech", 35.0),
    Service("27", "Smart Home Install", "Tech", 200.0),
    Service("28", "Car Wash & Detail", "Auto", 30.0),
    Service("29", "Bike Servicing", "Auto", 50.0),
    Service("30", "Car Battery Jump", "Auto", 25.0),
    Service("31", "Event Photography", "Events", 150.0),
    Service("32", "Party Decoration", "Events", 300.0),
    Service("33", "DJ & Sound Setup", "Events", 500.0),
)

fun getCategoryColor(category: String): Color = when (category) {
    "Cleaning" -> Color(0xFF1565C0)
    "Repair" -> Color(0xFFE65100)
    "Plumbing" -> Color(0xFF0277BD)
    "Electric" -> Color(0xFFF57F17)
    "Wellness" -> Color(0xFF6A1B9A)
    "Tech" -> Color(0xFF00695C)
    "Auto" -> Color(0xFFBF360C)
    "Events" -> Color(0xFFAD1457)
    else -> Color(0xFF1565C0)
}

fun getCategoryIcon(category: String): ImageVector = when (category) {
    "Cleaning" -> Icons.Default.CleaningServices
    "Repair" -> Icons.Default.Build
    "Plumbing" -> Icons.Default.WaterDrop
    "Electric" -> Icons.Default.ElectricBolt
    "Wellness" -> Icons.Default.Spa
    "Tech" -> Icons.Default.Computer
    "Auto" -> Icons.Default.DirectionsCar
    "Events" -> Icons.Default.Celebration
    else -> Icons.Default.Handyman
}
