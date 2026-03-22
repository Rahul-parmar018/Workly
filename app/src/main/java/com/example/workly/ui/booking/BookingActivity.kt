package com.example.workly.ui.booking

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.workly.R
import com.example.workly.data.model.Booking
import com.example.workly.ui.payment.PaymentActivity
import com.google.android.gms.location.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class BookingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etAddress: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var rvTimeSlots: RecyclerView
    private lateinit var etInstructions: EditText
    private lateinit var btnFindPros: MaterialButton
    private lateinit var loadingOverlay: View
    private lateinit var ivServiceSummary: ImageView
    private lateinit var tvServiceNameSummary: TextView
    private lateinit var tvServiceCategorySummary: TextView
    private lateinit var tvServicePriceSummary: TextView

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedDate = ""
    private var selectedTime: String? = null
    private var serviceName = ""
    private var serviceCategory = ""
    private var basePrice = 0.0
    private var userLat = 0.0
    private var userLon = 0.0

    private val providerPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val prosName = result.data?.getStringExtra("SELECTED_PROVIDER_NAME")
            val prosId = result.data?.getStringExtra("SELECTED_PROVIDER_ID")
            val prosRate = result.data?.getDoubleExtra("SELECTED_PROVIDER_RATE", basePrice) ?: basePrice
            
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("SERVICE_NAME", serviceName)
                putExtra("SERVICE_PRICE", prosRate)
                putExtra("PROVIDER_NAME", prosName)
                putExtra("BASE_PRICE", basePrice)
            }
            paymentLauncher.launch(intent)
        }
    }

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val method = result.data?.getStringExtra("PAYMENT_METHOD") ?: "Cash"
            saveBookingToFirestore(method)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fetchLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""
        basePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupListeners()
        updateProceedButton()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        etAddress = findViewById(R.id.etAddress)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        rvTimeSlots = findViewById(R.id.rvTimeSlots)
        etInstructions = findViewById(R.id.etInstructions)
        btnFindPros = findViewById(R.id.btnFindPros)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        ivServiceSummary = findViewById(R.id.ivServiceSummary)
        tvServiceNameSummary = findViewById(R.id.tvServiceNameSummary)
        tvServiceCategorySummary = findViewById(R.id.tvServiceCategorySummary)
        tvServicePriceSummary = findViewById(R.id.tvServicePriceSummary)

        tvServiceNameSummary.text = serviceName
        tvServiceCategorySummary.text = serviceCategory
        tvServicePriceSummary.text = "From ₹${basePrice.toInt()}+"

        Glide.with(this)
            .load(getServiceImageUrl(serviceCategory))
            .into(ivServiceSummary)

        val slots = listOf("9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM")
        timeSlotAdapter = TimeSlotAdapter(slots, selectedTime) { slot ->
            selectedTime = slot
            timeSlotAdapter.updateSelected(slot)
            updateProceedButton()
        }
        rvTimeSlots.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTimeSlots.adapter = timeSlotAdapter
    }

    private fun setupListeners() {
        findViewById<View>(R.id.cardDatePicker).setOnClickListener {
            showDatePicker()
        }

        findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutAddress).setEndIconOnClickListener {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        btnFindPros.setOnClickListener {
            val intent = Intent(this, ProviderListActivity::class.java).apply {
                putExtra("SERVICE_NAME", serviceName)
                putExtra("SERVICE_CATEGORY", serviceCategory)
                putExtra("USER_LAT", userLat)
                putExtra("USER_LON", userLon)
            }
            providerPickerLauncher.launch(intent)
        }
        
        etAddress.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateProceedButton() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            selectedDate = "$day ${months[month]} $year"
            tvSelectedDate.text = selectedDate
            tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.black))
            updateProceedButton()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        loadingOverlay.visibility = View.VISIBLE
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                userLat = loc.latitude
                userLon = loc.longitude
                updateAddressFromLatLng(loc.latitude, loc.longitude)
            } else {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).setMaxUpdates(1).build()
                fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val fresh = result.lastLocation ?: return
                        userLat = fresh.latitude
                        userLon = fresh.longitude
                        updateAddressFromLatLng(fresh.latitude, fresh.longitude)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }, Looper.getMainLooper())
            }
        }.addOnFailureListener {
            loadingOverlay.visibility = View.GONE
        }
    }

    private fun updateAddressFromLatLng(lat: Double, lon: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull()?.getAddressLine(0) ?: "$lat, $lon"
            etAddress.setText(addr)
        } catch (e: Exception) {
            etAddress.setText("$lat, $lon")
        }
        loadingOverlay.visibility = View.GONE
        updateProceedButton()
    }

    private fun updateProceedButton() {
        val canProceed = etAddress.text.isNotBlank() && selectedDate.isNotBlank() && selectedTime != null
        btnFindPros.isEnabled = canProceed
        btnFindPros.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (canProceed) ContextCompat.getColor(this, R.color.nav_item_color)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )
    }

    private fun saveBookingToFirestore(paymentMethod: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("bookings").document()
        
        val booking = hashMapOf(
            "id" to docRef.id,
            "userId" to userId,
            "serviceName" to serviceName,
            "serviceCategory" to serviceCategory,
            "date" to selectedDate,
            "time" to selectedTime,
            "address" to etAddress.text.toString(),
            "finalPrice" to basePrice,
            "status" to "Pending",
            "paymentMethod" to paymentMethod,
            "timestamp" to System.currentTimeMillis()
        )

        loadingOverlay.visibility = View.VISIBLE
        docRef.set(booking).addOnSuccessListener {
            val intent = Intent(this, BookingSuccessActivity::class.java).apply {
                putExtra("BOOKING_ID", docRef.id)
            }
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            loadingOverlay.visibility = View.GONE
            Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getServiceImageUrl(category: String): String {
        return when (category.lowercase()) {
            "cleaning" -> "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&fit=crop"
            "repair" -> "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=400&fit=crop"
            else -> "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&fit=crop"
        }
    }
}
