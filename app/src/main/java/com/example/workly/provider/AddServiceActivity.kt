package com.example.workly.provider

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.ProfessionalBlue
import com.example.workly.theme.TextPrimary
import com.example.workly.theme.TextSecondary
import com.example.workly.theme.WorklyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AddServiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                AddServiceScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Service", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Service Title (e.g. AC Repair)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = ProfessionalBlue,
                    unfocusedLabelColor = TextSecondary,
                    focusedPlaceholderColor = Color.DarkGray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Base Price (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = ProfessionalBlue,
                    unfocusedLabelColor = TextSecondary,
                    focusedPlaceholderColor = Color.DarkGray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (title.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (user != null) {
                        isLoading = true
                        val serviceId = UUID.randomUUID().toString()
                        val serviceData = hashMapOf(
                            "id" to serviceId,
                            "providerId" to user.uid,
                            "title" to title,
                            "price" to (price.toLongOrNull() ?: 0L),
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("services").document(serviceId).set(serviceData)
                            .addOnSuccessListener {
                                isLoading = false
                                Toast.makeText(context, "Service Published!", Toast.LENGTH_SHORT).show()
                                onBack() // automatically go back to see real-time listener update
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publish Service", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
