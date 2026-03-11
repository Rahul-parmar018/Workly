package com.example.workly.booking

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.google.android.gms.location.LocationServices

class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        
        setContent {
            WorklyTheme {
                BookingScreen(
                    serviceName = serviceName,
                    onBackClick = { finish() },
                    onConfirmClick = {
                        startActivity(Intent(this, BookingSuccessActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    serviceName: String,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf("Tap to get location") }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            
            Toast.makeText(context, "Fetching location...", Toast.LENGTH_SHORT).show()
            locationStatus = "Fetching..."
            
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        try {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            // Deprecated in API 33 but simple for now. Ideally use the listener version.
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                address = addresses[0].getAddressLine(0) ?: "Address found"
                                locationStatus = "Location Found"
                            } else {
                                address = "${location.latitude}, ${location.longitude}"
                                locationStatus = "Address not found, using coordinates"
                            }
                        } catch (e: Exception) {
                           address = "${location.latitude}, ${location.longitude}"
                           locationStatus = "Geocoder failed: ${e.message}"
                        }
                    } else {
                        locationStatus = "Location is null. Try opening Maps first."
                        Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                locationStatus = "Permission Error"
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            locationStatus = "Permission Denied"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book $serviceName", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        bottomBar = {
            Button(
                onClick = { 
                    if (address.isNotEmpty()) onConfirmClick() 
                    else Toast.makeText(context, "Please enter or fetch address", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue)
            ) {
                Text("Confirm Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Location Section
            Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Get Location", tint = ProfessionalBlue)
                    }
                }
            )
            Text(
                text = locationStatus,
                style = MaterialTheme.typography.bodySmall,
                color = if (locationStatus == "Location Found") ElectricTeal else TextSecondary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Time Section (Static for Minimal Code)
            Text("Date & Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Mon, 24 Oct", fontWeight = FontWeight.Bold)
                        Text("10:00 AM", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}
