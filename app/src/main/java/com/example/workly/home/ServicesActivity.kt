package com.example.workly.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.theme.*
import com.example.workly.data.Service
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ServicesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeDataStore = ThemeDataStore(this)
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())
            WorklyTheme(themeMode = themeMode) {
                ServicesScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val initialFilter = (context as? ComponentActivity)?.intent?.getStringExtra("FILTER_CATEGORY") ?: ""

    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf(initialFilter) }

    LaunchedEffect(Unit) {
        com.example.workly.data.MockDataSeeder.seedMissingCategoriesOnce()
        
        FirebaseFirestore.getInstance().collection("services")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    services = snapshot.documents.mapNotNull { doc ->
                        try { doc.toObject(Service::class.java) } catch (e: Exception) { null }
                    }
                }
                isLoading = false
            }
    }

    val filteredServices = if (searchQuery.trim().isEmpty()) services else services.filter {
        it.title.contains(searchQuery.trim(), ignoreCase = true) || it.category.contains(searchQuery.trim(), ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MARKETPLACE", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBlack,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = PremiumBlack
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = PremiumBlackSurface,
                border = BorderStroke(1.dp, PremiumSilver.copy(0.1f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = PremiumSilver, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(PremiumSilver),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) Text("Search services...", color = Color.White.copy(0.3f), fontSize = 14.sp)
                            innerTextField()
                        }
                    )
                    Icon(Icons.Default.FilterList, null, tint = PremiumSilver, modifier = Modifier.size(20.dp))
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumSilver)
                }
            } else if (filteredServices.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No services found", color = Color.White.copy(0.5f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredServices) { service ->
                        ServiceFeedCard(service)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceFeedCard(service: Service) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable {
            context.startActivity(android.content.Intent(context, ServiceDetailActivity::class.java).apply {
                putExtra("SERVICE_TITLE", service.title)
                putExtra("SERVICE_PRICE", service.price)
                putExtra("SERVICE_CATEGORY", service.category)
                putExtra("SERVICE_ID", service.id)
                putExtra("SERVICE_DURATION", service.duration)
                putExtra("SERVICE_DESC", service.description)
                putExtra("SERVICE_IMG", service.imageUrl.ifEmpty { "" })
            })
        },
        shape = RoundedCornerShape(20.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = service.imageUrl.ifEmpty { getPremiumImageForCategory(service.category) },
                contentDescription = null,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(service.category.uppercase(), color = PremiumSilver, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = PremiumSilver, modifier = Modifier.size(14.dp))
                    Text(" 4.9", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("₹${service.price.toInt()}", color = PremiumSilver, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}

fun getAllServices(): List<Service> = emptyList()

fun getPremiumImageForCategory(category: String): String {
    return when (category.lowercase()) {
        "cleaning" -> "https://images.unsplash.com/photo-1581578731548-c64695cc6954?auto=format&fit=crop&w=800&q=80"
        "plumbing" -> "https://images.unsplash.com/photo-1505798577917-a65157d3320a?auto=format&fit=crop&w=800&q=80"
        "electric" -> "https://images.unsplash.com/photo-1621905231291-0074d241d044?auto=format&fit=crop&w=800&q=80"
        "painting" -> "https://images.unsplash.com/photo-1562259949-e8e7689d7828?auto=format&fit=crop&w=800&q=80"
        "repair", "ac repair" -> "https://images.unsplash.com/photo-1581094288338-2314dddb7ecb?auto=format&fit=crop&w=800&q=80"
        "kitchen" -> "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=800&q=80"
        "carpentry" -> "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?auto=format&fit=crop&w=800&q=80"
        "support" -> "https://images.unsplash.com/photo-1534536281715-e28d76689b4d?auto=format&fit=crop&w=800&q=80"
        else -> "https://images.unsplash.com/photo-1581578731548-c64695cc6954?auto=format&fit=crop&w=800&q=80"
    }
}
