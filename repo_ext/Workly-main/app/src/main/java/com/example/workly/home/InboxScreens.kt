package com.example.workly.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.workly.chat.ChatActivity
import com.example.workly.theme.*
import com.example.workly.data.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class ChatPreview(
    val id: String,
    val receiverName: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

@Composable
fun InboxScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: ""
    
    var chats by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            db.collection("chats")
                .whereArrayContains("members", uid)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        android.util.Log.e("InboxScreen", "Listen failed.", e)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val previews = mutableListOf<ChatPreview>()
                        val docs = snapshot.documents
                        if (docs.isEmpty()) {
                            isLoading = false
                            chats = emptyList()
                            return@addSnapshotListener
                        }
                        
                        docs.forEach { doc ->
                            val members = doc.get("members") as? List<String> ?: emptyList()
                            val otherId = members.find { it != uid } ?: ""
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val ts = doc.getTimestamp("lastTimestamp")?.toDate() ?: Date()
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(ts)
                            
                            if (otherId.isEmpty()) {
                                previews.add(ChatPreview("", "Unknown", lastMsg, timeStr, 0))
                                if (previews.size == docs.size) {
                                    chats = previews.sortedByDescending { it.time }
                                    isLoading = false
                                }
                                return@forEach
                            }

                            // 🚀 SECONDARY: Fetch OTHER member's name from users OR providers
                            db.collection("users").document(otherId).get().addOnCompleteListener { task ->
                                val userDoc = if (task.isSuccessful) task.result else null
                                val userName = userDoc?.getString("name")
                                
                                if (userName.isNullOrEmpty()) {
                                    db.collection("providers").document(otherId).get().addOnCompleteListener { pTask ->
                                        val pDoc = if (pTask.isSuccessful) pTask.result else null
                                        val pName = pDoc?.getString("name") ?: "Pro"
                                        
                                        previews.add(ChatPreview(otherId, pName, lastMsg, timeStr, 0))
                                        if (previews.size == docs.size) {
                                            chats = previews.sortedByDescending { it.time }
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    previews.add(ChatPreview(otherId, userName, lastMsg, timeStr, 0))
                                    if (previews.size == docs.size) {
                                        chats = previews.sortedByDescending { it.time }
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = ProfessionalBlue
        )
        Text(
            text = "Connect with your service team",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfessionalBlue)
            }
        } else if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Assignment, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No messages yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(chats) { chat ->
                    ChatListItem(chat)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatPreview) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("RECEIVER_NAME", chat.receiverName)
                    putExtra("RECEIVER_ID", chat.id)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = BackgroundGray
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = ProfessionalBlue)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = chat.receiverName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = chat.time, fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
