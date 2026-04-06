package com.example.workly.booking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.home.HomeActivity
import com.example.workly.theme.*

class BookingSuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"
        val bookingId = intent.getStringExtra("BOOKING_ID") ?: ""
        val date = intent.getStringExtra("DATE") ?: ""
        val time = intent.getStringExtra("TIME") ?: ""
        val address = intent.getStringExtra("ADDRESS") ?: ""
        val price = intent.getDoubleExtra("PRICE", 0.0)
        val providerId = intent.getStringExtra("PROVIDER_ID") ?: ""

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
                BookingSuccessScreen(
                    serviceName = serviceName,
                    providerName = providerName,
                    bookingId = bookingId,
                    date = date,
                    time = time,
                    address = address,
                    price = price,
                    providerId = providerId,
                    onGoHome = {
                        startActivity(Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun BookingSuccessScreen(
    serviceName: String,
    providerName: String,
    bookingId: String,
    date: String,
    time: String,
    address: String,
    price: Double,
    providerId: String,
    onGoHome: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val bg = MaterialTheme.colorScheme.background

    // Check icon bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Scaffold(
        containerColor = bg,
        bottomBar = {
            Surface(
                color = surface,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Chat Pro Button
                    OutlinedButton(
                        onClick = {
                            val chatIntent = Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                                putExtra("RECEIVER_NAME", providerName)
                                putExtra("RECEIVER_ID", providerId)
                            }
                            context.startActivity(chatIntent)
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primary)
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat Pro", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Go Home Button
                    Button(
                        onClick = onGoHome,
                        modifier = Modifier.weight(1.3f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Home, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Go Home", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Success Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(primary, primary.copy(alpha = 0.85f))
                        )
                    )
                    .statusBarsPadding()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Animated check icon
                    Surface(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = Color.White.copy(0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                color = Color.White.copy(0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Booking Confirmed! 🎉",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your professional is on the way",
                        color = Color.White.copy(0.8f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Booking Details Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = surface,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, onSurf.copy(alpha = 0.04f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header with booking ID
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Booking Details", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)
                            Surface(shape = RoundedCornerShape(10.dp), color = primary.copy(0.08f)) {
                                Text(
                                    "#${bookingId.take(8).uppercase()}",
                                    color = primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = onSurf.copy(alpha = 0.05f))

                        // Detail items
                        DetailItem(Icons.Default.Handyman, "Service", serviceName, primary, onSurf)
                        DetailItem(Icons.Default.Person, "Professional", providerName, primary, onSurf)
                        if (date.isNotEmpty()) DetailItem(Icons.Default.CalendarMonth, "Date", date, primary, onSurf)
                        if (time.isNotEmpty()) DetailItem(Icons.Default.Schedule, "Time", time, primary, onSurf)
                        if (address.isNotEmpty()) DetailItem(Icons.Default.LocationOn, "Address", address, primary, onSurf)

                        HorizontalDivider(color = onSurf.copy(alpha = 0.05f))

                        // Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Amount Paid", fontSize = 12.sp, color = onSurf.copy(alpha = 0.5f))
                                Text("₹${price.toInt()}", color = primary, fontWeight = FontWeight.Black, fontSize = 26.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paid", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }

                // ── What's Next Card ──
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = primary.copy(alpha = 0.04f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("What happens next?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurf)
                        Spacer(modifier = Modifier.height(14.dp))

                        val steps = listOf(
                            "Professional is being notified" to Icons.Default.Notifications,
                            "They'll confirm within 5 minutes" to Icons.Default.Timer,
                            "Track live progress in My Bookings" to Icons.Default.Map
                        )

                        steps.forEachIndexed { index, (text, icon) ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = primary.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${index + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text, fontSize = 13.sp, color = onSurf.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, accent: Color, onSurf: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = accent.copy(alpha = 0.08f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(label, color = onSurf.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurf)
        }
    }
}
