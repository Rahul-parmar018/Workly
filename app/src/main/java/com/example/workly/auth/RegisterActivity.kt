package com.example.workly.auth

import com.example.workly.home.HomeActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.example.workly.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                RegisterScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Full Screen Professional Gradient Background ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D47A1), // Professional Blue
                            Color(0xFF01579B), // Light Professional Blue
                            Color.White        // Transition to bottom white
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Large Immersive Header (Restored) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Curved Clip for the Header Design with Premium Image
                Surface(
                    modifier = Modifier.fillMaxSize().padding(bottom = 40.dp),
                    shape = RoundedCornerShape(bottomStart = 80.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_header_premium),
                        contentDescription = "Workly Premium Header",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Header Navigation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, start = 32.dp)
                ) {
                    IconButton(
                        onClick = { (context as? ComponentActivity)?.finish() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black)
                    }
                }    
    
                // Profile Image Placeholder
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-32).dp, y = 0.dp)
                        .shadow(15.dp, CircleShape),
                    border = BorderStroke(4.dp, Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user_pfp),
                        contentDescription = "User Avatar",
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Main Form Content ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Full Name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ProfessionalBlue.copy(alpha = 0.05f),
                        unfocusedContainerColor = ProfessionalBlue.copy(alpha = 0.02f),
                        focusedBorderColor = ProfessionalBlue,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ProfessionalBlue.copy(alpha = 0.05f),
                        unfocusedContainerColor = ProfessionalBlue.copy(alpha = 0.02f),
                        focusedBorderColor = ProfessionalBlue,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ProfessionalBlue.copy(alpha = 0.05f),
                        unfocusedContainerColor = ProfessionalBlue.copy(alpha = 0.02f),
                        focusedBorderColor = ProfessionalBlue,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // GET STARTED Button
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val profileUpdates = userProfileChangeRequest {
                                            displayName = name
                                        }
                                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                            isLoading = false
                                            context.startActivity(Intent(context, HomeActivity::class.java))
                                            (context as? ComponentActivity)?.finishAffinity()
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("GET STARTED", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", color = Color.Gray.copy(alpha = 0.8f))
                    Text(
                        "Sign In",
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(context, LoginActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}
