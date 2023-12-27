package com.wolf8017.twochat.view.activities.startup

import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.user.User
import com.wolf8017.twochat.view.MainActivity
import com.wolf8017.twochat.view.activities.chats.ChatsActivity
import java.io.File

class SplashScreenActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Check if the app is started from a notification
        val fromNotification = intent.extras != null && intent.extras!!.getString("userID") != null

        if (fromNotification) {
            handleNotificationStart()
        } else {
            handleRegularStart()
        }
    }

    private fun handleNotificationStart() {
        val userId: String = intent.extras!!.getString("userID").toString()
        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        firestore.collection("User")
            .document(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val model: User? = task.result.toObject(User::class.java)
                    val chatsIntent = Intent(this@SplashScreenActivity, ChatsActivity::class.java)
                    val mainIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)

                    mainIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(mainIntent)

                    model?.let {
                        chatsIntent.putExtra("userID", it.userID)
                            .putExtra("userProfile", it.imageProfile)
                            .putExtra("userName", it.userName)
                            .putExtra("fcmToken", it.fcmToken)
                    }
                    chatsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    startActivity(chatsIntent)
                    finish()
                }
            }
    }

    private fun handleRegularStart() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            }, 4000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashScreenActivity, WelcomeScreenActivity::class.java))
                finish()
            }, 4000)
        }
    }

}
