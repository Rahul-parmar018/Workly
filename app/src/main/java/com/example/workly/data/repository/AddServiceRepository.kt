package com.example.workly.data.repository

import android.net.Uri
import com.example.workly.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddServiceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun publishService(
        title: String,
        description: String,
        price: Double,
        category: String,
        duration: String,
        location: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val imageUrl = if (imageUri != null) {
                uploadImage(imageUri)
            } else null

            val service = hashMapOf(
                "name" to title,
                "description" to description,
                "basePrice" to price,
                "category" to category,
                "duration" to duration,
                "location" to location,
                "imageUrl" to imageUrl,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("services").add(service).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("services/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
