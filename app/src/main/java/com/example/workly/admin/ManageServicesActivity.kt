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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.Service
import com.example.workly.theme.*
import com.google.firebase.firestore.FirebaseFirestore

class ManageServicesActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeDataStore = remember { ThemeDataStore(this) }
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())

            WorklyTheme(themeMode = themeMode) {
                ServiceGovernanceScreen(
                    onBack = { finish() },
                    onApprove = { s -> approveService(s) },
                    onReject = { s -> rejectService(s) }
                )
            }
        }
    }

    private fun approveService(service: Service) {
        db.collection("services").document(service.id)
            .update("isApproved", true, "status", "approved")
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Service Live: ${service.title}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectService(service: Service) {
        db.collection("services").document(service.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Service Rejected", Toast.LENGTH_SHORT).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceGovernanceScreen(onBack: () -> Unit, onApprove: (Service) -> Unit, onReject: (Service) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var requests by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("services")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    requests = it.documents.mapNotNull { doc ->
                        doc.toObject(Service::class.java)?.copy(id = doc.id)
                    }
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = PremiumBlack,
        topBar = {
            TopAppBar(
                title = { Text("SERVICE GOVERNANCE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumSilver) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // ── FLOW DESCRIPTOR ──
            Surface(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                color = PremiumBlackSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = PremiumSilver, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Reviewing ${requests.size} human-submitted service requests", color = PremiumSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumSilver)
                }
            } else if (requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = PremiumSilver.copy(alpha = 0.2f), modifier = Modifier.size(60.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Queue Clear", color = PremiumSilver.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(requests) { service ->
                        ServiceRequestCard(service, onApprove, onReject)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceRequestCard(service: Service, onApprove: (Service) -> Unit, onReject: (Service) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Category Tag
                Surface(
                    color = PremiumSilver.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(service.category.uppercase(), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = PremiumSilver, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.weight(1f))
                Text("₹${service.price.toInt()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            Spacer(Modifier.height(12.dp))
            
            Text(service.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(service.description, color = PremiumSilver.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(20.dp))

            // Provider Attribution
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, null, tint = PremiumSilver, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("REQUEST BY ID:", color = PremiumSilver.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(6.dp))
                Text(service.providerId.take(12), color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onApprove(service) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("AUTHORIZE LIVE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = { onReject(service) },
                    modifier = Modifier.weight(0.7f).height(48.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
