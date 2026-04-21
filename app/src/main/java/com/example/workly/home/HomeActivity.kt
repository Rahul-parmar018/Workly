package com.example.workly.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workly.auth.LoginActivity
import com.example.workly.auth.AuthSelectionActivity
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.workly.data.Service

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val themeDataStore = remember { ThemeDataStore(this) }
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = themeDataStore.getInitialThemeMode())
            WorklyTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: HomeViewModel = viewModel()) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // ── Firestore User Data State ──
    var userName by remember { mutableStateOf("Loading...") }
    var userRole by remember { mutableStateOf("user") }
    var dataLoaded by remember { mutableStateOf(false) }
    var totalUnreadCount by remember { mutableIntStateOf(0) }

    val startTime = remember { System.currentTimeMillis() }
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            
            // Start Background Sync Service
            val serviceIntent = Intent(context, com.example.workly.notifications.WorklyBackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // Total Unread Count Listener
            db.collection("chats")
                .whereArrayContains("members", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        var total = 0
                        snapshot.documents.forEach { doc ->
                            val lastSenderId = doc.getString("lastSenderId")
                            val isRead = doc.getBoolean("isRead") ?: true
                            if (!isRead && lastSenderId != user.uid) {
                                total++
                            }
                        }
                        totalUnreadCount = total
                    }
                }

            // ── User Data Listener ──
            db.collection("users").document(user.uid)
                .addSnapshotListener { doc, error ->
                    if (doc != null && doc.exists()) {
                        userName = doc.getString("name") ?: (user.displayName ?: "User")
                        userRole = doc.getString("role") ?: "user"
                    } else if (error != null) {
                        userName = user.displayName ?: "User"
                    } else {
                        userName = user.displayName ?: "User"
                        userRole = "user"
                    }
                    dataLoaded = true
                }

            // ── Live Notification Listener (For System Tray) ──
            db.collection("notifications")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("isRead", false)
                .whereGreaterThan("createdAt", startTime)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val doc = change.document
                            val type = doc.getString("type") ?: ""
                            if (type == "chat") {
                                val sName = doc.getString("senderName") ?: "New Message"
                                val msg = doc.getString("message") ?: ""
                                val cId = doc.getString("chatId") ?: ""
                                val sId = doc.getString("senderId") ?: ""
                                
                                // Only show notification if NOT currently in this chat
                                if (com.example.workly.chat.ChatSessionManager.activeChatId != cId) {
                                    com.example.workly.notifications.WorklyNotificationManager.showChatNotification(
                                        context, sName, msg, cId, sId
                                    )
                                }
                                
                                // Mark as seen in notifications collection so we don't show it again in this listener
                                doc.reference.update("isRead", true) 
                            }
                        }
                    }
                }
        } else {
            dataLoaded = true
        }
    }

    Scaffold(containerColor = PremiumBlack) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = selectedItem, label = "ContentFade") { target ->
                    when (target) {
                        0 -> HomeScreenContent(
                            innerPadding = innerPadding,
                            viewModel = viewModel,
                            userName = userName,
                            userRole = userRole,
                            onSeeAllServices = {
                                context.startActivity(Intent(context, ServicesActivity::class.java))
                            },
                            onNavigateToProfile = { selectedItem = 3 }
                        )
                        1 -> ExploreScreenContent(
                            innerPadding = innerPadding,
                            onServiceClick = { service: com.example.workly.data.Service ->
                                val intent = Intent(context, ServiceDetailActivity::class.java).apply {
                                    putExtra("SERVICE_TITLE", service.title)
                                    putExtra("SERVICE_PRICE", service.price)
                                    putExtra("SERVICE_CATEGORY", service.category)
                                    putExtra("SERVICE_ID", service.id)
                                    putExtra("SERVICE_DURATION", service.duration)
                                    putExtra("SERVICE_DESC", service.description)
                                    putExtra("PROVIDER_NAME", service.providerName)
                                    putExtra("PROVIDER_ID", service.providerId)
                                    putExtra("SERVICE_IMG", service.imageUrl.ifEmpty { getPremiumImageForCategory(service.category) })
                                }
                                context.startActivity(intent)
                            }
                        )
                        2 -> InboxScreen()
                        3 -> ProfileScreenContent(
                            userName = userName,
                            userRole = userRole,
                            onLogout = { performLogout(context) }
                        )
                    }
                }
            }

            // Floating bottom nav
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                FloatingBottomBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    unreadCount = totalUnreadCount
                )
            }


        }
    }

}

fun performLogout(context: Context) {
    // Stop Background Sync
    val serviceIntent = Intent(context, com.example.workly.notifications.WorklyBackgroundService::class.java)
    context.stopService(serviceIntent)
    
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(context, AuthSelectionActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}