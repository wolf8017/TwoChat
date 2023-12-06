package com.wolf8017.twochat.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.wolf8017.twochat.R
import com.wolf8017.twochat.model.chat.MessageModel
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    var messageModels: MutableList<MessageModel>,
    var context: Context,
    var recId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var SENDER_VIEW_TYPE: Int = 1
    var RECEIVER_VIEW_TYPE: Int = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENDER_VIEW_TYPE) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false)
            SenderViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageModels[position].uID.equals(FirebaseAuth.getInstance().uid)) {
            SENDER_VIEW_TYPE
        } else {
            RECEIVER_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int {
        return messageModels.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageModel: MessageModel = messageModels[position]


        //Delete the message
        holder.itemView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("yes") { dialog, which ->
                    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

                    // Delete on sender side
                    val senderRoom: String = FirebaseAuth.getInstance().uid!! + recId
                    database.reference.child("chats")
                        .child(senderRoom)
                        .child(messageModel.messageID.toString())
                        .setValue(null)

                }
                .setNegativeButton("no") { dialog, which -> dialog?.dismiss() }
                .show()
            return@setOnLongClickListener false}


        //Display the message
        if (holder.javaClass == SenderViewHolder::class.java) {
            (holder as SenderViewHolder).senderMsg.text = messageModel.message
        } else {
            (holder as ReceiverViewHolder).receiverMsg.text = messageModel.message
        }

        // Display the timestamp
        val formattedTime = createTimestamp(messageModel.timestamp!!)

        if (holder is SenderViewHolder) {
            holder.senderTime.text = formattedTime
        } else if (holder is ReceiverViewHolder) {
            holder.receiverTime.text = formattedTime
        }
    }

    class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var receiverMsg: TextView
        var receiverTime: TextView

        init {
            receiverMsg = itemView.findViewById(R.id.receiverText)
            receiverTime = itemView.findViewById(R.id.receiverTime)
        }
    }

    class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var senderMsg: TextView
        var senderTime: TextView

        init {
            senderMsg = itemView.findViewById(R.id.senderText)
            senderTime = itemView.findViewById(R.id.senderTime)
        }
    }

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
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}
