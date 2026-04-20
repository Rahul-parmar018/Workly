package com.example.workly.booking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val onBg = MaterialTheme.colorScheme.onBackground
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Success Gradient Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(primary, primary.copy(alpha = 0.85f))
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                // Success Icon with Outer Glow
                Box(contentAlignment = Alignment.Center) {
                    // Glow effect
                    Surface(
                        modifier = Modifier.size(130.dp),
                        shape = CircleShape,
                        color = Color.White.copy(0.12f)
                    ) {}
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = primary,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                
                Text(
                    "Booking Confirmed!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Color.White.copy(0.18f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Your professional is on the way 🚀",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // ── Card and Details ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp) // Pull it up into the header slightly for a "card overlap" look
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            Card(
                shape = RoundedCornerShape(28.dp), 
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Booking Details", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                        Surface(shape = RoundedCornerShape(12.dp), color = PremiumSilver.copy(0.15f)) {
                            Text("#${bookingId.take(8).uppercase()}", color = PremiumSilver, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    DetailItem(Icons.Default.Handyman, "Service", serviceName, PremiumSilver, Color.White)
                    DetailItem(Icons.Default.Person, "Professional", providerName, PremiumSilver, Color.White)
                    if (date.isNotEmpty()) DetailItem(Icons.Default.CalendarMonth, "Date", date, PremiumSilver, Color.White)
                    if (time.isNotEmpty()) DetailItem(Icons.Default.Schedule, "Time", time, PremiumSilver, Color.White)
                    if (address.isNotEmpty()) DetailItem(Icons.Default.LocationOn, "Address", address, PremiumSilver, Color.White)

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Amount Paid", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text("₹${price.toInt()}", color = PremiumSilver, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = {
                        val chatIntent = android.content.Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                            putExtra("RECEIVER_NAME", providerName)
                            putExtra("RECEIVER_ID", providerId)
                        }
                        context.startActivity(chatIntent)
                    },
                    modifier = Modifier.weight(1f).height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PremiumBlackSurface,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.3f)),
                    elevation = null
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat Pro", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
                
                Button(
                    onClick = onGoHome,
                    modifier = Modifier.weight(1f).height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver, contentColor = Color.Black),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(20.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go Home", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.Black)
                }
            }
            
            // Critical fix for navigation bar clipping
            Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, accent: Color, onSurf: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = accent.copy(alpha = 0.08f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(label, color = onSurf.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurf)
        }
    }
}
