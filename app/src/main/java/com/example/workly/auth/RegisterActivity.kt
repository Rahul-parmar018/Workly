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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

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

@Composable
fun RegisterScreen() {
    var name by remember { mutableStateOf("") }
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
                    center = Offset(0f, 0f),
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
                            text = "Get Started",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Create your free account",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = ProfessionalBlue) },
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

                        Spacer(modifier = Modifier.height(40.dp))

                        // Register Button
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser
                                                val profileUpdates = UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name)
                                                    .build()

                                                user?.updateProfile(profileUpdates)
                                                    ?.addOnCompleteListener { profileTask ->
                                                        isLoading = false
                                                        if (profileTask.isSuccessful) {
                                                            Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show()
                                                            context.startActivity(Intent(context, HomeActivity::class.java))
                                                            (context as? ComponentActivity)?.finishAffinity()
                                                        } else {
                                                             Toast.makeText(context, "Profile Update Failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            } else {
                                                isLoading = false
                                                Log.e("RegisterActivity", "Sign Up Failed", task.exception)
                                                Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfessionalBlue
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // --- Google Sign In ---
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                            Text(" OR ", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp))
                            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(com.example.workly.R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)

                        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            if (result.resultCode == android.app.Activity.RESULT_OK) {
                                isLoading = true
                                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                                try {
                                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                                    val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
                                    auth.signInWithCredential(credential)
                                        .addOnCompleteListener { authTask ->
                                            isLoading = false
                                            if (authTask.isSuccessful) {
                                                Toast.makeText(context, "Google Sign-Up Successful", Toast.LENGTH_SHORT).show()
                                                context.startActivity(Intent(context, HomeActivity::class.java))
                                                (context as? ComponentActivity)?.finishAffinity()
                                            } else {
                                                Toast.makeText(context, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } catch (e: com.google.android.gms.common.api.ApiException) {
                                    isLoading = false
                                    Toast.makeText(context, "Google Sign-In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        Button(
                            onClick = { launcher.launch(googleSignInClient.signInIntent) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                            Text("Sign up with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Already have an account? ", color = TextSecondary)
                            Text(
                                "Log In",
                                color = ProfessionalBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
