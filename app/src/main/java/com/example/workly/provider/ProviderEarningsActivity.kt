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
    var categoryDistribution by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var topServices by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var avgOrderValue by remember { mutableDoubleStateOf(0.0) }
    var weeklyGrowth by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("orders")
                .whereEqualTo("providerId", user.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val docs = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
                        transactions = docs
                        
                        var total = 0.0
                        var pending = 0.0
                        var count = 0
                        
                        val dayMap = mutableMapOf<String, Float>()
                        val catMap = mutableMapOf<String, Float>()
                        val serviceMap = mutableMapOf<String, Double>()
                        
                        // Time-series Logic
                        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                        val cal = Calendar.getInstance()
                        val days = mutableListOf<String>()
                        for(i in 0..6) {
                            val d = sdf.format(cal.time)
                            days.add(d)
                            dayMap[d] = 0f
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                        }
                        
                        val now = System.currentTimeMillis()
                        val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
                        val prevWeekStart = now - (14 * 24 * 60 * 60 * 1000L)
                        
                        var currentWeekTotal = 0.0
                        var prevWeekTotal = 0.0
                        
                        for (doc in docs) {
                            val price = (doc["finalPrice"] as? Number)?.toDouble() ?: (doc["price"] as? Number)?.toDouble() ?: 0.0
                            val status = doc["status"]?.toString() ?: OrderStatus.PENDING
                            val cat = doc["serviceCategory"]?.toString() ?: "Other"
                            val sName = doc["serviceName"]?.toString() ?: "Unnamed Task"
                            
                            val rawTs = doc["createdAt"]
                            val ts = when (rawTs) {
                                is com.google.firebase.Timestamp -> rawTs.toDate().time
                                is Long -> rawTs
                                is Number -> rawTs.toLong()
                                else -> 0L
                            }
                            
                            if (status == OrderStatus.ACCEPTED || status == OrderStatus.COMPLETED) {
                                total += price
                                if (status == OrderStatus.ACCEPTED) pending += price
                                if (status == OrderStatus.COMPLETED) {
                                    count++
                                    serviceMap[sName] = (serviceMap[sName] ?: 0.0) + price
                                }
                                
                                // Category distribution
                                catMap[cat] = (catMap[cat] ?: 0f) + price.toFloat()
                                
                                // Trend Data
                                val dateStr = sdf.format(Date(ts))
                                if (dayMap.containsKey(dateStr)) {
                                    dayMap[dateStr] = dayMap[dateStr]!! + price.toFloat()
                                }
                                
                                // Growth Calculation
                                if (ts > weekAgo) currentWeekTotal += price
                                else if (ts > prevWeekStart) prevWeekTotal += price
                            }
                        }
                        
                        avgOrderValue = if (count > 0) total / count else 0.0
                        weeklyGrowth = if (prevWeekTotal > 0) ((currentWeekTotal - prevWeekTotal) / prevWeekTotal) * 100 else 0.0
                        totalEarned = total
                        pendingClearance = pending
                        completedCount = count
                        chartData = days.reversed().map { it to (dayMap[it] ?: 0f) }
                        categoryDistribution = catMap.toList().sortedByDescending { it.second }.take(4)
                        topServices = serviceMap.toList().sortedByDescending { it.second }.take(3)
                        
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Financial Strategic Hub", 
                        fontWeight = FontWeight.ExtraBold, 
                        color = PremiumWhite,
                        letterSpacing = 0.5.sp
                    ) 
                },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 📊 Primary Financial Metrics
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EarningsSummaryCard(
                            title = "REALIZED WEALTH",
                            amount = "₹${totalEarned.toInt()}",
                            growth = weeklyGrowth,
                            icon = Icons.Default.AccountBalanceWallet
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                SmallStatCard("IN TRANSIT", "₹${pendingClearance.toInt()}", EnergyOrange)
                            }
                            Box(Modifier.weight(1f)) {
                                SmallStatCard("ALPHA YIELD (AVG)", "₹${avgOrderValue.toInt()}", Color(0xFF10B981))
                            }
                        }
                    }
                }

                // 🚀 Alpha Services Matrix
                if (topServices.isNotEmpty()) {
                    item {
                        Text(
                            "ALPHA SERVICES MATRIX", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 11.sp, 
                            color = PremiumSilver.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        AlphaServicesList(topServices)
                    }
                }

                // 🏗️ Categorical Distribution Matrix
                if (categoryDistribution.isNotEmpty()) {
                    item {
                        Text(
                            "CATEGORY REVENUE BREAKDOWN", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 11.sp, 
                            color = PremiumSilver.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        CategoryMatrix(categoryDistribution)
                    }
                }

                // 📈 Revenue Velocity Chart
                item {
                    Text(
                        "REVENUE VELOCITY (7D)", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 11.sp, 
                        color = PremiumSilver.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(PremiumBlackSurface),
                        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            SimpleBarChart(chartData)
                        }
                    }
                }

                // 📝 Strategic Ledger
                item {
                    Text(
                        "STRATEGIC LEDGER", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 11.sp, 
                        color = PremiumSilver.copy(alpha = 0.5f),
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(transactions.take(15)) { tx ->
                    TransactionItem(tx)
                }
            }
        }
    }
}

@Composable
fun AlphaServicesList(topServices: List<Pair<String, Double>>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(PremiumBlackSurface),
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            topServices.forEach { (name, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PremiumSilver.copy(0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, null, tint = PremiumSilver, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(name, fontSize = 14.sp, color = PremiumWhite, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Text("₹${value.toInt()}", fontSize = 14.sp, color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun EarningsSummaryCard(title: String, amount: String, growth: Double, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(PremiumBlackSurface),
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(32.dp))
                if (growth != 0.0) {
                    Surface(
                        color = if (growth > 0) Color(0xFF10B981).copy(0.1f) else Color.Red.copy(0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = (if (growth > 0) "+" else "") + String.format("%.1f%%", growth),
                            color = if (growth > 0) Color(0xFF10B981) else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                title, 
                color = PremiumSilver.copy(alpha = 0.6f), 
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                amount, 
                color = PremiumWhite, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 42.sp,
                letterSpacing = (-1).sp
            )
        }
    }
}

@Composable
fun CategoryMatrix(distribution: List<Pair<String, Float>>) {
    val total = distribution.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(PremiumBlackSurface),
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            distribution.forEach { (cat, value) ->
                val percentage = (value / total) * 100
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat, fontSize = 13.sp, color = PremiumWhite, fontWeight = FontWeight.Bold)
                        Text("₹${value.toInt()}", fontSize = 13.sp, color = PremiumSilver, fontWeight = FontWeight.Medium)
                    }
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(PremiumSilver.copy(0.05f))) {
                        Box(
                            Modifier.fillMaxWidth(value / total)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(PremiumSilver, PremiumSilver.copy(0.4f))))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallStatCard(title: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(PremiumBlackSurface),
        border = BorderStroke(1.dp, color.copy(0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                title, 
                color = PremiumSilver.copy(alpha = 0.5f), 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
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
            val barHeightRatio = (value / maxVal).coerceIn(0.05f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight(barHeightRatio)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(PremiumSilver, PremiumSilver.copy(0.2f))
                            )
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(label, fontSize = 10.sp, color = PremiumSilver.copy(0.5f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TransactionItem(tx: Map<String, Any>) {
    val price = (tx["finalPrice"] as? Number)?.toDouble() ?: (tx["price"] as? Number)?.toDouble() ?: 0.0
    val status = tx["status"]?.toString() ?: ""
    val name = tx["serviceName"]?.toString() ?: "Workly Task"
    val rawTs = tx["createdAt"]
    val ts = when (rawTs) {
        is com.google.firebase.Timestamp -> rawTs.toDate().time
        is Long -> rawTs
        is Number -> rawTs.toLong()
        else -> 0L
    }
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ts))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.05f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(PremiumSilver.copy(0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, null, tint = PremiumSilver, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PremiumWhite)
                Text(date, fontSize = 12.sp, color = PremiumSilver.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+₹${price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF10B981))
                Text(status.uppercase(), fontSize = 10.sp, color = if(status == OrderStatus.PENDING) EnergyOrange else PremiumSilver.copy(alpha = 0.3f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}
