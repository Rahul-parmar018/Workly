package com.example.workly.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.workly.data.Message
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
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
    val bg        = MaterialTheme.colorScheme.background
    val primary   = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface   = MaterialTheme.colorScheme.surface
    val surfVar   = MaterialTheme.colorScheme.surfaceVariant
    val outline   = MaterialTheme.colorScheme.outline
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
    var userRole by remember { mutableStateOf("user") }
    var actualReceiverName by remember { mutableStateOf(receiverName) }
    var isOtherTyping by remember { mutableStateOf(false) }

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var savedAddress by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // Fetch latest booking address between these users
    LaunchedEffect(userId, rId) {
        if (userId.isNotEmpty() && rId.isNotEmpty()) {
            firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("providerId", rId)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        savedAddress = snapshot.documents[0].getString("address") ?: ""
                    }
                }
        }
    }

    fun shareSavedLocation() {
        if (savedAddress.isNotEmpty()) {
            val locationUrl = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(savedAddress)}"
            sendChatMessage(firestore, userId, currentUserName, rId, actualReceiverName, locationUrl, type = "location")
        } else {
            android.widget.Toast.makeText(context, "No saved booking address found for this provider.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var showCancelDialog by remember { mutableStateOf(false) }

    fun cancelService() {
        firestore.collection("chats").document(chatId)
            .update("status", "completed")
            .addOnSuccessListener {
                sendChatMessage(
                    firestore, userId, currentUserName, rId, actualReceiverName,
                    "❌ This service has been cancelled by the user.",
                    type = "system"
                )
                android.widget.Toast.makeText(context, "Service cancelled.", android.widget.Toast.LENGTH_SHORT).show()
                showCancelDialog = false
            }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Service?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel this service? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { cancelService() }) {
                    Text("Yes, Cancel", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No, Keep it")
                }
            },
            containerColor = surface,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(0.7f)
        )
    }

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
                        snapshot.getString("name_$rId")?.let { actualReceiverName = it }
                    }
                }
        }
    }

    // ─── Fetch Names and Mark as Read ────────────────────────────────────────
    LaunchedEffect(userId, rId) {
        if (userId.isNotEmpty() && rId.isNotEmpty()) {
            // Fallback: Fetch receiver name from users OR providers
            firestore.collection("users").document(rId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    actualReceiverName = name
                    // Sync back to chat metadata for Inbox display
                    firestore.collection("chats").document(chatId).update("name_$rId", name)
                } else {
                    // Try providers collection
                    firestore.collection("providers").document(rId).get().addOnSuccessListener { pDoc ->
                        val name = pDoc.getString("name") ?: ""
                        actualReceiverName = name
                        firestore.collection("chats").document(chatId).update("name_$rId", name)
                    }
                }
            }
            
            // Sync current user name and role to metadata
            firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val role = doc.getString("role") ?: "user"
                    currentUserName = name
                    userRole = role
                    firestore.collection("chats").document(chatId).update("name_$userId", name)
                }
            }

            // Mark as read ONLY if we are the recipient of the last message
            firestore.collection("chats").document(chatId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val lastSenderId = doc.getString("lastSenderId")
                    if (lastSenderId != null && lastSenderId != userId) {
                        firestore.collection("chats").document(chatId).update("isRead", true)
                    }
                }
            }
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

    var isChatReadByOther by remember { mutableStateOf(false) }
    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            firestore.collection("chats").document(chatId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val lastSenderId = snapshot.getString("lastSenderId")
                        val isRead = snapshot.getBoolean("isRead") ?: false
                        // If I am the last sender and isRead is true, the other person read it
                        if (lastSenderId == userId && isRead) {
                            isChatReadByOther = true
                        } else if (lastSenderId != userId) {
                            // If I am the receiver, the other person's messages are effectively "read" by me
                            isChatReadByOther = false 
                        } else {
                            isChatReadByOther = false
                        }
                    }
                }
        }
    }

    Scaffold(
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
                                Text(actualReceiverName, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✔ Verified", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = primary)
                            }
                            if (isOtherTyping) {
                                Text("✍ Typing...", color = primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Online \u2022 Responds in 5 min", color = Color.White.copy(0.6f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Chat", color = Color.White) },
                            onClick = {
                                // Delete all messages in the sub-collection
                                firestore.collection("chats").document(chatId).collection("messages")
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val batch = firestore.batch()
                                        snapshot.documents.forEach { batch.delete(it.reference) }
                                        batch.commit().addOnSuccessListener {
                                            messages = emptyList()
                                            android.widget.Toast.makeText(context, "Chat cleared.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                        DropdownMenuItem(
                            text = { Text("Contact Support", color = Color.White) },
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@workly.com"))
                                context.startActivity(intent)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = primary) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(PremiumBlack).navigationBarsPadding().imePadding()) {
                if (userRole.lowercase() == "user") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val actions = listOf("📍 Share Location", "❌ Cancel Service", "📞 Call Info", "❌ Issue Report")
                        items(actions) { action ->
                            Surface(
                                modifier = Modifier.clickable { 
                                    if (action.contains("Location")) {
                                        shareSavedLocation()
                                    } else if (action.contains("Cancel")) {
                                        showCancelDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(1.dp, outline.copy(alpha = 0.1f)),
                                color = surface
                            ) {
                                Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = primary)
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PremiumBlack,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            placeholder = { Text("Type a message...", color = Color.White.copy(0.3f), fontSize = 15.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedBorderColor = Color.White.copy(alpha = 0.1f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                cursorColor = primary
                            ),
                            maxLines = 4
                        )
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            onClick = {
                                val text = messageText.trim()
                                if (text.isNotEmpty() && userId.isNotEmpty()) {
                                    sendChatMessage(firestore, userId, currentUserName, rId, actualReceiverName, text) { success ->
                                        if (!success) {
                                            android.widget.Toast.makeText(context, "Cloud sync failed. Check connectivity.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    messageText = ""
                                }
                            },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = primary,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = PremiumBlack
    ) { innerPadding ->
        if (messages.isEmpty()) {
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
                    Text("Secure Chat Environment", fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ask about timing, special requirements, or preparation details.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    WhatsAppChatBubble(message, isMe = message.senderId == userId, isChatReadByOther = isChatReadByOther)
                }
            }
        }
    }
}

@Composable
fun WhatsAppChatBubble(message: Message, isMe: Boolean, isChatReadByOther: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val bubbleColor = if (isMe) Color(0xFF1E2A78) else Color(0xFF262626)
    val contentColor = Color.White
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp?.toDate() ?: Date())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (isMe) 20.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 20.dp
            ),
            border = if (!isMe) BorderStroke(1.dp, Color.White.copy(0.05f)) else null,
            shadowElevation = 2.dp
        ) {
            SelectionContainer {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    if (message.type == "system") {
                        Text(
                            text = message.content,
                            color = Color.White.copy(0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    } else if (message.type == "location") {
                        val context = LocalContext.current
                        Column(modifier = Modifier.widthIn(max = 260.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = if (isMe) Color.White else primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Shared Location",
                                    color = contentColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(message.content))
                                    context.startActivity(mapIntent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMe) Color.White.copy(0.2f) else primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View on Map", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = message.content,
                            color = contentColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.widthIn(max = 260.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = Color.White.copy(0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.DoneAll,
                                null,
                                tint = if (isChatReadByOther) Color(0xFF34B7F1) else Color.White.copy(0.3f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
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
    type: String = "text",
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
        type = type,
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
