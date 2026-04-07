package com.example.workly.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

    val safeImg = if (imgUrl.isNullOrEmpty() || imgUrl.contains("placehold.co")) {
        "https://images.unsplash.com/photo-1628177142898-93e36e4e3a50?auto=format&fit=crop&q=80&w=800"
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
                modifier = Modifier.fillMaxWidth().shadow(24.dp),
                color = surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Price", color = onSurf.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("₹${price.toInt()}", color = primary, fontSize = 24.sp, fontWeight = FontWeight.Black)
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
                            .weight(1.5f)
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Book Now", fontWeight = FontWeight.Black, fontSize = 17.sp)
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
                // Info Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = onBg,
                            lineHeight = 34.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(16.dp))
                            Text(" 4.8 (2.4k reviews)", fontSize = 14.sp, color = onBg.copy(0.6f), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailPill(icon = "⏱", label = duration, modifier = Modifier.weight(1f), surfVar)
                    DetailPill(icon = "👤", label = safeProviderName, modifier = Modifier.weight(1f), surfVar)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // About the service
                Text(
                    text = "Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = onBg
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (description.isBlank() || description == "No description provided.") 
                        "Professional $category service with top-rated equipment and verified experts. We ensure 100% satisfaction and deep cleaning results." 
                        else description,
                    fontSize = 15.sp,
                    color = onBg.copy(alpha = 0.7f),
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Premium Features
                Text("Service Highlights", fontSize = 20.sp, fontWeight = FontWeight.Black, color = onBg)
                Spacer(modifier = Modifier.height(16.dp))
                HighlightItem("Verified Professionals", "Every pro is background checked")
                HighlightItem("Satisfaction Guaranteed", "Not happy? We'll re-clean for free")
                HighlightItem("24/7 Support", "Always here to help you")
                
                Spacer(modifier = Modifier.height(40.dp))
            }
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
