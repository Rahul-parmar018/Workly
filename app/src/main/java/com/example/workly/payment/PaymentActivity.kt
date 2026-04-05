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
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Order summary
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(surface), border = androidx.compose.foundation.BorderStroke(1.dp, onSurf.copy(0.05f))) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryRow("Service", serviceName, onSurf = onSurf)
                    SummaryRow("Professional", providerName, valueColor = primary, onSurf = onSurf)
                    if (basePrice != price) {
                        SummaryRow("Base Price", "₹${basePrice.toInt()}", valueColor = onSurf.copy(0.5f), onSurf = onSurf)
                        SummaryRow("Pro Rate", "₹${price.toInt()}/hr", onSurf = onSurf)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = onSurf.copy(alpha = 0.05f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)
                        Text("₹${price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = EnergyOrange)
                    }
                }
            }

            // Payment methods
            Text("Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)
            paymentMethods.forEach { (method, icon, subtitle) ->
                val isSelected = method == selectedMethod
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isSelected) 12.dp else 2.dp, RoundedCornerShape(18.dp), spotColor = if (isSelected) primary.copy(0.5f) else Color.Black.copy(0.1f))
                        .clickable { selectedMethod = method },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) primary.copy(0.05f) else surface,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, primary) else androidx.compose.foundation.BorderStroke(1.dp, onSurf.copy(0.05f))
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = if (isSelected) primary.copy(0.1f) else surfVar) {
                            Icon(icon, null, tint = if (isSelected) primary else onSurf.copy(0.5f), modifier = Modifier.padding(10.dp).size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(method, fontWeight = FontWeight.Bold, color = onSurf, fontSize = 16.sp)
                            Text(subtitle, fontSize = 12.sp, color = onSurf.copy(0.5f))
                        }
                        RadioButton(selected = isSelected, onClick = { selectedMethod = method }, colors = RadioButtonDefaults.colors(selectedColor = primary))
                    }
                }
            }

            // Shield Escrow info
            Surface(shape = RoundedCornerShape(14.dp), color = ElectricTeal.copy(0.08f)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, null, tint = ElectricTeal, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Your payment is protected. Funds released to pro only after job completion.", fontSize = 12.sp, color = ElectricTeal)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color? = null, onSurf: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = onSurf.copy(0.5f), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = valueColor ?: onSurf)
    }
}
