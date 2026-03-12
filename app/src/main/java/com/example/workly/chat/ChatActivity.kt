package com.example.workly.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.data.Message
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val proName = intent.getStringExtra("PRO_NAME") ?: "Professional"
        val proId = intent.getStringExtra("PRO_ID") ?: "pro_${proName.replace(" ", "_")}"
        setContent {
            WorklyTheme {
                ChatScreen(proName = proName, proId = proId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(proName: String, proId: String, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Real-time message listener
    LaunchedEffect(proId) {
        if (userId.isNotEmpty() && proId.isNotEmpty()) {
            val chatId = if (userId < proId) "${userId}_$proId" else "${proId}_$userId"
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
        containerColor = BackgroundGray, // ← THIS FIXES THE BLACK SCREEN
        topBar = {
            Surface(shadowElevation = 4.dp, color = Color.White) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = ProfessionalBlue.copy(0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = ProfessionalBlue)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(proName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Online", fontSize = 12.sp, color = ElectricTeal)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 12.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = ProfessionalBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    FloatingActionButton(
                        onClick = {
                            val text = messageText.trim()
                            if (text.isNotEmpty() && userId.isNotEmpty()) {
                                sendChatMessage(firestore, userId, proId, text)
                                messageText = ""
                            }
                        },
                        containerColor = ProfessionalBlue,
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = ProfessionalBlue.copy(0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = ProfessionalBlue, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Start your conversation", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("with $proName", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message, isMe = message.senderId == userId)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ProfessionalBlue.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = ProfessionalBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            Surface(
                color = if (isMe) ProfessionalBlue else Color.White,
                shape = RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (isMe) Color.White else TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
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
    docRef.set(message)
}
