package com.example.workly.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.R
import com.example.workly.booking.BookingActivity
import com.example.workly.data.Service
import com.example.workly.theme.*

class ServicesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                ServicesScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBackClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val services = remember { getMockServices() }
    val filteredServices = services.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Services", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Search 30+ services...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = ProfessionalBlue
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredServices) { service ->
                    ServiceGridCard(service)
                }
            }
        }
    }
}

@Composable
fun ServiceGridCard(service: Service) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable {
                val intent = Intent(context, BookingActivity::class.java).apply {
                    putExtra("SERVICE_NAME", service.name)
                    putExtra("SERVICE_PRICE", service.basePrice)
                    putExtra("SERVICE_ICON", service.iconRes)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = BackgroundGray
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getIconForCategory(service.category),
                        contentDescription = null,
                        tint = ProfessionalBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = service.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "$${service.basePrice.toInt()}/hr",
                color = ProfessionalBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Home" -> Icons.Default.Home
        "Wellness" -> Icons.Default.Spa
        "Tech" -> Icons.Default.Computer
        "Auto" -> Icons.Default.DirectionsCar
        "Events" -> Icons.Default.Event
        else -> Icons.Default.Build
    }
}

fun getMockServices(): List<Service> {
    return listOf(
        Service("1", "House Cleaning", "Home", 40.0, R.drawable.workly_logo),
        Service("2", "Electrician", "Home", 45.0, R.drawable.workly_logo),
        Service("3", "Plumber", "Home", 50.0, R.drawable.workly_logo),
        Service("4", "Pest Control", "Home", 60.0, R.drawable.workly_logo),
        Service("5", "AC Service", "Home", 35.0, R.drawable.workly_logo),
        Service("6", "Deep Cleaning", "Home", 80.0, R.drawable.workly_logo),
        Service("7", "Gardening", "Home", 30.0, R.drawable.workly_logo),
        Service("8", "Painting", "Home", 70.0, R.drawable.workly_logo),
        Service("9", "Massage", "Wellness", 100.0, R.drawable.workly_logo),
        Service("10", "Yoga Trainer", "Wellness", 50.0, R.drawable.workly_logo),
        Service("11", "Personal Trainer", "Wellness", 60.0, R.drawable.workly_logo),
        Service("12", "Hair Salon", "Wellness", 40.0, R.drawable.workly_logo),
        Service("13", "Manicure", "Wellness", 30.0, R.drawable.workly_logo),
        Service("14", "Physiotherapy", "Wellness", 90.0, R.drawable.workly_logo),
        Service("15", "Laptop Repair", "Tech", 50.0, R.drawable.workly_logo),
        Service("16", "Mobile Repair", "Tech", 40.0, R.drawable.workly_logo),
        Service("17", "WiFi Setup", "Tech", 30.0, R.drawable.workly_logo),
        Service("18", "Smart Home Setup", "Tech", 100.0, R.drawable.workly_logo),
        Service("19", "CCTV Install", "Tech", 120.0, R.drawable.workly_logo),
        Service("20", "Data Recovery", "Tech", 150.0, R.drawable.workly_logo),
        Service("21", "Car Wash", "Auto", 25.0, R.drawable.workly_logo),
        Service("22", "Car Service", "Auto", 150.0, R.drawable.workly_logo),
        Service("23", "Tyre Change", "Auto", 40.0, R.drawable.workly_logo),
        Service("24", "Interior Detail", "Auto", 100.0, R.drawable.workly_logo),
        Service("25", "Bike Service", "Auto", 50.0, R.drawable.workly_logo),
        Service("26", "Towing", "Auto", 120.0, R.drawable.workly_logo),
        Service("27", "Party Planner", "Events", 200.0, R.drawable.workly_logo),
        Service("28", "Catering", "Events", 500.0, R.drawable.workly_logo),
        Service("29", "Photography", "Events", 150.0, R.drawable.workly_logo),
        Service("30", "Decorator", "Events", 300.0, R.drawable.workly_logo),
        Service("31", "DJ / Music", "Events", 100.0, R.drawable.workly_logo)
    )
}
