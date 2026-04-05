package com.example.workly.data

import com.google.firebase.Timestamp

/**
 * Message schema for Workly chat.
 * Firestore path: /chats/{chatId}/messages/{messageId}
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: String = "text", // text | image | booking_card
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)
