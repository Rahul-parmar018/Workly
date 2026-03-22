package com.example.workly.ui.services

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workly.R
import com.example.workly.data.model.Service
import com.example.workly.ui.booking.BookingActivity
import com.google.android.material.appbar.MaterialToolbar

class ServicesActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etSearch: EditText
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvServices: RecyclerView
    private lateinit var tvServiceCount: TextView

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var servicesAdapter: ServicesAdapter

    private var allServices = listOf<Service>()
    private var selectedCategory = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        initViews()
        setupData()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        etSearch = findViewById(R.id.etSearch)
        rvCategories = findViewById(R.id.rvCategories)
        rvServices = findViewById(R.id.rvServices)
        tvServiceCount = findViewById(R.id.tvServiceCount)

        // Setup Category RV
        categoryAdapter = CategoryAdapter(
            listOf("All", "Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events"),
            selectedCategory
        ) { category ->
            selectedCategory = category
            categoryAdapter.updateSelected(category)
            updateTitle()
            filterServices()
        }
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        // Setup Services RV
        servicesAdapter = ServicesAdapter { service ->
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("SERVICE_NAME", service.name)
                putExtra("SERVICE_PRICE", service.basePrice)
                putExtra("SERVICE_CATEGORY", service.category)
                putExtra("SERVICE_ID", service.id)
            }
            startActivity(intent)
        }
        rvServices.layoutManager = GridLayoutManager(this, 2)
        rvServices.adapter = servicesAdapter
    }

    private fun setupData() {
        allServices = getAllServices()
        val initialCategory = intent.getStringExtra("CATEGORY") ?: "All"
        selectedCategory = initialCategory
        categoryAdapter.updateSelected(selectedCategory)
        updateTitle()
        filterServices()
    }

    private fun setupListeners() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                filterServices()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateTitle() {
        supportActionBar?.title = if (selectedCategory == "All") "All Services" else selectedCategory
    }

    private fun filterServices() {
        val filtered = allServices.filter { service ->
            val matchesCategory = selectedCategory == "All" || service.category == selectedCategory
            val matchesSearch = searchQuery.isEmpty() ||
                    service.name.contains(searchQuery, ignoreCase = true) ||
                    service.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        servicesAdapter.submitList(filtered)
        tvServiceCount.text = "${filtered.size} services available"
    }

    // Static data for now, should come from repo later
    private fun getAllServices(): List<Service> = listOf(
        Service("1", "Full Home Cleaning", "Cleaning", 40.0),
        Service("2", "Deep Kitchen Clean", "Cleaning", 60.0),
        Service("3", "Bathroom Scrub", "Cleaning", 30.0),
        Service("4", "Sofa & Carpet Clean", "Cleaning", 80.0),
        Service("7", "AC Service & Repair", "Repair", 80.0),
        Service("12", "Tap & Pipe Fixing", "Plumbing", 35.0),
        Service("16", "Fan & Light Fitting", "Electric", 45.0),
        Service("20", "Home Massage", "Wellness", 100.0),
        Service("24", "Laptop Repair", "Tech", 50.0),
        Service("28", "Car Wash", "Auto", 30.0),
        Service("31", "Event Photo", "Events", 150.0)
    )
}
