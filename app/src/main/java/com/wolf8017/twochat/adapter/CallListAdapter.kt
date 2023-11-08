package com.wolf8017.twochat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikhaellopez.circularimageview.CircularImageView
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.CallList

class CallListAdapter() : RecyclerView.Adapter<CallListAdapter.Holder>() {
    private var list: MutableList<CallList> = mutableListOf()
    private lateinit var context: Context

    constructor(list: MutableList<CallList>, context: Context) : this() {
        this.list = list
        this.context = context
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView
        var tvDate: TextView
        var profile: CircularImageView
        var arrow: ImageView

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            tvDate = itemView.findViewById(R.id.tv_date)
            profile = itemView.findViewById(R.id.image_profile)
            arrow = itemView.findViewById(R.id.img_arrow)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_call_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val callList: CallList = list[position]
        holder.tvName.text = callList.userName
        holder.tvDate.text = callList.date

        when (callList.callType) {
            "missed" -> {
                holder.arrow.setImageDrawable(context.getDrawable(R.drawable.baseline_arrow_downward_24))
                holder.arrow.drawable.setTint(context.resources.getColor(android.R.color.holo_red_dark))
            }
            "income" -> {
                holder.arrow.setImageDrawable(context.getDrawable(R.drawable.baseline_arrow_downward_24))
                holder.arrow.drawable.setTint(context.resources.getColor(android.R.color.holo_green_dark))
            }
            else -> {
                holder.arrow.setImageDrawable(context.getDrawable(R.drawable.baseline_arrow_upward_24))
                holder.arrow.drawable.setTint(context.resources.getColor(android.R.color.holo_green_dark))
            }
        }

        //for image, I need library
        Glide.with(context).load(callList.urlProfile).into(holder.profile)
    }

}

