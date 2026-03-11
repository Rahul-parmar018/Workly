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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                        3 -> PlaceholderScreen("Bookings")
                        4 -> ProfileScreenContent(onLogout = { performLogout(context) })
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
        
        Spacer(modifier = Modifier.height(24.dp))

        // 2. Search Field
        HomeSearchSection()

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Hero Carousel
        HeroCarouselSection()

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Categories Grid
        CategoriesSection()

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Popular Services Horizontal List
        PopularServicesSection()
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HomeHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning,",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Text(
                text = "Rahul Sharma",
                style = MaterialTheme.typography.headlineMedium.copy(brush = PrimaryGradient),
                fontWeight = FontWeight.Bold
            )
        }
        
        Surface(
            modifier = Modifier
                .size(50.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = TextPrimary
                )
                // Notification Dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(8.dp)
                        .background(EnergyOrange, CircleShape)
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
        // Search Box
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ProfessionalBlue
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search services...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Filter Button
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(ProfessionalBlue, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = Color.White
            )
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
    val (title, subtitle, gradient) = when(page) {
        0 -> Triple("30% OFF", "Home Cleaning", PrimaryGradient)
        1 -> Triple("New Arrival", "AC Repair", SecondaryGradient)
        else -> Triple("Expert Help", "Plumbing", Brush.linearGradient(listOf(ElectricTeal, ProfessionalBlue)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Background patterns
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    center = Offset(size.width, size.height),
                    radius = size.height * 0.8f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    center = Offset(0f, 0f),
                    radius = size.height * 0.6f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "LIMITED TIME",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Icon(
                    imageVector = if(page==0) Icons.Default.CleaningServices else if(page==1) Icons.Default.AcUnit else Icons.Default.Plumbing,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CategoriesSection() {
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
                fontWeight = FontWeight.Bold
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
                                if (name == "Cleaning") {
                                    context.startActivity(Intent(context, com.example.workly.cleaning.CleaningActivity::class.java))
                                }
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
        PaddingHeader(title = "Popular Services")
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(10) {
                PopularServiceCardNew()
            }
        }
    }
}

@Composable
fun PopularServiceCardNew() {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Deep Cleaning",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Start at $40",
                        color = ProfessionalBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = EnergyOrange, modifier = Modifier.size(16.dp))
                    Text(" 4.8 (120)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun FloatingBottomBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    Surface(
        color = Color.Black.copy(alpha = 0.9f), // Dark Glass
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 20.dp,
        modifier = Modifier.height(72.dp).fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Icons.Filled.Home,
                Icons.Outlined.Explore,
                Icons.Outlined.ChatBubbleOutline,
                Icons.Outlined.PersonOutline
            )
            
            items.forEachIndexed { index, icon ->
                val isSelected = selectedItem == index
                val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "scale")
                val color = if (isSelected) EnergyOrange else Color.White.copy(alpha = 0.5f)
                
                IconButton(
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.scale(scale)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
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