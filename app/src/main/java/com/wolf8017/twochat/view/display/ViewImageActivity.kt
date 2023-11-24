package com.wolf8017.twochat.view.display

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.wolf8017.twochat.R
import com.wolf8017.twochat.common.Common
import com.wolf8017.twochat.databinding.ActivityViewImageBinding

class ViewImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_image)

        binding.myZoomageView.setImageBitmap(Common.IMAGE_BITMAP)
    }
}
