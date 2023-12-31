package com.wolf8017.twochat.managers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wolf8017.twochat.managers.interfaces.OnReadChatCallBack
import com.wolf8017.twochat.model.chat.Chats
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ChatService(
    private var context: Context,
    private var receiverID: String
) {
    private var dbUrl: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://twochat-de7f8-default-rtdb.asia-southeast1.firebasedatabase.app")
    private var reference: DatabaseReference = dbUrl.reference
    private var user = FirebaseAuth.getInstance().currentUser!!

    fun readChatData(onCallBack: OnReadChatCallBack) {
        reference.child("Chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: MutableList<Chats> = mutableListOf()
                for (snapshot in dataSnapshot.children) {
                    val chats = snapshot.getValue(Chats::class.java)
                    if (chats != null && (chats.sender == user.uid && chats.receiver == receiverID)
                        || (chats?.receiver == user.uid && chats.sender == receiverID)
                    ) {
                        list.add(chats)
                    }
                }
                onCallBack.onReadSuccess(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onCallBack.onReadFailed()
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    fun sendTextMsg(text: String) {
        val chats = Chats(
            getCurrenDate(),
            text,
            "",
            "TEXT",
            user.uid,
            receiverID
        )

        reference.child("Chats").push().setValue(chats)

        //Add to ChatList
        val chatRef1 = FirebaseDatabase.getInstance()
            .getReference("ChatList")
            .child(user.uid)
            .child(receiverID)

        val updateReceiver = hashMapOf(
            "chatId" to receiverID,
            "lastMessage" to text,
            "timestamp" to System.currentTimeMillis(),
            "senderId" to user.uid
        )

        chatRef1.setValue(updateReceiver)

        val chatRef2 = FirebaseDatabase.getInstance()
            .getReference("ChatList")
            .child(receiverID)
            .child(user.uid)

        val updateSender = hashMapOf(
            "chatId" to user.uid,
            "lastMessage" to text,
            "timestamp" to System.currentTimeMillis(),
            "senderId" to user.uid
        )
        chatRef2.setValue(updateSender)

    }

    fun sendImage(imageUrl: String) {
        val chats = Chats(
            getCurrenDate(),
            "",
            imageUrl,
            "IMAGE",
            user.uid,
            receiverID
        )

        reference.child("Chats").push().setValue(chats)

        //Add to ChatList
        val chatRef1 = FirebaseDatabase.getInstance()
            .getReference("ChatList")
            .child(user.uid)
            .child(receiverID)

        val updateReceiver = hashMapOf(
            "chatId" to receiverID,
            "lastMessage" to "Sent an image",
            "timestamp" to System.currentTimeMillis(),
            "senderId" to user.uid
        )

        chatRef1.setValue(updateReceiver)

        val chatRef2 = FirebaseDatabase.getInstance()
            .getReference("ChatList")
            .child(receiverID)
            .child(user.uid)

        val updateSender = hashMapOf(
            "chatId" to user.uid,
            "lastMessage" to "Sent an image",
            "timestamp" to System.currentTimeMillis(),
            "senderId" to user.uid
        )
        chatRef2.setValue(updateSender)
    }

    fun sendVoice(audioPath: String) {
        val uriAudio: Uri = Uri.fromFile(File(audioPath))
        val audioRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("Chats/Voice/" + System.currentTimeMillis())

        audioRef.putFile(uriAudio).addOnSuccessListener {
            val urlTask: Task<Uri> = it.storage.downloadUrl
            while (!urlTask.isSuccessful) {
            }

            val voiceUrl: String = urlTask.result.toString()

            val chats = Chats(
                getCurrenDate(),
                "",
                voiceUrl,
                "VOICE",
                user.uid,
                receiverID
            )

            reference.child("Chats").push().setValue(chats)

            //Add to ChatList
            val chatRef1 = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(user.uid)
                .child(receiverID)

            val updateReceiver = hashMapOf(
                "chatId" to receiverID,
                "lastMessage" to "Sent a voice ",
                "timestamp" to System.currentTimeMillis(),
                "senderId" to user.uid
            )

            chatRef1.setValue(updateReceiver)

            val chatRef2 = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(receiverID)
                .child(user.uid)

            val updateSender = hashMapOf(
                "chatId" to user.uid,
                "lastMessage" to "Sent a voice",
                "timestamp" to System.currentTimeMillis(),
                "senderId" to user.uid
            )
            chatRef2.setValue(updateSender)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrenDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val today = formatter.format(date)

        val currentDateTime = Calendar.getInstance()
        val df = SimpleDateFormat("hh:mm a")
        val currentTime = df.format(currentDateTime.time)

        return "$today $currentTime"
    }
}
