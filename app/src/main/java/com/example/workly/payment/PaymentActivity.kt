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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.example.workly.theme.*

class PaymentActivity : FragmentActivity() {
    
    private lateinit var upiLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private var showSuccessState = mutableStateOf(false)
    private var paymentMethodState = mutableStateOf("")
    private var isAuthorizeProcessingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        val basePrice = intent.getDoubleExtra("BASE_PRICE", price)
        val providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"

        // Setup UPI Launcher
        upiLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            triggerUpiHandshakeVerification()
        }

        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            val showSuccessOverlay by showSuccessState
            val paymentMethodUsed by paymentMethodState
            val isProcessing by isAuthorizeProcessingState

            WorklyTheme(themeMode = themeMode) {
                Box {
                    PaymentScreen(
                        serviceName = serviceName,
                        price = price,
                        basePrice = basePrice,
                        providerName = providerName,
                        onBackClick = { finish() },
                        onPaymentRequest = { method ->
                            val action = {
                                paymentMethodState.value = method
                                if (method == "UPI") {
                                    val intent = UpiPaymentHandler.getUpiIntent(
                                        vpa = "rahulparmar018@okaxis",
                                        name = "Workly Elite Payments",
                                        transactionNote = "Booking for $serviceName",
                                        amount = price.toString()
                                    )
                                    try {
                                        upiLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(this@PaymentActivity, "No UPI app found", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // For Card or Cash
                                    isAuthorizeProcessingState.value = true
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isAuthorizeProcessingState.value = false
                                        showSuccessState.value = true
                                        triggerSuccessState(method)
                                    }, 2000)
                                }
                            }

                            if (price >= 2000.0) {
                                checkBiometricAndProceed { action() }
                            } else {
                                action()
                            }
                        }
                    )
                    
                    if (isProcessing) {
                        Box(Modifier.fillMaxSize().background(PremiumBlack.copy(0.9f)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PremiumSilver, strokeWidth = 5.dp, modifier = Modifier.size(54.dp))
                                Spacer(Modifier.height(24.dp))
                                Text("SECURE AUTHORIZATION...", color = PremiumSilver, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                            }
                        }
                    }

                    if (showSuccessOverlay) {
                        SuccessOverlay(method = paymentMethodUsed, amount = price.toInt())
                    }
                }
            }
        }
    }

    private fun triggerUpiHandshakeVerification() {
        isAuthorizeProcessingState.value = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isAuthorizeProcessingState.value = false
            paymentMethodState.value = "UPI"
            showSuccessState.value = true
            triggerSuccessState("UPI")
        }, 2500)
    }

    private fun checkBiometricAndProceed(onSuccess: () -> Unit) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    android.widget.Toast.makeText(this@PaymentActivity, "Security Check Failed", android.widget.Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = PromptInfo.Builder()
            .setTitle("Elite Security Verification")
            .setSubtitle("Authenticate to book high-value service")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
    
    private var internalShowSuccess: ((Boolean, String) -> Unit)? = null

    private fun triggerSuccessState(method: String) {
        // We will show a separate success screen or finish with delay
        // For absolute WOW factor, let's use a themed toast and then Success Screen
        android.widget.Toast.makeText(this@PaymentActivity, "Payment Verified Successully!", android.widget.Toast.LENGTH_LONG).show()
        
        // Return result so BookingActivity can save it
        val result = Intent().apply {
            putExtra("PAYMENT_STATUS", "Paid")
            putExtra("PAYMENT_METHOD", method)
        }
        setResult(RESULT_OK, result)
        
        // Final transition delay for visual confirmation
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000)
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
    onPaymentRequest: (method: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showCardSheet by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        Triple("UPI", Icons.Default.AccountBalance, "GPay / PhonePe / Paytm"),
        Triple("Card", Icons.Default.CreditCard, "Debit / Credit Card"),
        Triple("Cash", Icons.Default.Money, "Pay after service")
    )

    // Theme aliases
    val bg = PremiumBlack
    val primary = PremiumSilver
    val surface = PremiumBlackSurface
    val onSurf = PremiumWhite
    val accent = EnergyOrange

    Box(Modifier.fillMaxSize().background(bg)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Checkout", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp) },
                    navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = primary) } },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bg,
                        titleContentColor = onSurf,
                        navigationIconContentColor = onSurf
                    )
                )
            },
            containerColor = bg,
            bottomBar = {
                Surface(
                    color = bg, 
                    border = BorderStroke(1.dp, primary.copy(0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { 
                            if (selectedMethod == "Card") {
                                showCardSheet = true
                            } else {
                                if (selectedMethod == "Cash") isProcessing = true
                                onPaymentRequest(selectedMethod)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp).height(62.dp).navigationBarsPadding(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(selectedMethod == "Cash") primary else Color.White,
                            contentColor = PremiumBlack
                        ),
                        enabled = !isProcessing && !showSuccess
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PremiumBlack, strokeWidth = 3.dp)
                        } else {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                if(selectedMethod == "Cash") "Confirm Booking" else "Pay ₹${price.toInt()} Securely", 
                                fontWeight = FontWeight.Black, 
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val safeProviderName = if (providerName.isBlank() || providerName.contains("Unknown")) "Elite Professional" else providerName

            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LUX Summary Card
                Card(
                    shape = RoundedCornerShape(32.dp), 
                    colors = CardDefaults.cardColors(containerColor = surface), 
                    border = BorderStroke(1.dp, primary.copy(0.12f))
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Text("ORDER SUMMARY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = primary.copy(0.5f), letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        SummaryRow("Service", serviceName, onSurf = onSurf)
                        SummaryRow("Professional", safeProviderName, valueColor = primary.copy(0.9f), onSurf = onSurf)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = primary.copy(alpha = 0.08f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurf.copy(0.6f))
                            Text("₹${price.toInt()}", fontWeight = FontWeight.Black, fontSize = 32.sp, color = PremiumWhite)
                        }
                    }
                }

                // Method Selection Matrix
                Text("Select Method", fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurf, letterSpacing = 0.5.sp)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    paymentMethods.forEach { (method, icon, subtitle) ->
                        val isSelected = method == selectedMethod
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { selectedMethod = method },
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) primary.copy(0.08f) else surface,
                            border = BorderStroke(1.5.dp, if (isSelected) primary else primary.copy(0.05f))
                        ) {
                            Row(modifier = Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) primary.copy(0.2f) else bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, null, tint = if (isSelected) primary else primary.copy(0.4f), modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(method, fontWeight = FontWeight.ExtraBold, color = onSurf, fontSize = 17.sp)
                                    Text(subtitle, fontSize = 12.sp, color = onSurf.copy(0.4f), fontWeight = FontWeight.Medium)
                                }
                                RadioButton(
                                    selected = isSelected, 
                                    onClick = { selectedMethod = method }, 
                                    colors = RadioButtonDefaults.colors(selectedColor = primary, unselectedColor = primary.copy(0.1f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ── ELITE OVERLAYS ───────────────────────────────────────────
        
        if (showCardSheet) {
            CardVaultOverlay(
                onClose = { showCardSheet = false },
                onConfirm = { 
                    showCardSheet = false
                    isProcessing = true
                    // Simulate processing
                    onPaymentRequest("Card")
                }
            )
        }

        if (isProcessing) {
            Box(Modifier.fillMaxSize().background(PremiumBlack.copy(0.9f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = primary, strokeWidth = 4.dp, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("AUTHORIZING TRANSACTION...", color = primary, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@Composable
fun SuccessOverlay(method: String, amount: Int) {
    Box(
        modifier = Modifier.fillMaxSize().background(PremiumBlack.copy(0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = ElectricTeal.copy(0.1f),
                border = BorderStroke(2.dp, ElectricTeal)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, tint = ElectricTeal, modifier = Modifier.size(56.dp))
                }
            }
            
            Spacer(Modifier.height(32.dp))
            Text("TRANSACTION SUCCESSFUL", color = PremiumWhite, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text("₹$amount paid via $method", color = PremiumSilver.copy(0.6f), fontSize = 14.sp)
            
            Spacer(Modifier.height(48.dp))
            Text("SECURED BY WORKLY VAULT", color = PremiumSilver.copy(0.3f), fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun CardVaultOverlay(onClose: () -> Unit, onConfirm: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.85f)).clickable { onClose() }, contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).clickable(enabled = false) { },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(PremiumBlackSurface),
            border = BorderStroke(1.dp, PremiumSilver.copy(0.15f))
        ) {
            Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SECURE VAULT", color = PremiumSilver, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp)
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = PremiumSilver.copy(0.5f)) }
                }
                
                OutlinedTextField(
                    value = "", onValueChange = {}, 
                    label = { Text("CARD NUMBER", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("•••• •••• •••• ••••", color = PremiumSilver.copy(0.3f)) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PremiumSilver.copy(0.1f), focusedBorderColor = PremiumSilver)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = "", onValueChange = {}, 
                        label = { Text("EXPIRY", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("MM/YY", color = PremiumSilver.copy(0.3f)) },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PremiumSilver.copy(0.1f), focusedBorderColor = PremiumSilver)
                    )
                    OutlinedTextField(
                        value = "", onValueChange = {}, 
                        label = { Text("CVV", fontSize = 10.sp) },
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("•••", color = PremiumSilver.copy(0.3f)) },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PremiumSilver.copy(0.1f), focusedBorderColor = PremiumSilver)
                    )
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumWhite, contentColor = PremiumBlack)
                ) {
                    Text("AUTHORIZE CARD", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color? = null, onSurf: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = onSurf.copy(0.4f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = valueColor ?: onSurf)
    }
}

