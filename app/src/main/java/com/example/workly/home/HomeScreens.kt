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

// ─── Dimensional Void Color Palette ──────────────────────────────────────────
private val VoidBlack       = Color(0xFF0E0E0E)
private val SurfaceCard     = Color(0xFF201F1F)
private val SurfaceCardLow  = Color(0xFF1C1B1B)
private val SurfaceCardHigh = Color(0xFF2A2A2A)
private val SurfaceHighest  = Color(0xFF353534)
private val OnSurfaceLight  = Color(0xFFE5E2E1)
private val OutlineGray     = Color(0xFF919191)
private val BorderFaint     = Color.White.copy(alpha = 0.05f)
private val NavGlass        = Color(0xFF1A1A1A).copy(alpha = 0.8f)

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {

        // ── Top App Bar ──────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A1A).copy(alpha = 0.95f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Menu icon
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    // App Name
                    Text(
                        "WORKLY",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    )
                    // Profile Avatar
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        AsyncImage(
                            model = effectiveAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // ── Hero / Search ────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                Text(
                    "Command",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 52.sp,
                    letterSpacing = (-1).sp
                )
                Text(
                    "The Void.",
                    color = OutlineGray,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 52.sp,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                // Search bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) },
                    shape = CircleShape,
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = OutlineGray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search services...", color = OutlineGray.copy(alpha = 0.6f), fontSize = 15.sp)
                    }
                }
            }
        }

        // ── Bento Grid — Row 1: Plumbing (large) + Cleaning (small) ─────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(320.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LARGE: Plumbing card
                BentoCard(
                    modifier = Modifier.weight(1.6f).fillMaxHeight(),
                    onClick = {
                        context.startActivity(Intent(context, ServicesActivity::class.java).apply {
                            putExtra("CATEGORY", "Plumbing")
                        })
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(140.dp)
                                .background(
                                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)),
                                    CircleShape
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    "Premier Tier",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Plumbing",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Precision hydraulic systems & emergency services.",
                                color = OnSurfaceLight.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(18.dp)
                                .size(36.dp)
                        )
                    }
                }

                // SMALL: Cleaning card
                BentoCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = {
                        context.startActivity(Intent(context, ServicesActivity::class.java).apply {
                            putExtra("CATEGORY", "Cleaning")
                        })
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceCardHigh
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CleaningServices, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Cleaning", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Molecular-level sanitation.",
                                color = OnSurfaceLight.copy(alpha = 0.55f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        // Rating box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceHighest.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("4.9", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                                Text("RATING", color = OutlineGray, fontSize = 8.sp, letterSpacing = 2.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Spacing ──────────────────────────────────────────────────────────
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // ── Bento Grid — Row 2: Tech Support + Explore All ───────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tech Support card
                BentoCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = {
                        context.startActivity(Intent(context, ServicesActivity::class.java).apply {
                            putExtra("CATEGORY", "Tech")
                        })
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.07f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(120.dp)
                                .offset(x = 30.dp, y = 20.dp)
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(20.dp)
                        ) {
                            Text("Tech Support", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Hardware & software diagnostics.",
                                color = OnSurfaceLight.copy(alpha = 0.55f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(context, ServicesActivity::class.java))
                                }
                            ) {
                                Text(
                                    "Connect Now",
                                    color = Color(0xFF0E0E0E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Explore All — White contrast card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSeeAllServices() },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Explore\nAll Tiers",
                            color = Color(0xFF3B3B3B),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val cats = listOf(
                                Pair(Icons.Default.ElectricBolt, false),
                                Pair(Icons.Default.Build, false),
                                Pair(Icons.Default.Security, false),
                                Pair(Icons.Default.Add, true)
                            )
                            cats.forEach { (icon, isDark) ->
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, null,
                                            tint = if (isDark) Color.White else Color(0xFF424242),
                                            modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Spacing ──────────────────────────────────────────────────────────
        item { Spacer(modifier = Modifier.height(24.dp)) }

        // ── Upcoming Booking (if any) ────────────────────────────────────────
        if (bookings.isNotEmpty()) {
            item {
                VoidSectionHeader("Upcoming Booking", null)
                Spacer(modifier = Modifier.height(12.dp))
                UpcomingBookingCard(bookings.first())
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ── Top Rated Services ───────────────────────────────────────────────
        item {
            VoidSectionHeader("Top Rated Services") { onSeeAllServices() }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(popularServices.size) { idx ->
                    VoidServiceCard(popularServices[idx])
                }
            }
        }

        // ── Stats Section ────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(32.dp))
            VoidSectionHeader("Platform Stats", null)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("12k+", "Serviced"),
                    Pair("14ms", "Response"),
                    Pair("99.9%", "Success"),
                    Pair("★ Gold", "Status")
                ).forEach { (value, label) ->
                    VoidStatCard(value = value, label = label, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Promo Banner ─────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) }
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
                            fontSize = 17.sp
                        )
                        Text(
                            "Get 20% off your first service!",
                            color = OnSurfaceLight.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
                            Text(
                                "Use code FIRST20",
                                color = Color(0xFF0E0E0E),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Icon(
                        Icons.Default.LocalOffer,
                        null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

// ─── Bento Card Container ──────────────────────────────────────────────────
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        content()
    }
}

// ─── Void Section Header ───────────────────────────────────────────────────
@Composable
fun VoidSectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(
                    "See All",
                    color = OutlineGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ─── Keep old SectionHeader for compat ────────────────────────────────────
@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    VoidSectionHeader(title, onSeeAll)
}

// ─── Void Service Card ─────────────────────────────────────────────────────
@Composable
fun VoidServiceCard(service: Service) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable {
                context.startActivity(
                    Intent(context, Class.forName("com.example.workly.home.ServiceDetailActivity")).apply {
                        putExtra("SERVICE_TITLE", service.title)
                        putExtra("SERVICE_PRICE", service.price)
                        putExtra("SERVICE_CATEGORY", service.category)
                        putExtra("SERVICE_ID", service.id)
                        putExtra("SERVICE_DURATION", service.duration)
                        putExtra("SERVICE_DESC", service.description)
                        putExtra("SERVICE_IMG", service.imageUrl.ifEmpty { getServiceCardImageUrl(service.title, service.category) })
                        putExtra("PROVIDER_NAME", service.providerName)
                        putExtra("PROVIDER_ID", service.providerId)
                    })
            },
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column {
            AsyncImage(
                model = service.imageUrl.ifEmpty { getServiceCardImageUrl(service.title, service.category) },
                contentDescription = service.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    service.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(12.dp))
                    Text(
                        " " + if (service.rating > 0) service.rating.toString() else "4.8",
                        fontSize = 11.sp,
                        color = OutlineGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "₹${service.price.toInt()}+",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── Keep old PopularServiceCard as alias ─────────────────────────────────
@Composable
fun PopularServiceCard(service: Service) = VoidServiceCard(service)

// ─── Void Stat Card ─────────────────────────────────────────────────────────
@Composable
fun VoidStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCardLow,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label.uppercase(),
                color = OutlineGray,
                fontSize = 8.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Upcoming Booking Card ──────────────────────────────────────────────────
@Composable
fun UpcomingBookingCard(booking: Order) {
    val statusColor = when (booking.status) {
        OrderStatus.ACCEPTED -> Color(0xFF00BCD4)
        OrderStatus.PENDING  -> Color(0xFFFF9800)
        OrderStatus.COMPLETED -> Color(0xFF4CAF50)
        else -> OutlineGray
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Handyman, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                val dateStr = booking.createdAt.toDate().toString()
                Text(dateStr, color = OutlineGray, fontSize = 11.sp)
                if (booking.providerName.isNotEmpty()) {
                    Text("Pro: ${booking.providerName}", color = Color(0xFF64B5F6), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(30.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF141414).copy(alpha = 0.98f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.height(36.dp)) {
                        if (isSelected) {
                            // White glow pill behind active icon
                            Box(
                                modifier = Modifier
                                    .size(width = 60.dp, height = 32.dp)
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            )
                        }
                        Icon(
                            if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Profile header — dark gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A1A), VoidBlack)
                        )
                    )
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
                        ?: "https://ui-avatars.com/api/?name=${userName.replace(" ", "+")}&background=1a1a1a&color=fff&bold=true&rounded=true&size=200"
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)),
                        color = SurfaceCard
                    ) {
                        AsyncImage(
                            model = effectiveAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(user?.email ?: "your@email.com", color = OutlineGray, fontSize = 13.sp)
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoidSectionLabel("Account")
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
                VoidProfileMenuItem(Icons.Default.ReceiptLong, bookingsLabel, bookingsSubtitle) {
                    if (userRole == "provider") {
                        context.startActivity(Intent(context, ProviderOrdersActivity::class.java))
                    } else {
                        context.startActivity(Intent(context, MyBookingsActivity::class.java))
                    }
                }
                VoidProfileMenuItem(Icons.Default.LocationOn, "Saved Addresses", "Home, work & more") {}

                if (userRole == "provider") {
                    Spacer(modifier = Modifier.height(4.dp))
                    VoidSectionLabel("Provider Tools")
                    VoidProfileMenuItem(Icons.Default.AddBusiness, "My Services", "Manage your listed services") {
                        context.startActivity(Intent(context, MyServicesActivity::class.java))
                    }
                    VoidProfileMenuItem(Icons.Default.PostAdd, "Add Service", "Create a new service listing") {
                        context.startActivity(Intent(context, AddServiceActivity::class.java))
                    }
                    VoidProfileMenuItem(Icons.Default.AccountBalanceWallet, "Earnings", "₹$lifetimeEarnings from completed orders") {
                        context.startActivity(Intent(context, ProviderEarningsActivity::class.java))
                    }
                }

                if (userRole == "admin") {
                    Spacer(modifier = Modifier.height(4.dp))
                    VoidSectionLabel("Management")
                    VoidProfileMenuItem(Icons.Default.AdminPanelSettings, "Admin Dashboard", "Manage approvals & providers") {
                        context.startActivity(Intent(context, AdminDashboardActivity::class.java))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                VoidSectionLabel("Preferences")
                VoidProfileMenuItem(Icons.Default.Notifications, "Notifications", "Push, SMS & email") {}
                VoidProfileMenuItem(Icons.Default.Language, "Language", "English") {}
                VoidProfileMenuItem(Icons.Default.Palette, "Appearance", "Dark Mode") {}

                Spacer(modifier = Modifier.height(4.dp))
                VoidSectionLabel("Support")
                VoidProfileMenuItem(Icons.Default.HelpOutline, "Help & Support", "FAQ, live chat") {}
                VoidProfileMenuItem(Icons.Default.Star, "Rate the App", "Share your feedback") {}
                VoidProfileMenuItem(Icons.Default.Info, "About Workly", "Version 1.0.0") {}

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() },
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Red.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VoidSectionLabel(text: String) {
    Text(
        text,
        color = OutlineGray,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoidProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.06f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                Text(subtitle, fontSize = 12.sp, color = OutlineGray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = OutlineGray.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) = VoidProfileMenuItem(icon, title, subtitle, onClick)
