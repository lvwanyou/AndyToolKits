package com.example.openglexample.surfaceView

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer(context)

        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float = event.x
        val y: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx: Float = x - previousX
                renderer.angle += (dx) * TOUCH_SCALE_FACTOR

                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 1080f
    }
}