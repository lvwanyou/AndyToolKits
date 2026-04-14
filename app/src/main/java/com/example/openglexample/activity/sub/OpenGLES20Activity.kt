package com.example.openglexample.activity.sub

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.openglexample.surfaceView.MyGLSurfaceView

class OpenGLES20Activity: AppCompatActivity() {
    private lateinit var glView: GLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        glView = MyGLSurfaceView(this)
        setContentView(glView)
    }
}