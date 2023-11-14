package com.wolf8017.twochat.view.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firestore = FirebaseFirestore.getInstance()

        getInfo()

    }

    private fun getInfo() {
        firestore.collection("User")
            .document(firebaseUser.uid)
            .get()
            .addOnSuccessListener{
                val userName: String = it["userName"].toString()
                val userPhone: String = it["userPhone"].toString()

                binding.tvUsername.text = userName
                binding.tvPhone.text = userPhone

            }
            .addOnFailureListener{

            }
    }
}
