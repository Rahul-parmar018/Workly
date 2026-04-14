package com.example.workly.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.firestore.FirebaseFirestore

class UserDirectoryActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeDataStore = remember { ThemeDataStore(this) }
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())

            WorklyTheme(themeMode = themeMode) {
                NetworkOversightScreen(
                    onBack = { finish() },
                    onUpdateRole = { uid, role -> updateRole(uid, role) }
                )
            }
        }
    }

    private fun updateRole(uid: String, role: String) {
        db.collection("users").document(uid).update("role", role)
            .addOnSuccessListener {
                Toast.makeText(this, "Identity authority updated to: $role", Toast.LENGTH_SHORT).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkOversightScreen(onBack: () -> Unit, onUpdateRole: (String, String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<AdminUserData>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("all") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snap, _ ->
            snap?.let {
                users = it.documents.mapNotNull { doc ->
                    doc.toObject(AdminUserData::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            }
        }
    }

    val filteredList = users.filter { 
        (it.name.contains(searchQuery, true) || it.email.contains(searchQuery, true)) &&
        (selectedRole == "all" || it.role.equals(selectedRole, true))
    }

    Scaffold(
        containerColor = PremiumBlack,
        topBar = {
            TopAppBar(
                title = { Text("NETWORK OVERSIGHT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumSilver) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // ── SEARCH HUD ──
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Search by Identity/Email...", color = PremiumSilver.copy(alpha = 0.4f), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumSilver) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PremiumSilver,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = PremiumSilver,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // ── ROLE MATRIX FILTERS ──
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val roles = listOf("all", "user", "provider", "admin")
                items(roles) { r ->
                    RoleChip(r, selectedRole == r) { selectedRole = r }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumSilver)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { user ->
                        UserIdentityCard(user, onUpdateRole)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) PremiumSilver else PremiumBlackSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) PremiumSilver else DarkBorder)
    ) {
        Text(
            label.uppercase(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.Black else PremiumSilver,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun UserIdentityCard(user: AdminUserData, onUpdateRole: (String, String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(44.dp), shape = CircleShape, color = PremiumSilver.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(user.name.take(1).uppercase(), color = PremiumSilver, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(user.email, color = PremiumSilver.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                
                Surface(
                    color = when(user.role) {
                        "admin" -> Color(0xFF6366F1)
                        "provider" -> Color(0xFF10B981)
                        else -> PremiumSilver.copy(alpha = 0.2f)
                    }.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (user.role == "admin") Color(0xFF6366F1).copy(alpha = 0.3f) else Color.Transparent)
                ) {
                    Text(user.role.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = if (user.role == "admin") Color(0xFF6366F1) else PremiumSilver, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "HIDE ACTIONS" else "MANAGE AUTHORITY", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 12.dp)) {
                    Divider(color = DarkBorder)
                    Spacer(Modifier.height(16.dp))
                    Text("AUTHORITY OVERRIDE", color = PremiumSilver.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AuthorityButton("PROVIDER", Icons.Default.Engineering) { onUpdateRole(user.id, "provider") }
                        AuthorityButton("USER", Icons.Default.Person) { onUpdateRole(user.id, "user") }
                        AuthorityButton("VERIFY", Icons.Default.Verified) { /* Verification Logic */ }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.AuthorityButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumSilver)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}
