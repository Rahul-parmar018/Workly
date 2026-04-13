package com.example.workly.provider

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.OrderStatus
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProviderOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
                ProviderOrdersScreenFinal(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderOrdersScreenFinal(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    var ordersList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("orders")
                .whereEqualTo("providerId", user.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ProviderOrders", "Error: ${error.message}")
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        ordersList = snapshot.documents.mapNotNull { 
                            val data = it.data?.toMutableMap() ?: mutableMapOf()
                            data["id"] = it.id
                            data
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Operative Console", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 20.sp, 
                            color = PremiumWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "High-priority service tasks", 
                            fontSize = 11.sp, 
                            color = PremiumSilver.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PremiumSilver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        },
        containerColor = PremiumBlack
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PremiumSilver)
            } else if (ordersList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(PremiumBlackSurface, CircleShape)
                            .border(1.dp, PremiumSilver.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AssignmentLate, 
                            null, 
                            Modifier.size(48.dp), 
                            PremiumSilver.copy(alpha = 0.2f)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "No active requests", 
                        color = PremiumSilver, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Standing by for premium tasks...", 
                        color = PremiumSilver.copy(alpha = 0.5f), 
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(ordersList) { order ->
                        PremiumProviderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumProviderCard(order: Map<String, Any>) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val orderId = order["id"]?.toString() ?: ""
    
    // 🔥 DATA FIX: Use serviceName instead of generic Title
    val serviceTitle = order["serviceName"]?.toString() ?: order["serviceTitle"]?.toString() ?: "Service Job"
    
    val userName = order["userName"]?.toString() ?: "Customer"
    val userPhone = order["userPhone"]?.toString() ?: ""
    val address = order["address"]?.toString() ?: "Location not provided"
    val bookingTime = "${order["date"]} at ${order["time"]}"
    val price = order["finalPrice"]?.toString() ?: order["price"]?.toString() ?: "0"
    val status = (order["status"]?.toString() ?: OrderStatus.PENDING).lowercase()
    
    var isBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Service + Badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = serviceTitle, 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 19.sp, 
                        color = PremiumWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Booked by $userName", 
                        fontSize = 14.sp, 
                        color = PremiumSilver.copy(alpha = 0.7f), 
                        fontWeight = FontWeight.SemiBold
                    )
                }
                ModernStatusBadge(status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Operative Info Cluster
            OperativeRow(Icons.Default.LocationOn, address)
            Spacer(Modifier.height(8.dp))
            OperativeRow(Icons.Default.AccessTime, bookingTime)
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = PremiumSilver.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Pricing & Action Cluster
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        "POTENTIAL EARNING", 
                        fontSize = 10.sp, 
                        color = PremiumSilver.copy(alpha = 0.5f), 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "₹$price", 
                        fontSize = 28.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = PremiumWhite
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED) {
                        Surface(
                            onClick = { if (userPhone.isNotEmpty()) context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$userPhone"))) },
                            shape = CircleShape, 
                            color = PremiumBlack, 
                            border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.3f)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Phone, null, tint = PremiumSilver, modifier = Modifier.size(22.dp))
                            }
                        }
                        Surface(
                            onClick = {
                                val intent = Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                                    putExtra("RECEIVER_ID", order["userId"]!!.toString())
                                    putExtra("RECEIVER_NAME", userName)
                                }
                                context.startActivity(intent)
                            },
                            shape = CircleShape, 
                            color = PremiumBlack, 
                            border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.3f)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Chat, null, tint = PremiumSilver, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔥 COMPONENT: HERO ACTION BUTTON (The Fix)
            if (status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED) {
                HeroActionEngine(
                    status = status,
                    order = order,
                    onBusy = { isBusy = true },
                    onDone = { isBusy = false }
                )
            } else if (status == OrderStatus.COMPLETED) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("TASK CRYSTALLIZED ✅", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OperativeRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = PremiumSilver)
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, color = PremiumSilver.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ModernStatusBadge(status: String) {
    val (color, label) = when(status) {
        "pending" -> Color(0xFFFF9800) to "New Request"
        "accepted" -> Color(0xFF2196F3) to "Ready"
        "arriving" -> Color(0xFF9C27B0) to "On Way"
        "started" -> Color(0xFF3F51B5) to "In Service"
        "completed" -> Color(0xFF4CAF50) to "Completed"
        else -> Color.Gray to status.uppercase()
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun HeroActionEngine(
    status: String,
    order: Map<String, Any>,
    onBusy: () -> Unit,
    onDone: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var updating by remember { mutableStateOf(false) }
    
    val orderId = order["id"]?.toString() ?: ""
    val price = order["finalPrice"]?.toString() ?: order["price"]?.toString() ?: "0"
    val providerId = order["providerId"]?.toString() ?: ""

    val (label, targetStatus) = when (status) {
        "pending" -> "Accept Request" to "accepted"
        "accepted" -> "Go to Customer 🚗" to "arriving"
        "arriving" -> "Begin Service 🛠️" to "started"
        "started" -> "Finish Job ✅" to "completed"
        else -> "" to ""
    }

    if (label.isNotEmpty()) {
        val gradient = Brush.horizontalGradient(listOf(PremiumSilver, Color(0xFFB0BEC5)))
        
        Button(
            onClick = {
                updating = true
                onBusy()
                if (targetStatus == "completed") {
                    db.runTransaction { tr ->
                        val oRef = db.collection("orders").document(orderId)
                        val pRef = db.collection("providers").document(providerId)
                        tr.update(oRef, mapOf("status" to "completed", "completedAt" to System.currentTimeMillis()))
                        val earn = tr.get(pRef).getDouble("earnings") ?: 0.0
                        tr.update(pRef, "earnings", earn + (price.toDoubleOrNull() ?: 0.0))
                    }.addOnCompleteListener { 
                        // Trigger Notification for User
                        val userId = order["userId"]?.toString() ?: ""
                        if (userId.isNotEmpty()) {
                            val notif = mapOf(
                                "userId" to userId,
                                "title" to "Task Crystallized ✅",
                                "message" to "Your service for ${order["serviceName"]} is complete.",
                                "createdAt" to System.currentTimeMillis(),
                                "isRead" to false
                            )
                            db.collection("notifications").add(notif)
                        }
                        updating = false; onDone() 
                    }
                } else {
                    db.collection("orders").document(orderId)
                        .update("status", targetStatus)
                        .addOnCompleteListener { 
                            // Trigger Notification for User
                            val userId = order["userId"]?.toString() ?: ""
                            if (userId.isNotEmpty()) {
                                val msg = when(targetStatus) {
                                    "accepted" -> "Provider accepted your request!"
                                    "arriving" -> "Provider is on the way!"
                                    "started" -> "Work has begun on your service."
                                    else -> "Update on your service."
                                }
                                val notif = mapOf(
                                    "userId" to userId,
                                    "title" to "Service Pulse ⚡",
                                    "message" to msg,
                                    "createdAt" to System.currentTimeMillis(),
                                    "isRead" to false
                                )
                                db.collection("notifications").add(notif)
                            }
                            updating = false; onDone() 
                        }
                }
            },
            enabled = !updating,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(gradient),
                contentAlignment = Alignment.Center
            ) {
                if (updating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PremiumBlack, strokeWidth = 3.dp)
                } else {
                    Text(text = label, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PremiumBlack)
                }
            }
        }
    }
}
