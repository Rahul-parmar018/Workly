package com.example.workly.booking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.Order
import com.example.workly.theme.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class MyBookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                MyBookingsScreenFinal(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreenFinal(onBack: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var bookings by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Completed", "Cancelled")

    // 🔥 INSTANT REAL-TIME SNAPSHOT LISTENER (MANDATORY)
    LaunchedEffect(userId) {
        if (userId.isEmpty()) return@LaunchedEffect
        firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserBookings", "Error: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val raw = snapshot.toObjects(Order::class.java)
                    bookings = raw.sortedByDescending { it.getSafeDate() }
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("My Dashboard", fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text("Real-time service updates", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF1E2A78),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = Color(0xFF1E2A78), height = 3.dp)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1E2A78))
                }
            } else {
                val filtered = bookings.filter { b ->
                    val s = (b.status ?: "pending").lowercase()
                    when(selectedTabIndex) {
                        0 -> listOf("pending", "accepted", "arriving", "started").contains(s)
                        1 -> s == "completed"
                        2 -> s == "cancelled"
                        else -> true
                    }
                }
                
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items to show", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filtered) { b -> UserOrderCard(b) }
                    }
                }
            }
        }
    }
}

@Composable
fun UserOrderCard(order: Order) {
    val context = LocalContext.current
    val status = (order.status ?: "pending").lowercase()
    
    val statusColor = when (status) {
        "pending" -> Color(0xFF64748B)      // Gray
        "accepted" -> Color(0xFF3B82F6)     // Blue
        "arriving" -> Color(0xFF8B5CF6)     // Purple
        "started" -> Color(0xFF1E2A78)      // Dark Blue (Workly Brand)
        "completed" -> Color(0xFF10B981)    // Green
        "cancelled" -> Color(0xFFEF4444)    // Red
        else -> Color(0xFF64748B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp), color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(order.serviceTitle ?: "Service", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))
                    Text("Price: ₹${order.getSafePrice()}", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
                
                // Status Badge
                Surface(color = statusColor.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = statusColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(16.dp))

            // Details
            UserOrderInfoBit(Icons.Default.LocationOn, order.address ?: "Address", Color(0xFF1E2A78))
            UserOrderInfoBit(Icons.Default.CalendarToday, "${order.date} at ${order.time}", Color(0xFF1E2A78))

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status == "completed") {
                    UserActionBtn(Icons.Default.Star, "Rate") { /* Rate logic */ }
                } else if (status != "cancelled") {
                    // TRACK BUTTON (Pulsing if Arriving/Started)
                    UserActionBtn(Icons.Default.Map, "Track Status") {
                        val intent = Intent(context, com.example.workly.booking.TrackOrderActivity::class.java).apply {
                            putExtra("ORDER_ID", order.id)
                        }
                        context.startActivity(intent)
                    }
                    
                    UserActionBtn(Icons.Default.Chat, null) {
                        if (!order.providerId.isNullOrEmpty()) {
                            val intent = Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                                putExtra("RECEIVER_ID", order.providerId)
                                putExtra("RECEIVER_NAME", order.providerName ?: "Pro")
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserOrderInfoBit(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun UserActionBtn(icon: ImageVector, label: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF1E2A78), modifier = Modifier.size(16.dp))
            if (label != null) {
                Spacer(Modifier.width(8.dp))
                Text(label, fontSize = 12.sp, color = Color(0xFF1E2A78), fontWeight = FontWeight.Black)
            }
        }
    }
}
