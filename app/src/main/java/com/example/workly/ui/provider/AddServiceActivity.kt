package com.example.workly.ui.provider

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.workly.R
import com.example.workly.data.repository.AddServiceRepository
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class AddServiceActivity : AppCompatActivity() {

    private lateinit var repo: AddServiceRepository
    private var selectedImageUri: Uri? = null

    // UI Elements
    private lateinit var ivPreview: ImageView
    private lateinit var etTitle: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etLocation: AutoCompleteTextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerDuration: Spinner
    private lateinit var btnPublish: Button
    private lateinit var loadingOverlay: View

    private val categories = listOf("Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events")
    private val durations = listOf("1 hr", "2 hr", "3 hr", "4 hr", "5 hr")
    private val cities = listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Mumbai", "Delhi", "Bangalore", "Pune")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        repo = AddServiceRepository(this)
        
        initViews()
        setupAdapters()
        setupListeners()
    }

    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ivPreview = findViewById(R.id.ivPreview)
        etTitle = findViewById(R.id.etTitle)
        etPrice = findViewById(R.id.etPrice)
        etLocation = findViewById(R.id.etLocation)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerDuration = findViewById(R.id.spinnerDuration)
        btnPublish = findViewById(R.id.btnPublish)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        
        findViewById<TextInputLayout>(R.id.layoutLocation)
            .setEndIconOnClickListener { fetchCurrentCity() }
    }

    private fun setupAdapters() {
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = catAdapter

        val durAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        durAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDuration.adapter = durAdapter

        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        etLocation.setAdapter(cityAdapter)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.imageCard).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnPublish.setOnClickListener {
            validateAndPublish()
        }
    }

    private fun fetchCurrentCity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
                        etLocation.setText(city)
                    }
                } catch (e: Exception) {
                    showError("Failed to get city: ${e.message}")
                }
            } else {
                showError("Location not found. Enable GPS.")
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { granted -> granted }) {
            fetchCurrentCity()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            ivPreview.setImageURI(uri)
            ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            findViewById<View>(R.id.tvAddPhoto).visibility = View.GONE
        }
    }

    private fun validateAndPublish() {
        val title = etTitle.text.toString().trim()
        val priceText = etPrice.text.toString().trim()
        val price = priceText.toDoubleOrNull()
        val location = etLocation.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val duration = spinnerDuration.selectedItem.toString()

        if (selectedImageUri == null) {
            showError("Please select a service photo")
            return
        }
        if (title.isBlank() || priceText.isBlank() || location.isBlank()) {
            showError("Please fill all required fields")
            return
        }
        if (price == null || price <= 0) {
            showError("Please enter a valid price")
            return
        }

        publishService(title, category, location, duration, price)
    }

    private fun publishService(title: String, category: String, location: String, duration: String, price: Double) {
        showLoading(true)

        lifecycleScope.launch {
            var tempFile: File? = null
            try {
                tempFile = repo.compressImage(selectedImageUri!!)
                if (tempFile == null) throw Exception("Failed to compress image")

                val uploadResult = repo.uploadImageWithRetry(tempFile)
                if (uploadResult == null) throw Exception("Image upload failed")

                val providerName = repo.getProviderName()
                val success = repo.saveServiceToFirestore(
                    title, category, location, duration, price,
                    uploadResult.first, uploadResult.second, providerName
                )

                if (success) {
                    showSuccessDialog()
                } else {
                    throw Exception("Failed to save service details")
                }

            } catch (e: Exception) {
                showErrorDialog(e.message ?: "Unknown error occurred")
            } finally {
                showLoading(false)
                tempFile?.delete()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnPublish.isEnabled = !show
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Failed to Publish")
            .setMessage(msg)
            .setPositiveButton("Retry") { _, _ -> validateAndPublish() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Service Published!")
            .setMessage("Your service is now live.")
            .setPositiveButton("Done") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}
