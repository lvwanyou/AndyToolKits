package com.example.openglexample.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.openglexample.R
import com.example.openglexample.decoder.SimpleVideoDecoder
import com.example.openglexample.utils.AppFileUtils
import com.example.openglexample.utils.AssertsUtils
import java.io.File
import java.io.IOException

class MediaCodecSurfaceViewActivity : AppCompatActivity() {
    private lateinit var surfaceView: SurfaceView

    @SuppressLint("MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediacodec_sv_test)

        var videoRootPath = "decoder"
        try {
            val templateFiles = assets.list(videoRootPath)
            for (templateFile in templateFiles!!) {
                AssertsUtils.writeToStorage(this, videoRootPath + "/" + templateFile, true)
            }

            videoRootPath = AppFileUtils.getCache(this) + videoRootPath
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val videoPath = videoRootPath + File.separator + "7260802466709635107.MP4"

        surfaceView = findViewById(R.id.glSv)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val surface: Surface = holder.surface
                val decoder = SimpleVideoDecoder(videoPath, surface)

                // 在一个新线程中运行解码过程
                Thread {
                    decoder.decode()
                }.start()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // SurfaceView 大小或格式发生变化时调用
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // SurfaceView 被销毁时调用
            }
        })
    }

}