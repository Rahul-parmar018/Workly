package com.example.workly.provider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.OrderStatus
import com.example.workly.theme.ElectricTeal
import com.example.workly.theme.EnergyOrange
import com.example.workly.theme.ProfessionalBlue
import com.example.workly.theme.TextPrimary
import com.example.workly.theme.WorklyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProviderOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                ProviderOrdersScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderOrdersScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    var ordersList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("orders")
                .whereEqualTo("providerId", user.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("ProviderOrders", "Error: ${error.message}")
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        ordersList = snapshot.documents.mapNotNull { 
                            val data = it.data?.toMutableMap() ?: mutableMapOf()
                            data["id"] = it.id
                            data
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Incoming Orders", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ProfessionalBlue)
            } else if (ordersList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No orders received yet.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordersList) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Map<String, Any>) {
    val db = FirebaseFirestore.getInstance()
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
    val context = androidx.compose.ui.platform.LocalContext.current
    val orderId = order["id"]?.toString() ?: ""
    val serviceTitle = order["serviceName"]?.toString() ?: "Order Task"
    val userName = order["userName"]?.toString() ?: "Customer"
    val price = order["finalPrice"]?.toString() ?: order["price"]?.toString() ?: "0"
    val status = order["status"]?.toString() ?: OrderStatus.PENDING
    
    var isProcessing by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = serviceTitle, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                    Text(text = "From: $userName", fontSize = 13.sp, color = Color.Gray)
                }
                Text(
                    text = status.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when(status) {
                        OrderStatus.COMPLETED -> Color(0xFF2E7D32)
                        OrderStatus.ACCEPTED -> ProfessionalBlue
                        else -> EnergyOrange
                    },
                    modifier = Modifier
                        .background(
                            when(status) {
                                OrderStatus.COMPLETED -> Color(0xFFE8F5E9)
                                OrderStatus.ACCEPTED -> ProfessionalBlue.copy(0.1f)
                                else -> EnergyOrange.copy(0.1f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Payout: ₹$price", fontSize = 16.sp, color = ProfessionalBlue, fontWeight = FontWeight.Bold)
                
                if (status != OrderStatus.CANCELLED && status != OrderStatus.COMPLETED) {
                    OutlinedButton(
                        onClick = {
                            val targetId = order["userId"]?.toString() ?: ""
                            val targetName = order["userName"]?.toString() ?: "Customer"
                            val chatIntent = android.content.Intent(context, com.example.workly.chat.ChatActivity::class.java).apply {
                                putExtra("RECEIVER_ID", targetId)
                                putExtra("RECEIVER_NAME", targetName)
                            }
                            context.startActivity(chatIntent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Assignment, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Chat")
                    }
                }

                if (status == OrderStatus.PENDING) {
                    Button(
                        onClick = {
                            isProcessing = true
                            db.runTransaction { transaction ->
                                val ref = db.collection("orders").document(orderId)
                                val snap = transaction.get(ref)
                                
                                // Firestore requires ALL reads before ANY writes!
                                val providerRef = db.collection("providers").document(user.uid)
                                val providerSnap = transaction.get(providerRef)
                                
                                if (snap.getString("status") == OrderStatus.PENDING) {
                                    val currentEarnings = providerSnap.getDouble("earnings") ?: 0.0
                                    val orderPrice = price.toDoubleOrNull() ?: 0.0
                                    
                                    // Writes
                                    transaction.update(ref, mapOf(
                                        "status" to OrderStatus.ACCEPTED,
                                        "acceptedAt" to System.currentTimeMillis()
                                    ))
                                    
                                    transaction.set(
                                        providerRef, 
                                        mapOf("earnings" to (currentEarnings + orderPrice)), 
                                        com.google.firebase.firestore.SetOptions.merge()
                                    )
                                }
                                null
                            }.addOnSuccessListener {
                                isProcessing = false
                                android.widget.Toast.makeText(context, "Order Accepted!", android.widget.Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e ->
                                isProcessing = false
                                android.widget.Toast.makeText(context, "Accept Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                android.util.Log.e("ProviderOrders", "Accept Transaction Error", e)
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Accept", fontWeight = FontWeight.Bold)
                    }
                } else if (status == OrderStatus.ACCEPTED) {
                    Button(
                        onClick = {
                            isProcessing = true
                            db.runTransaction { transaction ->
                                val ref = db.collection("orders").document(orderId)
                                val snap = transaction.get(ref)
                                if (snap.getString("status") == OrderStatus.ACCEPTED) {
                                    transaction.update(ref, mapOf(
                                        "status" to OrderStatus.COMPLETED,
                                        "completedAt" to System.currentTimeMillis()
                                    ))
                                }
                                null
                            }.addOnSuccessListener {
                                isProcessing = false
                                android.widget.Toast.makeText(context, "Order completed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e ->
                                isProcessing = false
                                android.widget.Toast.makeText(context, "Complete Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                android.util.Log.e("ProviderOrders", "Complete Transaction Error", e)
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Complete", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
