package com.example.workly.chat

/**
 * Global object to track the currently active chat session.
 * Used to suppress system notifications if the user is already viewing the conversation.
 */
object ChatSessionManager {
    var activeChatId: String? = null
}
