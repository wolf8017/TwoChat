package com.wolf8017.twochat.view.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wolf8017.twochat.R
import com.wolf8017.twochat.databinding.ActivityProfileBinding
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private var bottomSheetDialog: BottomSheetDialog? = null
    private lateinit var imgUri: Uri
    private lateinit var processDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firestore = FirebaseFirestore.getInstance()

        processDialog = ProgressDialog(this)


        getInfo()
        initActionClick()
    }

    private fun initActionClick() {
        binding.fabCamera.setOnClickListener {
            showBottomSheetPickPhoto()
        }
    }

    @SuppressLint("InflateParams", "ObsoleteSdkInt")
    private fun showBottomSheetPickPhoto() {

        bottomSheetDialog = BottomSheetDialog(this)
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_pick, null)

        view.findViewById<View>(R.id.ln_gallery)?.setOnClickListener {
            openGallery()
            bottomSheetDialog?.dismiss()
        }

        view.findViewById<View>(R.id.ln_camera)?.setOnClickListener {
            Toast.makeText(applicationContext, "Camera", Toast.LENGTH_SHORT).show()
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.setContentView(view)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bottomSheetDialog?.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        bottomSheetDialog?.setOnDismissListener {
            bottomSheetDialog = null
        }

        bottomSheetDialog?.show()
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                imgUri = data?.data!!
                uploadToFirebase()
                try {
                    binding.imageProfile.setImageURI(imgUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private fun openGallery() {
        val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        changeImage.launch(pickImg)
    }

    private fun getFileExtention(uri: Uri): String? {
        val contentResolver: ContentResolver = getContentResolver()
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadToFirebase() {

        processDialog.setMessage("uploading...")
        processDialog.show()
        val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
            .child("ImageProfile/" + System.currentTimeMillis() + "." + getFileExtention(imgUri))
        storageRef.putFile(imgUri)
            .addOnSuccessListener {
                val urlTask: Task<Uri> = it.storage.downloadUrl
                while (!urlTask.isSuccessful) {
                }
                val downloadUrl: String = urlTask.result.toString()
//                    val sdownload_url: String = downloadUrl.toString()

                val hashMap: MutableMap<String, Any> = mutableMapOf()
                hashMap["imageProfile"] = downloadUrl

                processDialog.dismiss()

                firestore.collection("User").document(firebaseUser.uid)
                    .update(hashMap)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "upload successfully", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "upload failed", Toast.LENGTH_SHORT).show()
                getInfo()
            }
    }

    private fun getInfo() {
        firestore.collection("User")
            .document(firebaseUser.uid)
            .get()
            .addOnSuccessListener {
                val userName: String = it["userName"].toString()
                val userPhone: String = it["userPhone"].toString()
                val imageProfile: String = it["imageProfile"].toString()

                binding.tvUsername.text = userName
                binding.tvPhone.text = userPhone
                Glide.with(this@ProfileActivity).load(imageProfile).into(binding.imageProfile)
            }
            .addOnFailureListener {

            }
    }
}
