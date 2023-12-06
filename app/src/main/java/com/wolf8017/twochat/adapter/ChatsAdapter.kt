package com.wolf8017.twochat.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.chat.Chats

class ChatsAdapter(
    var list: MutableList<Chats> = mutableListOf(), var context: Context
) : RecyclerView.Adapter<ChatsAdapter.Holder>() {


    private val MSG_TYPE_LEFT: Int = 0
    private val MSG_TYPE_RIGHT: Int = 1

    private lateinit var firebaseUser: FirebaseUser

    inner class Holder(itemView: View) : ViewHolder(itemView) {
        private var textMessage: TextView

        init {
            textMessage = itemView.findViewById(R.id.tv_text_message)
        }

        fun bind(chats: Chats?) {
            textMessage.text = chats?.textMessage
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return if (viewType == MSG_TYPE_LEFT) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false)
            Holder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false)
            Holder(view)
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        return if (list[position].sender == firebaseUser.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }
}
