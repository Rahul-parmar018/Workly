package com.example.workly.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                ForgotPasswordScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen() {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        containerColor = ProfessionalBlue
    ) { innerPadding ->
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Fancy Background
             Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(brush = PrimaryGradient)
                 drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width * 0.8f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header
                Row(
                   modifier = Modifier.padding(24.dp),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { (context as? ComponentActivity)?.finish() },
                    ) {
                        Icon(
                             Icons.Default.ArrowBack, 
                             contentDescription = "Back",
                             modifier = Modifier.padding(12.dp),
                             tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Sheet Content
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = Color.White,
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ProfessionalBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(80.dp)
                        ) {
                             Icon(
                                 Icons.Default.LockReset,
                                 contentDescription = null,
                                 tint = ProfessionalBlue,
                                 modifier = Modifier.padding(20.dp)
                             )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Enter your email address to receive\na password reset link.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = ProfessionalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProfessionalBlue,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = ProfessionalBlue
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    isLoading = true
                                    auth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Reset link sent!", Toast.LENGTH_LONG).show()
                                                (context as? ComponentActivity)?.finish()
                                            } else {
                                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfessionalBlue
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Send Reset Link", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
