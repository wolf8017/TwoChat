package com.wolf8017.twochat.view.activities.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
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
import com.wolf8017.twochat.databinding.ActivitySetUserInfoBinding
import com.wolf8017.twochat.model.user.User
import com.wolf8017.twochat.view.MainActivity
import java.io.FileDescriptor
import java.io.IOException

class SetUserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetUserInfoBinding
    private lateinit var progressDialog: ProgressDialog

    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore

    private var bottomSheetDialog: BottomSheetDialog? = null

    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_user_info)


        // Check user is new or not
        user = FirebaseAuth.getInstance().currentUser!!
        db = FirebaseFirestore.getInstance()
        db.collection("User")
            .document(user.uid)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val factory = Editable.Factory.getInstance()
                    val userName: String? = it.result.getString("userName")

                    if (userName.isNullOrEmpty()) {
                        binding.edName.text = factory.newEditable("")
                    } else {
                        binding.edName.text = factory.newEditable(it.result.getString("userName"))
                    }

                    val imageProfile: String? = it.result.getString("imageProfile")

                    if (imageProfile != "") {
                        Glide.with(this@SetUserInfoActivity)
                            .load(imageProfile)
                            .placeholder(R.drawable.profile_avatar)
                            .into(binding.imageProfile)
                    } else {
                        Glide.with(this@SetUserInfoActivity)
                            .load(R.drawable.profile_avatar)
                            .placeholder(R.drawable.profile_avatar)
                            .into(binding.imageProfile)
                    }
                } else {
                    Log.w(TAG, "Error getting document: ", it.exception)
                }
            }

        progressDialog = ProgressDialog(this)
        initButtonCLick()
    }

    private fun initButtonCLick() {
        binding.btnNext.setOnClickListener {
            if (TextUtils.isEmpty(binding.edName.text.toString())) {
                Toast.makeText(applicationContext, "Please input your name!!!", Toast.LENGTH_SHORT).show()
            } else {
                doUpdate()
                uploadToFirebase()
            }
        }

        binding.imageProfile.setOnClickListener {
//            pickImage()
            showBottomSheetPickPhoto()
        }
    }

    private fun doUpdate() {
        progressDialog.setMessage("Please wait...")
        progressDialog.show()

        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            val userID: String = firebaseUser.uid
            val users = User(
                userID, binding.edName.text.toString(),
                firebaseUser.phoneNumber.toString(),
                "",
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
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Update Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Update Failed " + it.message, Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(applicationContext, "You need to login first", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
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
            //Open camera
            checkCameraPermission()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    val permission = arrayOf<String>(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestPermissions(permission, 112)
                } else {
                    openCamera()
                }
            } else {
                openCamera()
            }

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

    private fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf<String>(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, 112)
            }
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
        cameraActivityResultLauncher.launch(cameraIntent)
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                if (data != null) {
                    imgUri = data.data!!
//                    uploadToFirebase()
                    try {
                        Glide.with(this@SetUserInfoActivity)
                            .load(imgUri)
                            .placeholder(R.drawable.profile_avatar)
                            .into(binding.imageProfile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // Handle the case when data is null
                    Toast.makeText(this, "Failed to retrieve image data", Toast.LENGTH_SHORT).show()
                }

            }
        }

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
            if (it.resultCode == RESULT_OK) {
                val inputImage = uriToBitmap(imgUri!!)
                val rotated = rotateBitmap(inputImage!!)
                binding.imageProfile.setImageBitmap(rotated)
                uploadToFirebase()
            }
        }
    )

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("Range", "Recycle")
    fun rotateBitmap(input: Bitmap): Bitmap {
        val orientationColumn =
            arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur: Cursor? = contentResolver.query(imgUri!!, orientationColumn, null, null, null)
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
        }
        Log.d("tryOrientation", orientation.toString() + "")
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation.toFloat())
        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
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
        progressDialog.setMessage("uploading...")
        progressDialog.show()

        val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
            .child("ImageProfile/" + System.currentTimeMillis() + "." + imgUri?.let { getFileExtention(it) })
        imgUri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener {
                    val urlTask: Task<Uri> = it.storage.downloadUrl
                    while (!urlTask.isSuccessful) {
                    }
                    val downloadUrl: String = urlTask.result.toString()

                    val hashMap: MutableMap<String, Any> = mutableMapOf()
                    hashMap["imageProfile"] = downloadUrl
                    hashMap["userName"] = binding.edName.text.toString()

                    progressDialog.dismiss()

                    db.collection("User").document(user.uid)
                        .update(hashMap)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "upload successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "upload failed", Toast.LENGTH_SHORT).show()
                    //                getInfo()
                }
        }
    }
}
