package com.lwy.andytoolkits.activity.sub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lwy.andytoolkits.textureView.EGL14TextureView
import com.lwy.andytoolkits.textureView.MyES30Renderer

class TextureViewES30Activity : AppCompatActivity() {
    private lateinit var eglTextureView: EGL14TextureView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eglTextureView = EGL14TextureView(this)
        eglTextureView.setRenderer(MyES30Renderer())
        setContentView(eglTextureView)
    }

    override fun onResume() {
        super.onResume()
        eglTextureView.onResume()
    }

    override fun onPause() {
        super.onPause()
        eglTextureView.onPause()
    }
}