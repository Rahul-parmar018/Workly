package com.example.workly.provider

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AddServiceRepository(private val context: Context) {

    private val TAG = "AddServiceRepo"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun compressImage(uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                Log.e(TAG, "Cannot open InputStream for: $uri")
                return null
            }
            val bitmap = inputStream.use { BitmapFactory.decodeStream(it) } ?: run {
                Log.e(TAG, "BitmapFactory returned null for: $uri")
                return null
            }
            val file = File(context.cacheDir, "svc_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }
            bitmap.recycle()
            Log.d(TAG, "Compressed OK -> ${file.length()} bytes, path: ${file.absolutePath}")
            if (!file.exists() || file.length() == 0L) {
                Log.e(TAG, "Compressed file empty!")
                return null
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Compress FAIL: ${e.message}", e)
            null
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔥 THE FIX: Nested callbacks — NO race condition
    // putFile -> onSuccess -> downloadUrl -> onSuccess -> return
    // ═══════════════════════════════════════════════════════════════
    suspend fun uploadImage(compressedUri: Uri): Pair<String, StorageReference> {
        val user = auth.currentUser ?: throw Exception("Not logged in")
        val fileRef = storage.reference.child("services/${user.uid}/${System.currentTimeMillis()}.jpg")

        Log.d(TAG, "Starting upload: $compressedUri -> ${fileRef.path}")

        return suspendCancellableCoroutine { continuation ->
            fileRef.putFile(compressedUri)
                .addOnSuccessListener { _ ->
                    Log.d(TAG, "putFile SUCCESS — now getting downloadUrl")

                    // ONLY after upload SUCCESS → get URL
                    fileRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            Log.d(TAG, "downloadUrl SUCCESS: $downloadUri")
                            continuation.resume(Pair(downloadUri.toString(), fileRef))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "downloadUrl FAIL: ${e.message}", e)
                            continuation.resumeWithException(Exception("Failed to get URL: ${e.message}"))
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "putFile FAIL: ${e.message}", e)
                    continuation.resumeWithException(Exception("Upload failed: ${e.message}"))
                }
        }
    }

    suspend fun saveService(
        title: String, category: String, location: String,
        duration: String, price: Int, imageUrl: String,
        fileRef: StorageReference, providerName: String
    ): Boolean {
        val user = auth.currentUser ?: return false
        val docRef = db.collection("services").document()
        val data = hashMapOf(
            "id" to docRef.id,
            "title" to title,
            "category" to category,
            "location" to location,
            "duration" to duration,
            "basePrice" to price.toDouble(),
            "imageUrl" to imageUrl,
            "providerId" to user.uid,
            "providerName" to providerName,
            "isApproved" to false,
            "status" to "pending",
            "createdAt" to System.currentTimeMillis(),
            "keywords" to (title + " " + category).lowercase().split(" ")
                .map { it.trim() }.filter { it.length > 2 }.distinct()
        )
        return try {
            docRef.set(data).await()
            Log.d(TAG, "Firestore save OK: ${docRef.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore FAIL: ${e.message}", e)
            try { fileRef.delete().await() } catch (_: Exception) {}
            false
        }
    }

    fun getProviderName(): String {
        val prefs = context.getSharedPreferences("WorklyPrefs", Context.MODE_PRIVATE)
        return prefs.getString("name", null) ?: "Provider"
    }

    suspend fun getProviderNameRemote(): String {
        val user = auth.currentUser ?: return "Provider"
        return try {
            db.collection("users").document(user.uid).get().await().getString("name") ?: "Provider"
        } catch (_: Exception) { "Provider" }
    }
}
