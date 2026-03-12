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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Brush.verticalGradient(listOf(ProfessionalBlue, ElectricTeal))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(60.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Booking Confirmed! 🎉", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your professional is on the way", color = Color.White.copy(0.85f), fontSize = 15.sp)
            }
        }

        // Booking details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Booking ID chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ProfessionalBlue.copy(0.08f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Booking ID: #${bookingId.take(8).uppercase()}",
                    color = ProfessionalBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Booking Details", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    HorizontalDivider(color = Color.LightGray.copy(0.4f))

                    DetailRow(Icons.Default.Handyman, "Service", serviceName)
                    DetailRow(Icons.Default.Person, "Professional", providerName)
                    if (date.isNotEmpty()) DetailRow(Icons.Default.CalendarMonth, "Date", date)
                    if (time.isNotEmpty()) DetailRow(Icons.Default.Schedule, "Time", time)
                    if (address.isNotEmpty()) DetailRow(Icons.Default.LocationOn, "Address", address)

                    HorizontalDivider(color = Color.LightGray.copy(0.4f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Paid", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("₹${price.toInt()}", color = ElectricTeal, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Navigate to chat */ },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Pro", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onGoHome,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue)
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go Home", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = ProfessionalBlue, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        }
    }
}
