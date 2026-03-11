package com.example.workly.home

import com.example.workly.location.WorkLogActivity
import com.example.workly.auth.LoginActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import com.example.workly.theme.*
import com.example.workly.R
import com.google.firebase.auth.FirebaseAuth
import com.example.workly.booking.BookingActivity
import com.example.workly.data.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest



class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        containerColor = BackgroundGray,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, WorkLogActivity::class.java))
                },
                containerColor = EnergyOrange
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Work", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Dynamic Background Mesh (Subtle)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ProfessionalBlue.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.8f, 0f),
                        radius = size.width * 0.8f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(EnergyOrange.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(0f, size.height * 0.4f),
                        radius = size.width * 0.6f
                    )
                )
            }

            // Main Content
            Box(modifier = Modifier.padding(bottom = 80.dp)) { // Leave space for floating bar
                Crossfade(targetState = selectedItem, label = "ContentFade") { target ->
                    when (target) {
                        0 -> HomeScreenContent(innerPadding)
                        1 -> PlaceholderScreen("Discover")
                        2 -> PlaceholderScreen("Chat")
                        3 -> ProfileScreenContent(onLogout = { performLogout(context) })
                    }
                }
            }

            // Floating Bottom Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                FloatingBottomBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it }
                )
            }
        }
    }
}

fun performLogout(context: Context) {
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(context, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}

@Composable
fun HomeScreenContent(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Vibrant Header
        HomeHeaderSection()
        
        Spacer(modifier = Modifier.height(20.dp))

        // 2. Upcoming Bookings (New Phase 1 Feature)
        UpcomingBookingsSection()
        
        Spacer(modifier = Modifier.height(24.dp))

        // 3. Search Field
        HomeSearchSection()

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Hero Carousel
        HeroCarouselSection()

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Categories Grid
        val context = LocalContext.current
        CategoriesSection(onSeeAllClick = {
            context.startActivity(Intent(context, ServicesActivity::class.java))
        })

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Popular Services Horizontal List
        PopularServicesSection()
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HomeHeaderSection() {
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val userName = currentUser?.displayName ?: "User"
    val photoUrl = currentUser?.photoUrl
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture with dynamic loading
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(8.dp, CircleShape)
                    .clickable {
                         context.startActivity(Intent(context, com.example.workly.admin.AdminDashboardActivity::class.java))
                    },
                shape = CircleShape,
                color = Color.White
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl.toString())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalBlue
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(ProfessionalBlue, ElectricTeal)
                        )
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
        }
        
        // Premium Glass Notification Button
        Surface(
            modifier = Modifier
                .size(54.dp)
                .shadow(12.dp, CircleShape),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
                // Vibrant Status Dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(10.dp)
                        .background(
                            Brush.sweepGradient(listOf(EnergyOrange, Color.Red)),
                            CircleShape
                        )
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
fun HomeSearchSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-end Search Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(15.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ProfessionalBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "What help do you need?",
                    color = Color.Gray.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Enhanced Filter Button
        Surface(
            modifier = Modifier
                .size(56.dp)
                .shadow(15.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = ProfessionalBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarouselSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while(true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp
        ) { page ->
            HeroCard(page)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { iteration ->
                val color = if (pagerState.currentPage == iteration) ProfessionalBlue else Color.LightGray.copy(alpha=0.5f)
                val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun HeroCard(page: Int) {
    val title = when(page) {
        0 -> "Summer Sale"
        1 -> "Pro Repair"
        else -> "Elite Pros"
    }
    val subtitle = when(page) {
        0 -> "30% OFF - Home Cleaning"
        1 -> "Instant AC Maintenance"
        else -> "Top Rated Plumbing"
    }
    val gradient = when(page) {
        0 -> PrimaryGradient
        1 -> SecondaryGradient
        else -> Brush.linearGradient(listOf(ElectricTeal, ProfessionalBlue))
    }
    val icon = when(page) {
        0 -> Icons.Default.CleaningServices
        1 -> Icons.Default.AcUnit
        else -> Icons.Default.Plumbing
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Background artistic circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    center = Offset(size.width * 0.9f, size.height * 0.2f),
                    radius = size.height * 0.5f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    center = Offset(size.width * 0.1f, size.height * 0.9f),
                    radius = size.height * 0.4f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "OFFICIAL PARTNER",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 32.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(onSeeAllClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                color = ProfessionalBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
        
        val context = LocalContext.current
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val categories = listOf(
            Triple("Cleaning", Icons.Default.CleaningServices, Color(0xFFE3F2FD)),
            Triple("Repair", Icons.Default.Build, Color(0xFFFFF3E0)),
            Triple("Painting", Icons.Default.FormatPaint, Color(0xFFF3E5F5)),
            Triple("More", Icons.Default.GridView, Color(0xFFEEEEEE))
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEach { (name, icon, color) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp))
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .clickable {
                                val intent = Intent(context, BookingActivity::class.java).apply {
                                    putExtra("SERVICE_NAME", name)
                                    putExtra("SERVICE_PRICE", if (name == "Cleaning") 40.0 else 25.0)
                                    putExtra("SERVICE_ICON", R.drawable.workly_logo) // Default icon for categories for now
                                }
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                         Box(
                             modifier = Modifier
                                 .size(50.dp)
                                 .background(color, RoundedCornerShape(14.dp)),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = icon,
                                 contentDescription = name,
                                 tint = Color.Black.copy(alpha = 0.7f),
                                 modifier = Modifier.size(24.dp)
                             )
                         }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PaddingHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "See All",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { /* TODO */ }
        )
    }
}

@Composable
fun PopularServicesSection() {
    Column {
        PaddingHeader(title = "Top Rated Services")
        Spacer(modifier = Modifier.height(20.dp))
        
        val services = listOf(
            Triple("Full Home Cleaning", 40, R.drawable.img_service_cleaner),
            Triple("AC Deep Repair", 25, R.drawable.img_service_electrician), // Reusing electrician for variety if needed
            Triple("Premium Plumbing", 15, R.drawable.img_service_plumber)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(services.size) { index ->
                val service = services[index]
                PopularServiceCardNew(service.first, service.second, service.third)
            }
        }
    }
}

@Composable
fun PopularServiceCardNew(name: String, price: Int, imageRes: Int) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(150.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clickable {
                val intent = Intent(context, BookingActivity::class.java).apply {
                    putExtra("SERVICE_NAME", name)
                    putExtra("SERVICE_PRICE", price.toDouble())
                    putExtra("SERVICE_ICON", imageRes)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Actual Service Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Starts at $$price",
                        color = ProfessionalBlue,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = EnergyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " 4.9 (250+)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingBookingsSection() {
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val userId = auth.currentUser?.uid
    
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, e ->
                    isLoading = false
                    if (snapshot != null) {
                        bookings = snapshot.toObjects(Booking::class.java)
                    }
                }
        } else {
            isLoading = false
        }
    }

    if (bookings.isNotEmpty()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Upcoming Bookings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(bookings.size) { index ->
                    val booking = bookings[index]
                    BookingMiniCard(booking)
                }
            }
        }
    }
}

@Composable
fun BookingMiniCard(booking: Booking) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ProfessionalBlue)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = booking.serviceName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${booking.date} • ${booking.time}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun FloatingBottomBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    Surface(
        color = Color.Black.copy(alpha = 0.92f), // Deep Glass
        shape = RoundedCornerShape(36.dp),
        shadowElevation = 25.dp,
        modifier = Modifier
            .height(76.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Icons.Filled.Home to "Home",
                Icons.Outlined.Explore to "Explore",
                Icons.Outlined.ChatBubbleOutline to "Inbox",
                Icons.Outlined.PersonOutline to "Profile"
            )
            
            items.forEachIndexed { index, (icon, label) ->
                val isSelected = selectedItem == index
                val scale by animateFloatAsState(if (isSelected) 1.25f else 1f, label = "scale")
                val color = if (isSelected) EnergyOrange else Color.White.copy(alpha = 0.5f)
                
                Column(
                    modifier = Modifier
                        .clickable(interactionSource = null, indication = null) { onItemSelected(index) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(scale)
                    )
                }
            }
        }
    }
}

// ... Profile Section (Reused but styled) ...
@Composable
fun ProfileScreenContent(onLogout: () -> Unit) { 
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
         Button(onClick = onLogout) { Text("Logout") }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Color.Gray)
    }
}