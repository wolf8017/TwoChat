package com.wolf8017.twochat.view.activities.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
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
import com.wolf8017.twochat.common.Common
import com.wolf8017.twochat.databinding.ActivityProfileBinding
import com.wolf8017.twochat.view.activities.display.ViewImageActivity
import com.wolf8017.twochat.view.activities.settings.SettingsActivity
import com.wolf8017.twochat.view.activities.startup.SplashScreenActivity
import java.io.FileDescriptor
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bsDialogEditName: BottomSheetDialog? = null
    private var imgUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    //for camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firestore = FirebaseFirestore.getInstance()

        progressDialog = ProgressDialog(this)

        getInfo()
        initActionClick()
    }

    private fun initActionClick() {
        binding.fabCamera.setOnClickListener {
            showBottomSheetPickPhoto()
        }

        binding.lnEditName.setOnClickListener {
            showBottomSheetEditName()
        }

        binding.imageProfile.setOnClickListener {
            binding.imageProfile.invalidate()

            val dr: Drawable = binding.imageProfile.drawable
            Common.IMAGE_BITMAP = ((dr.current) as? BitmapDrawable)?.bitmap

            val activityOptionCompat: ActivityOptionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this@ProfileActivity, binding.imageProfile, "image")

            val intent: Intent = Intent(this@ProfileActivity, ViewImageActivity::class.java)
            startActivity(intent, activityOptionCompat.toBundle())
        }

        binding.btnLogOut.setOnClickListener {
            showDialogSignOut()
        }
    }

    private fun showDialogSignOut() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@ProfileActivity)
        builder.setMessage("Do you want to sign out?")
        builder.setPositiveButton("Sign out") { dialog, which ->
            dialog.cancel()
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this@ProfileActivity, SplashScreenActivity::class.java))
            finish()
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.cancel()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
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
                == PackageManager.PERMISSION_DENIED) {
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


    @SuppressLint("InflateParams", "ObsoleteSdkInt")
    private fun showBottomSheetEditName() {
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)

        view.findViewById<View>(R.id.btn_cancel)?.setOnClickListener {
            bsDialogEditName?.dismiss()
        }

        val edUserName: EditText = view.findViewById(R.id.ed_username)

        view.findViewById<View>(R.id.btn_save)?.setOnClickListener {

            if (TextUtils.isEmpty(edUserName.text.toString())) {
                Toast.makeText(applicationContext, "Name can't be empty", Toast.LENGTH_SHORT).show()
            } else {
                updatName(edUserName.text.toString())
                bsDialogEditName?.dismiss()
            }
        }

        bsDialogEditName = BottomSheetDialog(this)
        bsDialogEditName?.setContentView(view)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bsDialogEditName?.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        bsDialogEditName?.setOnDismissListener {
            bsDialogEditName = null
        }

        bsDialogEditName?.show()
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                if (data != null) {
                    imgUri = data.data!!
                    uploadToFirebase()
                    try {
                        binding.imageProfile.setImageURI(imgUri)
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
            .child("ImageProfile/" + System.currentTimeMillis() + "." + getFileExtention(imgUri!!))
        storageRef.putFile(imgUri!!)
            .addOnSuccessListener {
                val urlTask: Task<Uri> = it.storage.downloadUrl
                while (!urlTask.isSuccessful) {
                }
                val downloadUrl: String = urlTask.result.toString()
//                    val sdownload_url: String = downloadUrl.toString()

                val hashMap: MutableMap<String, Any> = mutableMapOf()
                hashMap["imageProfile"] = downloadUrl

                progressDialog.dismiss()

                firestore.collection("User").document(firebaseUser.uid)
                    .update(hashMap)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "upload successfully", Toast.LENGTH_SHORT).show()
                        getInfo()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "upload failed", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }

    private fun updatName(newName: String) {
        firestore.collection("User")
            .document(firebaseUser.uid)
            .update("userName", newName)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "update successfully", Toast.LENGTH_SHORT).show()
                getInfo()
            }
            .addOnFailureListener {}
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
                if (imageProfile.isEmpty()) {
                    Glide.with(this@ProfileActivity).load(R.drawable.profile_avatar).into(binding.imageProfile)
                } else {
                    Glide.with(this@ProfileActivity).load(imageProfile).into(binding.imageProfile)
                }
            }
            .addOnFailureListener {

            }
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent: Intent = Intent(this@ProfileActivity, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
