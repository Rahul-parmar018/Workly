package com.example.workly.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.Booking
import com.example.workly.data.OrderStatus
import com.example.workly.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class AllBookingsActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeDataStore = remember { ThemeDataStore(this) }
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())

            WorklyTheme(themeMode = themeMode) {
                OrderIntelligenceScreen(
                    onBack = { finish() },
                    onUpdateStatus = { bid, status -> updateStatus(bid, status) }
                )
            }
        }
    }

    private fun updateStatus(bookingId: String, status: String) {
        db.collection("orders").document(bookingId).update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Order #$bookingId updated to $status", Toast.LENGTH_SHORT).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderIntelligenceScreen(onBack: () -> Unit, onUpdateStatus: (String, String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("all") }
    var isLoading by remember { mutableStateOf(true) }

    // Real-Time Stream with Serialization Fix
    LaunchedEffect(Unit) {
        db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    bookings = it.documents.mapNotNull { doc ->
                        try {
                            // Manual Mapping to prevent Timestamp/Long ClassCastException
                            val b = doc.toObject(Booking::class.java)?.copy(id = doc.id)
                            
                            // Ensure createdAt is valid regardless of type in DB
                            val createdAtVal = doc.get("createdAt").let { v ->
                                when (v) {
                                    is Long -> v
                                    is com.google.firebase.Timestamp -> v.toDate().time
                                    else -> System.currentTimeMillis()
                                }
                            }
                            b?.copy(createdAt = createdAtVal)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    isLoading = false
                }
            }
    }

    val filteredList = if (selectedFilter == "all") bookings else bookings.filter { it.status == selectedFilter }

    Scaffold(
        containerColor = PremiumBlack,
        topBar = {
            TopAppBar(
                title = { Text("ORDER INTELLIGENCE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumSilver) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // ── STATUS FILTERS ──
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filters = listOf("all", OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.STARTED, OrderStatus.COMPLETED, OrderStatus.CANCELLED)
                items(filters) { f ->
                    FilterChip(f, selectedFilter == f) { selectedFilter = f }
                }
            }

            // ── RESULTS ──
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumSilver)
                }
            } else if (filteredList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Matching Records", color = PremiumSilver.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { booking ->
                        OrderHudCard(booking, onUpdateStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) PremiumSilver else PremiumBlackSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) PremiumSilver else DarkBorder)
    ) {
        Text(
            label.uppercase(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.Black else PremiumSilver,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun OrderHudCard(booking: Booking, onUpdateStatus: (String, String) -> Unit) {
    var showActions by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(Modifier.padding(20.dp)) {
            // Header: Status & Price
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusTag(booking.status)
                Text("₹${booking.finalPrice.toInt()}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(16.dp))

            // Info: Service & User
            Text(booking.serviceName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Person, null, tint = PremiumSilver, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(booking.userName, color = PremiumSilver, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Meta: Time & Location
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = PremiumSilver.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("${booking.date} • ${booking.time}", color = PremiumSilver.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showActions = !showActions }) {
                    Icon(if (showActions) Icons.Default.KeyboardArrowUp else Icons.Default.Tune, null, tint = PremiumSilver)
                }
            }

            // Admin Actions HUD
            AnimatedVisibility(visible = showActions) {
                Column(Modifier.padding(top = 16.dp)) {
                    Divider(color = DarkBorder, thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("ADMIN OVERRIDE", color = PremiumSilver, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusButton("COMPLETE", Color(0xFF10B981)) { onUpdateStatus(booking.id, OrderStatus.COMPLETED) }
                        StatusButton("CANCEL", Color.Red) { onUpdateStatus(booking.id, OrderStatus.CANCELLED) }
                        StatusButton("RESET", PremiumSilver) { onUpdateStatus(booking.id, OrderStatus.PENDING) }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val color = when (status) {
        OrderStatus.COMPLETED -> Color(0xFF10B981)
        OrderStatus.PENDING -> Color(0xFFF59E0B)
        OrderStatus.CANCELLED -> Color.Red
        else -> PremiumSilver
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RowScope.StatusButton(label: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
