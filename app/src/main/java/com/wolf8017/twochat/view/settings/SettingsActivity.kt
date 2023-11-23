package com.wolf8017.twochat.view.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.databinding.ActivitySettingsBinding
import com.wolf8017.twochat.view.profile.ProfileActivity
import java.util.Objects

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        firestore = FirebaseFirestore.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        getInfo()

        initClickAction()
    }

    private fun initClickAction() {
        binding.lnProfile.setOnClickListener {
            startActivity(Intent(this@SettingsActivity, ProfileActivity::class.java))
        }
    }

    private fun getInfo() {
        firestore.collection("User")
            .document(firebaseUser.uid)
            .get()
            .addOnSuccessListener {
                val userName: String = Objects.requireNonNull(it["userName"].toString())
                val imageProfile: String = it["imageProfile"].toString()
                binding.tvUsername.text = userName
                Glide.with(this@SettingsActivity).load(imageProfile).into(binding.imageProfile)

            }
            .addOnFailureListener {
                Log.d("Get Data", "onFailure: $it")
            }

    }


}
