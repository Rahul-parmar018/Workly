package com.workly.app.splash

import com.workly.app.home.HomeActivity
import com.workly.app.auth.LoginActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workly.app.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WorklyTheme {
                var showSplash by remember { mutableStateOf(true) }
                val auth = FirebaseAuth.getInstance()
                val currentUser = remember { auth.currentUser }

                LaunchedEffect(Unit) {
                    delay(2500) // Slightly longer for animation
                    
                    if (currentUser != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUser.uid).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val role = userDoc.getString("role") ?: "user"
                                    
                                    when (role) {
                                        "admin" -> {
                                            startActivity(Intent(this@SplashActivity, com.workly.app.admin.AdminDashboardActivity::class.java))
                                            finish()
                                        }
                                        "provider" -> {
                                            // Check approval status for providers
                                            db.collection("providers").document(currentUser.uid).get()
                                                .addOnSuccessListener { provDoc ->
                                                    val approved = provDoc.getBoolean("isApproved") ?: false
                                                    if (approved) {
                                                        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                                        finish()
                                                    } else {
                                                        // If not approved, we might want to still show splash/onboarding or a msg
                                                        // For now, let's send to login/auth hub to avoid getting stuck
                                                        startActivity(Intent(this@SplashActivity, com.workly.app.auth.AuthSelectionActivity::class.java))
                                                        finish()
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                                    finish()
                                                }
                                        }
                                        else -> { // Standard user
                                            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                            finish()
                                        }
                                    }
                                } else {
                                    showSplash = false
                                }
                            }
                            .addOnFailureListener {
                                showSplash = false
                            }
                    } else {
                        // No user session, show onboarding
                        showSplash = false
                    }
                }

                Crossfade(targetState = showSplash, label = "SplashFade") { isSplash ->
                    if (isSplash) {
                        SplashScreen()
                    } else {
                        OnboardingScreen(
                            onGetStarted = {
                                startActivity(Intent(this@SplashActivity, com.workly.app.auth.AuthSelectionActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), label = "alpha"
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ProfessionalBlue, ProfessionalBlueDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icon
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim.value)
                    .shadow(elevation = 20.dp, shape = CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                 Box(contentAlignment = Alignment.Center) {
                     Icon(
                         imageVector = Icons.Default.Build,
                         contentDescription = "Logo",
                         modifier = Modifier.size(60.dp),
                         tint = ProfessionalBlue
                     )
                 }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Workly",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            
            Text(
                text = "Professional Services on Demand",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
        
        // Loading indicator at bottom
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)) {
             CircularProgressIndicator(
                 color = EnergyOrange,
                 modifier = Modifier.size(32.dp),
                 strokeWidth = 3.dp
             )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Slider Section
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPage(page = page)
            }

            // Bottom Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { iteration ->
                         val isSelected = pagerState.currentPage == iteration
                         val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "width")
                         val color by animateColorAsState(if (isSelected) ProfessionalBlue else Color.LightGray, label = "color")

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }

                // FAB / Next Button
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < 2) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                onGetStarted()
                            }
                        }
                    },
                    containerColor = ProfessionalBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val title = when (page) {
        0 -> "Spotless Cleaning"
        1 -> "Expert Electricians"
        2 -> "Reliable Plumbing"
        else -> ""
    }

    val description = when (page) {
        0 -> "Hire top-rated professional cleaners to make your space shine effortlessly."
        1 -> "Safe and reliable electrical repairs and installations at your doorstep."
        2 -> "Fast and efficient plumbing services for all your repair and maintenance needs."
        else -> ""
    }
    
    val imageRes = when(page) {
        0 -> com.workly.app.R.drawable.img_service_cleaner
        1 -> com.workly.app.R.drawable.img_service_electrician
        2 -> com.workly.app.R.drawable.img_service_plumber
        else -> com.workly.app.R.drawable.img_service_cleaner
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
             Image(
                 painter = painterResource(id = imageRes),
                 contentDescription = title,
                 modifier = Modifier.fillMaxSize(),
                 contentScale = ContentScale.Fit
             )
        }
        
        Spacer(modifier = Modifier.height(40.dp))

        // Text Content
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 24.sp
        )
    }
}
