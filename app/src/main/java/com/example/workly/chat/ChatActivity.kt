package com.example.workly.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.Message
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val receiverName = intent.getStringExtra("RECEIVER_NAME") ?: intent.getStringExtra("PRO_NAME") ?: "User"
        val receiverId = intent.getStringExtra("RECEIVER_ID") ?: intent.getStringExtra("PRO_ID") ?: ""
        setContent {
            WorklyTheme {
                ChatScreen(receiverName = receiverName, receiverId = receiverId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(receiverName: String, receiverId: String, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // ─── Real-time message listener ──────────────────────────────────────────
    LaunchedEffect(receiverId) {
        if (userId.isNotEmpty() && receiverId.isNotEmpty()) {
            val chatId = if (userId < receiverId) "${userId}_$receiverId" else "${receiverId}_$userId"
            firestore.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        messages = try { snapshot.toObjects(Message::class.java) } catch (e: Exception) { emptyList() }
                        scope.launch {
                            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9), // Light background like WhatsApp
        topBar = {
            Column {
                // 🔥 1. HEADER (Upgrade)
                Surface(shadowElevation = 6.dp, color = Color.White) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(42.dp)) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, null, tint = Color(0xFF1E2A78))
                                        }
                                    }
                                    // Verified Badge
                                    Surface(
                                        modifier = Modifier.align(Alignment.BottomEnd).size(14.dp),
                                        shape = CircleShape,
                                        color = Color.White
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF1E2A78), modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(receiverName, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✔ Verified", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3FA3))
                                    }
                                    Text("\ud83d\udfe2 Online \u2022 Responds in 5 min", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF0F172A))
                            }
                        }
                    )
                }

                // 🔥 2. SERVICE CONTEXT CARD (Sticky)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CleaningServices, null, tint = Color(0xFF1E2A78), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Full Home Cleaning", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("\u20b91499 \u2022 3 hrs", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEAF6FF)) {
                            Text("Accepted", color = Color(0xFF1E2A78), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.background(Color.White).navigationBarsPadding().imePadding()) {
                // 🔥 4. QUICK ACTIONS
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val actions = listOf("\ud83d\udccd Share Location", "\ud83d\udcde Call Provider", "\u274c Cancel Booking")
                    items(actions) { action ->
                        Surface(
                            modifier = Modifier.clickable { /* Action */ },
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            color = Color.White
                        ) {
                            Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Color(0xFF1E2A78))
                        }
                    }
                }

                // 🔥 5. INPUT BAR (Upgraded)
                Row(
                    modifier = Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(48.dp).clickable { /* Attach */ }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFF1E2A78))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type a message...", color = Color(0xFF64748B), fontSize = 15.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            maxLines = 3
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        onClick = {
                            val text = messageText.trim()
                            if (text.isNotEmpty() && userId.isNotEmpty()) {
                                sendChatMessage(firestore, userId, receiverId, text)
                                messageText = ""
                            }
                        },
                        shape = CircleShape,
                        color = Color(0xFF1E2A78),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            // 🔥 6. EMPTY STATE (Upgrade)
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
                    Surface(shape = CircleShape, color = Color(0xFFEAF6FF), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QuestionAnswer, null, tint = Color(0xFF1E2A78), modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Start chatting with your provider", fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ask about timing, special requirements, or preparation before they arrive.", fontSize = 14.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
            ) {
                items(messages) { message ->
                    WhatsAppChatBubble(message, isMe = message.senderId == userId)
                }
            }
        }
    }
}

@Composable
fun WhatsAppChatBubble(message: Message, isMe: Boolean) {
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp.toDate())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMe) Color(0xFF1E2A78) else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = if (isMe) Color.White else Color(0xFF0F172A),
                    fontSize = 15.sp,
                    modifier = Modifier.widthIn(max = 240.dp)
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(0.7f) else Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DoneAll,
                            null,
                            tint = if (message.isRead) Color(0xFF34B7F1) else Color.White.copy(0.7f),
                            modifier = Modifier.size(14.dp).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

fun sendChatMessage(
    firestore: FirebaseFirestore,
    senderId: String,
    receiverId: String,
    content: String
) {
    val chatId = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"
    val colRef = firestore.collection("chats").document(chatId).collection("messages")
    val docRef = colRef.document()
    val message = Message(
        id = docRef.id,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        timestamp = com.google.firebase.Timestamp.now()
    )
    
    val parentRef = firestore.collection("chats").document(chatId)
    val chatData = mapOf(
        "lastMessage" to content,
        "lastTimestamp" to com.google.firebase.Timestamp.now(),
        "members" to listOf(senderId, receiverId)
    )
    
    firestore.runBatch { batch ->
        batch.set(docRef, message)
        batch.set(parentRef, chatData, com.google.firebase.firestore.SetOptions.merge())
    }
}
