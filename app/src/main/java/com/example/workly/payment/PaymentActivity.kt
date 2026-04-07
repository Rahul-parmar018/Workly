package com.example.workly.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import com.example.workly.theme.*

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val basePrice = intent.getDoubleExtra("BASE_PRICE", price)
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)

            WorklyTheme(themeMode = themeMode) {
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

    // Theme aliases
    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = onSurf,
                    navigationIconContentColor = onSurf
                )
            )
        },
        containerColor = bg,
        bottomBar = {
            Surface(color = surface, shadowElevation = 24.dp) {
                Button(
                    onClick = { isLoading = true; onPaymentComplete(selectedMethod) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(58.dp).navigationBarsPadding(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pay ₹${price.toInt()} Securely", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        val safeProviderName = when {
            providerName.isBlank() -> "Service Pro"
            providerName.equals("Unknown", true) -> "Service Pro"
            providerName.contains("Unknown", true) -> "Service Pro"
            else -> providerName
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Order summary
            Card(
                shape = RoundedCornerShape(28.dp), 
                colors = CardDefaults.cardColors(containerColor = surface), 
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, onSurf.copy(0.08f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.Black, fontSize = 20.sp, color = onSurf)
                    Spacer(modifier = Modifier.height(20.dp))
                    SummaryRow("Service", serviceName, onSurf = onSurf)
                    SummaryRow("Professional", safeProviderName, valueColor = primary, onSurf = onSurf)
                    if (basePrice != price && basePrice > 0) {
                        SummaryRow("Base Price", "₹${basePrice.toInt()}", valueColor = onSurf.copy(0.5f), onSurf = onSurf)
                        SummaryRow("Pro Rate", "₹${price.toInt()}/hr", onSurf = onSurf)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = onSurf.copy(alpha = 0.08f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurf)
                        Text("₹${price.toInt()}", fontWeight = FontWeight.Black, fontSize = 26.sp, color = primary)
                    }
                }
            }

            // Payment methods
            Text("Select Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = onSurf)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                paymentMethods.forEach { (method, icon, subtitle) ->
                    val isSelected = method == selectedMethod
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method },
                        shape = RoundedCornerShape(22.dp),
                        color = if (isSelected) primary.copy(0.04f) else surface,
                        border = BorderStroke(1.5.dp, if (isSelected) primary else onSurf.copy(0.05f)),
                        shadowElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(14.dp), 
                                color = if (isSelected) primary.copy(0.12f) else surfVar
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = if (isSelected) primary else onSurf.copy(0.6f), modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method, fontWeight = FontWeight.Black, color = onSurf, fontSize = 16.sp)
                                Text(subtitle, fontSize = 12.sp, color = onSurf.copy(0.5f), fontWeight = FontWeight.Medium)
                            }
                            RadioButton(
                                selected = isSelected, 
                                onClick = { selectedMethod = method }, 
                                colors = RadioButtonDefaults.colors(selectedColor = primary, unselectedColor = onSurf.copy(0.2f))
                            )
                        }
                    }
                }
            }

            // Protection Badge
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp), 
                color = ElectricTeal.copy(0.06f),
                border = BorderStroke(1.dp, ElectricTeal.copy(0.15f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = ElectricTeal, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Workly Security Policy", fontSize = 14.sp, color = ElectricTeal, fontWeight = FontWeight.Bold)
                        Text("Escrow-secured: Funds released after service.", fontSize = 12.sp, color = ElectricTeal.copy(0.8f))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color? = null, onSurf: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = onSurf.copy(0.5f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = valueColor ?: onSurf)
    }
}
