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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.workly.data.Booking
import com.example.workly.payment.PaymentActivity
import java.util.UUID

class BookingActivity : ComponentActivity() {
    
    // Result Launchers for Phase 2
    private lateinit var providerPickerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var paymentLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    private var selectedProviderName: String? = null
    private var selectedProviderRate: Double = 0.0
    private var paymentStatus: String = "Pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val iconRes = intent.getIntExtra("SERVICE_ICON", 0)

        // Initialize Launchers
        providerPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedProviderName = result.data?.getStringExtra("SELECTED_PROVIDER_NAME")
                selectedProviderRate = result.data?.getDoubleExtra("SELECTED_PROVIDER_RATE", 0.0) ?: 0.0
                
                // Once provider is picked, go to Payment
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("SERVICE_NAME", serviceName)
                    putExtra("SERVICE_PRICE", if (selectedProviderRate > 0) selectedProviderRate else price)
                    putExtra("PROVIDER_NAME", selectedProviderName)
                }
                paymentLauncher.launch(intent)
            }
        }

        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                paymentStatus = result.data?.getStringExtra("PAYMENT_STATUS") ?: "Pending"
                // Payment done, now save to Firestore (Triggered via a state change or handled here)
                // For simplicity in this demo, we'll use a broadcast or a shared state if needed, 
                // but let's just trigger the final save logic.
                saveBookingToFirestore(serviceName, if (selectedProviderRate > 0) selectedProviderRate else price, iconRes)
            }
        }

        setContent {
            WorklyTheme {
                BookingScreen(
                    serviceName = serviceName,
                    price = price,
                    iconRes = iconRes,
                    onBackClick = { finish() },
                    onConfirmClick = {
                        // Start the Phase 2 Chain: Pick Provider -> Pay -> Save
                        val intent = Intent(this, ProviderListActivity::class.java).apply {
                            putExtra("SERVICE_NAME", serviceName)
                            // In real app, get actual lat/lon from fused location
                            putExtra("USER_LAT", 0.01) 
                            putExtra("USER_LON", 0.01)
                        }
                        providerPickerLauncher.launch(intent)
                    }
                )
            }
        }
    }

    private fun saveBookingToFirestore(serviceName: String, price: Double, iconRes: Int) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        
        val bookingId = UUID.randomUUID().toString()
        val booking = Booking(
            id = bookingId,
            userId = userId,
            serviceName = serviceName,
            date = "Mon, 24 Oct", 
            time = "10:00 AM",
            address = "Saved Address", // In real app, pass from screen
            status = if (paymentStatus == "Held") "PaymentHeld" else "Pending",
            price = price,
            serviceIcon = iconRes
        )
        
        firestore.collection("bookings")
            .document(bookingId)
            .set(booking)
            .addOnSuccessListener {
                startActivity(Intent(this, BookingSuccessActivity::class.java))
                finish()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    serviceName: String,
    price: Double,
    iconRes: Int,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var address by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf("Tap to get location") }
    var isSaving by remember { mutableStateOf(false) }
    
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
                    if (address.isNotEmpty()) {
                        onConfirmClick()
                    } else {
                        Toast.makeText(context, "Please enter or fetch address", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Find Professionals", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
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
