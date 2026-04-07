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
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
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

    // Theme aliases
    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val onBg = MaterialTheme.colorScheme.onBackground
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

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
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("My Dashboard", fontWeight = FontWeight.Black, fontSize = 22.sp, color = onSurf)
                        Text("Real-time service updates", fontSize = 12.sp, color = onSurf.copy(alpha = 0.55f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = onSurf)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surface)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = surface,
                contentColor = primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = primary, height = 3.dp)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                        selectedContentColor = primary,
                        unselectedContentColor = onSurf.copy(alpha = 0.4f)
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary)
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, null, tint = onSurf.copy(alpha = 0.1f), modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No bookings here yet", color = onSurf.copy(alpha = 0.45f), fontWeight = FontWeight.SemiBold)
                        }
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
    
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    val statusColor = when (status) {
        "pending" -> Color(0xFF64748B)
        "accepted" -> Color(0xFF3B82F6)
        "arriving" -> Color(0xFF8B5CF6)
        "started" -> primary
        "completed" -> Color(0xFF10B981)
        "cancelled" -> Color(0xFFEF4444)
        else -> Color(0xFF64748B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp), 
        color = surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(order.serviceName.takeIf { it.isNotEmpty() } ?: order.serviceTitle.takeIf { it.isNotEmpty() } ?: "Service", fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurf)
                    Text("Total: ₹${order.getSafePrice()}", fontSize = 13.sp, color = onSurf.copy(alpha = 0.55f), fontWeight = FontWeight.Bold)
                }
                
                // Status Badge
                Surface(color = statusColor.copy(0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = statusColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = onSurf.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(16.dp))

            // Details Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UserOrderInfoRow(Icons.Default.LocationOn, order.address ?: "Address", onSurf)
                UserOrderInfoRow(Icons.Default.CalendarToday, "${order.date} at ${order.time}", onSurf)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Professional Profile (if active)
            if (!order.providerName.isNullOrEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = surfVar.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = primary, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(order.providerName?.take(1) ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(order.providerName ?: "Professional", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurf)
                        Spacer(Modifier.weight(1f))
                        if (status != "completed" && status != "cancelled") {
                            IconButton(onClick = {
                                val intent = Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                                    putExtra("RECEIVER_ID", order.providerId)
                                    putExtra("RECEIVER_NAME", order.providerName ?: "Pro")
                                }
                                context.startActivity(intent)
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Chat, null, tint = primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (status == "completed") {
                    UserOrderAction(Icons.Default.Star, "Rate Service", primary, surface, true) { /* Logic */ }
                } else if (status != "cancelled") {
                    UserOrderAction(Icons.Default.Map, "Track Live", primary, surface, true) {
                        val intent = Intent(context, com.example.workly.booking.TrackOrderActivity::class.java).apply {
                            putExtra("ORDER_ID", order.id)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun UserOrderInfoRow(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = color.copy(alpha = 0.5f))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun UserOrderAction(icon: ImageVector, label: String, primary: Color, surface: Color, isPrimary: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) primary else surface,
        border = if (isPrimary) null else androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isPrimary) Color.White else primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, color = if (isPrimary) Color.White else primary, fontWeight = FontWeight.ExtraBold)
        }
    }
}
