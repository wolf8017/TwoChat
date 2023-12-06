package com.wolf8017.twochat.view.activities.chats

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.ChatsAdapter
import com.wolf8017.twochat.adapter.MessageAdapter
import com.wolf8017.twochat.databinding.ActivityChatsBinding
import com.wolf8017.twochat.model.chat.Chats
import com.wolf8017.twochat.model.chat.MessageModel
import com.wolf8017.twochat.view.activities.profile.UserProfileActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.logging.SimpleFormatter


class ChatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatsBinding

    private lateinit var receiverID: String
//    private lateinit var senderID: String

    private lateinit var dbUrl: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    private val messageModels: MutableList<MessageModel> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    //This is 11 hours
    private lateinit var user: FirebaseUser
    private lateinit var chatAdapter: ChatsAdapter
    private var list: MutableList<Chats> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chats)

        user = FirebaseAuth.getInstance().currentUser!!
        dbUrl = FirebaseDatabase.getInstance("https://twochat-de7f8-default-rtdb.asia-southeast1.firebasedatabase.app")
        reference = dbUrl.reference

//        val auth = FirebaseAuth.getInstance()
//        senderID = auth.uid.toString()

        val userName: String = intent.getStringExtra("userName").toString()
        receiverID = intent.getStringExtra("userID").toString()
        val userProfile: String = intent.getStringExtra("userProfile").toString()

        binding.tvUsername.text = userName

        if (userProfile == "") {
            binding.imageProfile.setImageResource(R.drawable.profile_avatar) // Set default image if profile user is null
        } else {
            Glide.with(this@ChatsActivity).load(userProfile).into(binding.imageProfile)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.edMessage.doOnTextChanged { text, start, before, count ->
            try {
                if (TextUtils.isEmpty(binding.edMessage.text.toString())) {
                    binding.btnSend.setImageDrawable(getDrawable(R.drawable.baseline_mic_24))
                } else {
                    binding.btnSend.setImageDrawable(getDrawable(R.drawable.baseline_send_24))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        messageAdapter = MessageAdapter(messageModels, this@ChatsActivity, receiverID)
//        binding.recycleViewChat.adapter = messageAdapter
//
//        val layoutManager = LinearLayoutManager(this)
//        layoutManager.stackFromEnd = true
//        binding.recycleViewChat.layoutManager = layoutManager
//
//        val senderRoom: String = senderID + receiverID
//        val receiverRoom: String = receiverID + senderID
//
//        dbUrl.getReference().child("chats")
//            .child(senderRoom)
//            .addValueEventListener(object : ValueEventListener {
//                @SuppressLint("NotifyDataSetChanged")
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    messageModels.clear()
//
//                    for (snapshot: DataSnapshot in dataSnapshot.children) {
//                        val model: MessageModel? = snapshot.getValue(MessageModel::class.java)!!
//                        model?.messageID = snapshot.key
//                        model?.let {
//                            messageModels.add(it)
//                        }
//                    }
//                    initRecyclerView()
//                    messageAdapter.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.w("ChatsActivity", "Failed to read value.", error.toException())
//                }
//            })
//
//        binding.btnSend.setOnClickListener {
//            val message: String = binding.edMessage.text.toString()
//            val model = MessageModel(senderID, message)
//            model.timestamp = Date().time
//            binding.edMessage.setText("")
//            dbUrl.getReference().child("chats")
//                .child(senderRoom)
//                .push()
//                .setValue(model)
//                .addOnSuccessListener {
//                    dbUrl.getReference().child("chats")
//                        .child(receiverRoom)
//                        .push()
//                        .setValue(model)
//                        .addOnSuccessListener {}
//                }
//        }

        chatAdapter = ChatsAdapter(list, this@ChatsActivity)
        binding.recycleViewChat.adapter = chatAdapter

        initBtnClick()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.recycleViewChat.layoutManager = layoutManager

        readChat()

    }

    private fun readChat() {
        val reference = dbUrl.reference
        reference.child("Chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list.clear()
                for (snapshot in dataSnapshot.children) {
                    val chats = snapshot.getValue(Chats::class.java)
                    if (chats != null && (chats.sender == user.uid && chats.receiver == receiverID)
                        || (chats?.receiver == user.uid && chats.sender == receiverID)
                    ) {
                        list.add(chats)
                    }
                }
                initRecyclerView()
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun initBtnClick() {
        binding.btnSend.setOnClickListener {
            if (!TextUtils.isEmpty(binding.edMessage.text.toString())) {
                sendTextMessage(binding.edMessage.text.toString())
                binding.edMessage.setText("")
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.imageProfile.setOnClickListener{
            startActivity(Intent(this@ChatsActivity, UserProfileActivity::class.java))
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun sendTextMessage(text: String) {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val today = formatter.format(date)

        val currentDateTime = Calendar.getInstance()
        val df = SimpleDateFormat("hh:mm a")
        val currentTime = df.format(currentDateTime.time)

        val chats: Chats = Chats(
            "$today $currentTime",
            text,
            "TEXT",
            user.uid,
            receiverID
        )

        reference.child("Chats").push().setValue(chats)
            .addOnSuccessListener {
                Log.d("SEND", "onSuccess: $it")
            }
            .addOnFailureListener {
                Log.d("SEND", "onFailure: $it")
            }

        //Add to ChatList
        val chatRef1 = FirebaseDatabase.getInstance()
            .getReference("ChatList")
            .child(user.uid)
            .child(receiverID)
        chatRef1.child("chatId").setValue(receiverID)

        val chatRef2 = FirebaseDatabase.getInstance()
            .getReference("ChatList")
            .child(receiverID)
            .child(user.uid)
        chatRef2.child("chatId").setValue(user.uid)
    }

    //function makes recyclerView chat at bottom, always
    private fun initRecyclerView() {
        var isAtBottom = true
        val rv = findViewById<RecyclerView>(R.id.recycleView_chat)
        val adapter = ChatsAdapter(list, this@ChatsActivity)
        rv.adapter = adapter

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true

        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (isAtBottom) {
                    rv.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isAtBottom = !recyclerView.canScrollVertically(1)
            }
        })

        adapter.registerAdapterDataObserver(observer)
        rv.layoutManager = linearLayoutManager
    }
}
