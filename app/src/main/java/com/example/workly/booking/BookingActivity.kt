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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
            WorklyTheme {
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
                // Initial chat setup
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
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            isLoadingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        isLoadingLocation = false
                        userLat = loc.latitude; userLon = loc.longitude
                        val geocoder = android.location.Geocoder(context, Locale.getDefault())
                        val addrs = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        address = addrs?.firstOrNull()?.getAddressLine(0) ?: "${loc.latitude}, ${loc.longitude}"
                        onAddressChange(address)
                    }
                }.addOnFailureListener { isLoadingLocation = false }
            } catch (e: Exception) { isLoadingLocation = false }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book $serviceName", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        bottomBar = {
            val canProceed = address.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank()
            Surface(color = Color.White, shadowElevation = 12.dp) {
                Button(
                    onClick = { onFindPros(address, selectedDate, selectedTime, userLat, userLon) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp).navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (canProceed) ProfessionalBlue else Color.LightGray),
                    enabled = canProceed
                ) {
                    Text("Proceed to Payment", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Address
            Text("📍 Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; onAddressChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your full address") },
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    IconButton(onClick = { locationPermLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }) {
                        if (isLoadingLocation) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Icon(Icons.Default.MyLocation, null, tint = ProfessionalBlue)
                    }
                }
            )

            // Date picking
            Text("📅 Select Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 1.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = ProfessionalBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(selectedDate.ifEmpty { "Pick a date" })
                }
            }

            // Time Slots
            Text("⏰ Select Time", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(timeSlots.size) { idx ->
                    val slot = timeSlots[idx]
                    val isSelected = slot == selectedTime
                    Surface(
                        modifier = Modifier.clickable { selectedTime = slot; onDateTimeChange(selectedDate, slot) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ProfessionalBlue else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(slot, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = if (isSelected) Color.White else Color.Black)
                    }
                }
            }
        }
    }
}
