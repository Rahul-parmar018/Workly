package com.example.workly.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.workly.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class PaymentActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var layoutSummaryRows: LinearLayout
    private lateinit var tvTotalAmount: TextView
    private lateinit var layoutPaymentMethods: LinearLayout
    private lateinit var btnPay: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedMethod = "UPI"
    private var serviceName = ""
    private var providerName = ""
    private var price = 0.0
    private var basePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        serviceName = intent.getStringExtra("SERVICE_NAME") ?: "Service"
        price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        basePrice = intent.getDoubleExtra("BASE_PRICE", price)
        providerName = intent.getStringExtra("PROVIDER_NAME") ?: "Professional"

        initViews()
        setupSummary()
        setupPaymentMethods()
        updatePayButton()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        layoutSummaryRows = findViewById(R.id.layoutSummaryRows)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        layoutPaymentMethods = findViewById(R.id.layoutPaymentMethods)
        btnPay = findViewById(R.id.btnPay)
        progressBar = findViewById(R.id.progressBar)

        tvTotalAmount.text = "₹${price.toInt()}"

        btnPay.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btnPay.isEnabled = false
            
            // Simulate payment delay
            btnPay.postDelayed({
                val result = Intent().apply {
                    putExtra("PAYMENT_STATUS", "Paid")
                    putExtra("PAYMENT_METHOD", selectedMethod)
                }
                setResult(RESULT_OK, result)
                finish()
            }, 1500)
        }
    }

    private fun setupSummary() {
        addSummaryRow("Service", serviceName)
        addSummaryRow("Professional", providerName, isHighlighted = true)
        if (basePrice != price) {
            addSummaryRow("Base Price", "₹${basePrice.toInt()}", isSubtle = true)
            addSummaryRow("Pro Rate", "₹${price.toInt()}/hr")
        }
    }

    private fun addSummaryRow(label: String, value: String, isHighlighted: Boolean = false, isSubtle: Boolean = false) {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_detail_row, layoutSummaryRows, false)
        val tvLabel = view.findViewById<TextView>(R.id.tvLabel)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)

        tvLabel.text = label
        tvValue.text = value

        if (isHighlighted) {
            tvValue.setTextColor(ContextCompat.getColor(this, R.color.nav_item_color))
        } else if (isSubtle) {
            tvValue.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }

        layoutSummaryRows.addView(view)
    }

    private fun setupPaymentMethods() {
        val methods = listOf(
            Triple("UPI", "Google Pay / PhonePe / Paytm", R.drawable.ic_home), // Replace with UPI icon
            Triple("Card", "Debit / Credit Card", R.drawable.ic_search), // Replace with Card icon
            Triple("Cash", "Pay after service", R.drawable.ic_chat) // Replace with Money icon
        )

        methods.forEach { (name, subtitle, iconRes) ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_payment_method, layoutPaymentMethods, false)
            val card = view.findViewById<MaterialCardView>(R.id.cardPaymentMethod)
            val ivIcon = view.findViewById<ImageView>(R.id.ivMethodIcon)
            val tvName = view.findViewById<TextView>(R.id.tvMethodName)
            val tvSubtitle = view.findViewById<TextView>(R.id.tvMethodSubtitle)
            val rb = view.findViewById<RadioButton>(R.id.rbMethod)

            tvName.text = name
            tvSubtitle.text = subtitle
            ivIcon.setImageResource(iconRes)
            
            val isSelected = name == selectedMethod
            rb.isChecked = isSelected
            updateCardState(card, isSelected)

            view.setOnClickListener {
                selectedMethod = name
                refreshPaymentMethods()
                updatePayButton()
            }

            layoutPaymentMethods.addView(view)
        }
    }

    private fun refreshPaymentMethods() {
        for (i in 0 until layoutPaymentMethods.childCount) {
            val view = layoutPaymentMethods.getChildAt(i)
            val tvName = view.findViewById<TextView>(R.id.tvMethodName)
            val rb = view.findViewById<RadioButton>(R.id.rbMethod)
            val card = view.findViewById<MaterialCardView>(R.id.cardPaymentMethod)
            
            val isSelected = tvName.text == selectedMethod
            rb.isChecked = isSelected
            updateCardState(card, isSelected)
        }
    }

    private fun updateCardState(card: MaterialCardView, isSelected: Boolean) {
        if (isSelected) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white)) // Or very light blue
            card.strokeWidth = 4
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            card.strokeWidth = 0
        }
    }

    private fun updatePayButton() {
        btnPay.text = "Pay ₹${price.toInt()} Securely"
    }
}
