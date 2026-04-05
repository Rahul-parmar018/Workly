package com.example.workly.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = onSurf,
                    navigationIconContentColor = onSurf
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val chatIntent = Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                                putExtra("RECEIVER_NAME", providerName)
                                putExtra("RECEIVER_ID", providerId)
                            }
                            context.startActivity(chatIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary.copy(alpha = 0.1f),
                            contentColor = primary
                        )
                    ) {
                        Text("Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            val bookIntent = Intent(context, BookingActivity::class.java).apply {
                                putExtra("SERVICE_TITLE", title)
                                putExtra("SERVICE_PRICE", price)
                                putExtra("SERVICE_CATEGORY", category)
                                putExtra("SERVICE_ID", serviceId)
                                putExtra("PROVIDER_NAME", providerName)
                                putExtra("PROVIDER_ID", providerId)
                            }
                            context.startActivity(bookIntent)
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Book Service", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                .padding(16.dp)
        ) {
            // Service Image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = surfVar
            ) {
                AsyncImage(
                    model = imgUrl ?: "https://placehold.co/600x400/1E2A78/FFFFFF?text=Service",
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onBg
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price
            Text(
                text = "₹${price.toInt()}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Meta Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(label = "⏱ $duration", onBg = onBg, surfVar = surfVar)
                InfoChip(label = "👤 $providerName", onBg = onBg, surfVar = surfVar)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = "Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = onBg
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = onBg.copy(alpha = 0.7f),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun InfoChip(label: String, onBg: Color, surfVar: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = surfVar,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = onBg.copy(alpha = 0.8f)
        )
    }
}
