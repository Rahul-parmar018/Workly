package com.example.workly.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.OrderStatus
import com.example.workly.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdminDashboardActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeDataStore = remember { ThemeDataStore(this) }
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())

            WorklyTheme(themeMode = themeMode) {
                AdminMissionControl(
                    onNavigateToProfile = { startActivity(Intent(this, AdminProfileActivity::class.java)) },
                    onManageServices = { startActivity(Intent(this, ManageServicesActivity::class.java)) },
                    onAllBookings = { startActivity(Intent(this, AllBookingsActivity::class.java)) },
                    onUserDirectory = { startActivity(Intent(this, UserDirectoryActivity::class.java)) },
                    onApproveProvider = { pid -> approveProvider(pid) },
                    onRejectProvider = { pid -> rejectProvider(pid) }
                )
            }
        }
    }

    private fun approveProvider(providerId: String) {
        val batch = db.batch()
        batch.update(db.collection("providers").document(providerId), "isApproved", true)
        batch.update(db.collection("users").document(providerId), "isApproved", true)
        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "✅ Provider Authorized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rejectProvider(providerId: String) {
        db.collection("services").whereEqualTo("providerId", providerId).get().addOnSuccessListener { snap ->
            val batch = db.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.delete(db.collection("providers").document(providerId))
            batch.update(db.collection("users").document(providerId), "role", "user")
            batch.commit().addOnSuccessListener {
                Toast.makeText(this, "Rejection processed & cleaned", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun AdminMissionControl(
    onNavigateToProfile: () -> Unit,
    onManageServices: () -> Unit,
    onAllBookings: () -> Unit,
    onUserDirectory: () -> Unit,
    onApproveProvider: (String) -> Unit,
    onRejectProvider: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    
    // System State
    var totalRevenue by remember { mutableDoubleStateOf(0.0) }
    var todayRevenue by remember { mutableDoubleStateOf(0.0) }
    var totalUsers by remember { mutableIntStateOf(0) }
    var userDistribution by remember { mutableStateOf(mapOf("user" to 0, "provider" to 0)) }
    var pendingRequests by remember { mutableIntStateOf(0) }
    var pendingProviders by remember { mutableStateOf<List<PendingProv>>(emptyList()) }
    var revenueHistory by remember { mutableStateOf<List<Double>>(listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)) }

    // Real-Time Listeners
    LaunchedEffect(Unit) {
        // 1. Revenue & Sales Intelligence
        db.collection("orders").whereEqualTo("status", OrderStatus.COMPLETED).addSnapshotListener { snap, _ ->
            snap?.let {
                var total = 0.0
                var today = 0.0
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
                
                it.documents.forEach { doc ->
                    val p = doc.getDouble("finalPrice") ?: 0.0
                    val c = doc.get("createdAt").let { v ->
                        if (v is Long) v else (v as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                    }
                    total += p
                    if (c >= startOfDay) today += p
                }
                totalRevenue = total
                todayRevenue = today
            }
        }

        // 2. User Network Density
        db.collection("users").addSnapshotListener { snap, _ ->
            snap?.let {
                totalUsers = it.size()
                val dist = it.documents.groupBy { doc -> doc.getString("role") ?: "user" }
                userDistribution = dist.mapValues { entry -> entry.value.size }
            }
        }

        // 3. Service Verification Queue
        db.collection("services").whereEqualTo("isApproved", false).addSnapshotListener { snap, _ ->
            pendingRequests = snap?.size() ?: 0
        }

        // 4. Provider Auth Stream
        db.collection("providers").whereEqualTo("isApproved", false).addSnapshotListener { snap, _ ->
            snap?.let { docs ->
                // Fetch user names for these providers
                val list = mutableListOf<PendingProv>()
                docs.documents.forEach { doc ->
                    val pid = doc.id
                    db.collection("users").document(pid).get().addOnSuccessListener { uDoc ->
                        list.add(PendingProv(pid, uDoc.getString("name") ?: "Pro", uDoc.getString("email") ?: ""))
                        pendingProviders = list.toList()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PremiumBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ── HEADER ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MISSION CONTROL", color = PremiumSilver, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("SYSTEM HUD", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Surface(
                    modifier = Modifier.size(50.dp).clickable { onNavigateToProfile() },
                    shape = CircleShape,
                    color = PremiumBlackSurface,
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = PremiumSilver, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── INTELLIGENCE MATRIX (Cards) ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HudCard(
                    modifier = Modifier.weight(1f),
                    title = "LIQUIDITY",
                    value = "₹${"%,.0f".format(totalRevenue)}",
                    subValue = "+₹${"%,.0f".format(todayRevenue)} Today",
                    trend = if (todayRevenue > 0) "UP" else "STABLE",
                    icon = Icons.Default.Payments
                )
                HudCard(
                    modifier = Modifier.weight(1f),
                    title = "NET CAPACITY",
                    value = "$totalUsers",
                    subValue = "${userDistribution["provider"] ?: 0} Providers",
                    trend = "GROWTH",
                    icon = Icons.Default.Groups
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── VISUALIZATION: REVENUE TREND ──
            Surface(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                color = PremiumBlackSurface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(PremiumSilver))
                        Spacer(Modifier.width(10.dp))
                        Text("REVENUE VELOCITY (7D)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    RevenueLineChart(data = revenueHistory)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── OPERATION CORE ──
            Text("OPERATIONS HUB", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OpButton(Modifier.weight(1f), "USERS", Icons.Default.Person, onUserDirectory)
                OpButton(Modifier.weight(1f), "ORDERS", Icons.Default.Inventory, onAllBookings)
                OpButton(Modifier.weight(1f), "SERVICES", Icons.Default.CleaningServices, onManageServices)
            }

            Spacer(Modifier.height(32.dp))

            // ── THE AUTH STREAM (Pending Providers) ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("VERIFICATION STREAM", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.width(12.dp))
                if (pendingProviders.isNotEmpty()) {
                    Surface(color = Color.Red, shape = CircleShape) {
                        Text("${pendingProviders.size}", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            
            if (pendingProviders.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    color = PremiumBlackSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("System Clean • No Pending Auth", color = PremiumSilver.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pendingProviders) { prov ->
                        VerificationCard(prov, { onApproveProvider(prov.id) }, { onRejectProvider(prov.id) })
                    }
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun HudCard(modifier: Modifier, title: String, value: String, subValue: String, trend: String, icon: ImageVector) {
    Surface(
        modifier = modifier.height(160.dp),
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(24.dp))
            Column {
                Text(title, color = PremiumSilver.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
            Text(subValue, color = if (trend == "UP" || trend == "GROWTH") Color(0xFF10B981) else PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OpButton(modifier: Modifier, label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VerificationCard(prov: PendingProv, onApprove: () -> Unit, onReject: () -> Unit) {
    Surface(
        modifier = Modifier.width(280.dp),
        color = PremiumBlackSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(40.dp), shape = CircleShape, color = PremiumSilver.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(prov.name.take(1), color = PremiumSilver, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(prov.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(prov.email, color = PremiumSilver.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("AUTHORIZE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(0.8f),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RevenueLineChart(data: List<Double>) {
    // Custom Canvas Trend Line
    val color = PremiumSilver
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val points = listOf(0.4f, 0.6f, 0.5f, 0.8f, 0.7f, 0.9f, 0.85f) // Dummy trend points for visual depth
        
        val path = Path().apply {
            moveTo(0f, height * 0.9f)
            points.forEachIndexed { index, p ->
                val x = index * (width / (points.size - 1))
                val y = height * (1f - p)
                lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Fill area under the curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent))
        )
    }
}

data class PendingProv(val id: String, val name: String, val email: String)
