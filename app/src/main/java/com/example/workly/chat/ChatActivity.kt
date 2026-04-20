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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val receiverName = intent.getStringExtra("RECEIVER_NAME") ?: intent.getStringExtra("PRO_NAME") ?: "User"
        val receiverId = intent.getStringExtra("RECEIVER_ID") ?: intent.getStringExtra("PRO_ID") ?: ""
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
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
    val userId = (auth.currentUser?.uid ?: "").trim()
    val rId = receiverId.trim()
    val context = LocalContext.current
    val chatId = remember(userId, rId) { 
        if (userId < rId) "${userId}_$rId" else "${rId}_$userId" 
    }

    // ─── Manage Chat Session ────────────────────────────────────────────────
    DisposableEffect(chatId) {
        ChatSessionManager.activeChatId = chatId
        onDispose {
            ChatSessionManager.activeChatId = null
        }
    }
    
    var currentUserName by remember { mutableStateOf("User") }
    var actualReceiverName by remember { mutableStateOf(receiverName) }

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isOtherTyping by remember { mutableStateOf(false) }

    // ─── Typing Indicator (Self Update) ──────────────────────────────────────
    LaunchedEffect(messageText) {
        if (userId.isNotEmpty() && chatId.isNotEmpty()) {
            val isTyping = messageText.isNotEmpty()
            firestore.collection("chats").document(chatId)
                .update("typing_$userId", isTyping)
            
            if (isTyping) {
                delay(2000) // Debounce: stop typing status after 2 seconds of inactivity
                firestore.collection("chats").document(chatId)
                    .update("typing_$userId", false)
            }
        }
    }

    // ─── Typing Indicator (Listen for Other) ─────────────────────────────────
    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            firestore.collection("chats").document(chatId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        isOtherTyping = snapshot.getBoolean("typing_$rId") ?: false
                    }
                }
        }
    }

    // ─── Fetch Names and Mark as Read ────────────────────────────────────────
    LaunchedEffect(userId, rId) {
        if (userId.isNotEmpty() && rId.isNotEmpty()) {
            // Fetch current user name
            firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
                currentUserName = doc.getString("name") ?: "User"
            }
            
            // Fetch receiver name if default
            if (receiverName == "User" || receiverName.startsWith("User ")) {
                firestore.collection("users").document(rId).get().addOnSuccessListener { doc ->
                    doc.getString("name")?.let { actualReceiverName = it }
                }
            }

            // Mark as read
            firestore.collection("chats").document(chatId).update("isRead", true)
        }
    }

    // ─── Real-time message listener ──────────────────────────────────────────
    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty() && !chatId.startsWith("_") && !chatId.endsWith("_")) {
            firestore.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null) {
                        messages = snapshot.toObjects(Message::class.java)
                        scope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                }
        }
    }

    val bg        = MaterialTheme.colorScheme.background
    val primary   = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface   = MaterialTheme.colorScheme.surface
    val surfVar   = MaterialTheme.colorScheme.surfaceVariant
    val outline   = MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp)) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = surfVar
                            ) {
                                ChatAvatar(actualReceiverName)
                            }
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).size(14.dp),
                                shape = CircleShape,
                                color = surface
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = primary, modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(actualReceiverName, fontSize = 16.sp, fontWeight = FontWeight.Black, color = onSurface)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✔ Verified", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = primary)
                            }
                            if (isOtherTyping) {
                                Text("✍ Typing...", fontSize = 11.sp, color = primary, fontWeight = FontWeight.Bold)
                            } else {
                                Text("🟢 Online \u2022 Responds in 5 min", fontSize = 11.sp, color = onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surface)
            )
        },

        bottomBar = {
            Column(modifier = Modifier.background(surface).navigationBarsPadding().imePadding()) {
                // Quick Actions
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val actions = listOf("📍 Share Location", "📞 Call Info", "❌ Issue Report")
                    items(actions) { action ->
                        Surface(
                            modifier = Modifier.clickable { /* Action */ },
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(1.dp, outline.copy(alpha = 0.1f)),
                            color = surface
                        ) {
                            Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = primary)
                        }
                    }
                }

                // Input Bar (Upgraded)
                Row(
                    modifier = Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = surfVar, modifier = Modifier.size(48.dp).clickable { /* Attach */ }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = surfVar
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type a message...", color = onSurface.copy(alpha = 0.4f), fontSize = 15.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                focusedTextColor = onSurface,
                                unfocusedTextColor = onSurface
                            ),
                            maxLines = 3
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        onClick = {
                            val text = messageText.trim()
                            if (text.isNotEmpty() && userId.isNotEmpty()) {
                                sendChatMessage(firestore, userId, currentUserName, receiverId, actualReceiverName, text) { success ->
                                    if (!success) {
                                        android.widget.Toast.makeText(context, "Cloud sync failed. Check connectivity.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                messageText = ""
                            }
                        },
                        shape = CircleShape,
                        color = primary,
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
            // Empty State
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
                    Surface(shape = CircleShape, color = primary.copy(alpha = 0.08f), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QuestionAnswer, null, tint = primary, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Secure Chat Environment", fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center, color = onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ask about timing, special requirements, or preparation details.", fontSize = 14.sp, color = onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
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
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMe) primary else surface,
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
                    color = if (isMe) Color.White else onSurface,
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
                        color = if (isMe) Color.White.copy(0.7f) else onSurface.copy(alpha = 0.5f),
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
    senderName: String,
    receiverId: String,
    receiverName: String,
    content: String,
    onComplete: (Boolean) -> Unit = {}
) {
    if (senderId.isEmpty() || receiverId.isEmpty()) return

    val chatId = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"
    
    val parentRef = firestore.collection("chats").document(chatId)
    val colRef = parentRef.collection("messages")
    val docRef = colRef.document()
    
    val message = Message(
        id = docRef.id,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        timestamp = com.google.firebase.Timestamp.now()
    )
    
    val chatUpdate = mapOf(
        "lastMessage" to content,
        "lastTimestamp" to com.google.firebase.Timestamp.now(),
        "members" to listOf(senderId, receiverId),
        "isRead" to false,
        "lastSenderId" to senderId,
        "name_$senderId" to senderName,
        "name_$receiverId" to receiverName
    )
    
    // Execute message & chat preview update in a batch
    firestore.runBatch { batch ->
        batch.set(docRef, message)
        batch.set(parentRef, chatUpdate, com.google.firebase.firestore.SetOptions.merge())
    }.addOnSuccessListener {
        onComplete(true)
        // Only attempt notification if the message was successful
        val notificationRef = firestore.collection("notifications").document()
        val notificationData = mapOf(
            "id" to notificationRef.id,
            "userId" to receiverId,
            "title" to "New Message from $senderName",
            "message" to content,
            "type" to "chat",
            "chatId" to chatId,
            "senderId" to senderId,
            "senderName" to senderName,
            "createdAt" to System.currentTimeMillis(),
            "isRead" to false
        )
        firestore.collection("notifications").document(notificationRef.id).set(notificationData)
            .addOnFailureListener { e ->
                android.util.Log.e("ChatSync", "Notification failed: ${e.message}")
            }
    }.addOnFailureListener { e ->
        onComplete(false)
        android.util.Log.e("ChatSync", "Batch failed (Message not sent): ${e.message}")
    }
}

@Composable
fun ChatAvatar(receiverName: String) {
    AsyncImage(
        model = "https://ui-avatars.com/api/?name=${receiverName.replace(" ", "+")}&background=1E2A78&color=fff&bold=true",
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
