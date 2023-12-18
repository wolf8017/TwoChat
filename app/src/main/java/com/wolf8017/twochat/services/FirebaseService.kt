package com.wolf8017.twochat.services

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseService(private var context: Context) {


    fun uploadImageToFirebaseStorage(uri: Uri, onCallBack: OnCallBack) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
            .child("ImageChats/" + System.currentTimeMillis() + "." + getFileExtention(uri))
        storageRef.putFile(uri)
            .addOnSuccessListener {
                val urlTask: Task<Uri> = it.storage.downloadUrl
                while (!urlTask.isSuccessful) { }
                val downloadUrl: String = urlTask.result.toString()
//                    val sdownload_url: String = downloadUrl.toString()

                onCallBack.onUploadSuccess(downloadUrl)
            }
            .addOnFailureListener {
                onCallBack.onUploadFailed(it)
            }
    }

    private fun getFileExtention(uri: Uri): String? {
        val contentResolver: ContentResolver = context.getContentResolver()
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    interface OnCallBack {
        fun onUploadSuccess(imageUrl: String)
        fun onUploadFailed(e: Exception)
    }
}
