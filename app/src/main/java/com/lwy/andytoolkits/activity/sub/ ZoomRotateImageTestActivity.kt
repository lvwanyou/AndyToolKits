package com.lwy.andytoolkits.activity.sub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lwy.andytoolkits.R
import com.lwy.andytoolkits.widget.zoomImgView.GestureDPImageView

class ZoomRotateImageTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom_rotate_test)
        initViews()
    }

    private fun initViews() {
        val button1 = findViewById<GestureDPImageView>(R.id.zoomRotateImg1)
        val button2 = findViewById<GestureDPImageView>(R.id.zoomRotateImg2)
    }
}