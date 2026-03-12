package com.example.workly.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.data.Booking
import com.example.workly.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                AdminDashboardScreen()
            }
        }
    }
}

data class AdminProvider(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val rating: Double = 0.0,
    val jobsDone: Int = 0,
    val isActive: Boolean = true,
    val phone: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val firestore = FirebaseFirestore.getInstance()
    var selectedTab by remember { mutableIntStateOf(0) }
    var allBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var providers by remember { mutableStateOf<List<AdminProvider>>(emptyList()) }
    var isLoadingBookings by remember { mutableStateOf(true) }
    var isLoadingProviders by remember { mutableStateOf(true) }

    // Load bookings from Firestore in real-time
    LaunchedEffect(Unit) {
        firestore.collection("bookings")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                allBookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                isLoadingBookings = false
            }
        firestore.collection("providers")
            .addSnapshotListener { snapshot, _ ->
                providers = snapshot?.toObjects(AdminProvider::class.java) ?: listOf(
                    AdminProvider("pro_1", "Alex Johnson", "Cleaning", 4.9, 127, true, "+91 98765 43210"),
                    AdminProvider("pro_2", "Maria Garcia", "Wellness", 4.8, 85, true, "+91 87654 32109"),
                    AdminProvider("pro_3", "David Smith", "Repair", 4.7, 200, true, "+91 76543 21098"),
                    AdminProvider("pro_4", "Priya Sharma", "Plumbing", 4.9, 156, true, "+91 65432 10987"),
                    AdminProvider("pro_5", "Raj Kumar", "Electric", 4.6, 98, false, "+91 54321 09876"),
                )
                isLoadingProviders = false
            }
    }

    // Stats derived from real data
    val totalBookings = allBookings.size
    val pendingCount = allBookings.count { it.status == "Pending" }
    val confirmedCount = allBookings.count { it.status == "Confirmed" }
    val completedCount = allBookings.count { it.status == "Completed" }
    val totalRevenue = allBookings.filter { it.status == "Completed" }.sumOf { it.finalPrice }
    val activeProviders = providers.count { it.isActive }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(ProfessionalBlue, ElectricTeal)))
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Admin Panel", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Text("Workly Dashboard", color = Color.White.copy(0.8f), fontSize = 13.sp)
                    }
                    Surface(shape = CircleShape, color = Color.White.copy(0.2f)) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.padding(10.dp).size(24.dp))
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = ProfessionalBlue,
                edgePadding = 8.dp
            ) {
                listOf(
                    "Overview" to Icons.Default.Dashboard,
                    "Bookings" to Icons.Default.BookOnline,
                    "Providers" to Icons.Default.Groups,
                    "Analytics" to Icons.Default.BarChart
                ).forEachIndexed { idx, (title, icon) ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(icon, null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            Crossfade(targetState = selectedTab, label = "AdminTab") { tab ->
                when (tab) {
                    0 -> AdminOverviewTab(
                        totalBookings = totalBookings,
                        pendingCount = pendingCount,
                        confirmedCount = confirmedCount,
                        completedCount = completedCount,
                        totalRevenue = totalRevenue,
                        activeProviders = activeProviders,
                        recentBookings = allBookings.take(5)
                    )
                    1 -> AdminBookingsTab(
                        bookings = allBookings,
                        isLoading = isLoadingBookings,
                        onStatusChange = { booking, newStatus ->
                            firestore.collection("bookings").document(booking.id)
                                .update("status", newStatus, "updatedAt", Timestamp.now())
                        }
                    )
                    2 -> AdminProvidersTab(
                        providers = providers,
                        isLoading = isLoadingProviders,
                        onToggleActive = { provider ->
                            firestore.collection("providers").document(provider.id)
                                .update("isActive", !provider.isActive)
                        }
                    )
                    3 -> AdminAnalyticsTab(
                        allBookings = allBookings,
                        totalRevenue = totalRevenue,
                        completedCount = completedCount
                    )
                }
            }
        }
    }
}

// ─── OVERVIEW TAB ───────────────────────────────────────────────────────────
@Composable
fun AdminOverviewTab(
    totalBookings: Int, pendingCount: Int, confirmedCount: Int,
    completedCount: Int, totalRevenue: Double, activeProviders: Int,
    recentBookings: List<Booking>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Quick Stats", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Bookings", totalBookings.toString(), Icons.Default.BookOnline, ProfessionalBlue, modifier = Modifier.weight(1f))
                StatCard("Revenue", "₹${totalRevenue.toInt()}", Icons.Default.CurrencyRupee, ElectricTeal, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Pending", pendingCount.toString(), Icons.Default.Pending, EnergyOrange, modifier = Modifier.weight(1f))
                StatCard("Completed", completedCount.toString(), Icons.Default.CheckCircle, ElectricTeal, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Active Pros", activeProviders.toString(), Icons.Default.Groups, ProfessionalBlue, modifier = Modifier.weight(1f))
                StatCard("Confirmed", confirmedCount.toString(), Icons.Default.Verified, Color(0xFF2E7D32), modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("Recent Bookings", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
        }

        if (recentBookings.isEmpty()) {
            item {
                EmptyState("No bookings yet", Icons.Default.BookOnline)
            }
        } else {
            items(recentBookings) { booking ->
                BookingListCard(booking = booking, onStatusChange = null, compact = true)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(0.12f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = color)
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── BOOKINGS TAB ────────────────────────────────────────────────────────────
@Composable
fun AdminBookingsTab(
    bookings: List<Booking>,
    isLoading: Boolean,
    onStatusChange: (Booking, String) -> Unit
) {
    var filterStatus by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Confirmed", "InProgress", "Completed", "Cancelled")
    val filtered = if (filterStatus == "All") bookings else bookings.filter { it.status == filterStatus }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(filterStatus).coerceAtLeast(0),
            containerColor = Color.White,
            contentColor = ProfessionalBlue,
            edgePadding = 8.dp
        ) {
            filters.forEach { status ->
                Tab(
                    selected = filterStatus == status,
                    onClick = { filterStatus = status },
                    text = {
                        val count = if (status == "All") bookings.size else bookings.count { it.status == status }
                        Text("$status ($count)", fontWeight = if (filterStatus == status) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfessionalBlue)
            }
        } else if (filtered.isEmpty()) {
            EmptyState("No ${if (filterStatus == "All") "" else filterStatus} bookings", Icons.Default.BookOnline)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { booking ->
                    BookingListCard(booking = booking, onStatusChange = { newStatus -> onStatusChange(booking, newStatus) })
                }
            }
        }
    }
}

@Composable
fun BookingListCard(booking: Booking, onStatusChange: ((String) -> Unit)?, compact: Boolean = false) {
    val statusColor = when (booking.status) {
        "Pending" -> EnergyOrange
        "Confirmed" -> ProfessionalBlue
        "InProgress" -> ElectricTeal
        "Completed" -> Color(0xFF2E7D32)
        "Cancelled" -> Color.Red
        else -> TextSecondary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Service icon
                Surface(shape = RoundedCornerShape(12.dp), color = ProfessionalBlue.copy(0.1f), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Handyman, null, tint = ProfessionalBlue, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(booking.serviceCategory, fontSize = 12.sp, color = TextSecondary)
                }
                // Status badge
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(0.12f)) {
                    Text(
                        booking.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Details grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip(Icons.Default.CalendarToday, "${booking.date} ${booking.time}", modifier = Modifier.weight(1f))
                InfoChip(Icons.Default.CurrencyRupee, "₹${booking.finalPrice.toInt()}", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (booking.providerName.isNotEmpty()) {
                InfoChip(Icons.Default.Person, booking.providerName)
            }
            if (booking.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                InfoChip(Icons.Default.LocationOn, booking.address, maxLines = 2)
            }

            // Action buttons (non-compact)
            if (onStatusChange != null && !compact && booking.status != "Completed" && booking.status != "Cancelled") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (booking.status) {
                        "Pending" -> {
                            AdminActionButton("Confirm", Icons.Default.Check, ElectricTeal, Modifier.weight(1f)) { onStatusChange("Confirmed") }
                            AdminActionButton("Cancel", Icons.Default.Close, Color.Red, Modifier.weight(1f)) { onStatusChange("Cancelled") }
                        }
                        "Confirmed" -> {
                            AdminActionButton("Start Job", Icons.Default.PlayArrow, ProfessionalBlue, Modifier.weight(1f)) { onStatusChange("InProgress") }
                            AdminActionButton("Cancel", Icons.Default.Close, Color.Red, Modifier.weight(1f)) { onStatusChange("Cancelled") }
                        }
                        "InProgress" -> {
                            AdminActionButton("Mark Complete", Icons.Default.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f)) { onStatusChange("Completed") }
                        }
                    }
                }
            }

            // Booking ID
            Spacer(modifier = Modifier.height(6.dp))
            Text("ID: #${booking.id.take(8).uppercase()}", fontSize = 10.sp, color = TextSecondary.copy(0.6f))
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary, maxLines = maxLines, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AdminActionButton(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ─── PROVIDERS TAB ───────────────────────────────────────────────────────────
@Composable
fun AdminProvidersTab(
    providers: List<AdminProvider>,
    isLoading: Boolean,
    onToggleActive: (AdminProvider) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${providers.size} Professionals", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${providers.count { it.isActive }} Active", color = ElectricTeal, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ProfessionalBlue) }
        } else if (providers.isEmpty()) {
            EmptyState("No providers yet", Icons.Default.Groups)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers, key = { it.id }) { provider ->
                    ProviderAdminCard(provider = provider, onToggleActive = { onToggleActive(provider) })
                }
            }
        }
    }
}

@Composable
fun ProviderAdminCard(provider: AdminProvider, onToggleActive: () -> Unit) {
    val avatarUrl = "https://ui-avatars.com/api/?name=${provider.name.replace(" ", "+")}&background=1565C0&color=fff&size=200&bold=true&rounded=true"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = avatarUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Surface(shape = CircleShape, color = if (provider.isActive) ElectricTeal.copy(0.12f) else Color.Red.copy(0.1f)) {
                        Text(
                            if (provider.isActive) "Active" else "Inactive",
                            color = if (provider.isActive) ElectricTeal else Color.Red,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(provider.category, fontSize = 12.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = EnergyOrange, modifier = Modifier.size(13.dp))
                        Text(" ${provider.rating}", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text("·", color = TextSecondary)
                    Text("${provider.jobsDone} jobs", fontSize = 12.sp, color = TextSecondary)
                    Text("·", color = TextSecondary)
                    Text(provider.phone, fontSize = 11.sp, color = ProfessionalBlue, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = provider.isActive,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricTeal)
            )
        }
    }
}

// ─── ANALYTICS TAB ───────────────────────────────────────────────────────────
@Composable
fun AdminAnalyticsTab(allBookings: List<Booking>, totalRevenue: Double, completedCount: Int) {
    val categoryRevenue = allBookings
        .filter { it.status == "Completed" }
        .groupBy { it.serviceCategory.ifEmpty { "Other" } }
        .mapValues { (_, bookings) -> bookings.sumOf { it.finalPrice } }
        .entries.sortedByDescending { it.value }

    val statusBreakdown = mapOf(
        "Pending" to allBookings.count { it.status == "Pending" },
        "Confirmed" to allBookings.count { it.status == "Confirmed" },
        "InProgress" to allBookings.count { it.status == "InProgress" },
        "Completed" to allBookings.count { it.status == "Completed" },
        "Cancelled" to allBookings.count { it.status == "Cancelled" }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Revenue overview
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Revenue Summary", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        RevenueItem("Total Revenue", "₹${totalRevenue.toInt()}", ProfessionalBlue)
                        VerticalDivider(modifier = Modifier.height(50.dp))
                        RevenueItem("Completed", completedCount.toString(), ElectricTeal)
                        VerticalDivider(modifier = Modifier.height(50.dp))
                        val avgOrderValue = if (completedCount > 0) totalRevenue / completedCount else 0.0
                        RevenueItem("Avg Order", "₹${avgOrderValue.toInt()}", EnergyOrange)
                    }
                }
            }
        }

        item {
            // Status breakdown
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Booking Status Breakdown", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    val total = allBookings.size.coerceAtLeast(1)
                    statusBreakdown.forEach { (status, count) ->
                        val pct = count.toFloat() / total
                        val color = when (status) {
                            "Pending" -> EnergyOrange
                            "Confirmed" -> ProfessionalBlue
                            "InProgress" -> ElectricTeal
                            "Completed" -> Color(0xFF2E7D32)
                            else -> Color.Red.copy(0.7f)
                        }
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(status, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("$count (${(pct * 100).toInt()}%)", fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = color.copy(0.15f)
                            )
                        }
                    }
                }
            }
        }

        if (categoryRevenue.isNotEmpty()) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Revenue by Category", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        val maxRev = categoryRevenue.maxOfOrNull { it.value } ?: 1.0
                        categoryRevenue.forEach { (category, revenue) ->
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(category, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("₹${revenue.toInt()}", fontSize = 13.sp, color = ProfessionalBlue, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (revenue / maxRev).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = ProfessionalBlue,
                                    trackColor = ProfessionalBlue.copy(0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun EmptyState(message: String, icon: ImageVector) {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = ProfessionalBlue.copy(0.08f)) {
                Icon(icon, null, tint = ProfessionalBlue, modifier = Modifier.padding(20.dp).size(40.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}
