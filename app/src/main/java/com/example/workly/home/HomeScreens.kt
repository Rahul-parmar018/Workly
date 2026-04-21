package com.example.workly.home

import android.content.Intent
import com.example.workly.provider.MyServicesActivity
import com.example.workly.provider.AddServiceActivity
import com.example.workly.provider.ProviderOrdersActivity
import com.example.workly.provider.ProviderEarningsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.graphicsLayer
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

// Workly Premium Colors
private val SilverMetallicGradient = Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E)))
private val DeepObsidian = Color(0xFF020202)

@Composable
fun getWorklyPrimary() = MaterialTheme.colorScheme.primary

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

    var firestoreServices by remember { mutableStateOf<List<Service>>(emptyList()) }
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("services")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
    val popularServices = firestoreServices

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 180.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("WORKLY ELITE", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                        Text("Hi, $firstName", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(
                        modifier = Modifier.size(50.dp).clickable { onNavigateToProfile() },
                        shape = RoundedCornerShape(16.dp),
                        color = PremiumBlackSurface,
                        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.3f))
                    ) {
                        AsyncImage(model = effectiveAvatar, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clickable { context.startActivity(Intent(context, ServicesActivity::class.java)) },
                    shape = RoundedCornerShape(16.dp), color = PremiumBlackSurface, border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = PremiumSilver, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Search for 'Deep Cleaning'...", color = Color.White.copy(alpha = 0.4f), fontSize = 15.sp)
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text("What are you looking for?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(20.dp))
                val categories = listOf(
                    Triple("Cleaning", Icons.Default.CleaningServices, SilverMetallicGradient),
                    Triple("Electric", Icons.Default.ElectricalServices, SilverMetallicGradient),
                    Triple("Plumbing", Icons.Default.Plumbing, SilverMetallicGradient),
                    Triple("Kitchen", Icons.Default.Countertops, SilverMetallicGradient),
                    Triple("AC Repair", Icons.Default.AcUnit, SilverMetallicGradient),
                    Triple("Painting", Icons.Default.FormatPaint, SilverMetallicGradient),
                    Triple("Carpentry", Icons.Default.Handyman, SilverMetallicGradient),
                    Triple("Support", Icons.Default.SupportAgent, SilverMetallicGradient)
                )
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    for (i in categories.indices step 4) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (j in 0 until 4) {
                                if (i + j < categories.size) {
                                    val cat = categories[i + j]
                                    UrbanCategoryItem(cat.first, cat.second, Modifier.weight(1f))
                                } else { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader("Best of Workly") { onSeeAllServices() }
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(popularServices.size) { idx -> UrbanServiceCard(popularServices[idx]) }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp), color = PremiumBlackSurface, border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("REWARD PROGRAM", color = PremiumSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Get 20% cashback", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    Button(onClick = { }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver)) {
                        Text("JOIN", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingBottomBar(selectedItem: Int, onItemSelected: (Int) -> Unit, unreadCount: Int = 0) {
    val items = listOf(
        Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
        Triple("Explore", Icons.Default.Explore, Icons.Outlined.Explore),
        Triple("Messages", Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline),
        Triple("Profile", Icons.Default.Person, Icons.Outlined.Person)
    )
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp).padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 3D Tactical Vault (Grounded)
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp), ambientColor = Color.Black, spotColor = Color.Black.copy(alpha = 0.5f))
                .background(PremiumBlackSurface.copy(alpha = 0.98f), RoundedCornerShape(26.dp))
                .border(1.dp, PremiumSilver.copy(alpha = 0.15f), RoundedCornerShape(26.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp), 
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                val isSelected = selectedItem == index
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { 
                            if (!isSelected) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onItemSelected(index) 
                            }
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(if (isSelected) 56.dp else 40.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PremiumSilver.copy(alpha = 0.12f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.2f)) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSelected) filledIcon else outlinedIcon, 
                                    contentDescription = label, 
                                    tint = if (isSelected) Color.White else PremiumSilver.copy(alpha = 0.35f), 
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                // 🔥 Unread Badge (Only for Messages index 2)
                                if (index == 2 && unreadCount > 0) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp),
                                        shape = CircleShape,
                                        color = Color.Red,
                                        border = BorderStroke(1.5.dp, PremiumBlackSurface)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrbanCategoryItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier.clickable {
        context.startActivity(Intent(context, ServicesActivity::class.java).apply {
            putExtra("FILTER_CATEGORY", title)
        })
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(68.dp), shape = RoundedCornerShape(20.dp), color = PremiumBlackSurface, border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.15f))) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(31.dp)) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
fun UrbanServiceCard(service: Service) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.width(240.dp).clickable { 
            context.startActivity(Intent(context, ServiceDetailActivity::class.java).apply { 
                putExtra("SERVICE_TITLE", service.title)
                putExtra("SERVICE_PRICE", service.price)
                putExtra("SERVICE_CATEGORY", service.category)
                putExtra("SERVICE_ID", service.id)
                putExtra("SERVICE_DURATION", service.duration)
                putExtra("SERVICE_DESC", service.description)
                putExtra("PROVIDER_NAME", service.providerName)
                putExtra("PROVIDER_ID", service.providerId)
                putExtra("SERVICE_IMG", service.imageUrl.ifEmpty { getPremiumImageForCategory(service.category) }) 
            }) 
        },
        shape = RoundedCornerShape(24.dp), color = PremiumBlackSurface, border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.15f))
    ) {
        Column {
            AsyncImage(model = service.imageUrl.ifEmpty { getPremiumImageForCategory(service.category) }, contentDescription = service.title, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(18.dp)) {
                Text(service.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(service.category.uppercase(), color = PremiumSilver.copy(0.4f), fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = PremiumSilver, modifier = Modifier.size(16.dp))
                        Text(" 4.9", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                    Text("₹${service.price.toInt()}", color = PremiumSilver, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        if (onSeeAll != null) {
            Text("See All", color = PremiumSilver, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onSeeAll() })
        }
    }
}

@Composable
fun ProfileScreenContent(userName: String, userRole: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val isProvider = userRole.lowercase() == "provider"
    val user = FirebaseAuth.getInstance().currentUser
    
    var userImage by remember { mutableStateOf<String?>(null) }
    var showSupportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc -> userImage = doc.getString("imageUrl") }
        }
    }
    
    val effectiveAvatar = userImage ?: "https://ui-avatars.com/api/?name=${userName.replace(" ", "+")}&background=000&color=fff&size=200&bold=true"
    
    if (showSupportSheet) {
        SupportSelectionSheet(userName = userName) { showSupportSheet = false }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PremiumBlack),
        contentPadding = PaddingValues(bottom = 160.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header / Avatar
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.5f)),
                    color = PremiumBlackSurface
                ) {
                    AsyncImage(
                        model = effectiveAvatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(userName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    color = PremiumSilver.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        userRole.uppercase(), 
                        color = PremiumSilver, 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // ── PROFILE ACTIONS ──────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    if (isProvider) {
                        ProfileMenuButton(Icons.Default.Inventory2, "My Services") {
                            context.startActivity(Intent(context, MyServicesActivity::class.java))
                        }
                        ProfileMenuButton(Icons.Default.AddCircleOutline, "Add New Service") {
                            context.startActivity(Intent(context, AddServiceActivity::class.java))
                        }
                        ProfileMenuButton(Icons.Default.Assignment, "My Orders") {
                            context.startActivity(Intent(context, ProviderOrdersActivity::class.java))
                        }
                        ProfileMenuButton(Icons.Default.AccountBalanceWallet, "Earnings") {
                            context.startActivity(Intent(context, ProviderEarningsActivity::class.java))
                        }
                    } else {
                        ProfileMenuButton(Icons.Default.CalendarMonth, "My Bookings") {
                            context.startActivity(Intent(context, MyBookingsActivity::class.java))
                        }
                    }

                    ProfileMenuButton(Icons.Default.Settings, "Account Settings") {
                        context.startActivity(Intent(context, AccountSettingsActivity::class.java))
                    }
                    ProfileMenuButton(Icons.Default.SupportAgent, "Help & Support") { 
                        showSupportSheet = true
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onLogout() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, null, tint = Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("LOGOUT", color = Color(0xFFD32F2F), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSelectionSheet(userName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PremiumBlackSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = PremiumSilver.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text("Elite Support", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Select your preferred communication channel", color = PremiumSilver.copy(0.6f), fontSize = 13.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // ── WHATSAPP (Priority for India/Locale) ──────────
            SupportOptionItem(
                icon = Icons.Default.Chat,
                title = "Direct WhatsApp",
                subtitle = "Instant response from Elite Support",
                accent = Color(0xFF25D366)
            ) {
                val url = "https://api.whatsapp.com/send?phone=917600000000&text=Hi Support, I am $userName, I need help with Workly."
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "WhatsApp not installed", android.widget.Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ── EMAIL ESCALATION ──────────────────────────────
            SupportOptionItem(
                icon = Icons.Default.Email,
                title = "Official Escalation",
                subtitle = "Resolution within 2-4 business hours",
                accent = PremiumSilver
            ) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:support@worklyelite.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Elite Support Request: $userName")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "No email client", android.widget.Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ── TELEPHONIC SUPPORT ────────────────────────────
            SupportOptionItem(
                icon = Icons.Default.Phone,
                title = "Priority Concierge",
                subtitle = "Speak directly to a human agent",
                accent = Color.White
            ) {
                val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:+917600000000"))
                context.startActivity(intent)
                onDismiss()
            }
        }
    }
}

@Composable
fun SupportOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ProfileMenuButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = PremiumSilver, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(18.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = PremiumSilver.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }
}

