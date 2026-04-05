package com.example.workly.booking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
        // Success header with Brand Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(primary, primary.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp),
                    shape = CircleShape,
                    color = Color.White.copy(0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(60.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Booking Confirmed! 🎉", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Your professional is on the way", color = Color.White.copy(0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Booking details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Card
            Card(
                shape = RoundedCornerShape(28.dp), 
                colors = CardDefaults.cardColors(containerColor = surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Booking Details", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)
                        Surface(shape = RoundedCornerShape(8.dp), color = primary.copy(0.08f)) {
                            Text("#${bookingId.take(8).uppercase()}", color = primary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                    
                    HorizontalDivider(color = onSurf.copy(alpha = 0.05f))

                    DetailItem(Icons.Default.Handyman, "Service", serviceName, primary, onSurf)
                    DetailItem(Icons.Default.Person, "Professional", providerName, primary, onSurf)
                    if (date.isNotEmpty()) DetailItem(Icons.Default.CalendarMonth, "Date", date, primary, onSurf)
                    if (time.isNotEmpty()) DetailItem(Icons.Default.Schedule, "Time", time, primary, onSurf)
                    if (address.isNotEmpty()) DetailItem(Icons.Default.LocationOn, "Address", address, primary, onSurf)

                    HorizontalDivider(color = onSurf.copy(alpha = 0.05f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Amount Paid", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurf)
                        Text("₹${price.toInt()}", color = primary, fontWeight = FontWeight.Black, fontSize = 22.sp)
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
                        containerColor = primary.copy(alpha = 0.1f),
                        contentColor = primary
                    ),
                    elevation = null
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat Pro", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                
                Button(
                    onClick = onGoHome,
                    modifier = Modifier.weight(1f).height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go Home", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
