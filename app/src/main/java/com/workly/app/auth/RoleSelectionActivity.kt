package com.workly.app.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workly.app.R
import com.google.android.material.button.MaterialButton

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val btnUser = findViewById<MaterialButton>(R.id.btnUser)
        val btnProvider = findViewById<MaterialButton>(R.id.btnProvider)

        btnUser.setOnClickListener {
            goToAuth("user")
        }

        btnProvider.setOnClickListener {
            goToAuth("provider")
        }
    }

    private fun goToAuth(role: String) {
        val intent = Intent(this, AuthSelectionActivity::class.java)
        intent.putExtra("role", role)
        startActivity(intent)
    }
}
