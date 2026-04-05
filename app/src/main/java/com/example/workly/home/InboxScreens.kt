package com.example.workly.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val unreadCount: Int = 0,
    val onlineStatus: String = "Offline",
    val profileUrl: String = "",
    val isRead: Boolean = true,
    val status: String = "active" // active | completed
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: ""
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }
    var chats by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // REAL-TIME LISTENER
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            isLoading = true
            db.collection("chats")
                .whereArrayContains("members", uid)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val previews = snapshot.documents.mapNotNull { doc ->
                            val members = doc.get("members") as? List<String> ?: emptyList()
                            val otherId = members.find { it != uid } ?: "unknown"
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val ts = doc.getTimestamp("lastTimestamp")?.toDate() ?: Date()
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(ts)
                            
                            ChatPreview(
                                id = otherId,
                                receiverName = "User $otherId", // Fallback
                                lastMessage = lastMsg,
                                time = timeStr,
                                unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt(),
                                isRead = doc.getBoolean("isRead") ?: true,
                                onlineStatus = "Online",
                                status = doc.getString("status") ?: "active"
                            )
                        }
                        chats = previews
                        isLoading = false
                    } else {
                        isLoading = false
                    }
                }
        }
    }

    // ── Theme-reactive aliases ───────────────────────────────────────────────
    val bg        = MaterialTheme.colorScheme.background
    val onBg      = MaterialTheme.colorScheme.onBackground
    val primary   = MaterialTheme.colorScheme.primary
    val surface   = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfVar   = MaterialTheme.colorScheme.surfaceVariant

    // DUMMY DATA
    val dummyChats = listOf(
        ChatPreview("1", "Rahul Parmar", "I will arrive in 10 mins", "06:01 PM", 2, "Online", "https://i.pravatar.cc/150?u=rahul", false),
        ChatPreview("2", "Plumber Raj", "Found the leak, fixing now.", "04:20 PM", 0, "Last seen 2 min ago", "https://i.pravatar.cc/150?u=raj", true),
        ChatPreview("3", "Cleaning Team", "We are typing...", "10:30 AM", 0, "Typing...", "https://i.pravatar.cc/150?u=clean", true),
        ChatPreview("4", "Electrician Amit", "Is the wiring issue fixed?", "Yesterday", 1, "Offline", "https://i.pravatar.cc/150?u=amit", false)
    )

    val displayedChats = if (chats.isEmpty()) dummyChats else chats
    val filteredChats = displayedChats.filter { chat ->
        val matchesTab = selectedTab == "All" || chat.status.equals(selectedTab, true)
        val matchesSearch = searchQuery.isEmpty() || chat.receiverName.contains(searchQuery, true)
        matchesTab && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize().background(bg)) {
        // 🔥 HEADER
        Column(modifier = Modifier.padding(24.dp).statusBarsPadding()) {
            Text("Messages", fontSize = 28.sp, fontWeight = FontWeight.Black, color = onBg)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = primary, modifier = Modifier.size(14.dp))
                Text(" Ahmedabad \u2022 ${displayedChats.size} active chats", fontSize = 13.sp, color = onBg.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            }
        }

        // 🔥 SEARCH
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = onSurface.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = onSurface, fontSize = 15.sp),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) Text("Search conversations...", color = onSurface.copy(alpha = 0.4f), fontSize = 15.sp)
                        inner()
                    }
                )
            }
        }

        // 🔥 QUICK FILTER (TABS)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("All", "Active", "Completed").forEach { tab ->
                val isSel = selectedTab == tab
                Surface(
                    onClick = { selectedTab = tab },
                    shape = RoundedCornerShape(50.dp),
                    color = if (isSel) primary else surface,
                    shadowElevation = if (isSel) 4.dp else 1.dp
                ) {
                    Text(
                        tab,
                        color = if (isSel) Color.White else onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // 🔥 CHAT LIST
        if (isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredChats) { chat ->
                    ChatListItem(chat)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = onBg.copy(alpha = 0.05f))
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatPreview) {
    val context = LocalContext.current
    val isUnread = chat.unreadCount > 0
    
    val onCard   = MaterialTheme.colorScheme.onSurface
    val surfVar  = MaterialTheme.colorScheme.surfaceVariant
    val primary  = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("RECEIVER_NAME", chat.receiverName)
                    putExtra("RECEIVER_ID", chat.id)
                }
                context.startActivity(intent)
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = surfVar
            ) {
                AsyncImage(
                    model = chat.profileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${chat.receiverName}&background=1E2A78&color=fff" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (chat.onlineStatus.contains("Online") || chat.onlineStatus.contains("Typing")) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).size(14.dp).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    shape = CircleShape,
                    color = Color(0xFF10B981) // Online Green
                ) {}
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.receiverName,
                    fontWeight = if (isUnread) FontWeight.Black else FontWeight.Bold,
                    fontSize = 16.sp,
                    color = onCard
                )
                Text(
                    text = chat.time, 
                    fontSize = 11.sp, 
                    color = if (isUnread) primary else onCard.copy(alpha = 0.5f), 
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.onlineStatus.contains("Typing")) {
                    Text("\u270d Typing...", color = primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = if (isUnread) onCard else onCard.copy(alpha = 0.6f),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!isUnread) {
                        Icon(
                            Icons.Default.DoneAll, 
                            null, 
                            tint = if (chat.isRead) Color(0xFF34B7F1) else onCard.copy(alpha = 0.4f), 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (isUnread) {
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = primary,
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(chat.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
