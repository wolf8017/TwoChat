package com.wolf8017.twochat.menu

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.ChatListAdapter
import com.wolf8017.twochat.databinding.FragmentChatsBinding
import com.wolf8017.twochat.model.ChatList
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var user: FirebaseUser? = null
    private lateinit var reference: DatabaseReference
    private lateinit var firestore: FirebaseFirestore

    private var handler: Handler = Handler()

    private var list: MutableList<ChatList> = mutableListOf()
    private var allUserID: MutableList<String> = mutableListOf()


    private lateinit var adapter: ChatListAdapter

    private lateinit var binding: FragmentChatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats, container, false)

        binding.recycleViewChat.layoutManager = LinearLayoutManager(context)
        adapter = ChatListAdapter(list, requireContext())
        binding.recycleViewChat.adapter = adapter

        user = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        if (user != null) {
            getChatList()
        }

        return binding.root
    }

    private fun getChatList() {
        binding.progressCircular.visibility = View.VISIBLE

        reference.child("ChatList").child(user!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        list.clear()
                        allUserID.clear()
                        for (snapshot in dataSnapshot.children) {

                            val userID: String = Objects.requireNonNull(snapshot.child("chatId").value).toString()
                            binding.progressCircular.visibility = View.GONE
                            binding.lnInvite.visibility = View.GONE

                            allUserID.add(userID)
                        }
                        getUserData()
                    } else {
                        binding.progressCircular.visibility = View.GONE
                        binding.lnInvite.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getUserData() {
        handler.post {
            for (userID in allUserID) {
                firestore.collection("User").document(userID)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        try {
                            processUserSnapshot(userSnapshot, userID)
                        } catch (e: Exception) {
                            Log.d("ChatsFragment", "onSuccess: ${e.message}")
                        }
                    }
                    .addOnFailureListener {
                        Log.d("ChatsFragment", "onFailure: Error ${it.message}")
                    }
            }
        }
    }

    private fun processUserSnapshot(userSnapshot: DocumentSnapshot, userID: String) {
        val userName = userSnapshot["userName"].toString()
        val imageProfile = userSnapshot["imageProfile"].toString()
        val fcmToken = userSnapshot["fcmToken"].toString()

        reference.child("ChatList").child(user!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val chatListData = processChatListSnapshot(dataSnapshot)

                    val formattedTimestamp = createTimestamp(chatListData.timestamp)

                    val chat = if (chatListData.senderID != userID) {
                        ChatList(userID, userName, "You: ${chatListData.lastMessage}", formattedTimestamp, imageProfile, fcmToken)
                    } else {
                        ChatList(userID, userName, chatListData.lastMessage, formattedTimestamp, imageProfile, fcmToken)
                    }

                    list.add(chat)
                    adapter.notifyItemInserted(0)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatsFragment", "onCancelled: Error ${error.message}")
                }
            })
    }

    private fun processChatListSnapshot(dataSnapshot: DataSnapshot): ChatListData {
        var lastMessage = ""
        var senderID = ""
        var timestamp = 0L

        for (snapshot in dataSnapshot.children) {
            lastMessage = snapshot.child("lastMessage").value.toString()
            senderID = snapshot.child("senderId").value.toString()
            timestamp = snapshot.child("timestamp").value.toString().toLong()
        }

        return ChatListData(lastMessage, senderID, timestamp)
    }

    data class ChatListData(val lastMessage: String, val senderID: String, val timestamp: Long)

    private fun createTimestamp(messageTimestamp: Long): String {
        val currentDate = Calendar.getInstance()
        val messageDate = Calendar.getInstance()
        messageDate.timeInMillis = messageTimestamp

        return if (isSameDay(currentDate, messageDate)) {
            // Within the same day, show only the time
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate.time)
        } else {
            // Different day, show both date and time
            SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault()).format(messageDate.time)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.MONTH] == cal2[Calendar.MONTH] &&
                cal1[Calendar.DAY_OF_MONTH] == cal2[Calendar.DAY_OF_MONTH]
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
