package com.wolf8017.twochat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikhaellopez.circularimageview.CircularImageView
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.ChatList

class ChatListAdapter() : RecyclerView.Adapter<ChatListAdapter.Holder>() {
    private var list: MutableList<ChatList> = mutableListOf()
    private lateinit var context: Context

    constructor(list: MutableList<ChatList>, context: Context) : this(){
        this.list = list
        this.context = context
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView
        var tvDesc: TextView
        var tvDate: TextView
        var profile: CircularImageView

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            tvDesc = itemView.findViewById(R.id.tv_desc)
            tvDate = itemView.findViewById(R.id.tv_date)
            profile = itemView.findViewById(R.id.image_profile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var chatlist: ChatList = list[position]
        holder.tvName.text = chatlist.userName
        holder.tvDesc.text = chatlist.description
        holder.tvDate.text = chatlist.date

        //for image, I need library
        Glide.with(context).load(chatlist.urlProfile).into(holder.profile)
    }

}
