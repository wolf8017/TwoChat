package com.wolf8017.twochat.view.activities.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.wolf8017.twochat.R
import com.wolf8017.twochat.common.Common
import com.wolf8017.twochat.model.ChatList
import com.wolf8017.twochat.view.activities.chats.ChatsActivity
import com.wolf8017.twochat.view.activities.display.ViewImageActivity
import com.wolf8017.twochat.view.activities.profile.UserProfileActivity

class DialogViewUser(
    private var context: Context,
    private var chatList: ChatList
) {
    private var dialog: Dialog = Dialog(context)
    private lateinit var profile: ImageView
    private lateinit var userName: TextView
    private lateinit var btnChat: ImageButton
    private lateinit var btnCall: ImageButton
    private lateinit var btnVideoCall: ImageButton
    private lateinit var btnInfo: ImageButton

    private lateinit var activityOptionCompat: ActivityOptionsCompat

    init {
        initialize(chatList)
    }

    private fun initialize(chatList: ChatList) {
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR)
        dialog.setContentView(R.layout.dialog_view_user)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams

        userName = dialog.findViewById(R.id.tv_username)
        userName.text = chatList.userName

        profile = dialog.findViewById(R.id.image_profile)
        Glide.with(context)
            .load(chatList.urlProfile)
            .placeholder(R.drawable.profile_avatar)
            .into(profile)

        btnChat = dialog.findViewById(R.id.btn_chat)
        btnCall = dialog.findViewById(R.id.btn_call)
        btnVideoCall = dialog.findViewById(R.id.btn_video_call)
        btnInfo = dialog.findViewById(R.id.btn_info)

        btnChat.setOnClickListener {
            context.startActivity(
                Intent(context, ChatsActivity::class.java)
                    .putExtra("userID", chatList.userID)
                    .putExtra("userName", chatList.userName)
                    .putExtra("userProfile", chatList.urlProfile)
                    .putExtra("fcmToken", chatList.fcmToken)
            )
            dialog.dismiss()
        }
        btnCall.setOnClickListener {
            Toast.makeText(context, "Call clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnVideoCall.setOnClickListener {
            Toast.makeText(context, "Video Call clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnInfo.setOnClickListener {
            context.startActivity(
                Intent(context, UserProfileActivity::class.java)
                    .putExtra("userID", chatList.userID)
                    .putExtra("userProfile", chatList.urlProfile)
                    .putExtra("userName", chatList.userName)
            )
            dialog.dismiss()
        }

        profile.setOnClickListener{
            profile.invalidate()

            val dr: Drawable = profile.drawable
            Common.IMAGE_BITMAP = ((dr.current) as? BitmapDrawable)?.bitmap

            if (context is Activity){
                activityOptionCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(context as Activity, profile, "image")
            }

            val intent= Intent(context, ViewImageActivity::class.java)
            context.startActivity(intent, activityOptionCompat.toBundle())
        }

        dialog.show()
    }
}
