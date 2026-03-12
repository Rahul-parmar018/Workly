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
import androidx.compose.foundation.border
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
import com.example.workly.payment.PaymentActivity
import com.example.workly.theme.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class BookingActivity : ComponentActivity() {

    private lateinit var providerPickerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var paymentLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    private var selectedProviderName: String? = null
    private var selectedProviderId: String? = null
    private var selectedProviderRate: Double = 0.0
    private var savedAddress: String = ""
    private var savedDate: String = ""
    private var savedTime: String = ""
    private var paymentMethod: String = "Cash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        val basePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        providerPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedProviderName = result.data?.getStringExtra("SELECTED_PROVIDER_NAME")
                selectedProviderId = result.data?.getStringExtra("SELECTED_PROVIDER_ID")
                selectedProviderRate = result.data?.getDoubleExtra("SELECTED_PROVIDER_RATE", basePrice) ?: basePrice
                val payIntent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("SERVICE_NAME", serviceName)
                    putExtra("SERVICE_PRICE", selectedProviderRate)
                    putExtra("PROVIDER_NAME", selectedProviderName)
                    putExtra("BASE_PRICE", basePrice)
                }
                paymentLauncher.launch(payIntent)
            }
        }

        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                paymentMethod = result.data?.getStringExtra("PAYMENT_METHOD") ?: "Cash"
                saveBookingToFirestore(serviceName, serviceCategory, selectedProviderRate.takeIf { it > 0 } ?: basePrice)
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
                        val intent = Intent(this, ProviderListActivity::class.java).apply {
                            putExtra("SERVICE_NAME", serviceName)
                            putExtra("SERVICE_CATEGORY", serviceCategory)
                            putExtra("USER_LAT", lat)
                            putExtra("USER_LON", lon)
                        }
                        providerPickerLauncher.launch(intent)
                    }
                )
            }
        }
    }

    private fun saveBookingToFirestore(serviceName: String, serviceCategory: String, price: Double) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("bookings").document()
        val booking = Booking(
            id = docRef.id,
            userId = userId,
            serviceName = serviceName,
            serviceCategory = serviceCategory,
            date = savedDate,
            time = savedTime,
            address = savedAddress,
            basePrice = intent.getDoubleExtra("SERVICE_PRICE", price),
            finalPrice = price,
            providerId = selectedProviderId ?: "",
            providerName = selectedProviderName ?: "",
            status = "Pending",
            paymentStatus = "Paid",
            paymentMethod = paymentMethod,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        docRef.set(booking).addOnSuccessListener {
            startActivity(Intent(this, BookingSuccessActivity::class.java).apply {
                putExtra("SERVICE_NAME", serviceName)
                putExtra("PROVIDER_NAME", selectedProviderName ?: "")
                putExtra("BOOKING_ID", docRef.id)
                putExtra("DATE", savedDate)
                putExtra("TIME", savedTime)
                putExtra("ADDRESS", savedAddress)
                putExtra("PRICE", price)
            })
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Booking failed. Please try again.", Toast.LENGTH_SHORT).show()
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
    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLon by remember { mutableDoubleStateOf(0.0) }

    val timeSlots = listOf("9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM")
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Date picker
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
                    isLoadingLocation = false
                    if (loc != null) {
                        userLat = loc.latitude; userLon = loc.longitude
                        try {
                            @Suppress("DEPRECATION")
                            val addrs = android.location.Geocoder(context, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)
                            address = addrs?.firstOrNull()?.getAddressLine(0) ?: "${loc.latitude}, ${loc.longitude}"
                        } catch (e: Exception) { address = "${loc.latitude}, ${loc.longitude}" }
                        onAddressChange(address)
                    }
                }.addOnFailureListener { isLoadingLocation = false }
            } catch (e: SecurityException) { isLoadingLocation = false }
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
        containerColor = BackgroundGray,
        bottomBar = {
            val canProceed = address.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank()
            Surface(color = Color.White, shadowElevation = 12.dp) {
                Button(
                    onClick = { if (canProceed) onFindPros(address, selectedDate, selectedTime, userLat, userLon) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp).navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canProceed) ProfessionalBlue else Color.LightGray
                    ),
                    enabled = canProceed
                ) {
                    Icon(Icons.Default.Search, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find Professionals", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Service summary card
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = getServiceImageUrl(serviceCategory),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(serviceName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(serviceCategory.ifEmpty { "Service" }, color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("From ₹${basePrice.toInt()}", color = ProfessionalBlue, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }

            // Address section
            Text("📍 Your Address", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; onAddressChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your full address") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        locationPermLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }) {
                        if (isLoadingLocation) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Icon(Icons.Default.MyLocation, null, tint = ProfessionalBlue)
                    }
                },
                minLines = 2
            )

            // Date section
            Text("📅 Select Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = ProfessionalBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = selectedDate.ifEmpty { "Tap to pick a date" },
                        color = if (selectedDate.isEmpty()) TextSecondary else TextPrimary,
                        fontWeight = if (selectedDate.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Time slots
            Text("⏰ Select Time Slot", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(timeSlots.size) { idx ->
                    val slot = timeSlots[idx]
                    val isSelected = slot == selectedTime
                    Surface(
                        modifier = Modifier.clickable { selectedTime = slot; onDateTimeChange(selectedDate, slot) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ProfessionalBlue else Color.White,
                        shadowElevation = if (isSelected) 6.dp else 2.dp,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(0.5f))
                    ) {
                        Text(
                            text = slot,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Notes section
            Text("📝 Special Instructions (Optional)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("E.g. bring eco-friendly products, 2nd floor, etc.") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                ),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun getServiceImageUrl(category: String): String {
    return when (category.lowercase()) {
        "cleaning" -> "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&fit=crop"
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
