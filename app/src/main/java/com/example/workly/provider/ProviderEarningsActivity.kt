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
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var selectedPeriod by remember { mutableStateOf("Week") } 
    val context = LocalContext.current

    val todayLabel = remember(selectedPeriod) {
        val now = Calendar.getInstance()
        when (selectedPeriod) {
            "Day" -> {
                val h = now.get(Calendar.HOUR_OF_DAY)
                val bucket = (h / 3) * 3
                val bucketCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, bucket)
                    set(Calendar.MINUTE, 0)
                }
                SimpleDateFormat("HH:00", Locale.getDefault()).format(bucketCal.time)
            }
            "Week" -> {
                val map = mapOf(2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat", 1 to "Sun")
                map[now.get(Calendar.DAY_OF_WEEK)] ?: ""
            }
            "Month" -> SimpleDateFormat("'W'w", Locale.getDefault()).format(now.time)
            "Year" -> SimpleDateFormat("MMM", Locale.getDefault()).format(now.time)
            else -> ""
        }
    }

    LaunchedEffect(user?.uid, selectedPeriod) {
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
                        val allDocs = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
                        // ── STRICT SOURCE FILTER: Only real app services have a serviceId ──
                        val realDocs = allDocs.filter { (it["serviceId"] as? String)?.isNotEmpty() == true }
                        transactions = realDocs
                        
                        var total = 0.0
                        var pending = 0.0
                        var count = 0
                        
                        val catMap = mutableMapOf<String, Float>()
                        val serviceMap = mutableMapOf<String, Double>()
                        
                        // --- ENHANCED TREND LOGIC ---
                        val trendMap = mutableMapOf<String, Float>()
                        val labels = mutableListOf<String>()
                        val cal = Calendar.getInstance()

                        when (selectedPeriod) {
                            "Day" -> {
                                val sdfHour = SimpleDateFormat("HH:00", Locale.getDefault())
                                for (i in 0..7) {
                                    val label = sdfHour.format(cal.time)
                                    labels.add(label)
                                    trendMap[label] = 0f
                                    cal.add(Calendar.HOUR_OF_DAY, -3)
                                }
                            }
                             "Week" -> {
                                 // ── FIXED WEEK ORDER: Mon to Sun ──
                                 val fixedDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                 labels.addAll(fixedDays)
                                 fixedDays.forEach { trendMap[it] = 0f }
                             }
                            "Month" -> {
                                val sdfWeek = SimpleDateFormat("'W'w", Locale.getDefault())
                                for (i in 0..3) {
                                    val label = sdfWeek.format(cal.time)
                                    labels.add(label)
                                    trendMap[label] = 0f
                                    cal.add(Calendar.WEEK_OF_YEAR, -1)
                                }
                            }
                            "Year" -> {
                                val sdfMonth = SimpleDateFormat("MMM", Locale.getDefault())
                                for (i in 0..5) {
                                    val label = sdfMonth.format(cal.time)
                                    labels.add(label)
                                    trendMap[label] = 0f
                                    cal.add(Calendar.MONTH, -1)
                                }
                            }
                        }

                        for (doc in realDocs) {
                            val price = (doc["finalPrice"] as? Number)?.toDouble() ?: (doc["price"] as? Number)?.toDouble() ?: (doc["basePrice"] as? Number)?.toDouble() ?: 0.0
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
                            
                            // ── RECOGNIZED STATUSES ──
                            val isCompleted = status == OrderStatus.COMPLETED
                            val isPending = status == OrderStatus.ACCEPTED || status == OrderStatus.ASSIGNED || 
                                           status == OrderStatus.ARRIVING || status == OrderStatus.STARTED

                            if (isCompleted || isPending) {
                                total += price
                                count++
                                if (isPending) pending += price
                                if (status == OrderStatus.COMPLETED) {
                                    serviceMap[sName] = (serviceMap[sName] ?: 0.0) + price
                                }
                                
                                catMap[cat] = (catMap[cat] ?: 0f) + price.toFloat()
                                
                                // Grouping for Trend
                                val date = Date(ts)
                                val groupLabel = when (selectedPeriod) {
                                    "Day" -> SimpleDateFormat("HH:00", Locale.getDefault()).apply {
                                        val c = Calendar.getInstance().apply { time = date }
                                        val h = c.get(Calendar.HOUR_OF_DAY)
                                        val bucket = (h / 3) * 3
                                        c.set(Calendar.HOUR_OF_DAY, bucket)
                                        c.set(Calendar.MINUTE, 0)
                                    }.format(date)
                                     "Week" -> {
                                         val c = Calendar.getInstance().apply { time = date }
                                         val dayIdx = c.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
                                         val map = mapOf(2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat", 1 to "Sun")
                                         map[dayIdx] ?: ""
                                     }
                                    "Month" -> SimpleDateFormat("'W'w", Locale.getDefault()).format(date)
                                    "Year" -> SimpleDateFormat("MMM", Locale.getDefault()).format(date)
                                    else -> ""
                                }

                                if (trendMap.containsKey(groupLabel)) {
                                    trendMap[groupLabel] = trendMap[groupLabel]!! + price.toFloat()
                                }
                            }
                        }
                        
                        avgOrderValue = if (count > 0) total / count else 0.0
                        totalEarned = total
                        pendingClearance = pending
                        completedCount = count
                        val catList = mutableListOf<Pair<String, Float>>()
                        for ((cName, cVal) in catMap) {
                            catList.add(Pair(cName, cVal))
                        }
                        catList.sortByDescending { p -> p.second }
                        categoryDistribution = catList.take(4)

                        val svcList = mutableListOf<Pair<String, Double>>()
                        for ((sName, sVal) in serviceMap) {
                            svcList.add(Pair(sName, sVal))
                        }
                        svcList.sortByDescending { p -> p.second }
                        topServices = svcList.take(3)
                        
                        val chartList = mutableListOf<Pair<String, Float>>()
                        // Use labels as defined (Mon -> Sun for Week, others use their generated labels)
                        // If it's a Week view, use the fixed ordering; otherwise use whatever was generated.
                        val orderToUse = if (selectedPeriod == "Week") labels else labels.reversed()
                        
                        for (lab in orderToUse) {
                            chartList.add(Pair(lab, trendMap[lab] ?: 0f))
                        }
                        chartData = chartList
                        
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "REVENUE VELOCITY", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 11.sp, 
                            color = PremiumSilver.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Day", "Week", "Month", "Year").forEach { p ->
                                val sel = selectedPeriod == p
                                Surface(
                                    onClick = { selectedPeriod = p },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) PremiumSilver else Color.Transparent,
                                    border = if (sel) null else BorderStroke(1.dp, PremiumSilver.copy(0.1f)),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                                        Text(p, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (sel) PremiumBlack else PremiumSilver)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(PremiumBlackSurface),
                        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            SimpleBarChart(data = chartData, todayLabel = todayLabel)
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
fun SimpleBarChart(data: List<Pair<String, Float>>, todayLabel: String = "") {
    val maxVal = (data.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
    
    Column(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        // --- ── CHART AREA ───────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Horizontal Grid Lines
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                repeat(5) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(PremiumSilver.copy(alpha = 0.05f)))
                }
            }
            
            // Bars Area
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (label, value) ->
                    val ratio = (value / maxVal).coerceIn(0f, 1f)
                    val isToday = label == todayLabel
                    
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (value > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(ratio.coerceAtLeast(0.01f))
                                    .width(30.dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                PremiumSilver.copy(alpha = if (isToday) 0.6f else 0.4f),
                                                PremiumSilver.copy(alpha = 0.05f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = PremiumSilver.copy(alpha = if (isToday) 0.3f else 0.1f),
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    ),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Top Accent Line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(if (isToday) Color.White else PremiumSilver.copy(0.7f))
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // --- ── X-AXIS BASELINE ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(PremiumSilver.copy(alpha = 0.15f))
        )
        
        // --- ── LABELS AREA ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { (label, _) ->
                val isToday = label == todayLabel
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isToday) Color.White else PremiumSilver.copy(0.4f),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
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
