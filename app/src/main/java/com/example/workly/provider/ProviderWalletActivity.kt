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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workly.network.TransactionData
import com.example.workly.theme.*

class ProviderWalletActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // In a real app, this would come from a SessionManager
        val token = "MOCK_TOKEN" 

        setContent {
            WorklyTheme {
                val viewModel: WalletViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                
                LaunchedEffect(Unit) {
                    viewModel.fetchWalletOverview(token)
                }

                WalletScreen(
                    uiState = uiState,
                    onBackClick = { finish() },
                    onWithdrawClick = { amount ->
                        viewModel.requestWithdrawal(token, amount)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    uiState: WalletState,
    onBackClick: () -> Unit,
    onWithdrawClick: (Double) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Wallet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        when (uiState) {
            is WalletState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ProfessionalBlue)
                }
            }
            is WalletState.Success -> {
                val data = uiState.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { BalanceCard(data.availableForPayout, data.pendingEscrow) }
                    
                    item {
                        Button(
                            onClick = { onWithdrawClick(data.availableForPayout) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalBlue),
                            enabled = data.availableForPayout >= 100
                        ) {
                            Text("Withdraw Available Balance", fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    items(data.transactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
            is WalletState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.message}", color = Color.Red)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun BalanceCard(available: Double, pending: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ProfessionalBlue)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Available for Payout", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Text("₹${available.toInt()}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pending Escrow: ₹${pending.toInt()}", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if (transaction.type == "WITHDRAWAL") Icons.Default.CallMade else Icons.Default.CallReceived
                val color = if (transaction.type == "WITHDRAWAL") EnergyOrange else ElectricTeal
                
                Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.1f)) {
                    Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(transaction.type, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(transaction.status, fontSize = 12.sp, color = if (transaction.status == "SUCCESS") ElectricTeal else TextSecondary)
                }
            }
            Text("₹${transaction.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
