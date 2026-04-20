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
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

// ── Brand Colors ─────────────────────────────────────────────────────────────
private val BrandBlue     = Color(0xFF1A237E)
private val BrandBlueMid  = Color(0xFF0D47A1)
private val BrandBlueDark = Color(0xFF01579B)
private val BrandGlow     = Color(0xFF3F51B5).copy(alpha = 0.4f)
private val TrustGreen    = Color(0xFF4CAF50)

class SplashActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        val themeDataStore = ThemeDataStore(this)
        
        // --- ── ELITE DATA PROVISIONING ───────────────────────────────────
        // com.example.workly.data.MockDataSeeder.seedMultiServices()
        // ──────────────────────────────────────────────────────────────────

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())
            
            WorklyTheme(themeMode = themeMode) {
                var showSplash by remember { mutableStateOf(true) }
                val auth = FirebaseAuth.getInstance()
                val currentUser = remember { auth.currentUser }

                LaunchedEffect(Unit) {
                    if (currentUser != null) {
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
                            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                                .update("fcmToken", token)
                        }
                    }
                    
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
    val infiniteTransition = rememberInfiniteTransition(label = "3DAnim")

    // 3D Parallax Rotation
    val rotationX by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse), label = "rotX"
    )
    val rotationY by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutSine), RepeatMode.Reverse), label = "rotY"
    )

    // Floating Background Elements (Simulated 3D depth)
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(5000, easing = EaseInOutSine), RepeatMode.Reverse), label = "float"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(1200, easing = EaseOutBack), label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1000), label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        started = true
        val steps = 100
        repeat(steps) {
            delay(28)
            progress = (it + 1f) / steps
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(PremiumBlack),
        contentAlignment = Alignment.Center
    ) {
        // ─── 3D AMBIENT ENVIRONMENT ──────────────────────────────────────────
        // Depth Glow
        Box(
            modifier = Modifier.size(400.dp).background(
                Brush.radialGradient(listOf(PremiumSilver.copy(alpha = 0.05f), Color.Transparent)),
                CircleShape
            )
        )

        // Floating Titanium Shards (Simulated 3D)
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (if (i == 0) -120 else if (i == 1) 140 else 60).dp,
                        y = (if (i == 0) -250 else if (i == 1) -100 else 200).dp + (floatAnim * (i + 1) * 0.5f).dp
                    )
                    .size((40 + i * 20).dp)
                    .graphicsLayer {
                        rotationZ = 45f + (floatAnim * i)
                        alpha = 0.1f
                    }
                    .border(1.dp, PremiumSilver.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ─── 3D PARALLAX LOGO ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.rotationX = rotationX
                        this.rotationY = rotationY
                        this.cameraDistance = 12f * density
                    }
                    .shadow(
                        elevation = 40.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = PremiumSilver.copy(alpha = 0.3f)
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.workly_logo),
                    contentDescription = "Workly Elite",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .clip(RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Premium HUD Typography
            Text(
                "WORKLY ELITE",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 10.sp,
                modifier = Modifier.alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "ESTABLISHING PRO LINK",
                color = PremiumSilver.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(logoAlpha * 0.7f)
            )
        }

        // ─── ADVANCED SERVICE SCANNER (LOADING) ──────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icons = listOf(Icons.Default.Build, Icons.Default.Favorite, Icons.Default.Check)
            val activeIconIndex = (progress * 3).toInt().coerceIn(0, 2)
            
            Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                // Moving Icon "Sweeper"
                Icon(
                    imageVector = icons[activeIconIndex],
                    contentDescription = null,
                    tint = PremiumSilver,
                    modifier = Modifier
                        .offset(x = (progress * 240).dp - 20.dp)
                        .size(24.dp)
                        .scale(1.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Neon Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(Color.Transparent, PremiumSilver)),
                            CircleShape
                        )
                        .blur(2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "SYSTEM SCAN ${ (progress * 100).toInt() }%",
                color = PremiumSilver.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
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
            id = "cleaning",
            imageRes = R.drawable.onboard_cleaning,
            title = "Spotless cleaning,\nwithout the effort.",
            description = "Book trusted cleaners instantly.",
            trustLine = "10,000+ homes serviced",
            trustIcon = "✔",
            bgStart = Color(0xFF0F172A),
            bgEnd = Color(0xFF020617),
            ctaText = "Continue →"
        ),
        OnboardPage(
            id = "electrician",
            imageRes = R.drawable.onboard_electrician,
            title = "Reliable electrical help,\nwhenever you need it.",
            description = "Safe, instant electrical help anytime.",
            trustLine = "4.8 average rating",
            trustIcon = "⭐",
            bgStart = Color(0xFF1E1511),
            bgEnd = Color(0xFF0F0B09),
            ctaText = "Continue →"
        ),
        OnboardPage(
            id = "plumbing",
            imageRes = R.drawable.onboard_plumbing,
            title = "Quick, clean\nplumbing solutions.",
            description = "Verified plumbers for installs & repairs.",
            trustLine = "Background-verified professionals",
            trustIcon = "✔",
            bgStart = Color(0xFF081C15),
            bgEnd = Color(0xFF020806),
            ctaText = "Get Started →",
            microCopy = "No booking fee • Cancel anytime"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) { pageIndex ->
        val page = pages[pageIndex]

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HERO SECTION (NO BOX - FULL IMMERSIVE)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fills top half flexibly
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(page.bgStart, page.bgEnd),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Soft Depth Layer Backdrop (makes background feel slightly faded/deep)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f)),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Rive/Lottie equivalent Compose Animations
                val infiniteTransition = rememberInfiniteTransition(label = "page_anim")
                val breathingScale by infiniteTransition.animateFloat(
                    initialValue = 0.99f,
                    targetValue = 1.01f, // Extremely subtle focus
                    animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "breathing"
                )

                val floatOffset by infiniteTransition.animateFloat(
                    initialValue = -4f,
                    targetValue = 4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "floating"
                )
                
                val glowPulse by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowPulse"
                )

                // Interactive Elements / Alive Scene Environment (Subtle focus)
                if (page.id == "cleaning") {
                    // Minimal sparkles near mop head
                    Text("✨", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 120.dp, bottom = 120.dp).offset(y = floatOffset.dp).alpha(glowPulse))
                    Text("✨", fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 90.dp, bottom = 90.dp).offset(y = (floatOffset * 1.5f).dp).alpha(glowPulse * 0.7f))
                } else if (page.id == "electrician") {
                    // Soft glow pulse moving
                    Text("⚡", fontSize = 32.sp, color = Color(0xFFFFB300), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 90.dp, bottom = 60.dp).scale(glowPulse + 0.3f).alpha(glowPulse))
                } else if (page.id == "plumbing") {
                    // Gentle water drops trickling
                    Text("💧", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(end = 40.dp, bottom = 110.dp).offset(y = (floatOffset * 2.5f).dp).alpha(glowPulse))
                }

                // 3D Character Illustration (FULL BLEED)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Image(
                        painter = painterResource(id = page.imageRes),
                        contentDescription = page.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(breathingScale),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Soft Ellipse Ground Shadow (Simulates 25-40px Blur at 10-15% Opacity)
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth(0.65f).height(28.dp)) {
                        drawOval(
                            Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.12f), Color.Transparent) // Very soft 12% opacity
                            ),
                            size = size
                        )
                    }
                }
            }

            // EXACT SPACING SYSTEM IMPLEMENTATION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp)) // Hero → Heading: 24px
                
                Text(
                    text = page.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold, // Clean, strong, classic
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp)) // Heading → Subtext

                Text(
                    text = page.description,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal, // Lighter, more spacing
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp)) // Subtext → Trust pill

                // Classy Thin Trust Pill
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        if (page.trustIcon == "✔") {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        } else {
                            Text(page.trustIcon, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(page.trustLine, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp)) // Trust pill → CTA

                // Pagination Dots (Dynamically switching width shapes)
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(pages.size) { i ->
                        val isSelected = pagerState.currentPage == i
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
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

                // CTA Button (Interaction Gradient)
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "btn_scale")

                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (pageIndex < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pageIndex + 1) }
                        } else {
                            onGetStarted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .scale(buttonScale),
                    shape = RoundedCornerShape(29.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    interactionSource = interactionSource,
                    elevation = null // Disable native elevation to handle explicitly
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(29.dp), spotColor = Color(0xFF1E293B), ambientColor = Color(0xFF1E293B))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))), // Sophisticated slate
                                RoundedCornerShape(29.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            page.ctaText,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (page.microCopy != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        page.microCopy,
                        color = Color(0xFF90A4AE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // CTA → bottom: 16px
            }
        }
    }
}

// ─── Configuration ────────────────────────────────────────────────────────────
data class OnboardPage(
    val id: String,
    val imageRes: Int,
    val title: String,
    val description: String,
    val trustLine: String,
    val trustIcon: String,
    val bgStart: Color,
    val bgEnd: Color,
    val ctaText: String,
    val microCopy: String? = null
)

// ─── Legacy wrappers (kept for safety) ────────────────────────────────────────
@Composable fun SplashScreen() = PremiumSplashScreen()

@Composable fun OnboardingPage(page: Int) {}
