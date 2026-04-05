package com.example.workly.provider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.OrderStatus
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ProviderEarningsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
                EarningsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    
    var totalEarned by remember { mutableDoubleStateOf(0.0) }
    var pendingClearance by remember { mutableDoubleStateOf(0.0) }
    var completedCount by remember { mutableIntStateOf(0) }
    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var chartData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("orders")
                .whereEqualTo("providerId", user.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val docs = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
                        transactions = docs
                        
                        var total = 0.0
                        var pending = 0.0
                        var count = 0
                        
                        val dayMap = mutableMapOf<String, Float>()
                        // Last 7 days init
                        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                        val cal = Calendar.getInstance()
                        val days = mutableListOf<String>()
                        for(i in 0..6) {
                            val d = sdf.format(cal.time)
                            days.add(d)
                            dayMap[d] = 0f
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                        }
                        
                        for (doc in docs) {
                            val price = (doc["finalPrice"] as? Number)?.toDouble() ?: (doc["price"] as? Number)?.toDouble() ?: 0.0
                            val status = doc["status"]?.toString() ?: OrderStatus.PENDING
                            
                            if (status == OrderStatus.ACCEPTED || status == OrderStatus.COMPLETED) {
                                total += price
                                if (status == OrderStatus.ACCEPTED) pending += price
                                if (status == OrderStatus.COMPLETED) count++
                                
                                // Group for chart (by day name)
                                val ts = doc["createdAt"] as? Long ?: 0L
                                val dateStr = sdf.format(Date(ts))
                                if (dayMap.containsKey(dateStr)) {
                                    dayMap[dateStr] = dayMap[dateStr]!! + price.toFloat()
                                }
                            }
                        }
                        
                        totalEarned = total
                        pendingClearance = pending
                        completedCount = count
                        chartData = days.reversed().map { it to (dayMap[it] ?: 0f) }
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings Insight", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfessionalBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 📊 Financial Summary Cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        EarningsSummaryCard(
                            title = "Total Lifetime Revenue",
                            amount = "₹${totalEarned.toInt()}",
                            icon = Icons.Default.Payments,
                            color = ProfessionalBlue
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                SmallStatCard("Pending", "₹${pendingClearance.toInt()}", EnergyOrange)
                            }
                            Box(Modifier.weight(1f)) {
                                SmallStatCard("Completed", "$completedCount", Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                // 📈 Custom Chart Section
                item {
                    Text("Revenue Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            SimpleBarChart(chartData)
                        }
                    }
                }

                // 📝 Recent Transactions
                item {
                    Text("Recent Payouts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                }

                items(transactions.take(15)) { tx ->
                    TransactionItem(tx)
                }
            }
        }
    }
}

@Composable
fun EarningsSummaryCard(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(16.dp))
            Text(title, color = Color.White.copy(0.8f), fontSize = 14.sp)
            Text(amount, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
        }
    }
}

@Composable
fun SmallStatCard(title: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, color.copy(0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Pair<String, Float>>) {
    val maxVal = (data.maxOfOrNull { it.second } ?: 100f).coerceAtLeast(100f)
    
    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            val barHeightRatio = value / maxVal
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(barHeightRatio.coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(ProfessionalBlue, ProfessionalBlue.copy(0.4f))
                            )
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TransactionItem(tx: Map<String, Any>) {
    val price = (tx["finalPrice"] as? Number)?.toDouble() ?: (tx["price"] as? Number)?.toDouble() ?: 0.0
    val status = tx["status"]?.toString() ?: ""
    val name = tx["serviceName"]?.toString() ?: "Workly Task"
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx["createdAt"] as? Long ?: 0L))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(ProfessionalBlue.copy(0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, null, tint = ProfessionalBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(date, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+₹${price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                Text(status.uppercase(), fontSize = 10.sp, color = if(status == OrderStatus.PENDING) EnergyOrange else Color.Gray)
            }
        }
    }
}
