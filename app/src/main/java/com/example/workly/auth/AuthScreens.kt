package com.example.workly.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.R
import com.example.workly.theme.*
import com.example.workly.theme.PremiumSilver
import com.example.workly.theme.PremiumBlackSurface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun LoginScreen(
    onSignIn: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignUp: () -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Premium Silver Logo Container
            Surface(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                color = PremiumBlackSurface,
                border = BorderStroke(2.dp, PremiumSilver)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VpnKey, null, tint = PremiumSilver, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("AUTHENTICATE", color = PremiumSilver, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("Workly Premium", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(48.dp))

            // Glassmorphic Auth Panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = PremiumBlackSurface,
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email,
                        onSurf = onSurf
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        onSurf = onSurf,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onSignIn(email, password) },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        else Text("ACCESS ACCOUNT", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Request Access / Register instantly",
                        modifier = Modifier.clickable { onSignUp() },
                        color = PremiumSilver.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Divider(color = DarkBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 40.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { onGoogleSignIn() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("CONTINUE WITH GOOGLE", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSurf: Color,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = PremiumSilver.copy(alpha = 0.4f)) },
        leadingIcon = { Icon(icon, null, tint = PremiumSilver.copy(alpha = 0.4f)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = PremiumSilver.copy(alpha = 0.4f))
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PremiumSilver,
            unfocusedBorderColor = DarkBorder,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun AuthSelectionScreen(
    onRoleSelect: (String) -> Unit,
    onContinue: (String) -> Unit,
    onSignIn: () -> Unit,
    onAdminPortal: () -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    val primary = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize().background(bg).padding(24.dp).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Image(painter = painterResource(id = R.drawable.workly_logo), contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text("How would you like to use Workly?", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = onBg)
        Spacer(Modifier.height(40.dp))

        RoleCard(
            title = "I want to Book Services",
            description = "Find cleaners, plumbers & more",
            isSelected = selectedRole == "user",
            onClick = { selectedRole = "user" },
            onSurf = onSurf,
            primary = primary
        )

        Spacer(Modifier.height(16.dp))

        RoleCard(
            title = "I want to Offer Services",
            description = "Join as a professional partner",
            isSelected = selectedRole == "provider",
            onClick = { selectedRole = "provider" },
            onSurf = onSurf,
            primary = primary
        )

        Spacer(Modifier.weight(1f))

        if (selectedRole != null) {
            Button(
                onClick = { onContinue(selectedRole!!) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Already have an account? Sign In",
            modifier = Modifier.clickable { onSignIn() },
            color = primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        Text(
            "Admin Portal",
            modifier = Modifier.clickable { onAdminPortal() },
            color = onSurf.copy(0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSurf: Color,
    primary: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) primary.copy(0.1f) else onSurf.copy(0.05f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, primary) else null
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurf)
                Text(description, fontSize = 13.sp, color = onSurf.copy(0.6f))
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = primary)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onSignUp: (String, String, String) -> Unit,
    onSignIn: () -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text("JOIN THE ELITE", color = PremiumSilver, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("Create Account", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = PremiumBlackSurface,
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        icon = Icons.Default.Person,
                        onSurf = onSurf
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        icon = Icons.Default.Email,
                        onSurf = onSurf
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        onSurf = onSurf,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onSignUp(name, email, password) },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumSilver),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        else Text("CREATE ELITE ACCOUNT", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Already a member? Sign In",
                        modifier = Modifier.clickable { onSignIn() },
                        color = PremiumSilver.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ProviderRegisterScreen(
    onSignUp: (String, String, String) -> Unit,
    onSignIn: () -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primary = Color(0xFF1E2A78) // Keep Brand Blue for Provider flow
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(shape = CircleShape, color = primary.copy(0.1f), modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Engineering, null, tint = primary, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Partner Registration", color = onSurf, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Start earning with Workly today", color = onSurf.copy(0.6f), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(40.dp))

            AuthTextField(value = name, onValueChange = { name = it }, label = "Business/Full Name", icon = Icons.Default.Business, onSurf = onSurf)
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = email, onValueChange = { email = it }, label = "Professional Email", icon = Icons.Default.Email, onSurf = onSurf)
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, onSurf = onSurf, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onSignUp(name, email, password) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Register as Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Already a partner? Sign In",
                modifier = Modifier.clickable { onSignIn() },
                color = primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminLoginScreen(
    onSignIn: (String, String) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primary = Color(0xFFC62828) // Admin Red
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = onSurf)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Surface(shape = CircleShape, color = primary.copy(0.1f), modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = primary, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Admin Portal", color = onSurf, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Access management & controls", color = onSurf.copy(0.6f), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(40.dp))

            AuthTextField(value = email, onValueChange = { email = it }, label = "Admin Email", icon = Icons.Default.Email, onSurf = onSurf)
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = password, onValueChange = { password = it }, label = "Admin Password", icon = Icons.Default.Lock, onSurf = onSurf, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onSignIn(email, password) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Secure Admin Access", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProviderLoginScreen(
    onSignIn: (String, String) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primary = Color(0xFF1E2A78) // Brand Blue
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurf = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = onSurf)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Surface(shape = CircleShape, color = primary.copy(0.1f), modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Engineering, null, tint = primary, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Partner Login", color = onSurf, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Manage your services and earnings", color = onSurf.copy(0.6f), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(40.dp))

            AuthTextField(value = email, onValueChange = { email = it }, label = "Partner Email", icon = Icons.Default.Email, onSurf = onSurf)
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, onSurf = onSurf, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onSignIn(email, password) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Partner Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
