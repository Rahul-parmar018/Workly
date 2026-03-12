package com.example.workly.home

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.booking.BookingActivity
import com.example.workly.booking.MyBookingsActivity
import com.example.workly.admin.AdminDashboardActivity
import com.example.workly.data.Booking
import com.example.workly.data.Service
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// ─── Home Screen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    onSeeAllServices: () -> Unit
) {
    val context = LocalContext.current
    val bookings by viewModel.upcomingBookings.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser

    val banners = listOf(
        Triple("Professional Cleaning", "Starting ₹40/hr · Top rated pros", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=700&fit=crop"),
        Triple("Expert Repairs", "AC, fridge, washing machine & more", "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=700&fit=crop"),
        Triple("Plumbing & Electric", "24/7 emergency services available", "https://images.unsplash.com/photo-1621905252507-b35492cc74b4?w=700&fit=crop"),
        Triple("Beauty & Wellness", "Salon at home · Yoga · Massage", "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=700&fit=crop"),
    )
    val pagerState = rememberPagerState { banners.size }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
        }
    }

    val popularServices = remember { getAllServices().take(6) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // ── Header ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(ProfessionalBlue, ProfessionalBlue.copy(0.85f))))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    val avatarUrl = user?.let { "https://ui-avatars.com/api/?name=${(it.displayName ?: "U").replace(" ", "+")}&background=ffffff&color=1565C0&bold=true&rounded=true&size=120" }
                    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.White.copy(0.2f)) {
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hello, ${user?.displayName?.split(" ")?.firstOrNull() ?: "there"} 👋", color = Color.White.copy(0.85f), fontSize = 13.sp)
                        Text("What do you need today?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    // Notifications
                    IconButton(onClick = {}) {
                        Surface(shape = CircleShape, color = Color.White.copy(0.15f)) {
                            Icon(Icons.Default.NotificationsNone, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }

        // ── Search Bar ──
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-12).dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) },
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Search from 33+ services...", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = BackgroundGray) {
                        Icon(Icons.Default.Tune, null, tint = ProfessionalBlue, modifier = Modifier.padding(6.dp).size(16.dp))
                    }
                }
            }
        }

        // ── Hero Banner ──
        item {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 4.dp)) { page ->
                val (title, subtitle, imageUrl) = banners[page]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent)))
                    )
                    Column(modifier = Modifier.align(Alignment.CenterStart).padding(20.dp)) {
                        Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(subtitle, color = Color.White.copy(0.85f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) }
                        ) {
                            Text("Book Now →", color = ProfessionalBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                        }
                    }
                }
            }
            // Pager dots
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.Center) {
                repeat(banners.size) { idx ->
                    val isActive = pagerState.currentPage == idx
                    Surface(
                        modifier = Modifier.padding(horizontal = 3.dp).width(if (isActive) 20.dp else 6.dp).height(6.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = if (isActive) ProfessionalBlue else Color.LightGray
                    ) {}
                }
            }
        }

        // ── Upcoming Booking (if any) ──
        if (bookings.isNotEmpty()) {
            item {
                val booking = bookings.first()
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader("Upcoming Booking", null)
                UpcomingBookingCard(booking)
            }
        }

        // ── Categories ──
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Categories") { context.startActivity(Intent(context, ServicesActivity::class.java)) }
            Spacer(modifier = Modifier.height(12.dp))
            val categories = listOf("Cleaning","Repair","Plumbing","Electric","Wellness","Tech","Auto","Events")
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories.size) { idx ->
                    val cat = categories[idx]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(context, ServicesActivity::class.java).apply { putExtra("CATEGORY", cat) })
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = getCategoryColor(cat).copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(getCategoryIcon(cat), null, tint = getCategoryColor(cat), modifier = Modifier.size(30.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }

        // ── Popular Services ──
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader("Top Rated Services") { onSeeAllServices() }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(popularServices.size) { idx ->
                    val service = popularServices[idx]
                    PopularServiceCard(service)
                }
            }
        }

        // ── Promo Banner ──
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF00BCD4))))
                    .clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) }
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🎉 First Booking?", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text("Get 20% off your first service!", color = Color.White.copy(0.9f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
                            Text("Use code FIRST20", color = ProfessionalBlue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                    Icon(Icons.Default.LocalOffer, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(56.dp))
                }
            }
        }
    }
}

// ─── Section Header ────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("See All", color = ProfessionalBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ─── Upcoming Booking Card ──────────────────────────────────────────────────
@Composable
fun UpcomingBookingCard(booking: Booking) {
    val statusColor = when (booking.status) {
        "Confirmed" -> ElectricTeal
        "InProgress" -> EnergyOrange
        else -> TextSecondary
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = ProfessionalBlue.copy(0.1f), modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Handyman, null, tint = ProfessionalBlue, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${booking.date} ${booking.time}".trim(), color = TextSecondary, fontSize = 12.sp)
                if (booking.providerName.isNotEmpty()) {
                    Text("Pro: ${booking.providerName}", color = ProfessionalBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(0.12f)) {
                Text(booking.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

// ─── Popular Service Horizontal Card ───────────────────────────────────────
@Composable
fun PopularServiceCard(service: Service) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .clickable {
                context.startActivity(Intent(context, BookingActivity::class.java).apply {
                    putExtra("SERVICE_NAME", service.name)
                    putExtra("SERVICE_PRICE", service.basePrice)
                    putExtra("SERVICE_CATEGORY", service.category)
                    putExtra("SERVICE_ID", service.id)
                })
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column {
            AsyncImage(
                model = getServiceCardImageUrl(service.name, service.category),
                contentDescription = service.name,
                modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(service.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = EnergyOrange, modifier = Modifier.size(12.dp))
                    Text(" 4.8", fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("₹${service.basePrice.toInt()}+", fontWeight = FontWeight.ExtraBold, color = ProfessionalBlue, fontSize = 14.sp)
            }
        }
    }
}

// ─── Floating Bottom Bar ───────────────────────────────────────────────────
@Composable
fun FloatingBottomBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        Pair("Home", Icons.Default.Home),
        Pair("Explore", Icons.Default.Explore),
        Pair("Messages", Icons.Default.ChatBubbleOutline),
        Pair("Profile", Icons.Default.Person)
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF1A1A2E)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, (label, icon) ->
                val isSelected = selectedItem == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(index) }
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ProfessionalBlue else Color.Transparent,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon, null,
                                tint = if (isSelected) Color.White else Color.White.copy(0.45f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        label,
                        color = if (isSelected) Color.White else Color.White.copy(0.45f),
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
fun ProfileScreenContent(onLogout: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val avatarUrl = user?.let { "https://ui-avatars.com/api/?name=${(it.displayName ?: "User").replace(" ", "+")}&background=1565C0&color=fff&bold=true&rounded=true&size=200" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Profile header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(ProfessionalBlue, ElectricTeal)))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(modifier = Modifier.size(76.dp), shape = CircleShape, border = BorderStroke(3.dp, Color.White)) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user?.displayName ?: "Your Name", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(user?.email ?: "your@email.com", color = Color.White.copy(0.8f), fontSize = 13.sp)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))
                ProfileMenuItem(Icons.Default.ReceiptLong, "My Bookings", "View all your bookings") {
                    android.util.Log.d("Workly", "My Bookings clicked")
                    Toast.makeText(context, "Opening My Bookings...", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, MyBookingsActivity::class.java))
                }
                ProfileMenuItem(Icons.Default.LocationOn, "Saved Addresses", "Home, work & more") {}
                ProfileMenuItem(Icons.Default.CreditCard, "Payment Methods", "Cards, UPI & wallet") {}

                Spacer(modifier = Modifier.height(8.dp))
                Text("Management", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                ProfileMenuItem(Icons.Default.AdminPanelSettings, "Admin Dashboard", "Manage approvals & providers") {
                    context.startActivity(Intent(context, AdminDashboardActivity::class.java))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Preferences", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                ProfileMenuItem(Icons.Default.Notifications, "Notifications", "Push, SMS & email") {}
                ProfileMenuItem(Icons.Default.Language, "Language", "English") {}
                ProfileMenuItem(Icons.Default.Palette, "Appearance", "Light / Dark mode") {}

                Spacer(modifier = Modifier.height(8.dp))
                Text("Support", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                ProfileMenuItem(Icons.Default.HelpOutline, "Help & Support", "FAQ, live chat") {}
                ProfileMenuItem(Icons.Default.Star, "Rate the App", "Share your feedback") {}
                ProfileMenuItem(Icons.Default.Info, "About Workly", "Version 1.0.0") {}

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.08f), contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = ProfessionalBlue.copy(0.1f)) {
                Icon(icon, null, tint = ProfessionalBlue, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary.copy(0.5f))
        }
    }
}
