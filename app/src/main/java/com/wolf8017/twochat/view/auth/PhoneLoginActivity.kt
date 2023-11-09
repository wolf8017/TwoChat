package com.wolf8017.twochat.view.auth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.databinding.ActivityPhoneLoginBinding
import com.wolf8017.twochat.model.user.User
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneLoginBinding
    private val TAG: String = "PhoneLoginActivity"

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_login)

        //
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (firebaseUser != null) {
            startActivity(Intent(this@PhoneLoginActivity, SetUserInfoActivity::class.java))
        }

        progressDialog = ProgressDialog(this)
        binding.btnNext.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (binding.btnNext.text.toString() == "Next") {

                    var phone: String = "+" + binding.edCountryCode.text.toString() + binding.edPhone.text.toString()
                    startPhoneNumberVerification(phone)
                } else {
                    progressDialog.setMessage("Verifying .. ")
                    progressDialog.show()
                    verifyPhoneNumberWithCode(storedVerificationId, binding.edCode.text.toString())
                }
            }
        })

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                    }

                    is FirebaseTooManyRequestsException -> {
                        // The SMS quota for the project has been exceeded
                    }

                    is FirebaseAuthMissingActivityForRecaptchaException -> {
                        // reCAPTCHA verification attempted with null Activity
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                binding.btnNext.text = "Confirm"
                progressDialog.dismiss()
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        //verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                progressDialog.dismiss()
                Log.d(TAG, "signInWithCredential:success")
                val user = task.result?.user
                startActivity(Intent(this@PhoneLoginActivity, SetUserInfoActivity::class.java))

//                if (user != null) {
//                    val userID: String = user.uid
//                    val users: User = User(
//                        userID,
//                        "",
//                        user.phoneNumber.toString(),
//                        "",
//                        "",
//                        "",
//                        "",
//                        "",
//                        "",
//                        "",
//                    )
//                    firestore.collection("User")
//                        .document(userID)
//                        .parent
//                        .add(users)
//                        .addOnSuccessListener {
//                            startActivity(Intent(this@PhoneLoginActivity, SetUserInfoActivity::class.java))
//                        }
//                } else {
//                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
//                }
//               startActivity(Intent(this@PhoneLoginActivity, SetUserInfoActivity::class.java))
            } else {
                progressDialog.dismiss()
                // Sign in failed, display a message and update the UI
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                }
            }
        }
    }
}
