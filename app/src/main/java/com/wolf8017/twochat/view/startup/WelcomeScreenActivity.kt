package com.wolf8017.twochat.view.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.wolf8017.twochat.view.MainActivity
import com.wolf8017.twochat.R
import com.wolf8017.twochat.view.auth.PhoneLoginActivity

class WelcomeScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        val btnAgree: Button = findViewById(R.id.btn_agree)

        btnAgree.setOnClickListener { startActivity(Intent(this@WelcomeScreenActivity, PhoneLoginActivity::class.java)) }
    }
}
