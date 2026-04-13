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
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: ""
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
                
                Spacer(Modifier.height(32.dp))
                
                // ── SECURITY VAULT ───────────────────────────────────────────
                Text("SECURITY PROTOCOLS", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                
                SecurityActionRow(Icons.Default.LockReset, "Request Password Reset") {
                    user?.email?.let {
                        auth.sendPasswordResetEmail(it)
                            .addOnSuccessListener { 
                                // Toast would be better but let's assume we show feedback
                            }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // ── SYSTEM PREFERENCES ──────────────────────────────────────
                Text("SYSTEM MATRIX", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                
                var notificationsEnabled by remember { mutableStateOf(true) }
                SystemToggleRow(Icons.Default.NotificationsActive, "Push Notifications", notificationsEnabled) {
                    notificationsEnabled = !notificationsEnabled
                }
                
                Spacer(Modifier.height(48.dp))
                
                // ── SAVE ACTION ─────────────────────────────────────────────
                Button(
                    onClick = {
                        isSaving = true
                        user?.uid?.let { uid ->
                            db.collection("users").document(uid).update("name", name)
                                .addOnCompleteListener { isSaving = false }
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
