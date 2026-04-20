package com.example.workly.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.workly.chat.ChatSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * A Foreground Service that keeps a real-time listener alive even when the app is closed.
 * This ensures notifications are delivered instantly without needing a backend server.
 */
class WorklyBackgroundService : Service() {

    private var listenerRegistration: ListenerRegistration? = null
    private val CHANNEL_ID = "workly_background_service"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(999, createForegroundNotification())
        startNotificationListener()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workly Sync Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps your messages synced in real-time"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workly Elite")
            .setContentText("Messaging sync is active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun startNotificationListener() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        listenerRegistration = db.collection("notifications")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val chatId = doc.getString("chatId")
                        
                        // Only show if not currently in this chat
                        if (chatId != ChatSessionManager.activeChatId) {
                            val senderName = doc.getString("senderName") ?: "New Message"
                            val message = doc.getString("message") ?: ""
                            val senderId = doc.getString("senderId") ?: ""
                            
                            WorklyNotificationManager.showChatNotification(
                                this, senderName, message, chatId ?: "", senderId
                            )
                            
                            // Mark as read in notifications collection so we don't show it again
                            doc.reference.update("isRead", true)
                        }
                    }
                }
            }
    }

    override fun onDestroy() {
        listenerRegistration?.remove()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
