package com.example.workly.booking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.chat.ChatActivity
import com.example.workly.theme.ThemeDataStore
import com.example.workly.theme.WorklyTheme
import androidx.compose.runtime.collectAsState
import com.google.firebase.firestore.FirebaseFirestore

class TrackOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
                SmartTrackingScreen(orderId) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTrackingScreen(orderId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    
    // LIVE STATE
    var status by remember { mutableStateOf("pending") }
    var providerName by remember { mutableStateOf("Rahul") }
    var providerPhone by remember { mutableStateOf("") }
    var providerId by remember { mutableStateOf("") }
    
    // 🛰️ FIRESTORE LISTENER
    LaunchedEffect(orderId) {
        if (orderId.isNotEmpty()) {
            firestore.collection("orders").document(orderId)
                .addSnapshotListener { doc, error ->
                    if (doc != null && doc.exists()) {
                        status = (doc.getString("status") ?: "accepted").lowercase()
                        providerName = doc.getString("providerName") ?: "Rahul"
                        providerPhone = doc.getString("providerPhone") ?: ""
                        providerId = doc.getString("providerId") ?: ""
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Status", fontWeight = FontWeight.Black, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        val safeProviderName = when {
            providerName.isBlank() -> "Service Pro"
            providerName.equals("Unknown", true) -> "Service Pro"
            providerName.contains("Rahul", true) -> "Service Pro" 
            else -> providerName
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 👤 PROVIDER CARD
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(shape = CircleShape, modifier = Modifier.size(100.dp), shadowElevation = 8.dp) {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=$providerName+Provider&background=1E2A78&color=fff&size=200",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Online Status indicator with manual border-style logic or qualified modifier
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF10B981), CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(safeProviderName, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Text("⭐⭐⭐⭐ 4.8 Rating • Active now", fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1E2A78), modifier = Modifier.size(20.dp))
                            Text("2.3 km", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Text("Distance", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Timer, null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(20.dp))
                            Text("15 - 20 min", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Text("Arrival", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 📊 LIVE PROGRESS TRACKER
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    HorizontalStepTracker(status)
                    Spacer(modifier = Modifier.height(24.dp))
                    StatusNarrativeMsg(status, providerName)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 🎯 ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PillActionBtn(Icons.Default.Call, "Call", Color(0xFF1E3A8A)) {
                    if (providerPhone.isNotEmpty()) context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$providerPhone")))
                }
                PillActionBtn(Icons.Default.Chat, "Chat", Color(0xFF1E3A8A)) {
                    if (providerId.isNotEmpty()) {
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("RECEIVER_ID", providerId)
                            putExtra("RECEIVER_NAME", providerName)
                        }
                        context.startActivity(intent)
                    }
                }
                PillActionBtn(Icons.Default.Close, "Cancel", Color(0xFFEF4444)) {
                    firestore.collection("orders").document(orderId).update("status", "cancelled")
                    Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            }
        }
    }
}

@Composable
fun HorizontalStepTracker(status: String) {
    val steps = listOf("Accepted", "Assigned", "Arriving", "Started")
    val currentIndex = steps.indexOfFirst { it.lowercase() == status }.coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        val onSurf = Color(0xFF0F172A)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            steps.forEachIndexed { index, label ->
                val isDone = index <= currentIndex
                val isCurrent = index == currentIndex
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isDone) Color(0xFF1E3A8A) else Color(0xFFE2E8F0), 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            val pulse by rememberInfiniteTransition().animateFloat(
                                1f, 1.8f, 
                                infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(scaleX = pulse, scaleY = pulse)
                                    .background(Color(0xFF1E3A8A).copy(0.15f), CircleShape)
                            )
                            Icon(Icons.Default.RadioButtonChecked, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else if (isDone) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isDone) onSurf else Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Progress Bar with Segmented Look
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(6.dp)
                .background(Color(0xFFE2E8F0), CircleShape)
        ) {
            val progress = when(currentIndex) {
                0 -> 0.15f
                1 -> 0.45f
                2 -> 0.75f
                3 -> 1.0f
                else -> 0.05f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF1E2A78), Color(0xFF3B82F6))),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun StatusNarrativeMsg(status: String, name: String) {
    val msg = when(status) {
        "accepted" -> "Request confirmed. $name will update soon."
        "assigned" -> "$name is preparing your professional kit 🛠️"
        "arriving" -> "$name is arriving at your location 🚀"
        "started" -> "Work is in progress. Sit back and relax! ✅"
        else -> "Tracking your service progress..."
    }
    
    Text(
        text = msg,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E3A8A),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

@Composable
fun PillActionBtn(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(80.dp).graphicsLayer(scaleX = 0.98f, scaleY = 0.98f),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Black, color = tint)
        }
    }
}
