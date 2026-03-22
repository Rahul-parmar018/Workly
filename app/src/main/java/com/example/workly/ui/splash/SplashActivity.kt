package com.example.workly.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.workly.R
import com.example.workly.ui.auth.AuthSelectionActivity
import com.example.workly.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Check if user is already logged in
        val user = FirebaseAuth.getInstance().currentUser
        
        Handler(Looper.getMainLooper()).postDelayed({
            if (user != null) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, AuthSelectionActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
