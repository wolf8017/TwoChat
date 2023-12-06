package com.wolf8017.twochat.view.activities.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.databinding.ActivitySetUserInfoBinding
import com.wolf8017.twochat.model.user.User
import com.wolf8017.twochat.view.MainActivity

class SetUserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetUserInfoBinding
    private lateinit var progreessDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_user_info)

        progreessDialog = ProgressDialog(this)
        initButtonCLick()
    }

    private fun initButtonCLick() {
        binding.btnNext.setOnClickListener {
            if (TextUtils.isEmpty(binding.edName.text.toString())) {
                Toast.makeText(applicationContext, "Please input your name!!!", Toast.LENGTH_SHORT).show()
            } else {
                doUpdate()
            }

        }

        binding.imageProfile.setOnClickListener {
//            pickImage()
            Toast.makeText(applicationContext, "Please input username", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doUpdate() {
        progreessDialog.setMessage("Please wait...")
        progreessDialog.show()

        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            val userID: String = firebaseUser.uid
            val users: User = User(
                userID, binding.edName.text.toString(),
                firebaseUser.phoneNumber.toString(),
                "",
                "",
                "",
                "",
                "",
                "",
                "",
            )

            firestore.collection("User")
                .document(firebaseUser.uid)
                .set(users)
                .addOnSuccessListener {
                    progreessDialog.dismiss()
                    Toast.makeText(applicationContext, "Update Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }.addOnFailureListener {
                    progreessDialog.dismiss()
                    Toast.makeText(applicationContext, "Update Failed " + it.message, Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(applicationContext, "You need to login first", Toast.LENGTH_SHORT).show()
            progreessDialog.dismiss()
        }
    }
}
