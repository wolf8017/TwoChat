package com.wolf8017.twochat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.chat.Chats
import com.wolf8017.twochat.tools.AudioService

class ChatsAdapter(
    var list: MutableList<Chats> = mutableListOf(),
    var context: Context
) : RecyclerView.Adapter<ChatsAdapter.Holder>() {


    private val MSG_TYPE_LEFT: Int = 0
    private val MSG_TYPE_RIGHT: Int = 1

    private lateinit var firebaseUser: FirebaseUser

    private var tmpBtnPlay: ImageButton? = null
    private var audioService = AudioService(context)

    fun updateList(list: MutableList<Chats>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View) : ViewHolder(itemView) {
        private var textMessage: TextView
        private var layoutText: LinearLayout
        private var layoutImage: LinearLayout
        private var layoutVoice: LinearLayout
        private var imageMessage: ImageView
        private var btnPlay: ImageButton
//        private var tmpHolder: ViewHolder

        init {
            textMessage = itemView.findViewById(R.id.tv_text_message)
            layoutText = itemView.findViewById(R.id.ln_text)
            layoutImage = itemView.findViewById(R.id.ln_image)
            layoutVoice = itemView.findViewById(R.id.ln_voice)
            imageMessage = itemView.findViewById(R.id.image_chat)
            btnPlay = itemView.findViewById(R.id.btn_play_chat)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(chats: Chats?) {
            // Check chat type
            when (chats?.type) {
                "TEXT" -> {
                    layoutText.visibility = View.VISIBLE
                    layoutImage.visibility = View.GONE
                    layoutVoice.visibility = View.GONE

                    textMessage.text = chats.textMessage
                }

                "IMAGE" -> {
                    layoutText.visibility = View.GONE
                    layoutVoice.visibility = View.GONE
                    layoutImage.visibility = View.VISIBLE

                    Glide.with(context)
                        .load(chats.url)
                        .placeholder(R.drawable.error_photo)
                        .into(imageMessage)
                }

                "VOICE" -> {
                    layoutText.visibility = View.GONE
                    layoutImage.visibility = View.GONE
                    layoutVoice.visibility = View.VISIBLE

                    layoutVoice.setOnClickListener {
                        if (tmpBtnPlay != null) {
                            tmpBtnPlay!!.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.baseline_play_circle_24
                                )
                            )
                        }
                        btnPlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.baseline_play_circle_24
                            )
                        )

                        audioService.playAudioFromUrl(chats.url, object : AudioService.OnPlayCallBack {
                            override fun onStarted() {
                                btnPlay.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.baseline_pause_circle_24
                                    )
                                )
                            }

                            override fun onFinished() {
                                btnPlay.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        context,
                                        R.drawable.baseline_play_circle_24
                                    )
                                )
                            }
                        })
                        tmpBtnPlay = btnPlay
                    }
                }

            }
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

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        return if (list[position].sender == firebaseUser.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

}
