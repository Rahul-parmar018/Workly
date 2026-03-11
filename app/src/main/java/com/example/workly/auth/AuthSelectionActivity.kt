package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.example.workly.R
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import com.example.workly.home.HomeActivity

class AuthSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                AuthSelectionScreen(
                    onLoginClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun AuthSelectionScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var visible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Google Sign-In Configuration
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    isLoading = false
                    if (authTask.isSuccessful) {
                        context.startActivity(Intent(context, HomeActivity::class.java))
                        (context as? ComponentActivity)?.finishAffinity()
                    } else {
                        Toast.makeText(context, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: ApiException) {
            isLoading = false
            Toast.makeText(context, "Google Sign-In Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Full Screen Professional Gradient Background ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E), // Deep Blue
                            Color(0xFF0D47A1), // Professional Blue
                            Color(0xFF01579B)  // Light Professional Blue
                        )
                    )
                )
        )
        
        // Subtle Pattern Overlay
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            val spacing = 40.dp.toPx()
            for (x in 0..size.width.toInt() step spacing.toInt()) {
                for (y in 0..size.height.toInt() step spacing.toInt()) {
                    drawCircle(Color.White, radius = 2f, center = Offset(x.toFloat(), y.toFloat()))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Professional Circular Header with Official Logo (Restored Design)
            Box(contentAlignment = Alignment.Center) {
                if (visible) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(175.dp).shadow(25.dp, CircleShape),
                        border = BorderStroke(4.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.workly_logo),
                            contentDescription = "Workly Official Logo",
                            modifier = Modifier.padding(32.dp).fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                // Banana Mascot Overlay (Kept as part of the Design)
            }

            Spacer(modifier = Modifier.height(32.dp))


            Spacer(modifier = Modifier.height(16.dp))

            // Clean Subtext
            Text(
                text = "Join thousands of users finding expert help every day.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons (Polished 50dp height)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sign in with Google
                Button(
                    onClick = {
                        isLoading = true
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(10.dp, RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A237E)
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1A237E))
                    } else {
                        Text(
                            text = "Sign in with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Already have an account? Sign In",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onLoginClick() }
                )
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
