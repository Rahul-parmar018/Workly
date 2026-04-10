package com.example.workly.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import com.example.workly.theme.*

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val basePrice = intent.getDoubleExtra("BASE_PRICE", price)
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"

        setContent {
            WorklyTheme {
                PaymentScreen(
                    serviceName = serviceName,
                    price = price,
                    basePrice = basePrice,
                    providerName = providerName,
                    onBackClick = { finish() },
                    onPaymentComplete = { method ->
                        val result = Intent().apply {
                            putExtra("PAYMENT_STATUS", "Paid")
                            putExtra("PAYMENT_METHOD", method)
                        }
                        setResult(RESULT_OK, result)
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
    basePrice: Double,
    providerName: String,
    onBackClick: () -> Unit,
    onPaymentComplete: (method: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var isLoading by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        Triple("UPI", Icons.Default.QrCodeScanner, "Google Pay / PhonePe / Paytm"),
        Triple("Card", Icons.Default.CreditCard, "Debit / Credit Card"),
        Triple("Cash", Icons.Default.Money, "Pay after service")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Payment", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 24.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).navigationBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payment", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("₹${price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = TextPrimary)
                        }
                    }
                    Button(
                        onClick = { isLoading = true; onPaymentComplete(selectedMethod) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                        } else {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PAY SECURELY", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Premium Order Summary Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = ProfessionalBlue.copy(0.08f)) {
                            Icon(Icons.Default.Build, null, modifier = Modifier.padding(14.dp).size(26.dp), tint = ProfessionalBlue)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(serviceName, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TextPrimary)
                            Text("By $providerName", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                        drawLine(
                            color = Color.LightGray.copy(0.5f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (basePrice != price) {
                        SummaryRow("Base Amount", "₹${basePrice.toInt()}", TextSecondary)
                        SummaryRow("Professional Rate", "₹${price.toInt()}", TextSecondary)
                    } else {
                        SummaryRow("Service Total", "₹${price.toInt()}", TextSecondary)
                    }
                }
            }

            // Payment Methods Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                
                paymentMethods.forEach { (method, icon, subtitle) ->
                    val isSelected = method == selectedMethod
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ProfessionalBlue.copy(0.04f) else Color.White
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ProfessionalBlue) else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(0.3f)),
                        elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), color = if (isSelected) ProfessionalBlue.copy(0.1f) else Color(0xFFF1F5F9)) {
                                Icon(icon, null, tint = if (isSelected) ProfessionalBlue else TextSecondary, modifier = Modifier.padding(12.dp).size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = if (isSelected) ProfessionalBlue else TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(subtitle, fontSize = 13.sp, color = TextSecondary)
                            }
                            RadioButton(
                                selected = isSelected, 
                                onClick = { selectedMethod = method }, 
                                colors = RadioButtonDefaults.colors(selectedColor = ProfessionalBlue, unselectedColor = Color.LightGray)
                            )
                        }
                    }
                }
            }

            // Trust Badge
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE8F5E9)) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Your payment is secured by Escrow. Funds are released only after you confirm the job is complete.", 
                        fontSize = 13.sp, 
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = valueColor)
    }
}
