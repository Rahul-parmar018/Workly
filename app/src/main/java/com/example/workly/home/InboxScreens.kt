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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.workly.chat.ChatActivity
import com.example.workly.R
import com.example.workly.theme.*

data class ChatPreview(
    val id: String,
    val proName: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val proIcon: Int
)

@Composable
fun InboxScreen() {
    val chats = remember {
        listOf(
            ChatPreview("1", "John Plumber", "I'll be there by 2 PM.", "10:30 AM", 2, R.drawable.workly_logo),
            ChatPreview("2", "Alice Cleaner", "The job is complete!", "Yesterday", 0, R.drawable.workly_logo),
            ChatPreview("3", "Mike Electric", "Is the main switch off?", "Monday", 0, R.drawable.workly_logo)
        )
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
            text = "Chat with your service providers",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No messages yet", color = Color.Gray)
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
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("PRO_NAME", chat.proName)
                    putExtra("PRO_ID", chat.id)
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
            // Pro Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = BackgroundGray
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ProfessionalBlue)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = chat.proName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = if (chat.unreadCount > 0) TextPrimary else TextSecondary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                    if (chat.unreadCount > 0) {
                        Surface(
                            color = EnergyOrange,
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
