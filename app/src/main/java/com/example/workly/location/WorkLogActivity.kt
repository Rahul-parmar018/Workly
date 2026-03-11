package com.example.workly.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.workly.data.AppDatabase
import com.example.workly.data.WorkLog
import com.example.workly.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkLogActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvLocation: TextView
    private lateinit var ivPreview: ImageView
    private lateinit var btnLocation: Button
    private lateinit var btnCamera: Button
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentImagePath: String? = null

    // For simplicity, we use the TakePicturePreview contract which returns a bitmap
    // In a production app, you might want to use TakePicture with a File URI for high res
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            ivPreview.setImageBitmap(bitmap)
            // Save bitmap to file
            currentImagePath = saveBitmapToFile(bitmap)
        } else {
            Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_log)

        etTitle = findViewById(R.id.etTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvLocation = findViewById(R.id.tvLocation)
        ivPreview = findViewById(R.id.ivPreview)
        btnLocation = findViewById(R.id.btnLocation)
        btnCamera = findViewById(R.id.btnCamera)
        btnSave = findViewById(R.id.btnSave)
        btnShare = findViewById(R.id.btnShare)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupSpinner()

        btnLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        btnCamera.setOnClickListener {
            takePictureLauncher.launch(null)
        }

        btnSave.setOnClickListener {
            saveWorkLog()
        }

        btnShare.setOnClickListener {
            shareWorkLog()
        }
    }

    private fun setupSpinner() {
        val categories = listOf("Field Work", "Client Meeting", "Office Work", "Remote", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return
        }
        fetchLocation()
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                tvLocation.text = "Lat: ${location.latitude}, Lon: ${location.longitude}"
            } else {
                Toast.makeText(this, "Unable to get location. Try enabling GPS.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveWorkLog() {
        val title = etTitle.text.toString()
        val category = spinnerCategory.selectedItem.toString()

        if (title.isBlank()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        val workLog = WorkLog(
            title = title,
            category = category,
            latitude = currentLatitude,
            longitude = currentLongitude,
            imagePath = currentImagePath
        )

        lifecycleScope.launch {
            // Room Insert
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@WorkLogActivity).workDao().insert(workLog)
            }
            
            // Firebase Insert
            saveToFirebase(workLog)

            Toast.makeText(this@WorkLogActivity, "Work Log Saved Locally & Syncing...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveToFirebase(workLog: WorkLog) {
        val db = FirebaseFirestore.getInstance()
        val logMap = hashMapOf(
            "title" to workLog.title,
            "category" to workLog.category,
            "latitude" to workLog.latitude,
            "longitude" to workLog.longitude,
            "imagePath" to workLog.imagePath, // Local path, for demo
            "timestamp" to workLog.timestamp,
            "date" to workLog.getFormattedDate()
        )

        db.collection("work_logs")
            .add(logMap)
            .addOnSuccessListener { documentReference ->
               // Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
               // Log.w(TAG, "Error adding document", e)
            }
    }

    private fun shareWorkLog() {
        val title = etTitle.text.toString()
        val category = spinnerCategory.selectedItem.toString()
        val locText = if (currentLatitude != null) "Lat: $currentLatitude, Lon: $currentLongitude" else "No Location"
        
        val shareText = """
            Work Log Entry:
            Title: $title
            Category: $category
            Location: $locText
        """.trimIndent()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Work Log via")
        startActivity(shareIntent)
    }
}
