package com.example.workly.booking

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.data.Booking
import com.example.workly.data.OrderStatus
import com.example.workly.payment.PaymentActivity
import com.example.workly.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class BookingActivity : ComponentActivity() {

    private lateinit var paymentLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    private var selectedProviderName: String? = null
    private var selectedProviderId: String? = null
    private var savedAddress: String = ""
    private var savedDate: String = ""
    private var savedTime: String = ""
    private var userLat: Double = 0.0
    private var userLon: Double = 0.0
    private var paymentMethod: String = "Cash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_TITLE") ?: intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        val basePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        selectedProviderName = intent.getStringExtra("PROVIDER_NAME") ?: ""
        selectedProviderId = intent.getStringExtra("PROVIDER_ID") ?: ""

        val auth = FirebaseAuth.getInstance()
        val userName = auth.currentUser?.displayName ?: "User"

        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                paymentMethod = result.data?.getStringExtra("PAYMENT_METHOD") ?: "Cash"
                saveBookingToFirestore(serviceId, serviceName, serviceCategory, basePrice, userName, selectedProviderId!!, selectedProviderName!!)
            }
        }

        setContent {
            val themeDataStore = remember { ThemeDataStore(this) }
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())

            WorklyTheme(themeMode = themeMode) {
                BookingScreen(
                    serviceName = serviceName,
                    serviceCategory = serviceCategory,
                    basePrice = basePrice,
                    onBackClick = { finish() },
                    onAddressChange = { savedAddress = it },
                    onDateTimeChange = { date, time -> savedDate = date; savedTime = time },
                    onFindPros = { address, date, time, lat, lon ->
                        savedAddress = address; savedDate = date; savedTime = time
                        userLat = lat; userLon = lon 
                        val payIntent = Intent(this, PaymentActivity::class.java).apply {
                            putExtra("SERVICE_NAME", serviceName)
                            putExtra("SERVICE_PRICE", basePrice)
                            putExtra("PROVIDER_NAME", selectedProviderName)
                            putExtra("PROVIDER_ID", selectedProviderId)
                            putExtra("BASE_PRICE", basePrice)
                        }
                        paymentLauncher.launch(payIntent)
                    }
                )
            }
        }
    }

    private fun saveBookingToFirestore(serviceId: String, serviceTitle: String, serviceCategory: String, price: Double, userName: String, providerId: String, providerName: String) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val fetchedName = userDoc.getString("name") ?: userName
            val userPhone = userDoc.getString("phone") ?: ""
            
            val docRef = firestore.collection("orders").document()
            val booking = Booking(
                id = docRef.id,
                userId = userId,
                userName = fetchedName,
                userPhone = userPhone,
                serviceName = serviceTitle,
                serviceCategory = serviceCategory,
                serviceId = serviceId,
                date = savedDate,
                time = savedTime,
                address = savedAddress,
                latitude = userLat,
                longitude = userLon,
                basePrice = price,
                finalPrice = price,
                providerId = providerId,
                providerName = providerName,
                status = OrderStatus.PENDING,
                paymentStatus = "Paid",
                paymentMethod = paymentMethod,
                createdAt = System.currentTimeMillis()
            )
            
            docRef.set(booking).addOnSuccessListener {
                val chatId = if (userId < providerId) "${userId}_$providerId" else "${providerId}_$userId"
                val initialMessage = "I have booked your service: $serviceTitle."
                firestore.collection("chats").document(chatId).set(mapOf(
                    "members" to listOf(userId, providerId),
                    "lastMessage" to initialMessage,
                    "lastTimestamp" to Timestamp.now()
                ))
                
                startActivity(Intent(this, BookingSuccessActivity::class.java).apply {
                    putExtra("SERVICE_NAME", serviceTitle)
                    putExtra("PROVIDER_NAME", providerName)
                    putExtra("BOOKING_ID", docRef.id)
                    putExtra("DATE", savedDate)
                    putExtra("TIME", savedTime)
                    putExtra("ADDRESS", savedAddress)
                    putExtra("PRICE", price)
                    putExtra("PROVIDER_ID", providerId)
                })
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Booking failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    serviceName: String,
    serviceCategory: String,
    basePrice: Double,
    onBackClick: () -> Unit,
    onAddressChange: (String) -> Unit,
    onDateTimeChange: (String, String) -> Unit,
    onFindPros: (address: String, date: String, time: String, lat: Double, lon: Double) -> Unit
) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var userLat by remember { mutableDoubleStateOf(23.0225) }
    var userLon by remember { mutableDoubleStateOf(72.5714) }

    val timeSlots = listOf("9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM")
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Theme aliases
    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val onBg = MaterialTheme.colorScheme.onBackground
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(context, { _, year, month, day ->
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            selectedDate = "$day ${months[month]} $year"
            onDateTimeChange(selectedDate, selectedTime)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    val locationPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            isLoadingLocation = true
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(10f)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation
                    if (loc != null) {
                        isLoadingLocation = false
                        userLat = loc.latitude
                        userLon = loc.longitude
                        val geocoder = android.location.Geocoder(context, Locale.getDefault())
                        try {
                            val addrs = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            address = addrs?.firstOrNull()?.getAddressLine(0) ?: "${loc.latitude}, ${loc.longitude}"
                            onAddressChange(address)
                        } catch (e: Exception) {
                            address = "${loc.latitude}, ${loc.longitude}"
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } catch (e: SecurityException) {
                isLoadingLocation = false
            }
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Book $serviceName", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = onSurf,
                    navigationIconContentColor = onSurf
                )
            )
        },
        bottomBar = {
            val canProceed = address.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank()
            Surface(
                color = surface, 
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    // Small subtle summary above the button
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Service Summary", color = onSurf.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(serviceName, color = onSurf, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("₹${basePrice.toInt()}", color = primary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { onFindPros(address, selectedDate, selectedTime, userLat, userLon) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canProceed) primary else (if (isSystemInDarkTheme()) onSurf.copy(alpha = 0.08f) else onSurf.copy(alpha = 0.05f)),
                            contentColor = if (canProceed) Color.White else onSurf.copy(alpha = 0.4f)
                        ),
                        enabled = canProceed,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (canProceed) 4.dp else 0.dp)
                    ) {
                        Text("Proceed to Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Address Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("📍 Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onBg)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; onAddressChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your full address", color = onSurf.copy(alpha = 0.4f)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = onSurf.copy(alpha = 0.1f),
                        focusedTextColor = onSurf,
                        unfocusedTextColor = onSurf
                    ),
                    trailingIcon = {
                        IconButton(onClick = { locationPermLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }) {
                            if (isLoadingLocation) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = primary)
                            else Icon(Icons.Default.MyLocation, null, tint = primary)
                        }
                    }
                )
            }

            // Date Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("📅 Select Date", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onBg)
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(14.dp), 
                    color = surfVar,
                    tonalElevation = 1.dp
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(selectedDate.ifEmpty { "Pick a date" }, color = onSurf)
                    }
                }
            }

            // Time Slots Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("⏰ Select Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onBg)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(timeSlots.size) { idx ->
                        val slot = timeSlots[idx]
                        val isSelected = slot == selectedTime
                        Surface(
                            modifier = Modifier.clickable { selectedTime = slot; onDateTimeChange(selectedDate, slot) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) primary else surfVar,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, onSurf.copy(alpha = 0.05f))
                        ) {
                            Text(
                                slot, 
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), 
                                color = if (isSelected) Color.White else onSurf,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- Fill Blank Space ---

            // Special Instructions
            var instructions by remember { mutableStateOf("") }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("📝 Special Instructions (Optional)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onBg)
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("Example: Gate code is 1234, or park in the driveway...", color = onSurf.copy(alpha = 0.4f), fontSize = 14.sp) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = onSurf.copy(alpha = 0.1f),
                        focusedTextColor = onSurf,
                        unfocusedTextColor = onSurf
                    )
                )
            }

            // Trust & Safety signals
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = primary.copy(alpha = 0.03f),
                border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, tint = primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Workly Safety Guarantee", fontWeight = FontWeight.Bold, color = onSurf, fontSize = 14.sp)
                    }
                    
                    val signals = listOf(
                        Icons.Default.Verified to "Background-verified professionals",
                        Icons.Default.AttachMoney to "Zero hidden costs • Fixed pricing",
                        Icons.Default.Timer to "On-time arrival • 24/7 Support"
                    )
                    
                    signals.forEach { (icon, text) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = onSurf.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(text, color = onSurf.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }
            }
            
            // Extra spacer to ensure scrolling behavior feels right
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
