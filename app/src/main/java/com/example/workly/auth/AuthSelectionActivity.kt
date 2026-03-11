package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import kotlinx.coroutines.delay

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
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = ProfessionalBlue
    ) { innerPadding ->
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(brush = PrimaryGradient)
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f
                )
                drawCircle(
                    color = EnergyOrange.copy(alpha = 0.2f),
                    center = Offset(0f, size.height),
                    radius = size.width * 0.5f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                Spacer(modifier = Modifier.weight(1f))

                // Logo Animation
                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn() + fadeIn()
                ) {
                    Surface(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(20.dp, CircleShape),
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
                }
                
                Spacer(modifier = Modifier.height(40.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically { 50 } + fadeIn(tween(500, delayMillis = 300))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Workly",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Your trusted partner for\nall your daily needs.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Buttons
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically { 100 } + fadeIn(tween(500, delayMillis = 600))
                ) {
                    Column {
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = ProfessionalBlue
                            )
                        ) {
                            Text(
                                text = "Log In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha=0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = "Create Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
