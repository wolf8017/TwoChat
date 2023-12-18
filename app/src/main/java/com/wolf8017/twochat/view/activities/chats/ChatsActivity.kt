package com.wolf8017.twochat.view.activities.chats

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devlomi.record_view.OnRecordListener
import android.Manifest
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Vibrator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.ChatsAdapter
import com.wolf8017.twochat.adapter.MessageAdapter
import com.wolf8017.twochat.databinding.ActivityChatsBinding
import com.wolf8017.twochat.managers.ChatService
import com.wolf8017.twochat.managers.interfaces.OnReadChatCallBack
import com.wolf8017.twochat.model.chat.Chats
import com.wolf8017.twochat.model.chat.MessageModel
import com.wolf8017.twochat.services.FirebaseService
import com.wolf8017.twochat.view.activities.dialog.DialogReviewSendImage
import com.wolf8017.twochat.view.activities.profile.UserProfileActivity
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class ChatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatsBinding

    private lateinit var receiverID: String

    //    private lateinit var senderID: String
    private lateinit var userProfile: String
    private lateinit var userName: String


    private val messageModels: MutableList<MessageModel> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    //This is 11 hours
    private lateinit var chatAdapter: ChatsAdapter
    private var list: MutableList<Chats> = mutableListOf()

    private var isActionShown: Boolean = false

    private lateinit var chatService: ChatService

    //Image
    private lateinit var imgUri: Uri

    //Audio
    private val PERMISSION_REQUEST_CODE = 1001
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audio_path: String
    private lateinit var sTime: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chats)

        initialize()
        initBtnClick()
        readChat()
    }

    private fun initialize() {
        userName = intent.getStringExtra("userName").toString()
        receiverID = intent.getStringExtra("userID").toString()
        userProfile = intent.getStringExtra("userProfile").toString()

        binding.tvUsername.text = userName

        chatService = ChatService(this@ChatsActivity, receiverID)

        if (userProfile == "") {
            binding.imageProfile.setImageResource(R.drawable.profile_avatar) // Set default image if profile user is null
        } else {
            Glide.with(this@ChatsActivity).load(userProfile).into(binding.imageProfile)
        }

        binding.edMessage.doOnTextChanged { text, start, before, count ->
            try {
                if (TextUtils.isEmpty(binding.edMessage.text.toString())) {
                    binding.btnSend.visibility = View.INVISIBLE
                    binding.recordButton.visibility = View.VISIBLE
                } else {
                    binding.btnSend.visibility = View.VISIBLE
                    binding.recordButton.visibility = View.INVISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        chatAdapter = ChatsAdapter(list, this@ChatsActivity)
        binding.recycleViewChat.adapter = chatAdapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.recycleViewChat.layoutManager = layoutManager

        //initialize record button
        binding.recordButton.setRecordView(binding.recordView)
        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                //Start recording
                if (!checkPermissionFromDevice()) {
                    binding.btnEmoji.visibility = View.INVISIBLE
                    binding.btnFile.visibility = View.INVISIBLE
                    binding.btnCamera.visibility = View.INVISIBLE
                    binding.edMessage.visibility = View.INVISIBLE

                    startRecord()
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrator != null) {
                        vibrator.vibrate(100)
                    }
                } else {
//                    requestPermissions()
                    checkAndRequestPermissions()
                }
            }

            override fun onCancel() {
                try {
                    mediaRecorder?.reset()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                binding.btnEmoji.visibility = View.VISIBLE
                binding.btnFile.visibility = View.VISIBLE
                binding.btnCamera.visibility = View.VISIBLE
                binding.edMessage.visibility = View.VISIBLE

                //Stop recording
                try {
                    sTime = getHumanTimeText(recordTime)
                    stopRecord()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onLessThanSecond() {
                binding.btnEmoji.visibility = View.VISIBLE
                binding.btnFile.visibility = View.VISIBLE
                binding.btnCamera.visibility = View.VISIBLE
                binding.edMessage.visibility = View.VISIBLE
            }

            override fun onLock() {
                TODO("Not yet implemented")
            }
        })
        binding.recordView.setOnBasketAnimationEndListener {
            binding.btnEmoji.visibility = View.VISIBLE
            binding.btnFile.visibility = View.VISIBLE
            binding.btnCamera.visibility = View.VISIBLE
            binding.edMessage.visibility = View.VISIBLE
        }
    }

    private fun getHumanTimeText(milliseconds: Long): String {
        return String.format(
            "%02d",
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            )
        )
    }

    private fun initBtnClick() {
        binding.btnSend.setOnClickListener {
            if (!TextUtils.isEmpty(binding.edMessage.text.toString())) {
                chatService.sendTextMsg(binding.edMessage.text.toString())
                binding.edMessage.setText("")
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.imageProfile.setOnClickListener {
            startActivity(
                Intent(this@ChatsActivity, UserProfileActivity::class.java)
                    .putExtra("userID", receiverID)
                    .putExtra("userProfile", userProfile)
                    .putExtra("userName", userName)
            )
        }

        binding.btnFile.setOnClickListener {
            if (isActionShown) {
                binding.layoutAction.visibility = View.GONE
                isActionShown = false
            } else {
                binding.layoutAction.visibility = View.VISIBLE
                isActionShown = true
            }
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                imgUri = data?.data!!
//                uploadToFirebase()
                try {
//                    binding.imageProfile.setImageURI(imgUri)
                    Glide.with(this@ChatsActivity)
                        .load(imgUri)
                        .placeholder(R.drawable.error_photo)
                        .into(binding.imageProfile)
                    reviewImage(imgUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private fun reviewImage(uri: Uri) {
        DialogReviewSendImage(this@ChatsActivity, uri).show(onCallBack = object :
            DialogReviewSendImage.OnCallBack {
            override fun onButtonSendClick() {
                val progressDialog = ProgressDialog(this@ChatsActivity)
                progressDialog.setMessage("Sending image...")
                progressDialog.show()

                //hide button action
                binding.layoutAction.visibility = View.GONE
                isActionShown = false

                // to upload image to firebase storage to get url image
                FirebaseService(this@ChatsActivity).uploadImageToFirebaseStorage(
                    imgUri,
                    onCallBack = object : FirebaseService.OnCallBack {
                        override fun onUploadSuccess(imageUrl: String) {
                            // to send chat image
                            chatService.sendImage(imageUrl)
                            progressDialog.dismiss()
                        }

                        override fun onUploadFailed(e: Exception) {
                            e.printStackTrace()
                        }
                    })
            }
        })
    }

    private fun openGallery() {
        val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        changeImage.launch(pickImg)
    }

    private fun readChat() {
        chatService.readChatData(object : OnReadChatCallBack {
            override fun onReadSuccess(list: MutableList<Chats>) {
                chatAdapter.updateList(list)
                // Assume 'adapter' is your RecyclerView adapter
                val recyclerView = binding.recycleViewChat

                // Notify the adapter about the new data
                chatAdapter.notifyItemInserted(list.size - 1)

                // Scroll to the newly added item
                recyclerView.smoothScrollToPosition(list.size - 1)
            }

            override fun onReadFailed() {
                Log.d("ChatsActivity", "onReadFailed: ")
            }
        })
    }

    private fun checkPermissionFromDevice(): Boolean {
        val write_external_storage_result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val record_audio_result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        // Return true if any of the permissions is denied
        return write_external_storage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    // Call this method before starting the recording
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissionFromDevice()) {
                // Permissions are not granted, request them
                requestPermissions()
            } else {
                // Permissions are already granted, start recording
                startRecord()
            }
        } else {
            // Permissions are not needed for older versions, start recording
            startRecord()
        }
    }

    private fun startRecord() {
        try {
            setUpMediaRecorder()
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@ChatsActivity, "Recording Error, please restart your app", Toast.LENGTH_LONG).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopRecord() {
        try {
            mediaRecorder?.let {
                // stop recording and free up resources
                it.stop()
                it.reset()
                it.release()
                mediaRecorder = null
                // sendVoice()  // Uncomment this line if sendVoice() is implemented
                chatService.sendVoice(audio_path)
            } ?: Toast.makeText(applicationContext, "MediaRecorder is null", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Stop recording error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpMediaRecorder() {
        val directory = getExternalFilesDir(null)
        val path_save = File(directory, "${UUID.randomUUID()}_audio_record.m4a").toString()

        audio_path = path_save
        Log.d("Recording", "setUpMediaRecorder: $path_save")

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setOutputFile(path_save)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        } catch (e: Exception) {
            Log.d("ChatsActivity", "setUpMediaRecorder: ${e.message}")
        }
    }
}
