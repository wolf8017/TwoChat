package com.wolf8017.twochat.view.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.wolf8017.twochat.R
import com.wolf8017.twochat.view.MainActivity

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        if (firebaseUser != null)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            },4000)
        }
        else
        {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashScreenActivity, WelcomeScreenActivity::class.java))
                finish()
            },4000)
        }
        }


}
