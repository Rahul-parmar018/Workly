package com.example.workly.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Triple("UPI", Icons.Default.AccountBalance, "Google Pay / PhonePe / Paytm"),
        Triple("Card", Icons.Default.CreditCard, "Debit / Credit Card"),
        Triple("Cash", Icons.Default.Money, "Pay after service")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundGray,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundGray,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { isLoading = true; onPaymentComplete(selectedMethod) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryGradient), // Custom gradient background
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(), // Eliminate default padding for gradient
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Pay ₹${price.toInt()} Securely",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Order summary
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = ProfessionalBlue.copy(0.1f)) {
                            Icon(Icons.Default.MiscellaneousServices, null, tint = ProfessionalBlue, modifier = Modifier.padding(10.dp).size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Order Summary", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    SummaryRow("Service", serviceName)
                    SummaryRow("Professional", providerName, valueColor = ProfessionalBlue)
                    if (basePrice != price) {
                        SummaryRow("Base Price", "₹${basePrice.toInt()}", valueColor = TextSecondary)
                        SummaryRow("Pro Rate", "₹${price.toInt()}/hr")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Total Amount", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                        Text("₹${price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = EnergyOrange)
                    }
                }
            }

            // Payment methods
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                
                // Compact Shield Badge
                Surface(shape = RoundedCornerShape(100.dp), color = ElectricTeal.copy(0.08f), border = BorderStroke(1.dp, ElectricTeal.copy(0.2f))) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = ElectricTeal, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escrow Protected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricTeal)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                paymentMethods.forEach { (method, icon, subtitle) ->
                    val isSelected = method == selectedMethod
                    
                    // Animations
                    val scale by animateFloatAsState(if (isSelected) 1.03f else 1f, label = "scale")
                    val elevation by animateDpAsState(if (isSelected) 10.dp else 2.dp, label = "elevation")
                    val borderColor by animateColorAsState(if (isSelected) ProfessionalBlue else Color.Transparent, label = "border")
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                            .shadow(elevation, RoundedCornerShape(20.dp))
                            .clickable { selectedMethod = method }
                            .animateContentSize(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = if (isSelected) ProfessionalBlue.copy(0.1f) else BackgroundGray) {
                                Icon(icon, null, tint = if (isSelected) ProfessionalBlue else TextSecondary, modifier = Modifier.padding(10.dp).size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method, fontWeight = FontWeight.ExtraBold, color = if (isSelected) ProfessionalBlue else TextPrimary, fontSize = 16.sp)
                                Text(subtitle, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            }
                            
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = ProfessionalBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = valueColor)
    }
}
