package com.example.workly.provider

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.workly.data.Service
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class AddServiceRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun saveService(
        title: String,
        category: String,
        location: String,
        duration: String,
        price: Int,
        providerName: String,
        localImagePath: String,
        onComplete: (Boolean) -> Unit
    ) {
        val user = auth.currentUser ?: return
        val docRef = db.collection("services").document()
        
        val serviceData = hashMapOf(
            "id" to docRef.id,
            "title" to title,
            "category" to category,
            "description" to "",
            "location" to location,
            "duration" to duration,
            "price" to price.toDouble(),
            "imageUrl" to "", // empty for now (college project simplification)
            "imagePath" to localImagePath,
            "syncStatus" to "synced", // Only local, so "synced" status is fine for UI
            "providerId" to user.uid,
            "providerName" to providerName,
            "isActive" to true,
            "createdAt" to Timestamp.now()
        )

        docRef.set(serviceData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val fileName = "service_${java.util.UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
