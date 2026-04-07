package com.example.workly.home

import android.content.Intent
import com.example.workly.provider.MyServicesActivity
import com.example.workly.provider.AddServiceActivity
import com.example.workly.provider.ProviderOrdersActivity
import com.example.workly.provider.ProviderEarningsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.booking.BookingActivity
import com.example.workly.booking.MyBookingsActivity
import com.example.workly.admin.AdminDashboardActivity
import com.example.workly.data.Booking
import com.example.workly.data.Order
import com.example.workly.data.OrderStatus
import com.example.workly.data.Service
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Workly Premium Colors (Theme-aware aliases)
@Composable
fun getWorklyPrimary() = MaterialTheme.colorScheme.primary
@Composable
fun getWorklyOnSurface() = MaterialTheme.colorScheme.onSurface
@Composable
fun getWorklyBackground() = MaterialTheme.colorScheme.background

private val CleaningGradient = Brush.verticalGradient(listOf(Color(0xFFEAF6FF), Color(0xFFFFFFFF)))
private val ElectricGradient = Brush.verticalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFE0B2)))
private val PlumbingGradient = Brush.verticalGradient(listOf(Color(0xFFE6FFFA), Color(0xFFCCF2F4)))

// ─── Home Screen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    userName: String,
    userRole: String,
    onSeeAllServices: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val bookings by viewModel.upcomingBookings.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val firstName = userName.split(" ").firstOrNull() ?: "there"

    // Real-time Services Feed from Firestore
    var firestoreServices by remember { mutableStateOf<List<Service>>(emptyList()) }
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("services")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    firestoreServices = snapshot.documents.mapNotNull { doc ->
                        try { doc.toObject(Service::class.java) }
                        catch (e: Exception) { null }
                    }
                }
            }
    }
    val popularServices = firestoreServices.ifEmpty { getAllServices().take(6) }

    // User image fetch
    var userImage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc -> userImage = doc.getString("imageUrl") }
        }
    }
    val effectiveAvatar = userImage ?: "https://ui-avatars.com/api/?name=${userName.replace(" ", "+")}&background=ffffff&color=0e0e0e&bold=true&rounded=true&size=120"

    val screenBg = MaterialTheme.colorScheme.background
    val onBg     = MaterialTheme.colorScheme.onBackground
    val primary  = MaterialTheme.colorScheme.primary
    val surface  = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfVar   = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {

        // ── Header ───────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "👋 Hi $firstName",
                        color = onBg,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "What do you need cleaned today?",
                        color = onBg.copy(alpha = 0.55f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Profile Avatar
                Surface(
                    modifier = Modifier.size(44.dp).clickable { onNavigateToProfile() },
                    shape = CircleShape,
                    color = surfVar,
                    shadowElevation = 4.dp
                ) {
                    AsyncImage(
                        model = effectiveAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // ── Search + Trust Strip ─────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Search bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) },
                    shape = RoundedCornerShape(28.dp),
                    color = surfVar,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search for services...", color = onSurface.copy(alpha = 0.4f), fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mini Trust Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "✔ Verified • 4.8★ Rated • 10k+ users",
                        color = onBg.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Service Grid (3D System) ──────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Popular Services")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PremiumServiceCard(
                        title = "Home Cleaning",
                        gradient = CleaningGradient,
                        icon = Icons.Default.CleaningServices,
                        onClick = { /* Navigate */ }
                    )
                }
                item {
                    PremiumServiceCard(
                        title = "Electric",
                        gradient = ElectricGradient,
                        icon = Icons.Default.ElectricalServices,
                        glowPulse = true,
                        onClick = { /* Navigate */ }
                    )
                }
                item {
                    PremiumServiceCard(
                        title = "Plumbing",
                        gradient = PlumbingGradient,
                        icon = Icons.Default.Plumbing,
                        rippleEffect = true,
                        onClick = { /* Navigate */ }
                    )
                }
                item {
                    PremiumServiceCard(
                        title = "Kitchen Cleaning",
                        gradient = CleaningGradient,
                        icon = Icons.Default.Countertops,
                        onClick = { /* Navigate */ }
                    )
                }
            }
        }

        // ── (Book Now CTA Removed for Cleaner Layout) ──
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── Top Services ────────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader("Top Rated Services") { onSeeAllServices() }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(popularServices.size) { idx ->
                    PremiumServiceDetailCard(popularServices[idx])
                }
            }
        }

        // ── Offer Banner ─────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(primary)
                    .clickable { /* Action */ }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🎉 First Booking?",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Get 20% off your first service!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
                            Text(
                                "Use code FIRST20",
                                color = primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Icon(
                        Icons.Default.Celebration,
                        null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        // ── Trust Pills ──────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader("Why Workly?")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val trustItems = listOf("✔ Verified Professionals", "✔ Safe Products", "✔ 10,000+ Homes Cleaned", "✔ 24/7 Support")
                items(trustItems.size) { idx ->
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = surfVar,
                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(50.dp))
                    ) {
                        Text(
                            trustItems[idx],
                            color = onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // ── Stats Section ────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader("Platform Stats")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("12k+", "Serviced"),
                    Pair("14ms", "Response"),
                    Pair("99.9%", "Success")
                ).forEach { (value, label) ->
                    PremiumStatCard(value = value, label = label, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Screen (Professional Dashboard)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    userName: String,
    userRole: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    var lifetimeEarnings by remember { mutableIntStateOf(6500) }
    var jobsCompleted by remember { mutableIntStateOf(12) }
    var userRating by remember { mutableDoubleStateOf(4.8) }

    val bg        = MaterialTheme.colorScheme.background
    val primary   = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBg      = MaterialTheme.colorScheme.onBackground
    val surfVar   = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(bg),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 🔥 1. PROFILE HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(primary.copy(alpha = 0.1f), bg)
                        )
                    )
                    .statusBarsPadding()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var profileImage by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(user?.uid) {
                        user?.uid?.let { uid ->
                            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                .addOnSuccessListener { doc -> profileImage = doc.getString("imageUrl") }
                        }
                    }
                    val effectiveAvatar = profileImage
                        ?: "https://ui-avatars.com/api/?name=${userName.replace(" ", "+")}&background=1E2A78&color=fff&bold=true&rounded=true&size=200"

                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        border = BorderStroke(3.dp, Color.White),
                        color = surfVar,
                        shadowElevation = 12.dp
                    ) {
                        AsyncImage(
                            model = effectiveAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(userName, color = onBg, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Text(user?.email ?: "account@workly.com", color = onBg.copy(alpha = 0.6f), fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(16.dp))
                                Text(" $userRating Rating", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = primary, modifier = Modifier.size(16.dp))
                                Text(" $jobsCompleted Jobs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface)
                            }
                        }
                    }
                }
            }
        }

        // 🔥 2. EARNINGS CARD (Provider Specific)
        if (userRole == "provider") {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = primary,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Earnings", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹$lifetimeEarnings", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.15f)) {
                                Text("+ ₹500 today", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.1f))) {
                            Box(modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight().background(Color.White))
                        }
                    }
                }
            }
        }

        // 🔥 3. QUICK ACTIONS
        item {
            var showAddressSheet by remember { mutableStateOf(false) }
            var showSupportSheet by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (userRole == "provider") {
                    val providerActions = listOf(
                        Triple("My Orders", Icons.Default.ReceiptLong, { context.startActivity(Intent(context, ProviderOrdersActivity::class.java)) }),
                        Triple("My Services", Icons.Default.Inventory2, { context.startActivity(Intent(context, MyServicesActivity::class.java)) }),
                        Triple("Add Service", Icons.Default.AddBusiness, { context.startActivity(Intent(context, AddServiceActivity::class.java)) })
                    )
                    providerActions.forEach { (label, icon, action) ->
                        Surface(
                            modifier = Modifier.weight(1f).height(90.dp).clickable { action() },
                            shape = RoundedCornerShape(20.dp),
                            color = surfVar,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primary)
                            }
                        }
                    }
                } else {
                    // Bookings
                    Surface(
                        modifier = Modifier.weight(1f).height(90.dp).clickable { context.startActivity(Intent(context, MyBookingsActivity::class.java)) },
                        shape = RoundedCornerShape(20.dp), color = surfVar, shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, null, tint = primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primary)
                        }
                    }
                    // Addresses
                    Surface(
                        modifier = Modifier.weight(1f).height(90.dp).clickable { showAddressSheet = true },
                        shape = RoundedCornerShape(20.dp), color = surfVar, shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, null, tint = primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Addresses", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primary)
                        }
                    }
                    // Support
                    Surface(
                        modifier = Modifier.weight(1f).height(90.dp).clickable { showSupportSheet = true },
                        shape = RoundedCornerShape(20.dp), color = surfVar, shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HelpCenter, null, tint = primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Support", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primary)
                        }
                    }
                }
            }

            // Quick action sheets
            if (showAddressSheet) { AddressesSheet { showAddressSheet = false } }
            if (showSupportSheet) { SupportSheet { showSupportSheet = false } }
        }

        // 🔥 4 & 5. SECTIONS
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)) {

                // ── Sheet visibility states ──
                var showProfileInfo by remember { mutableStateOf(false) }
                var showPaymentMethods by remember { mutableStateOf(false) }
                var showSecurity by remember { mutableStateOf(false) }
                var showNotifications by remember { mutableStateOf(false) }
                var showLanguage by remember { mutableStateOf(false) }
                var showPrivacyPolicy by remember { mutableStateOf(false) }
                var showRateWorkly by remember { mutableStateOf(false) }
                var showAboutWorkly by remember { mutableStateOf(false) }
                
                DashboardSection("Account Dashboard") {
                    DashboardItem(Icons.Default.ManageAccounts, "Profile Information", "Update name, email & phone") { showProfileInfo = true }
                    DashboardItem(Icons.Default.Payment, "Payment Methods", "Manage cards & UPI") { showPaymentMethods = true }
                    DashboardItem(Icons.Default.VpnKey, "Security", "Passwords & permissions") { showSecurity = true }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Theme Logic Integration
                val themeDataStore = remember { ThemeDataStore(context) }
                val currentTheme by themeDataStore.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
                var showThemeSheet by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                val themeSubtitle = when(currentTheme) {
                    ThemeMode.LIGHT -> "Light Mode ☀️"
                    ThemeMode.DARK -> "Dark Mode 🌙"
                    ThemeMode.SYSTEM -> "System Default ⚙️"
                }

                DashboardSection("Preferences") {
                    DashboardItem(Icons.Default.NotificationsActive, "Notifications", "Alerts & updates") { showNotifications = true }
                    DashboardItem(Icons.Default.Language, "Language", "English (India)") { showLanguage = true }
                    DashboardItem(Icons.Default.DarkMode, "Appearance", themeSubtitle) {
                        showThemeSheet = true
                    }
                }

                if (showThemeSheet) {
                    AppearanceBottomSheet(
                        currentMode = currentTheme,
                        onDismiss = { showThemeSheet = false },
                        onModeSelected = { mode ->
                            coroutineScope.launch {
                                themeDataStore.setThemeMode(mode)
                                delay(200)
                                showThemeSheet = false
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DashboardSection("Support & Trust") {
                    DashboardItem(Icons.Default.Shield, "Privacy Policy", "How we protect your data") { showPrivacyPolicy = true }
                    DashboardItem(Icons.Default.StarRate, "Rate Workly", "Share your feedback") { showRateWorkly = true }
                    DashboardItem(Icons.Default.Info, "About Workly", "Version 1.0.4 Premium") { showAboutWorkly = true }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // LOGOUT
                Surface(
                    onClick = { onLogout() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFEBEE).copy(alpha = if (isSystemInDarkTheme()) 0.1f else 1f),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Log Out", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // ── Bottom Sheets ──
                if (showProfileInfo) { ProfileInfoSheet { showProfileInfo = false } }
                if (showPaymentMethods) { PaymentMethodsSheet { showPaymentMethods = false } }
                if (showSecurity) { SecuritySheet { showSecurity = false } }
                if (showNotifications) { NotificationsSheet { showNotifications = false } }
                if (showLanguage) { LanguageSheet { showLanguage = false } }
                if (showPrivacyPolicy) { PrivacyPolicySheet { showPrivacyPolicy = false } }
                if (showRateWorkly) { RateWorklySheet { showRateWorkly = false } }
                if (showAboutWorkly) { AboutWorklySheet { showAboutWorkly = false } }
            }
        }
    }
}

@Composable
fun DashboardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DashboardItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceBottomSheet(
    currentMode: com.example.workly.theme.ThemeMode,
    onDismiss: () -> Unit,
    onModeSelected: (com.example.workly.theme.ThemeMode) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp)) {
            Text("Appearance", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
            val options = listOf(
                Pair(com.example.workly.theme.ThemeMode.LIGHT, "Light Mode        ☀️"),
                Pair(com.example.workly.theme.ThemeMode.DARK,  "Dark Mode         🌙"),
                Pair(com.example.workly.theme.ThemeMode.SYSTEM,"System Default    ⚙️")
            )
            options.forEach { (mode, label) ->
                val isSelected = currentMode == mode
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { if (!isSelected) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onModeSelected(mode) } }.padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

// ─── Shared Components ───────────────────────────────────────────────────────
@Composable
fun PremiumServiceCard(title: String, gradient: Brush, icon: androidx.compose.ui.graphics.vector.ImageVector, glowPulse: Boolean = false, rippleEffect: Boolean = false, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.1f, targetValue = 0.3f, animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "")
    
    Surface(
        modifier = Modifier
            .size(130.dp, 145.dp)
            .shadow(12.dp, RoundedCornerShape(26.dp))
            .clickable { onClick() }, 
        shape = RoundedCornerShape(26.dp), 
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize().background(gradient)) {
            if (glowPulse) {
                Box(modifier = Modifier.align(Alignment.Center).size(80.dp).background(Brush.radialGradient(listOf(Color.White.copy(alpha = glowAlpha), Color.Transparent)), CircleShape))
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp), 
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon, 
                            contentDescription = null, 
                            modifier = Modifier.size(32.dp), 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    title, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Black, 
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PremiumServiceDetailCard(service: Service) {
    val context = LocalContext.current
    Surface(modifier = Modifier.width(260.dp).shadow(4.dp, RoundedCornerShape(20.dp)).clickable { context.startActivity(Intent(context, Class.forName("com.example.workly.home.ServiceDetailActivity")).apply { putExtra("SERVICE_TITLE", service.title); putExtra("SERVICE_PRICE", service.price); putExtra("SERVICE_CATEGORY", service.category); putExtra("SERVICE_ID", service.id); putExtra("SERVICE_DURATION", service.duration); putExtra("SERVICE_DESC", service.description); putExtra("SERVICE_IMG", service.imageUrl.ifEmpty { "https://placehold.co/600x400/1E2A78/FFFFFF?text=Service" }) }) }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
        Column {
            Box {
                AsyncImage(model = service.imageUrl.ifEmpty { "https://placehold.co/600x400/1E2A78/FFFFFF?text=Service" }, contentDescription = service.title, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f)))))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(service.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(16.dp))
                    Text(" 4.8 (2.1k reviews)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${service.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("⏱ ${service.duration}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PremiumStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(label.uppercase(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        if (onSeeAll != null) {
            Text("See All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onSeeAll() })
        }
    }
}

@Composable
fun UpcomingBookingCard(booking: com.example.workly.data.Order) {
    val context = LocalContext.current
    val status = (booking.status ?: "pending").lowercase()
    val statusColor = when (status) {
        "accepted" -> Color(0xFF3B82F6)
        "arriving" -> Color(0xFF8B5CF6)
        "started" -> MaterialTheme.colorScheme.primary
        else -> Color(0xFF64748B)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable {
                val intent = Intent(context, com.example.workly.booking.TrackOrderActivity::class.java).apply {
                    putExtra("ORDER_ID", booking.id)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(booking.serviceCategory.lowercase()) {
                                "cleaning" -> Icons.Default.CleaningServices
                                "plumbing" -> Icons.Default.WaterDrop
                                "electric" -> Icons.Default.FlashOn
                                else -> Icons.Default.Handyman
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.serviceName.ifEmpty { booking.serviceTitle }, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 15.sp, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Today at ${booking.time}", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(shape = RoundedCornerShape(10.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(
                        status.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingBottomBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
        Triple("Explore", Icons.Default.Explore, Icons.Outlined.Explore),
        Triple("Messages", Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline),
        Triple("Profile", Icons.Default.Person, Icons.Outlined.Person)
    )
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            .shadow(24.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                val isSelected = selectedItem == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { 
                            if (!isSelected) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onItemSelected(index) 
                            }
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.height(34.dp)) {
                        if (isSelected) {
                            Surface(
                                modifier = Modifier.size(width = 54.dp, height = 30.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {}
                        }
                        Icon(
                            if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        label,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }
    }
}
