package com.example.workly.booking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.home.HomeActivity
import com.example.workly.theme.*
import kotlinx.coroutines.delay

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

        setContent {
            WorklyTheme {
                BookingSuccessScreen(
                    serviceName = serviceName,
                    providerName = providerName,
                    bookingId = bookingId,
                    date = date,
                    time = time,
                    address = address,
                    price = price,
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
    onGoHome: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(scrollState)
            .navigationBarsPadding(), // Ensures safety from gesture bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(Brush.verticalGradient(listOf(ProfessionalBlue, ElectricTeal))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Success Icon with Glow and Pulse
                Box(contentAlignment = Alignment.Center) {
                    // Soft Radial Glow
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .graphicsLayer { alpha = 0.25f }
                            .background(Brush.radialGradient(listOf(Color.White, Color.Transparent)), CircleShape)
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = Color.White.copy(0.25f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(20.dp).fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Booking Confirmed! 🎉", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Your professional is on the way", color = Color.White.copy(0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Booking details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-30).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Centered Booking ID Badge
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White,
                    shadowElevation = 12.dp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Booking ID: #${bookingId.take(8).uppercase()}",
                        color = ProfessionalBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("Booking Details", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                    HorizontalDivider(color = Color.LightGray.copy(0.3f), thickness = 1.dp)

                    // Detail items
                    val rowItems = listOf(
                        Triple(Icons.Default.MiscellaneousServices, "Service", serviceName),
                        Triple(Icons.Default.Person, "Professional", providerName),
                        Triple(Icons.Default.CalendarMonth, "Date", date),
                        Triple(Icons.Default.Schedule, "Time", time),
                        Triple(Icons.Default.LocationOn, "Address", address)
                    )

                    rowItems.forEachIndexed { index, (icon, label, value) ->
                        if (value.isNotEmpty()) {
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInVertically { it * (index + 1) / 2 } + fadeIn(animationSpec = tween(600 + index * 100))
                            ) {
                                ComplexDetailRow(icon, label, value)
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(0.3f), thickness = 1.dp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Amount Paid", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                        Text("₹${price.toInt()}", color = EnergyOrange, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    }
                }
            }

            // High-Impact Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 48.dp), // Extra bottom padding for safety
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary CTA: Chat
                Button(
                    onClick = { /* Navigate to chat */ },
                    modifier = Modifier.weight(1f).height(56.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(SecondaryGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat Pro", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                
                // Primary CTA: Go Home (Right side for thumb accessibility)
                Button(
                    onClick = onGoHome,
                    modifier = Modifier.weight(1f).height(56.dp).shadow(10.dp, CircleShape).clip(CircleShape).background(PrimaryGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go Home", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ComplexDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(12.dp), color = ProfessionalBlue.copy(0.08f)) {
            Icon(icon, null, tint = ProfessionalBlue, modifier = Modifier.padding(10.dp).size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
        }
    }
}
