package com.wolf8017.twochat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.user.User
import com.wolf8017.twochat.view.activities.chats.ChatsActivity

class ContactsAdapter() : RecyclerView.Adapter<ContactsAdapter.Holder>() {

    private var list: MutableList<User> = mutableListOf()
    private lateinit var context: Context

    constructor(list: MutableList<User>, context: Context) : this() {
        this.list = list
        this.context = context
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: ImageView
        var username: TextView
        var desc: TextView

        init {
            imageProfile = itemView.findViewById(R.id.image_profile)
            username = itemView.findViewById(R.id.tv_username)
            desc = itemView.findViewById(R.id.tv_desc)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user: User = list[position]

        holder.username.text = user.userName
        holder.desc.text = user.bio

        Glide.with(context).load(user.imageProfile).into(holder.imageProfile)

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ChatsActivity::class.java)
                    .putExtra("userID", user.userID)
                    .putExtra("userName", user.userName)
                    .putExtra("userProfile", user.imageProfile)
            )
        }
    }
}
