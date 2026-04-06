package com.example.workly.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
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
// 1. PROFILE INFORMATION
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
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            try { val doc = db.collection("users").document(uid).get().await(); name = doc.getString("name") ?: user.displayName ?: ""; phone = doc.getString("phone") ?: "" } catch (_: Exception) {}
            isLoading = false
        }
    }
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Profile Information", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Update your personal details", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            if (isLoading) { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = primary) } } else {
                Text("Full Name", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f)); Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), placeholder = { Text("Enter your full name") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = primary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Email Address", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f)); Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = user?.email ?: "", onValueChange = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), enabled = false, leadingIcon = { Icon(Icons.Default.Email, null, tint = primary) }, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = onSurface.copy(alpha = 0.08f), disabledTextColor = onSurface.copy(alpha = 0.5f)))
                Text("Email can't be changed for security", fontSize = 11.sp, color = onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Phone Number", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f)); Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = phone, onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), placeholder = { Text("Enter 10-digit phone number") }, leadingIcon = { Text("+91", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold, color = primary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), singleLine = true)
                Spacer(modifier = Modifier.height(28.dp))
                Button(onClick = { if (name.isBlank()) { Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show(); return@Button }; isSaving = true; scope.launch { try { user?.updateProfile(userProfileChangeRequest { displayName = name })?.await(); user?.uid?.let { uid -> db.collection("users").document(uid).update(mapOf("name" to name, "phone" to phone)).await() }; Toast.makeText(context, "Profile updated! ✅", Toast.LENGTH_SHORT).show(); onDismiss() } catch (e: Exception) { Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show() }; isSaving = false } }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), enabled = !isSaving, colors = ButtonDefaults.buttonColors(containerColor = primary)) { if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
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
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Payment Methods", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Manage how you pay for services", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            PaymentMethodItem(Icons.Default.Money, "Cash on Delivery", "Pay when the service is completed", true, Color(0xFF4CAF50), primary, onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            PaymentMethodItem(Icons.Default.QrCode2, "UPI Payment", "GPay, PhonePe, Paytm supported", false, Color(0xFF7C3AED), primary, onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            PaymentMethodItem(Icons.Default.AccountBalanceWallet, "Workly Wallet", "Balance: ₹0.00", false, primary, primary, onSurface)
            Spacer(modifier = Modifier.height(24.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = primary.copy(alpha = 0.06f), border = BorderStroke(1.dp, primary.copy(alpha = 0.1f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = primary, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp))
                    Column { Text("Secure Payments", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = onSurface); Text("All payments are encrypted and processed securely.", fontSize = 11.sp, color = onSurface.copy(alpha = 0.6f)) }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(icon: ImageVector, title: String, subtitle: String, isDefault: Boolean, tint: Color, primary: Color, onSurface: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = if (isDefault) BorderStroke(1.5.dp, primary.copy(alpha = 0.3f)) else BorderStroke(1.dp, onSurface.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) } }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface); Text(subtitle, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f)) }
            if (isDefault) { Surface(shape = RoundedCornerShape(8.dp), color = primary.copy(alpha = 0.1f)) { Text("Default", color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) } }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. SECURITY
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySheet(onDismiss: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser; val context = LocalContext.current
    var currentPassword by remember { mutableStateOf("") }; var newPassword by remember { mutableStateOf("") }; var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }; var errorMessage by remember { mutableStateOf<String?>(null) }
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
            Text("Security", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Manage your password and account security", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Shield, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Account Protected", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4CAF50)) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user?.email ?: "N/A"}", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f))
                    Text("Created: ${user?.metadata?.creationTimestamp?.let { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)) } ?: "N/A"}", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(24.dp)); Text("Change Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface); Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it; errorMessage = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Current Password") }, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null, tint = primary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; errorMessage = null }, modifier = Modifier.fillMaxWidth(), label = { Text("New Password") }, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = primary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; errorMessage = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Confirm New Password") }, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = primary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), singleLine = true)
            if (errorMessage != null) { Spacer(modifier = Modifier.height(8.dp)); Text(errorMessage!!, color = Color(0xFFD32F2F), fontSize = 13.sp) }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { when { currentPassword.isEmpty() -> errorMessage = "Enter current password"; newPassword.length < 6 -> errorMessage = "Min 6 characters"; newPassword != confirmPassword -> errorMessage = "Passwords do not match"; else -> { isLoading = true; val cred = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword); user?.reauthenticate(cred)?.addOnSuccessListener { user.updatePassword(newPassword).addOnSuccessListener { Toast.makeText(context, "Password changed! 🔒", Toast.LENGTH_SHORT).show(); isLoading = false; onDismiss() }.addOnFailureListener { e -> errorMessage = e.message; isLoading = false } }?.addOnFailureListener { errorMessage = "Current password is incorrect"; isLoading = false } } } }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = primary)) { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. NOTIFICATIONS
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    var bookingUpdates by remember { mutableStateOf(true) }; var promotions by remember { mutableStateOf(true) }
    var chatMessages by remember { mutableStateOf(true) }; var paymentAlerts by remember { mutableStateOf(true) }
    var providerUpdates by remember { mutableStateOf(false) }; var emailNotifs by remember { mutableStateOf(true) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
            Text("Notifications", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Control what alerts you receive", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp)); Text("Push Notifications", fontSize = 14.sp, fontWeight = FontWeight.Black, color = onSurface.copy(alpha = 0.5f)); Spacer(modifier = Modifier.height(8.dp))
            NotifToggle("Booking Updates", "Order confirmations, status changes", bookingUpdates, onSurface, primary) { bookingUpdates = it }
            NotifToggle("Promotions & Offers", "Discounts, seasonal deals", promotions, onSurface, primary) { promotions = it }
            NotifToggle("Chat Messages", "New messages from providers", chatMessages, onSurface, primary) { chatMessages = it }
            NotifToggle("Payment Alerts", "Transaction confirmations", paymentAlerts, onSurface, primary) { paymentAlerts = it }
            NotifToggle("Provider Updates", "When your pro is en route", providerUpdates, onSurface, primary) { providerUpdates = it }
            Spacer(modifier = Modifier.height(20.dp)); Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Black, color = onSurface.copy(alpha = 0.5f)); Spacer(modifier = Modifier.height(8.dp))
            NotifToggle("Email Receipts", "Booking receipts and weekly summaries", emailNotifs, onSurface, primary) { emailNotifs = it }
        }
    }
}

@Composable
private fun NotifToggle(title: String, subtitle: String, checked: Boolean, onSurface: Color, primary: Color, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface); Text(subtitle, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f)) }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primary, uncheckedThumbColor = onSurface.copy(alpha = 0.3f), uncheckedTrackColor = onSurface.copy(alpha = 0.08f)))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 5. LANGUAGE
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    var selectedLanguage by remember { mutableStateOf("English (India)") }
    val languages = listOf("English (India)" to "🇮🇳", "हिन्दी" to "🇮🇳", "ગુજરાતી" to "🇮🇳", "मराठी" to "🇮🇳", "தமிழ்" to "🇮🇳", "తెలుగు" to "🇮🇳", "ಕನ್ನಡ" to "🇮🇳", "বাংলা" to "🇮🇳")
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Language", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(20.dp))
            languages.forEach { (name, flag) -> val isSelected = selectedLanguage == name
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { selectedLanguage = name }.background(if (isSelected) primary.copy(alpha = 0.08f) else Color.Transparent).padding(vertical = 14.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(flag, fontSize = 22.sp); Spacer(modifier = Modifier.width(14.dp)); Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp, color = onSurface, modifier = Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = primary, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 6. ADDRESSES
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesSheet(onDismiss: () -> Unit) {
    val db = FirebaseFirestore.getInstance(); val user = FirebaseAuth.getInstance().currentUser
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    val context = LocalContext.current; val scope = rememberCoroutineScope()
    data class Addr(val label: String, val address: String, val icon: ImageVector)
    var addresses by remember { mutableStateOf(listOf(Addr("Home", "Add your home address", Icons.Default.Home), Addr("Work", "Add your work address", Icons.Default.Business))) }
    var showAdd by remember { mutableStateOf(false) }; var newLabel by remember { mutableStateOf("") }; var newAddr by remember { mutableStateOf("") }
    LaunchedEffect(user?.uid) { user?.uid?.let { uid -> db.collection("users").document(uid).collection("addresses").addSnapshotListener { snap, _ -> snap?.let { val f = it.documents.mapNotNull { d -> val l = d.getString("label") ?: return@mapNotNull null; val a = d.getString("address") ?: return@mapNotNull null; Addr(l, a, when(d.getString("type")) { "home" -> Icons.Default.Home; "work" -> Icons.Default.Business; else -> Icons.Default.LocationOn }) }; if (f.isNotEmpty()) addresses = f } } } }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Saved Addresses", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface); Surface(onClick = { showAdd = true }, shape = RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.1f)) { Icon(Icons.Default.Add, null, tint = primary, modifier = Modifier.padding(8.dp).size(20.dp)) } }
            Spacer(modifier = Modifier.height(20.dp))
            addresses.forEach { addr -> Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(addr.icon, null, tint = primary, modifier = Modifier.size(22.dp)) } }; Spacer(modifier = Modifier.width(14.dp)); Column(modifier = Modifier.weight(1f)) { Text(addr.label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface); Text(addr.address, fontSize = 12.sp, color = onSurface.copy(alpha = 0.5f)) } }
            } }
            if (showAdd) { Spacer(modifier = Modifier.height(16.dp)); Text("Add New Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface); Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = newLabel, onValueChange = { newLabel = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Label") }, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = newAddr, onValueChange = { newAddr = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Full Address") }, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface), maxLines = 3)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { if (newLabel.isNotBlank() && newAddr.isNotBlank()) { scope.launch { user?.uid?.let { uid -> db.collection("users").document(uid).collection("addresses").add(mapOf("label" to newLabel, "address" to newAddr, "type" to "other")) }; Toast.makeText(context, "Address saved! 📍", Toast.LENGTH_SHORT).show(); newLabel = ""; newAddr = ""; showAdd = false } } }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = primary)) { Text("Save Address", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 7. SUPPORT
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary; val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
            Text("Help & Support", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("We're here to help 24/7", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            listOf(Triple(Icons.Default.Email, "Email Support") { try { context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:support@workly.in"); putExtra(Intent.EXTRA_SUBJECT, "Help Request") }) } catch (_: ActivityNotFoundException) { Toast.makeText(context, "No email app", Toast.LENGTH_SHORT).show() } },
                Triple(Icons.Default.Phone, "Call Us") { context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:+917600000000") }) },
                Triple(Icons.Default.Chat, "Live Chat") { Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show() }
            ).forEach { (icon, label, action) ->
                Surface(onClick = { action() }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(12.dp), color = primary.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = primary, modifier = Modifier.size(22.dp)) } }; Spacer(modifier = Modifier.width(14.dp)); Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface, modifier = Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = onSurface.copy(alpha = 0.3f), modifier = Modifier.size(20.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(20.dp)); Text("FAQs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface); Spacer(modifier = Modifier.height(12.dp))
            listOf("How do I book a service?" to "Go to Explore, select a service, choose date & time, and click 'Book Now'.", "Can I cancel my booking?" to "Yes, up to 2 hours before for a full refund.", "How are providers verified?" to "Background checks, skill verification, and minimum 4.0★ rating.", "What if I'm not satisfied?" to "100% satisfaction guarantee — we'll fix it or refund you.", "How do payments work?" to "Cash, UPI, or Workly Wallet. All encrypted and secure.").forEach { (q, a) -> var exp by remember { mutableStateOf(false) }
                Surface(onClick = { exp = !exp }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) { Column(modifier = Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(q, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurface, modifier = Modifier.weight(1f)); Icon(if (exp) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = onSurface.copy(alpha = 0.5f)) }; if (exp) { Spacer(modifier = Modifier.height(8.dp)); Text(a, fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f), lineHeight = 20.sp) } } }
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
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
            Text("Privacy Policy", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Text("Last updated: April 2026", fontSize = 13.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))
            listOf("1. Information We Collect" to "We collect name, email, phone, address, and device info to improve service delivery.", "2. How We Use Your Data" to "To provide services, match professionals, process payments, and personalize your experience.", "3. Data Sharing" to "Limited info shared with providers to fulfill bookings. We never sell your data.", "4. Data Security" to "Industry-standard AES-256 encryption and TLS 1.3. Regular security audits.", "5. Your Rights" to "Access, correct, delete, or export your data anytime. Contact support@workly.in.", "6. Changes" to "We notify you of policy changes via email or in-app notification.").forEach { (t, c) ->
                Text(t, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface); Spacer(modifier = Modifier.height(6.dp)); Text(c, fontSize = 14.sp, color = onSurface.copy(alpha = 0.7f), lineHeight = 22.sp); Spacer(modifier = Modifier.height(20.dp))
            }
            Text("Contact: support@workly.in", fontSize = 12.sp, color = primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 9. RATE WORKLY
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateWorklySheet(onDismiss: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary; val context = LocalContext.current
    var rating by remember { mutableIntStateOf(0) }; var feedback by remember { mutableStateOf("") }; var submitted by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (submitted) { Spacer(modifier = Modifier.height(24.dp)); Surface(shape = CircleShape, color = Color(0xFF4CAF50).copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Favorite, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp)) } }; Spacer(modifier = Modifier.height(20.dp)); Text("Thank You! ❤️", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = onSurface); Spacer(modifier = Modifier.height(8.dp)); Text("Your feedback helps us improve!", fontSize = 14.sp, color = onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center); Spacer(modifier = Modifier.height(24.dp)); Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = primary)) { Text("Done", fontWeight = FontWeight.Bold) }
            } else {
                Text("Rate Workly", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurface)
                Spacer(modifier = Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { (1..5).forEach { s -> Icon(if (s <= rating) Icons.Default.Star else Icons.Default.StarBorder, "$s star", tint = if (s <= rating) Color(0xFFF5A623) else onSurface.copy(alpha = 0.25f), modifier = Modifier.size(44.dp).clickable { rating = s }) } }
                Spacer(modifier = Modifier.height(8.dp)); Text(when(rating) { 1->"Poor 😞"; 2->"Fair 😐"; 3->"Good 🙂"; 4->"Great 😊"; 5->"Excellent! 🤩"; else->"Tap to rate" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (rating > 0) primary else onSurface.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = feedback, onValueChange = { feedback = it }, modifier = Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("What could we improve?", color = onSurface.copy(alpha = 0.4f)) }, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary, unfocusedBorderColor = onSurface.copy(alpha = 0.1f), focusedTextColor = onSurface, unfocusedTextColor = onSurface))
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { if (rating > 0) { FirebaseAuth.getInstance().currentUser?.uid?.let { uid -> FirebaseFirestore.getInstance().collection("ratings").add(mapOf("userId" to uid, "rating" to rating, "feedback" to feedback, "timestamp" to com.google.firebase.Timestamp.now())) }; submitted = true } else Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = primary)) { Text("Submit Review", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
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
    val onSurface = MaterialTheme.colorScheme.onSurface; val primary = MaterialTheme.colorScheme.primary
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(24.dp), color = primary, modifier = Modifier.size(80.dp), shadowElevation = 12.dp) { Box(contentAlignment = Alignment.Center) { Text("W", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black) } }
            Spacer(modifier = Modifier.height(16.dp)); Text("Workly", fontSize = 28.sp, fontWeight = FontWeight.Black, color = onSurface); Text("Home Services Marketplace", fontSize = 14.sp, color = onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp)); Surface(shape = RoundedCornerShape(8.dp), color = primary.copy(alpha = 0.1f)) { Text("Version 1.0.4 Premium", color = primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
            Spacer(modifier = Modifier.height(28.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) { Column(modifier = Modifier.padding(20.dp)) { Text("Our Mission", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface); Spacer(modifier = Modifier.height(8.dp)); Text("Workly connects you with verified professionals for all your home service needs — quality, safety, and convenience at your fingertips.", fontSize = 14.sp, color = onSurface.copy(alpha = 0.7f), lineHeight = 22.sp) } }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { listOf("10K+" to "Users", "500+" to "Providers", "25K+" to "Bookings").forEach { (v, l) -> Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) { Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(v, fontSize = 20.sp, fontWeight = FontWeight.Black, color = primary); Text(l, fontSize = 11.sp, color = onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold) } } } }
            Spacer(modifier = Modifier.height(20.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) { Column(modifier = Modifier.padding(20.dp)) { listOf("Developer" to "Rahul Parmar", "Built With" to "Kotlin, Compose, Firebase", "License" to "Proprietary Software", "Contact" to "support@workly.in").forEachIndexed { i, (k, v) -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(k, fontSize = 14.sp, color = onSurface.copy(alpha = 0.5f)); Text(v, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onSurface) }; if (i < 3) HorizontalDivider(color = onSurface.copy(alpha = 0.05f)) } } }
            Spacer(modifier = Modifier.height(20.dp)); Text("Made with ❤️ in India", fontSize = 13.sp, color = onSurface.copy(alpha = 0.4f)); Text("© 2026 Workly. All rights reserved.", fontSize = 11.sp, color = onSurface.copy(alpha = 0.3f))
        }
    }
}
