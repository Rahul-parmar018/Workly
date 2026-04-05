package com.example.workly.home

import android.content.Intent
import android.widget.Toast
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

// ─── Workly Premium Color Palette ──────────────────────────────────────────
private val WorklyBlueDeep   = Color(0xFF1E2A78)
private val WorklyBlueLight  = Color(0xFF2D3FA3)
private val WorklyBgLight    = Color(0xFFEAF6FF)
private val WorklyPureWhite  = Color(0xFFFFFFFF)
private val WorklyPillGray   = Color(0xFFF1F5F9)
private val WorklyTextDeep   = Color(0xFF0F172A)
private val WorklyTextMuted  = Color(0xFF64748B)

private val CleaningGradient = Brush.verticalGradient(listOf(Color(0xFFEAF6FF), Color(0xFFFFFFFF)))
private val ElectricGradient = Brush.verticalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFE0B2)))
private val PlumbingGradient = Brush.verticalGradient(listOf(Color(0xFFE6FFFA), Color(0xFFCCF2F4)))
private val CtaGradient      = Brush.horizontalGradient(listOf(WorklyBlueDeep, WorklyBlueLight))

// ─── Home Screen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    userName: String,
    userRole: String,
    onSeeAllServices: () -> Unit
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "What do you need cleaned today?",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Profile Avatar
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
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
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search for services...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 16.sp)
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
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
                        iconUrl = "https://cdn3d.iconscout.com/3d/premium/thumb/cleaning-vacuum-7170068-5813735.png",
                        onClick = { /* Navigate */ }
                    )
                }
                item {
                    PremiumServiceCard(
                        title = "Electric",
                        gradient = ElectricGradient,
                        iconUrl = "https://cdn3d.iconscout.com/3d/premium/thumb/electricity-flash-5349603-4475459.png",
                        glowPulse = true,
                        onClick = { /* Navigate */ }
                    )
                }
                item {
                    PremiumServiceCard(
                        title = "Plumbing",
                        gradient = PlumbingGradient,
                        iconUrl = "https://cdn3d.iconscout.com/3d/premium/thumb/plumbing-9190184-7546377.png",
                        rippleEffect = true,
                        onClick = { /* Navigate */ }
                    )
                }
                item {
                    PremiumServiceCard(
                        title = "Kitchen Cleaning",
                        gradient = CleaningGradient,
                        iconUrl = "https://cdn3d.iconscout.com/3d/premium/thumb/dishwashing-7170077-5813744.png",
                        onClick = { /* Navigate */ }
                    )
                }
            }
        }

        // ── Quick Book CTA ───────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(12.dp, RoundedCornerShape(29.dp)),
                    shape = RoundedCornerShape(29.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CtaGradient)
                            .clickable { /* Action */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "⚡ Book Now",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No booking fee • Cancel anytime",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

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
                    .background(WorklyBlueDeep)
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
                                color = WorklyBlueDeep,
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(50.dp))
                    ) {
                        Text(
                            trustItems[idx],
                            color = MaterialTheme.colorScheme.onSurface,
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

// ─── Premium Service Card (3D + Glow) ──────────────────────────────────────
@Composable
fun PremiumServiceCard(
    title: String,
    gradient: Brush,
    iconUrl: String,
    glowPulse: Boolean = false,
    rippleEffect: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val scale by animateFloatAsState(targetValue = 1f)

    Surface(
        modifier = Modifier
            .size(140.dp, 160.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Glow effect behind icon
            if (glowPulse) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(listOf(Color.White.copy(alpha = glowAlpha), Color.Transparent)),
                            CircleShape
                        )
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    title,
                    color = Color(0xFF0F172A), // always dark text — readable on light gradients
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Premium Service Detail Card (Top Services) ─────────────────────────────
@Composable
fun PremiumServiceDetailCard(service: Service) {
    val context = LocalContext.current
    val cardBg  = MaterialTheme.colorScheme.surface
    val onCard  = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .width(260.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable {
                context.startActivity(
                    Intent(context, Class.forName("com.example.workly.home.ServiceDetailActivity")).apply {
                        putExtra("SERVICE_TITLE", service.title)
                        putExtra("SERVICE_PRICE", service.price)
                        putExtra("SERVICE_CATEGORY", service.category)
                        putExtra("SERVICE_ID", service.id)
                        putExtra("SERVICE_DURATION", service.duration)
                        putExtra("SERVICE_DESC", service.description)
                        putExtra("SERVICE_IMG", service.imageUrl.ifEmpty { "https://placehold.co/600x400/1E2A78/FFFFFF?text=Service" })
                    })
            },
        shape = RoundedCornerShape(20.dp),
        color = cardBg
    ) {
        Column {
            Box {
                AsyncImage(
                    model = service.imageUrl.ifEmpty { "https://placehold.co/600x400/1E2A78/FFFFFF?text=Service" },
                    contentDescription = service.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f))
                            )
                        )
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(service.title, color = onCard, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(16.dp))
                    Text(" 4.8 (2.1k reviews)", color = onCard.copy(alpha = 0.55f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("₹${service.price.toInt()}", color = primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("⏱ ${service.duration}", color = onCard.copy(alpha = 0.55f), fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Premium Stat Card ──────────────────────────────────────────────────────
@Composable
fun PremiumStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(label.uppercase(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// ─── Section Header ────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        if (onSeeAll != null) {
            Text(
                "See All",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}
// ─── Upcoming Booking Card (Legacy but kept for logic) ──────────────────────
@Composable
fun UpcomingBookingCard(booking: Order) {
    val statusColor = when (booking.status) {
        OrderStatus.ACCEPTED -> WorklyBlueDeep
        OrderStatus.PENDING  -> Color(0xFFFF9800)
        OrderStatus.COMPLETED -> Color(0xFF4CAF50)
        else -> WorklyTextMuted
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = WorklyBgLight.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, WorklyBlueDeep.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WorklyPureWhite,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Handyman, null, tint = WorklyBlueDeep, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorklyTextDeep)
                Text("Scheduled for today", color = WorklyTextMuted, fontSize = 11.sp)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.12f)) {
                Text(
                    booking.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Floating Bottom Bar ───────────────────────────────────────────────────
@Composable
fun FloatingBottomBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
        Triple("Explore", Icons.Default.Explore, Icons.Outlined.Explore),
        Triple("Messages", Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline),
        Triple("Profile", Icons.Default.Person, Icons.Outlined.Person)
    )
    val navBg = MaterialTheme.colorScheme.surface
    val navActive = MaterialTheme.colorScheme.primary
    val navInactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = navBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                val isSelected = selectedItem == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(index) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.height(34.dp)) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 32.dp)
                                    .background(navActive.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                            )
                        }
                        Icon(
                            if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = label,
                            tint = if (isSelected) navActive else navInactive,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        label,
                        color = if (isSelected) navActive else navInactive,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─── Profile Screen ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(userName: String, userRole: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    var lifetimeEarnings by remember { mutableIntStateOf(0) }
    LaunchedEffect(user?.uid) {
        if (userRole == "provider" && user != null) {
            FirebaseFirestore.getInstance().collection("orders")
                .whereEqualTo("providerId", user.uid)
                .whereIn("status", listOf("accepted", "completed"))
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        var total = 0
                        for (doc in snapshot.documents) {
                            val amount = doc.getDouble("finalPrice") ?: doc.getDouble("price") ?: 0.0
                            total += amount.toInt()
                        }
                        lifetimeEarnings = total
                    }
                }
        }
    }

    val profileBg = MaterialTheme.colorScheme.background
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(profileBg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Profile header — premium blue gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(CtaGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    var profileImage by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(user?.uid) {
                        user?.uid?.let { uid ->
                            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                .addOnSuccessListener { doc -> profileImage = doc.getString("imageUrl") }
                        }
                    }
                    val effectiveAvatar = profileImage
                        ?: "https://ui-avatars.com/api/?name=${userName.replace(" ", "+")}&background=fff&color=1E2A78&bold=true&rounded=true&size=200"
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        AsyncImage(
                            model = effectiveAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(user?.email ?: "your@email.com", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumSectionLabel("Account")
                val bookingsLabel = when (userRole) {
                    "provider" -> "My Orders"
                    "admin" -> "All Orders"
                    else -> "My Bookings"
                }
                val bookingsSubtitle = when (userRole) {
                    "provider" -> "View incoming service orders"
                    "admin"    -> "View & manage all orders"
                    else       -> "View all your bookings"
                }
                PremiumProfileMenuItem(Icons.Default.ReceiptLong, bookingsLabel, bookingsSubtitle) {
                    if (userRole == "provider") {
                        context.startActivity(Intent(context, ProviderOrdersActivity::class.java))
                    } else {
                        context.startActivity(Intent(context, MyBookingsActivity::class.java))
                    }
                }
                PremiumProfileMenuItem(Icons.Default.LocationOn, "Saved Addresses", "Home, work & more") {}

                if (userRole == "provider") {
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumSectionLabel("Provider Tools")
                    PremiumProfileMenuItem(Icons.Default.AddBusiness, "My Services", "Manage your listed services") {
                        context.startActivity(Intent(context, MyServicesActivity::class.java))
                    }
                    PremiumProfileMenuItem(Icons.Default.PostAdd, "Add Service", "Create a new service listing") {
                        context.startActivity(Intent(context, AddServiceActivity::class.java))
                    }
                    PremiumProfileMenuItem(Icons.Default.AccountBalanceWallet, "Earnings", "₹$lifetimeEarnings from completed orders") {
                        context.startActivity(Intent(context, ProviderEarningsActivity::class.java))
                    }
                }

                if (userRole == "admin") {
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumSectionLabel("Management")
                    PremiumProfileMenuItem(Icons.Default.AdminPanelSettings, "Admin Dashboard", "Manage approvals & providers") {
                        context.startActivity(Intent(context, AdminDashboardActivity::class.java))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                PremiumSectionLabel("Preferences")
                PremiumProfileMenuItem(Icons.Default.Notifications, "Notifications", "Push, SMS & email") {}

                // Theme Selection state
                var showThemeSheet by remember { mutableStateOf(false) }
                val themeDataStore = remember { ThemeDataStore(context) }
                val currentTheme by themeDataStore.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
                val coroutineScope = rememberCoroutineScope()


                val themeSubtitle = when(currentTheme) {
                    ThemeMode.LIGHT -> "Light Mode ☀️"
                    ThemeMode.DARK -> "Dark Mode 🌙"
                    ThemeMode.SYSTEM -> "System Default ⚙️"
                }

                PremiumProfileMenuItem(Icons.Default.Palette, "Appearance", themeSubtitle) {
                    showThemeSheet = true
                }

                if (showThemeSheet) {
                    AppearanceBottomSheet(
                        currentMode = currentTheme,
                        onDismiss = { showThemeSheet = false },
                        onModeSelected = { mode ->
                            coroutineScope.launch {
                                themeDataStore.setThemeMode(mode)
                                // Auto-close after short delay for premium feel
                                kotlinx.coroutines.delay(200)
                                showThemeSheet = false
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Red.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sign Out", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumSectionLabel(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(44.dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
        }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Appearance",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val options = listOf(
                Pair(com.example.workly.theme.ThemeMode.LIGHT, "Light Mode        ☀️"),
                Pair(com.example.workly.theme.ThemeMode.DARK,  "Dark Mode         🌙"),
                Pair(com.example.workly.theme.ThemeMode.SYSTEM,"System Default    ⚙️")
            )

            options.forEach { (mode, label) ->
                val isSelected = currentMode == mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!isSelected) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onModeSelected(mode)
                            }
                        }
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null, // Handled by Row click
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
