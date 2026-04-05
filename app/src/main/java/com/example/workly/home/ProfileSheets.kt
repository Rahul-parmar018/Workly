package com.example.workly.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ═══════════════════════════════════════════════════════════════════════════════
// 1. PROFILE INFORMATION — Edit name, email, phone with Firebase sync
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoSheet(onDismiss: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Fetch from Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            try {
                val doc = db.collection("users").document(uid).get().await()
                name = doc.getString("name") ?: user.displayName ?: ""
                phone = doc.getString("phone") ?: ""
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text("Profile Information", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Update your personal details", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary)
                }
            } else {
                // Name
                Text("Full Name", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Enter your full name") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = primary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = onSurface.copy(alpha = 0.1f),
                        focusedTextColor = onSurface,
                        unfocusedTextColor = onSurface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email (read-only)
                Text("Email Address", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = user?.email ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = primary) },
                    trailingIcon = { Icon(Icons.Default.Lock, null, tint = onSurface.copy(alpha = 0.3f), modifier = Modifier.size(16.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = onSurface.copy(alpha = 0.08f),
                        disabledTextColor = onSurface.copy(alpha = 0.5f)
                    )
                )
                Text("Email can't be changed for security", fontSize = 11.sp, color = onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(start = 4.dp, top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // Phone
                Text("Phone Number", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Enter 10-digit phone number") },
                    leadingIcon = { Text("+91", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold, color = primary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = onSurface.copy(alpha = 0.1f),
                        focusedTextColor = onSurface,
                        unfocusedTextColor = onSurface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Save button
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                // Update Firebase Auth display name
                                user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
                                // Update Firestore
                                user?.uid?.let { uid ->
                                    db.collection("users").document(uid).update(
                                        mapOf("name" to name, "phone" to phone)
                                    ).await()
                                }
                                Toast.makeText(context, "Profile updated successfully! ✅", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. PAYMENT METHODS
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text("Payment Methods", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Manage how you pay for services", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            // Cash on Delivery
            PaymentMethodItem(
                icon = Icons.Default.Money,
                title = "Cash on Delivery",
                subtitle = "Pay when the service is completed",
                isDefault = true,
                tint = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // UPI
            PaymentMethodItem(
                icon = Icons.Default.QrCode2,
                title = "UPI Payment",
                subtitle = "GPay, PhonePe, Paytm supported",
                isDefault = false,
                tint = Color(0xFF7C3AED)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wallet
            PaymentMethodItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Workly Wallet",
                subtitle = "Balance: ₹0.00",
                isDefault = false,
                tint = primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = primary.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Secure Payments", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = onSurface)
                        Text("All payments are encrypted and processed securely through our verified payment partners.", fontSize = 11.sp, color = onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(icon: ImageVector, title: String, subtitle: String, isDefault: Boolean, tint: Color) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isDefault) BorderStroke(1.5.dp, primary.copy(alpha = 0.3f)) else BorderStroke(1.dp, onSurface.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface)
                Text(subtitle, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f))
            }
            if (isDefault) {
                Surface(shape = RoundedCornerShape(8.dp), color = primary.copy(alpha = 0.1f)) {
                    Text("Default", color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. SECURITY — Change password with Firebase Auth
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySheet(onDismiss: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Security", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Manage your password and account security", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            // Account Info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Account Protected", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4CAF50))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user?.email ?: "Not available"}", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f))
                    Text("Account created: ${user?.metadata?.creationTimestamp?.let { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)) } ?: "N/A"}", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f))
                    Text("Last sign-in: ${user?.metadata?.lastSignInTimestamp?.let { java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(it)) } ?: "N/A"}", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Change Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current Password") },
                shape = RoundedCornerShape(14.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = primary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New Password") },
                shape = RoundedCornerShape(14.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = primary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm New Password") },
                shape = RoundedCornerShape(14.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = primary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = Color(0xFFD32F2F), fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    when {
                        currentPassword.isEmpty() -> errorMessage = "Enter your current password"
                        newPassword.length < 6 -> errorMessage = "New password must be at least 6 characters"
                        newPassword != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> {
                            isLoading = true
                            val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
                            user?.reauthenticate(credential)?.addOnSuccessListener {
                                user.updatePassword(newPassword).addOnSuccessListener {
                                    Toast.makeText(context, "Password changed successfully! 🔒", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                    onDismiss()
                                }.addOnFailureListener { e ->
                                    errorMessage = e.message ?: "Failed to update password"
                                    isLoading = false
                                }
                            }?.addOnFailureListener {
                                errorMessage = "Current password is incorrect"
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. NOTIFICATION SETTINGS
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    var bookingUpdates by remember { mutableStateOf(true) }
    var promotions by remember { mutableStateOf(true) }
    var chatMessages by remember { mutableStateOf(true) }
    var paymentAlerts by remember { mutableStateOf(true) }
    var providerUpdates by remember { mutableStateOf(false) }
    var emailNotifs by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Notifications", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Control what alerts you receive", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            Text("Push Notifications", fontSize = 14.sp, fontWeight = FontWeight.Black, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            NotificationToggle("Booking Updates", "Order confirmations, status changes, completions", bookingUpdates) { bookingUpdates = it }
            NotificationToggle("Promotions & Offers", "Discounts, seasonal deals, referral bonuses", promotions) { promotions = it }
            NotificationToggle("Chat Messages", "New messages from providers or support", chatMessages) { chatMessages = it }
            NotificationToggle("Payment Alerts", "Transaction confirmations and refund updates", paymentAlerts) { paymentAlerts = it }
            NotificationToggle("Provider Updates", "When your assigned pro is en route or arrives", providerUpdates) { providerUpdates = it }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Email Notifications", fontSize = 14.sp, fontWeight = FontWeight.Black, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            NotificationToggle("Email Receipts & Summary", "Receive booking receipts and weekly summaries", emailNotifs) { emailNotifs = it }
        }
    }
}

@Composable
private fun NotificationToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface)
            Text(subtitle, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primary,
                uncheckedThumbColor = onSurface.copy(alpha = 0.3f),
                uncheckedTrackColor = onSurface.copy(alpha = 0.08f)
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 5. LANGUAGE SELECTOR
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var selectedLanguage by remember { mutableStateOf("English (India)") }

    val languages = listOf(
        Triple("English (India)", "English", "🇮🇳"),
        Triple("हिन्दी", "Hindi", "🇮🇳"),
        Triple("ગુજરાતી", "Gujarati", "🇮🇳"),
        Triple("मराठी", "Marathi", "🇮🇳"),
        Triple("தமிழ்", "Tamil", "🇮🇳"),
        Triple("తెలుగు", "Telugu", "🇮🇳"),
        Triple("ಕನ್ನಡ", "Kannada", "🇮🇳"),
        Triple("বাংলা", "Bengali", "🇮🇳")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text("Language", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Choose your preferred language", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            languages.forEach { (name, subtitle, flag) ->
                val isSelected = selectedLanguage == name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            selectedLanguage = name
                        }
                        .background(if (isSelected) primary.copy(alpha = 0.08f) else Color.Transparent)
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(flag, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp, color = onSurface)
                        Text(subtitle, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f))
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, null, tint = primary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 6. SAVED ADDRESSES
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesSheet(onDismiss: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    data class SavedAddress(val label: String, val address: String, val icon: ImageVector)

    var addresses by remember {
        mutableStateOf(
            listOf(
                SavedAddress("Home", "Add your home address", Icons.Default.Home),
                SavedAddress("Work", "Add your work address", Icons.Default.Business)
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }

    // Fetch from Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).collection("addresses")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val fetched = snapshot.documents.mapNotNull { doc ->
                            val label = doc.getString("label") ?: return@mapNotNull null
                            val addr = doc.getString("address") ?: return@mapNotNull null
                            val iconType = doc.getString("type") ?: "other"
                            SavedAddress(label, addr, when (iconType) {
                                "home" -> Icons.Default.Home
                                "work" -> Icons.Default.Business
                                else -> Icons.Default.LocationOn
                            })
                        }
                        if (fetched.isNotEmpty()) addresses = fetched
                    }
                }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Saved Addresses", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
                    Text("Manage your delivery locations", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
                }
                Surface(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = primary.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.Add, null, tint = primary, modifier = Modifier.padding(8.dp).size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            addresses.forEach { addr ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, onSurface.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(addr.icon, null, tint = primary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(addr.label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface)
                            Text(addr.address, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f), maxLines = 2)
                        }
                        Icon(Icons.Default.Edit, null, tint = onSurface.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (showAddDialog) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Add New Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Label (e.g., Home, Office)") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newAddress,
                    onValueChange = { newAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full Address") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (newLabel.isNotBlank() && newAddress.isNotBlank()) {
                            scope.launch {
                                user?.uid?.let { uid ->
                                    db.collection("users").document(uid).collection("addresses").add(
                                        mapOf("label" to newLabel, "address" to newAddress, "type" to "other")
                                    )
                                }
                                Toast.makeText(context, "Address saved! 📍", Toast.LENGTH_SHORT).show()
                                newLabel = ""; newAddress = ""; showAddDialog = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Save Address", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 7. SUPPORT / HELP CENTER
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Help & Support", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("We're here to help 24/7", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            // Quick contact options
            val contactOptions = listOf(
                Triple(Icons.Default.Email, "Email Support") {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@workly.in")
                        putExtra(Intent.EXTRA_SUBJECT, "Help Request from Workly App")
                    }
                    try { context.startActivity(intent) } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                    }
                },
                Triple(Icons.Default.Phone, "Call Us") {
                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:+917600000000") }
                    context.startActivity(intent)
                },
                Triple(Icons.Default.Chat, "Live Chat") {
                    Toast.makeText(context, "Live chat coming soon!", Toast.LENGTH_SHORT).show()
                }
            )

            contactOptions.forEach { (icon, label, action) ->
                Surface(
                    onClick = { action() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = primary, modifier = Modifier.size(22.dp)) }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = onSurface.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("FAQs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            val faqs = listOf(
                "How do I book a service?" to "Go to the Explore tab, select a service, choose your date & time, and click 'Book Now'. You'll be matched with verified professionals.",
                "Can I cancel my booking?" to "Yes, you can cancel up to 2 hours before the scheduled time for a full refund. Late cancellations may incur a small fee.",
                "How are providers verified?" to "All Workly professionals undergo background checks, skill verification, and maintain a minimum 4.0★ rating to stay active.",
                "What if I'm not satisfied?" to "Contact our support team within 24 hours. We offer a 100% satisfaction guarantee — we'll fix it or refund you.",
                "How do payments work?" to "We support Cash on Delivery, UPI (GPay, PhonePe, Paytm), and Workly Wallet. All transactions are encrypted and secure."
            )

            faqs.forEach { (question, answer) ->
                var expanded by remember { mutableStateOf(false) }
                Surface(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(question, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurface, modifier = Modifier.weight(1f))
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = onSurface.copy(alpha = 0.5f))
                        }
                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(answer, fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f), lineHeight = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 8. PRIVACY POLICY
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicySheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Privacy Policy", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Last updated: April 2026", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            val sections = listOf(
                "1. Information We Collect" to "We collect information you provide when creating an account (name, email, phone number), booking services (address, preferences), and payment details. We also collect device information and location data to improve service delivery.",
                "2. How We Use Your Data" to "Your data is used to: provide and improve our services, match you with verified professionals, process payments securely, send booking confirmations and updates, and personalize your experience.",
                "3. Data Sharing" to "We share limited information with service providers (your name, address, booking details) only to fulfill your bookings. We never sell your personal data to third parties. We may share anonymized analytics data to improve our platform.",
                "4. Data Security" to "We use industry-standard encryption (AES-256) and secure protocols (TLS 1.3) to protect your data. All payment transactions are processed through PCI DSS-compliant partners. We conduct regular security audits.",
                "5. Your Rights" to "You have the right to: access your personal data, correct inaccurate information, delete your account and data, export your data, and opt out of marketing communications. Contact support@workly.in for any requests.",
                "6. Cookies & Tracking" to "We use cookies and similar technologies to maintain your session, remember preferences, and improve our services. You can manage cookie preferences in your device settings.",
                "7. Children's Privacy" to "Workly is not intended for users under 18 years of age. We do not knowingly collect data from minors. If you believe a minor is using our service, please contact us immediately.",
                "8. Changes to Policy" to "We may update this privacy policy from time to time. We will notify you of significant changes via email or in-app notification. Continued use of Workly after changes constitutes acceptance."
            )

            sections.forEach { (title, content) ->
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(content, fontSize = 14.sp, color = onSurface.copy(alpha = 0.7f), lineHeight = 22.sp)
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text("Contact: support@workly.in | +91 76000 00000", fontSize = 12.sp, color = primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 9. RATE WORKLY
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateWorklySheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val context = LocalContext.current

    var selectedRating by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (submitted) {
                // Thank you state
                Spacer(modifier = Modifier.height(24.dp))
                Surface(shape = CircleShape, color = Color(0xFF4CAF50).copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Favorite, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Thank You! ❤️", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your feedback helps us make Workly better for everyone. We truly appreciate your support!", fontSize = 14.sp, color = onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = primary)) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Rate Workly", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
                Text("Tell us about your experience", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(28.dp))

                // Star rating
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            if (star <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$star star",
                            tint = if (star <= selectedRating) Color(0xFFF5A623) else onSurface.copy(alpha = 0.25f),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { selectedRating = star }
                        )
                    }
                }

                val ratingText = when (selectedRating) {
                    1 -> "Poor 😞"; 2 -> "Fair 😐"; 3 -> "Good 🙂"; 4 -> "Great 😊"; 5 -> "Excellent! 🤩"; else -> "Tap to rate"
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(ratingText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (selectedRating > 0) primary else onSurface.copy(alpha = 0.4f))

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("What could we improve? (optional)", color = onSurface.copy(alpha = 0.4f)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (selectedRating > 0) {
                            // Save to Firestore
                            val user = FirebaseAuth.getInstance().currentUser
                            user?.uid?.let { uid ->
                                FirebaseFirestore.getInstance().collection("ratings").add(
                                    mapOf(
                                        "userId" to uid,
                                        "rating" to selectedRating,
                                        "feedback" to feedback,
                                        "timestamp" to com.google.firebase.Timestamp.now()
                                    )
                                )
                            }
                            submitted = true
                        } else {
                            Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Submit Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 10. ABOUT WORKLY
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutWorklySheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo placeholder
            Surface(shape = RoundedCornerShape(24.dp), color = primary, modifier = Modifier.size(80.dp), shadowElevation = 12.dp) {
                Box(contentAlignment = Alignment.Center) {
                    Text("W", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Workly", fontSize = 28.sp, fontWeight = FontWeight.Black, color = onSurface)
            Text("Home Services Marketplace", fontSize = 14.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))

            Surface(shape = RoundedCornerShape(8.dp), color = primary.copy(alpha = 0.1f)) {
                Text("Version 1.0.4 Premium", color = primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Mission
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Our Mission", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Workly connects you with verified, trusted professionals for all your home service needs. From cleaning to repairs, we ensure quality, safety, and convenience — all at your fingertips.",
                        fontSize = 14.sp,
                        color = onSurface.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "10K+" to "Users",
                    "500+" to "Providers",
                    "25K+" to "Bookings"
                ).forEach { (value, label) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = primary)
                            Text(label, fontSize = 11.sp, color = onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Team & Legal
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val details = listOf(
                        "Developer" to "Rahul Parmar",
                        "Built With" to "Kotlin, Jetpack Compose, Firebase",
                        "Backend" to "Node.js, MongoDB, Express",
                        "License" to "Proprietary Software",
                        "Contact" to "support@workly.in"
                    )
                    details.forEachIndexed { index, (key, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(key, fontSize = 14.sp, color = onSurface.copy(alpha = 0.5f))
                            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onSurface)
                        }
                        if (index < details.size - 1) {
                            HorizontalDivider(color = onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Made with ❤️ in India", fontSize = 13.sp, color = onSurface.copy(alpha = 0.4f))
            Text("© 2026 Workly. All rights reserved.", fontSize = 11.sp, color = onSurface.copy(alpha = 0.3f))
        }
    }
}
