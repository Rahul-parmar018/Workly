package com.example.workly.auth

import com.example.workly.home.HomeActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
                    center = Offset(size.width, 0f),
                    radius = size.width
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
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Please login to your account",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = ProfessionalBlue) },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProfessionalBlue,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = ProfessionalBlue
                            ),
                            singleLine = true
                         )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Forgot Password?",
                            color = EnergyOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
                                }
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Login Button
                        Button(
                            onClick = {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                                context.startActivity(Intent(context, HomeActivity::class.java))
                                                (context as? ComponentActivity)?.finishAffinity()
                                            } else {
                                                Log.e("LoginActivity", "Login Failed", task.exception)
                                                Toast.makeText(context, "Login Failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
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
                                Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Don't have an account? ", color = TextSecondary)
                            Text(
                                "Sign Up",
                                color = ProfessionalBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(context, RegisterActivity::class.java))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
