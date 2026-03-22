package com.example.workly.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Message
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvChatTitle: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var layoutEmptyChat: LinearLayout
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var etMessage: EditText
    private lateinit var fabSend: FloatingActionButton
    
    private lateinit var chatAdapter: ChatAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId = ""
    private var proId = ""
    private var proName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userId = auth.currentUser?.uid ?: ""
        proName = intent.getStringExtra("PRO_NAME") ?: "Professional"
        proId = intent.getStringExtra("PRO_ID") ?: "pro_${proName.replace(" ", "_")}"

        initViews()
        setupChatListener()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tvChatTitle = findViewById(R.id.tvChatTitle)
        rvMessages = findViewById(R.id.rvMessages)
        layoutEmptyChat = findViewById(R.id.layoutEmptyChat)
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle)
        etMessage = findViewById(R.id.etMessage)
        fabSend = findViewById(R.id.fabSend)

        tvChatTitle.text = proName
        tvEmptySubtitle.text = "with $proName"

        chatAdapter = ChatAdapter()
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = chatAdapter

        fabSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isNotEmpty() && userId.isNotEmpty()) {
                sendMessage(content)
                etMessage.setText("")
            }
        }
    }

    private fun setupChatListener() {
        if (userId.isEmpty() || proId.isEmpty()) return
        
        val chatId = if (userId < proId) "${userId}_$proId" else "${proId}_$userId"
        firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    chatAdapter.submitList(messages) {
                        if (messages.isNotEmpty()) {
                            rvMessages.scrollToPosition(messages.size - 1)
                            layoutEmptyChat.visibility = View.GONE
                        } else {
                            layoutEmptyChat.visibility = View.VISIBLE
                        }
                    }
                }
            }
    }

    private fun sendMessage(content: String) {
        val chatId = if (userId < proId) "${userId}_$proId" else "${proId}_$userId"
        val colRef = firestore.collection("chats").document(chatId).collection("messages")
        val docRef = colRef.document()
        
        val message = Message(
            id = docRef.id,
            senderId = userId,
            receiverId = proId,
            content = content,
            timestamp = Timestamp.now()
        )
        docRef.set(message)
    }
}
