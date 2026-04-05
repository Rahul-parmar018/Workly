package com.example.workly.splash

import com.example.workly.home.HomeActivity
import com.example.workly.auth.LoginActivity

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.R
import com.example.workly.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Brand Colors ─────────────────────────────────────────────────────────────
private val BrandBlue     = Color(0xFF1A237E)
private val BrandBlueMid  = Color(0xFF0D47A1)
private val BrandBlueDark = Color(0xFF01579B)
private val BrandGlow     = Color(0xFF3F51B5).copy(alpha = 0.4f)
private val TrustGreen    = Color(0xFF4CAF50)

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
                    delay(2800)

                    if (currentUser != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUser.uid).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val role = userDoc.getString("role") ?: "user"
                                    when (role) {
                                        "admin" -> {
                                            startActivity(Intent(this@SplashActivity, com.example.workly.admin.AdminDashboardActivity::class.java))
                                            finish()
                                        }
                                        "provider" -> {
                                            val db2 = FirebaseFirestore.getInstance()
                                            db2.collection("providers").document(currentUser.uid).get()
                                                .addOnSuccessListener { provDoc ->
                                                    val approved = provDoc.getBoolean("isApproved") ?: false
                                                    if (approved) {
                                                        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                                    } else {
                                                        startActivity(Intent(this@SplashActivity, com.example.workly.auth.AuthSelectionActivity::class.java))
                                                    }
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                                    finish()
                                                }
                                        }
                                        else -> {
                                            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                            finish()
                                        }
                                    }
                                } else {
                                    showSplash = false
                                }
                            }
                            .addOnFailureListener { showSplash = false }
                    } else {
                        showSplash = false
                    }
                }

                Crossfade(targetState = showSplash, label = "SplashFade") { isSplash ->
                    if (isSplash) {
                        PremiumSplashScreen()
                    } else {
                        PremiumOnboardingScreen(
                            onGetStarted = {
                                startActivity(Intent(this@SplashActivity, com.example.workly.auth.AuthSelectionActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Premium Splash Screen ────────────────────────────────────────────────────
@Composable
fun PremiumSplashScreen() {
    var started by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.85f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 400),
        label = "textAlpha"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 700),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        started = true
        // Animate progress bar
        val steps = 50
        repeat(steps) {
            delay(40)
            progress = (it + 1f) / steps
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandBlue, BrandBlueMid, BrandBlueDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Soft radial glow behind logo
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        listOf(BrandGlow, Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo circle
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.workly_logo),
                        contentDescription = "Workly Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App name
            Text(
                "Workly",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Text(
                "Trusted Home Services in Minutes",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(taglineAlpha)
                    .padding(horizontal = 40.dp)
            )
        }

        // Progress bar at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .padding(horizontal = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

// ─── Premium Onboarding Screen ────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumOnboardingScreen(onGetStarted: () -> Unit) {
    val pages = listOf(
        OnboardPage(
            imageRes = R.drawable.onboard_cleaning,
            title = "Spotless Cleaning",
            description = "Professional cleaners at your doorstep in under 60 minutes.",
            trustLine = "10,000+ homes cleaned",
            bgStart = Color(0xFFE3F2FD),
            bgEnd = Color(0xFFBBDEFB)
        ),
        OnboardPage(
            imageRes = R.drawable.onboard_plumbing,
            title = "Reliable Plumbing",
            description = "Fix leaks, installs & repairs with verified experts.",
            trustLine = "Background-verified professionals",
            bgStart = Color(0xFFE8F5E9),
            bgEnd = Color(0xFFC8E6C9)
        ),
        OnboardPage(
            imageRes = R.drawable.onboard_electrician,
            title = "Expert Electricians",
            description = "Safe, fast electrical services anytime you need.",
            trustLine = "4.8★ average rating",
            bgStart = Color(0xFFFFFDE7),
            bgEnd = Color(0xFFFFF9C4)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            PremiumOnboardPage(page = pages[pageIndex])
        }

        // Bottom controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "dot_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) BrandBlue else Color(0xFFCFD8DC),
                        label = "dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }

            // CTA Button
            val isLastPage = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    scope.launch {
                        if (!isLastPage) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onGetStarted()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    },
                    label = "cta_text"
                ) { last ->
                    if (last) {
                        Text(
                            "Get Started",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Next",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Skip link (not on last page)
            AnimatedVisibility(visible = !isLastPage) {
                TextButton(onClick = { onGetStarted() }) {
                    Text(
                        "Skip",
                        color = Color(0xFF90A4AE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Individual Onboarding Page ───────────────────────────────────────────────
data class OnboardPage(
    val imageRes: Int,
    val title: String,
    val description: String,
    val trustLine: String,
    val bgStart: Color,
    val bgEnd: Color
)

@Composable
fun PremiumOnboardPage(page: OnboardPage) {
    var visible by remember { mutableStateOf(false) }

    // Stagger animation trigger
    LaunchedEffect(page.title) {
        visible = false
        delay(80)
        visible = true
    }

    val illustrationOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "illus_offset"
    )
    val illustrationAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "illus_alpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 150),
        label = "title_alpha"
    )
    val descAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 250),
        label = "desc_alpha"
    )
    val trustAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 350),
        label = "trust_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP 60% — Illustration with gradient bg
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .background(
                    Brush.verticalGradient(listOf(page.bgStart, page.bgEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .alpha(illustrationAlpha)
                    .offset(y = illustrationOffset),
                contentScale = ContentScale.Fit
            )
        }

        // BOTTOM 40% — Text content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Title
            Text(
                page.title,
                color = Color(0xFF1A237E),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                page.description,
                color = Color(0xFF546E7A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.alpha(descAlpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trust line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(trustAlpha)
                    .background(
                        Color(0xFF1A237E).copy(alpha = 0.06f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    page.trustLine,
                    color = Color(0xFF1A237E),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Legacy wrappers (kept for safety) ────────────────────────────────────────
@Composable fun SplashScreen() = PremiumSplashScreen()

@OptIn(ExperimentalFoundationApi::class)
@Composable fun OnboardingScreen(onGetStarted: () -> Unit) = PremiumOnboardingScreen(onGetStarted)

@Composable fun OnboardingPage(page: Int) {
    // Legacy – mapped to new composable
    val pages = listOf(
        OnboardPage(R.drawable.onboard_cleaning, "Spotless Cleaning",
            "Professional cleaners at your doorstep in under 60 minutes.", "10,000+ homes cleaned",
            Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
        OnboardPage(R.drawable.onboard_electrician, "Expert Electricians",
            "Safe, fast electrical services anytime you need.", "4.8★ average rating",
            Color(0xFFFFFDE7), Color(0xFFFFF9C4)),
        OnboardPage(R.drawable.onboard_plumbing, "Reliable Plumbing",
            "Fix leaks, installs & repairs with verified experts.", "Background-verified professionals",
            Color(0xFFE8F5E9), Color(0xFFC8E6C9))
    )
    if (page < pages.size) PremiumOnboardPage(pages[page])
}
