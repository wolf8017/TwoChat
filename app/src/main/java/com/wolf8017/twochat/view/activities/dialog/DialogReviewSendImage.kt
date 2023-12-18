package com.wolf8017.twochat.view.activities.dialog

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Window
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jsibbold.zoomage.ZoomageView
import com.wolf8017.twochat.R

class DialogReviewSendImage(private var context: Context, private var uri: Uri) {
    private var dialog: Dialog = Dialog(context)
    private lateinit var image: ZoomageView
    private lateinit var btnSend: FloatingActionButton

    init {
        initialize()
    }

    private fun initialize() {
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR)
        dialog.setContentView(R.layout.activity_review_send_image)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = layoutParams

        image = dialog.findViewById(R.id.myZoomageView)
        btnSend = dialog.findViewById(R.id.btn_send)
    }

    fun show(onCallBack: OnCallBack) {
        dialog.show()
        Glide.with(context)
            .load(uri)
            .into(image)
        btnSend.setOnClickListener {
            onCallBack.onButtonSendClick()
            dialog.dismiss()
        }
    }

    interface OnCallBack {
        fun onButtonSendClick()
    }
}
