package com.example.workly.booking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.Booking
import com.example.workly.data.Order
import com.example.workly.data.OrderStatus
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyBookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                MyBookingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(onBack: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("MyBookings", "Firestore Error: ${error.message}")
                        isLoading = false
                        return@addSnapshotListener
                    }
                    bookings = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            Booking(
                                id = doc.id,
                                serviceName = data["serviceName"]?.toString() ?: data["serviceTitle"]?.toString() ?: "Service",
                                providerName = data["providerName"]?.toString() ?: "",
                                providerId = data["providerId"]?.toString() ?: "",
                                finalPrice = (data["finalPrice"] as? Number)?.toDouble() ?: (data["price"] as? Number)?.toDouble() ?: 0.0,
                                status = data["status"]?.toString() ?: OrderStatus.PENDING,
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfessionalBlue)
            }
        } else if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = ProfessionalBlue.copy(0.05f)) {
                        Icon(Icons.Default.BookOnline, null, modifier = Modifier.padding(24.dp).size(48.dp), tint = ProfessionalBlue.copy(0.3f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No bookings found yet.", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings) { booking ->
                    BookingHistoryCard(booking)
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: Booking) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val statusColor = when (booking.status.lowercase()) {
        "pending" -> EnergyOrange
        "accepted" -> ProfessionalBlue
        "completed" -> Color(0xFF2E7D32)
        else -> TextSecondary
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = ProfessionalBlue.copy(0.1f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Handyman, null, tint = ProfessionalBlue, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Order ID: ${booking.id.take(8)}...", fontSize = 12.sp, color = TextSecondary)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(0.12f)) {
                    Text(
                        booking.status.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    val dateStr = try {
                        val sdf = java.text.SimpleDateFormat("dd MMM yyyy · hh:mm a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(booking.createdAt))
                    } catch (e: Exception) { "Unknown Date" }
                    
                    Text(dateStr, fontSize = 13.sp, color = TextSecondary)
                }
                Text("₹${booking.finalPrice.toInt()}", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
            }

            if (booking.providerName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = ProfessionalBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pro: ${booking.providerName}", color = ProfessionalBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val intent = android.content.Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                        putExtra("RECEIVER_NAME", booking.providerName)
                        putExtra("RECEIVER_ID", booking.providerId)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Assignment, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Message Pro", fontWeight = FontWeight.Bold)
            }
        }
    }
}
