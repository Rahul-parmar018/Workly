package com.example.workly.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        PaymentOption("UPI", Icons.Default.AccountBalance, "Google Pay / PhonePe / Paytm", Color(0xFF7C3AED)),
        PaymentOption("Card", Icons.Default.CreditCard, "Debit / Credit Card", Color(0xFFE65100)),
        PaymentOption("Cash", Icons.Default.Money, "Pay after service", Color(0xFF2E7D32))
    )

    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = surface) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Secure Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("₹${price.toInt()} • $serviceName", fontSize = 12.sp, color = onSurf.copy(alpha = 0.5f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        Surface(shape = CircleShape, color = Color(0xFF4CAF50).copy(alpha = 0.1f), modifier = Modifier.padding(end = 12.dp)) {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(8.dp).size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = surface,
                        titleContentColor = onSurf,
                        navigationIconContentColor = onSurf
                    )
                )
            }
        },
        containerColor = bg,
        bottomBar = {
            Surface(
                color = surface,
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding()) {
                    // Trust badge
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("256-bit SSL Encrypted • PCI DSS Compliant", fontSize = 10.sp, color = onSurf.copy(alpha = 0.4f), fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { isLoading = true; onPaymentComplete(selectedMethod) },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        enabled = !isLoading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Pay ₹${price.toInt()} Securely", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Order Summary Card ──
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = surface,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, onSurf.copy(0.04f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.08f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Receipt, null, tint = primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Order Summary", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Service row
                    SummaryItemRow(
                        label = "Service",
                        value = serviceName,
                        icon = Icons.Default.CleaningServices,
                        onSurf = onSurf,
                        primary = primary
                    )

                    SummaryItemRow(
                        label = "Professional",
                        value = providerName,
                        icon = Icons.Default.Person,
                        onSurf = onSurf,
                        primary = primary,
                        valueColor = primary
                    )

                    if (basePrice != price) {
                        SummaryItemRow("Base Price", "₹${basePrice.toInt()}", Icons.Default.Sell, onSurf, primary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = onSurf.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Amount", fontSize = 12.sp, color = onSurf.copy(alpha = 0.5f))
                            Text("₹${price.toInt()}", fontWeight = FontWeight.Black, fontSize = 28.sp, color = primary)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF4CAF50).copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Protected", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            }

            // ── Payment Methods ──
            Text("Choose Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = onSurf)

            paymentMethods.forEach { option ->
                val isSelected = option.name == selectedMethod
                val animBorderColor by animateColorAsState(
                    targetValue = if (isSelected) primary else onSurf.copy(alpha = 0.06f),
                    animationSpec = tween(300), label = "border"
                )
                val animBg by animateColorAsState(
                    targetValue = if (isSelected) primary.copy(alpha = 0.04f) else surface,
                    animationSpec = tween(300), label = "bg"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMethod = option.name },
                    shape = RoundedCornerShape(20.dp),
                    color = animBg,
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, animBorderColor),
                    shadowElevation = if (isSelected) 6.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon with accent color
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = option.tint.copy(alpha = 0.1f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(option.icon, null, tint = option.tint, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(option.name, fontWeight = FontWeight.Bold, color = onSurf, fontSize = 16.sp)
                            Text(option.subtitle, fontSize = 12.sp, color = onSurf.copy(0.5f))
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMethod = option.name },
                            colors = RadioButtonDefaults.colors(selectedColor = primary, unselectedColor = onSurf.copy(alpha = 0.2f))
                        )
                    }
                }
            }

            // ── Escrow Protection Banner ──
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF00897B).copy(alpha = 0.06f),
                border = BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(shape = CircleShape, color = Color(0xFF00897B).copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Security, null, tint = Color(0xFF00897B), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Escrow Protection", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00897B))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Your payment is held securely. Funds are released to the professional only after the job is completed to your satisfaction.",
                            fontSize = 12.sp,
                            color = onSurf.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class PaymentOption(val name: String, val icon: ImageVector, val subtitle: String, val tint: Color)

@Composable
fun SummaryItemRow(label: String, value: String, icon: ImageVector, onSurf: Color, primary: Color, valueColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = onSurf.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = onSurf.copy(0.5f), fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = valueColor ?: onSurf)
    }
}
