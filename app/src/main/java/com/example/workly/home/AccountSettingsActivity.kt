package com.example.workly.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
                AccountSettingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isPhoneVerified by remember { mutableStateOf(false) }
    var isRemovingPhone by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: ""
                    phone = doc.getString("phone") ?: ""
                    if (phone.isNotEmpty()) {
                        isPhoneVerified = true
                    }
                    notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Vault", fontWeight = FontWeight.ExtraBold, color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumSilver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        },
        containerColor = PremiumBlack
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumSilver)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ── IDENTITY NODE ────────────────────────────────────────────
                Text("ACCOUNT IDENTITY", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                
                EliteTextField(label = "Display Name", value = name, onValueChange = { name = it }, icon = Icons.Default.Person)
                Spacer(Modifier.height(16.dp))
                EliteTextField(label = "Email Address", value = email, onValueChange = { }, icon = Icons.Default.Email, enabled = false)
                Spacer(Modifier.height(16.dp))
                
                // PHONE OTP SECTION
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(modifier = Modifier.weight(1f)) {
                        EliteTextField(label = "Phone Number (+91)", value = phone, onValueChange = { phone = it; isPhoneVerified = false }, icon = Icons.Default.Phone, enabled = !isPhoneVerified)
                    }
                    if (!isPhoneVerified) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (phone.length < 10) {
                                    Toast.makeText(context, "Enter valid phone number.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                // Mock OTP Validation logic with Firestore duplicate check
                                val formattedPhone = if (phone.startsWith("+")) phone else "+91$phone"
                                
                                db.collection("users").whereEqualTo("phone", formattedPhone).get()
                                    .addOnSuccessListener { querySnapshot ->
                                        var isDuplicate = false
                                        for (doc in querySnapshot.documents) {
                                            if (doc.id != user?.uid) {
                                                isDuplicate = true
                                                break
                                            }
                                        }
                                        
                                        if (isDuplicate) {
                                            Toast.makeText(context, "This number is already registered to another account.", Toast.LENGTH_LONG).show()
                                        } else {
                                            val generatedOtp = (100000..999999).random().toString()
                                            verificationId = generatedOtp
                                            isOtpSent = true
                                            isRemovingPhone = false
                                            
                                            // Show OTP to User via genuine push notification
                                            sendSystemPushNotification(context, generatedOtp)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to verify network database.", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text("Send OTP", color = PremiumBlack, fontWeight = FontWeight.Bold)
                        }
                    } else if (phone.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showRemoveDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (showRemoveDialog) {
                    AlertDialog(
                        onDismissRequest = { showRemoveDialog = false },
                        title = { Text("Remove Authentication Phase") },
                        text = { Text("Are you sure you want to decouple your phone number? You must verify this action via OTP.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showRemoveDialog = false
                                val generatedOtp = (100000..999999).random().toString()
                                verificationId = generatedOtp
                                isOtpSent = true
                                isRemovingPhone = true
                                sendSystemPushNotification(context, generatedOtp)
                            }) {
                                Text("Proceed", color = Color(0xFFF44336))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRemoveDialog = false }) {
                                Text("Cancel", color = PremiumSilver)
                            }
                        },
                        containerColor = PremiumBlackSurface,
                        titleContentColor = Color.White,
                        textContentColor = Color.White
                    )
                }
                
                if (isOtpSent && (!isPhoneVerified || isRemovingPhone)) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Box(modifier = Modifier.weight(1f)) {
                            EliteTextField(label = "Enter OTP", value = otpCode, onValueChange = { otpCode = it }, icon = Icons.Default.Message)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (otpCode == verificationId) {
                                    if (isRemovingPhone) {
                                        phone = ""
                                        isPhoneVerified = false
                                        isRemovingPhone = false
                                        isOtpSent = false
                                        user?.uid?.let { uid ->
                                            db.collection("users").document(uid).update("phone", "")
                                        }
                                        Toast.makeText(context, "Phone Decoupled Successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isPhoneVerified = true
                                        isOtpSent = false
                                        val finalPhone = if (phone.startsWith("+")) phone else "+91$phone"
                                        phone = finalPhone
                                        user?.uid?.let { uid ->
                                            db.collection("users").document(uid).update("phone", finalPhone)
                                        }
                                        Toast.makeText(context, "Phone Authenticated & Locked!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Invalid OTP. Try again.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text("Verify", color = PremiumBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // ── SECURITY VAULT ───────────────────────────────────────────
                Text("SECURITY PROTOCOLS", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                
                SecurityActionRow(Icons.Default.LockReset, "Request Password Reset") {
                    user?.email?.let { e ->
                        auth.sendPasswordResetEmail(e)
                            .addOnSuccessListener { 
                                Toast.makeText(context, "Shield reset link sent to $e", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Security protocol failure.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // ── SYSTEM PREFERENCES ──────────────────────────────────────
                Text("SYSTEM MATRIX", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                
                SystemToggleRow(Icons.Default.NotificationsActive, "Push Notifications", notificationsEnabled) {
                    notificationsEnabled = !notificationsEnabled
                }
                
                Spacer(Modifier.height(48.dp))
                
                // ── SAVE ACTION ─────────────────────────────────────────────
                Button(
                    onClick = {
                        if (!isPhoneVerified && phone.isNotEmpty()) {
                            Toast.makeText(context, "Please verify your phone number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        user?.uid?.let { uid ->
                            val updates = mutableMapOf<String, Any>(
                                "name" to name,
                                "notificationsEnabled" to notificationsEnabled
                            )
                            if (isPhoneVerified) {
                                updates["phone"] = phone
                            }
                            db.collection("users").document(uid).update(updates)
                                .addOnCompleteListener { 
                                    isSaving = false
                                    Toast.makeText(context, "Elite Profile Synchronized", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = PremiumBlack, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SYNCHRONIZE CHANGES", color = PremiumBlack, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EliteTextField(label: String, value: String, onValueChange: (String) -> Unit, icon: ImageVector, enabled: Boolean = true) {
    Column {
        Text(label, color = PremiumSilver.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, null, tint = PremiumSilver) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = PremiumSilver,
                unfocusedBorderColor = PremiumSilver.copy(alpha = 0.2f),
                focusedContainerColor = PremiumBlackSurface,
                unfocusedContainerColor = PremiumBlackSurface,
                disabledContainerColor = PremiumBlackSurface
            )
        )
    }
}

@Composable
fun SecurityActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = PremiumSilver.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SystemToggleRow(icon: ImageVector, label: String, enabled: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = enabled, 
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PremiumBlack,
                    checkedTrackColor = PremiumSilver,
                    uncheckedThumbColor = PremiumSilver,
                    uncheckedTrackColor = PremiumBlackSurface
                )
            )
        }
    }
}

fun sendSystemPushNotification(context: android.content.Context, otp: String) {
    val channelId = "WorklyEliteOTP"
    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(channelId, "Elite System Notifications", android.app.NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Elite Authentication")
        .setContentText("Your verification OTP is: $otp")
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
}
