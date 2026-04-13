package com.example.workly.home

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.booking.BookingActivity
import com.example.workly.theme.*
import com.example.workly.theme.PremiumSilver
import com.example.workly.theme.PremiumBlackSurface
import com.example.workly.theme.DarkBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    title: String,
    description: String,
    price: Double,
    duration: String,
    providerName: String,
    providerId: String,
    category: String,
    serviceId: String,
    imgUrl: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    val safeImg = if (imgUrl.isNullOrEmpty()) {
        ""
    } else imgUrl

    val safeProviderName = if (providerName.isBlank() || providerName.equals("Unknown", true)) "Workly Professional" else providerName

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onSurf)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = onSurf,
                    navigationIconContentColor = onSurf
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PremiumBlackSurface,
                border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Starting from", color = onBg.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("â‚¹${price.toInt()}", color = PremiumSilver, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    
                    Button(
                        onClick = {
                            val bookIntent = Intent(context, BookingActivity::class.java).apply {
                                putExtra("SERVICE_TITLE", title)
                                putExtra("SERVICE_PRICE", price)
                                putExtra("SERVICE_CATEGORY", category)
                                putExtra("SERVICE_ID", serviceId)
                                putExtra("PROVIDER_NAME", safeProviderName)
                                putExtra("PROVIDER_ID", providerId)
                            }
                            context.startActivity(bookIntent)
                        },
                        modifier = Modifier
                            .width(180.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver)
                    ) {
                        Text("Book Service", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image with Gradient Overlay
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                AsyncImage(
                    model = safeImg,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                            )
                        )
                )
                // Category Badge
                Surface(
                    modifier = Modifier.padding(20.dp).align(Alignment.BottomStart),
                    color = primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        category.uppercase(), 
                        color = Color.White, 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Black, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Confidence Badge Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = PremiumSilver.copy(alpha = 0.1f)) {
                        Text("UC VERIFIED", color = PremiumSilver, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("4.8â˜…", color = onBg, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(" (2.4k reviews)", color = onBg.copy(alpha = 0.4f), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = onBg,
                    lineHeight = 38.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // Modern Detail Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    UrbanDetailItem("â± $duration", "Duration", Modifier.weight(1f))
                    UrbanDetailItem("ðŸ‘¤ Expert", safeProviderName, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(40.dp))

                // About the service (Urban Company Style)
                Text("Service Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onBg)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (description.isBlank()) "No description provided." else description,
                    fontSize = 15.sp,
                    color = onBg.copy(alpha = 0.6f),
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Premium Features
                Text("Service Highlights", fontSize = 20.sp, fontWeight = FontWeight.Black, color = onBg)
                Spacer(modifier = Modifier.height(16.dp))
                HighlightItem("Verified Professionals", "Every pro is background checked")
                HighlightItem("Satisfaction Guaranteed", "Not happy? We'll re-clean for free")
                HighlightItem("24/7 Support", "Always here to help you")
                
                Spacer(modifier = Modifier.height(40.dp))

                // --- 5. MULTI-SERVICE PORTFOLIO ---
                var providerServices by remember { mutableStateOf<List<com.example.workly.data.Service>>(emptyList()) }
                val repo = remember { HomeRepository() }
                
                LaunchedEffect(providerId) {
                    repo.getServicesByProvider(providerId).collect {
                        providerServices = it.filter { s -> s.id != serviceId }
                    }
                }

                if (providerServices.isNotEmpty()) {
                    Text("More by $safeProviderName", fontSize = 20.sp, fontWeight = FontWeight.Black, color = onBg)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        providerServices.forEach { s ->
                            Surface(
                                modifier = Modifier
                                    .width(200.dp)
                                    .clickable {
                                        // Refresh with new service
                                        val intent = Intent(context, ServiceDetailActivity::class.java).apply {
                                            putExtra("SERVICE_TITLE", s.title)
                                            putExtra("SERVICE_DESC", s.description)
                                            putExtra("SERVICE_PRICE", s.price)
                                            putExtra("SERVICE_DURATION", s.duration)
                                            putExtra("PROVIDER_NAME", s.providerName)
                                            putExtra("PROVIDER_ID", s.providerId)
                                            putExtra("SERVICE_CATEGORY", s.category)
                                            putExtra("SERVICE_ID", s.id)
                                            putExtra("SERVICE_IMG", s.imageUrl)
                                        }
                                        context.startActivity(intent)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = PremiumBlackSurface,
                                border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    AsyncImage(
                                        model = s.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(s.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("â‚¹${s.price.toInt()}", color = PremiumSilver, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun UrbanDetailItem(label: String, subtitle: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Black, color = PremiumSilver)
            Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        }
    }
}

@Composable
fun DetailPill(icon: String, label: String, modifier: Modifier, surfVar: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = surfVar.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun HighlightItem(title: String, subtitle: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(0.1f), modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
    }
}
