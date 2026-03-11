package com.example.workly.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.booking.BookingSuccessActivity
import com.example.workly.theme.*

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"

        setContent {
            WorklyTheme {
                PaymentScreen(
                    serviceName = serviceName,
                    price = price,
                    providerName = providerName,
                    onBackClick = { finish() },
                    onPaymentComplete = {
                        val resultIntent = Intent()
                        resultIntent.putExtra("PAYMENT_STATUS", "Held")
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    serviceName: String,
    price: Double,
    providerName: String,
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bill Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Order Summary", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(serviceName, fontWeight = FontWeight.Bold)
                        Text("$${price}/hr", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pro: $providerName", color = ProfessionalBlue, fontWeight = FontWeight.Medium)
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("$$price", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EnergyOrange)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Payment Method Placeholder
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CreditCard, null, tint = ProfessionalBlue)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Visa **** 4242", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(selected = true, onClick = null)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Escrow Logic Explanation
            Surface(
                color = ElectricTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Shield Escrow: Your payment is held securely and only released after job completion.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricTeal,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    isLoading = true
                    // Simulate processing
                    onPaymentComplete() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Securely Pay $$price", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
