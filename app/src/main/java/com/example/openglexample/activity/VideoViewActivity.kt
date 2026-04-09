package com.example.openglexample.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import com.example.openglexample.R


class VideoViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)
        val videoView = findViewById<View>(R.id.videoView) as VideoView
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        val uri = Uri.parse("https://1251413404.vod2.myqcloud.com/vodtranssh1251413404/1253642696544760604/v.f852.mp4")
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
        videoView.setOnCompletionListener {
            videoView.start()
        }
        videoView.setOnPreparedListener { mediaController.show(0) }
    }
}

