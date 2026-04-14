package com.example.openglexample.activity.sub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.openglexample.R
import com.example.openglexample.widget.zoomImgView.GestureDPImageView

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