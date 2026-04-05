package com.example.workly.home

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.data.Service
import com.example.workly.theme.*
import com.example.workly.home.getAllServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Brand accent — always the same regardless of theme
private val AccentBlue = Color(0xFF1E2A78)

private val CleaningGradient = Brush.verticalGradient(listOf(Color(0xFFEAF6FF), Color(0xFFFFFFFF)))
private val ElectricGradient = Brush.verticalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFE0B2)))
private val PlumbingGradient = Brush.verticalGradient(listOf(Color(0xFFE6FFFA), Color(0xFFCCF2F4)))
private val RepairGradient   = Brush.verticalGradient(listOf(Color(0xFFF3E8FF), Color(0xFFF9F5FF)))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedFilter by remember { mutableStateOf("Nearby") }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("services")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    services = snapshot.documents.mapNotNull { doc ->
                        try { doc.toObject(Service::class.java) }
                        catch (ex: Exception) { null }
                    }
                }
            }
    }

    val filtered = services.filter { service ->
        (selectedCategory == "All" || service.category == selectedCategory) &&
        (searchQuery.isEmpty() || service.title.contains(searchQuery, true))
    }

    // ── Theme-reactive local aliases ─────────────────────────────────────────
    val bg         = MaterialTheme.colorScheme.background
    val surface    = MaterialTheme.colorScheme.surface
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant
    val onBg       = MaterialTheme.colorScheme.onBackground
    val onSurface  = MaterialTheme.colorScheme.onSurface
    val primary    = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = bg,
        topBar = {
            Column(modifier = Modifier.background(bg).statusBarsPadding()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Explore Services", fontSize = 24.sp, fontWeight = FontWeight.Black, color = onBg)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = primary, modifier = Modifier.size(14.dp))
                            Text(" Ahmedabad", fontSize = 13.sp, color = primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = CircleShape, color = surfaceVar, modifier = Modifier.size(44.dp), shadowElevation = 2.dp) {
                        AsyncImage(model = "https://ui-avatars.com/api/?name=Rahul+Parmar&background=1E2A78&color=fff", contentDescription = null)
                    }
                }
                Text(
                    "Discover 33+ services near you",
                    fontSize = 14.sp,
                    color = onBg.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // Search Bar & Filter
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = onSurface.copy(alpha = 0.45f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(color = onSurface, fontSize = 15.sp),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) Text("Search cleaning, plumbing...", color = onSurface.copy(alpha = 0.4f), fontSize = 15.sp)
                                    inner()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(48.dp).clickable { /* Filters */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Tune, null, tint = primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val filters = listOf("Nearby", "Under ₹499", "4★+ Rated", "Fast Service")
                    items(filters.size) { idx ->
                        val f = filters[idx]
                        val isSel = selectedFilter == f
                        Surface(
                            onClick = { selectedFilter = f },
                            shape = RoundedCornerShape(50.dp),
                            color = if (isSel) primary else surface,
                            shadowElevation = if (isSel) 4.dp else 1.dp
                        ) {
                            Text(
                                f,
                                color = if (isSel) Color.White else onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Category Row
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    val cats = listOf(
                        Triple("Cleaning", CleaningGradient, "https://cdn3d.iconscout.com/3d/premium/thumb/cleaning-vacuum-7170068-5813735.png"),
                        Triple("Repair", RepairGradient, "https://cdn3d.iconscout.com/3d/premium/thumb/mechanical-9190180-7546373.png"),
                        Triple("Plumbing", PlumbingGradient, "https://cdn3d.iconscout.com/3d/premium/thumb/plumbing-9190184-7546377.png"),
                        Triple("Electric", ElectricGradient, "https://cdn3d.iconscout.com/3d/premium/thumb/electricity-flash-5349603-4475459.png")
                    )
                    items(cats.size) { idx ->
                        val (name, grad, icon) = cats[idx]
                        val isSel = selectedCategory == name
                        Surface(
                            onClick = { selectedCategory = if (isSel) "All" else name },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.size(80.dp, 100.dp),
                            color = surface,
                            border = if (isSel) BorderStroke(2.dp, primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            shadowElevation = if (isSel) 8.dp else 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(model = icon, contentDescription = name, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurface)
                            }
                        }
                    }
                }
            }

            // Popular Near You Header
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥 Popular Near You", fontSize = 18.sp, fontWeight = FontWeight.Black, color = onBg)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sort: Popular", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primary)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = primary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Service List
            items(filtered) { service ->
                RealMarketplaceCard(service)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RealMarketplaceCard(service: Service) {
    val context = LocalContext.current

    val displayRating  = if (service.rating > 0) service.rating.toString() else "4.8"
    val displayReviews = (service.id.hashCode().let { if (it < 0) -it else it } % 200 + 40).toString()
    val displayDuration = service.duration.ifEmpty { "45 mins" }
    val displayDistance = (service.id.hashCode().let { if (it < 0) -it else it } % 45 / 10.0 + 0.5).let { "%.1f".format(it) }
    val displayImg = service.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1581578731548-c64695ce6958?auto=format&fit=crop&q=80&w=300" }

    val cardBg   = MaterialTheme.colorScheme.surface
    val onCard   = MaterialTheme.colorScheme.onSurface
    val primary  = MaterialTheme.colorScheme.primary
    val outline  = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, outline)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                AsyncImage(
                    model = displayImg,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                if (displayRating.toDouble() >= 4.7) {
                    Surface(
                        modifier = Modifier.padding(6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = primary
                    ) {
                        Text("Trending", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onCard, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(14.dp))
                    Text(" $displayRating ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = onCard)
                    Text("($displayReviews reviews)", fontSize = 12.sp, color = onCard.copy(alpha = 0.55f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏲️ $displayDuration", fontSize = 12.sp, color = onCard.copy(alpha = 0.55f))
                    Text(" • ", color = onCard.copy(alpha = 0.3f))
                    Text("📍 $displayDistance km away", fontSize = 12.sp, color = onCard.copy(alpha = 0.55f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("₹${service.price.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = primary)
                    Surface(
                        onClick = {
                            context.startActivity(
                                Intent(context, Class.forName("com.example.workly.home.ServiceDetailActivity")).apply {
                                    putExtra("SERVICE_TITLE", service.title)
                                    putExtra("SERVICE_PRICE", service.price)
                                    putExtra("SERVICE_CATEGORY", service.category)
                                    putExtra("SERVICE_ID", service.id)
                                    putExtra("SERVICE_DURATION", displayDuration)
                                    putExtra("SERVICE_DESC", service.description)
                                    putExtra("SERVICE_IMG", displayImg)
                                })
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = primary
                    ) {
                        Text("Book Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}
