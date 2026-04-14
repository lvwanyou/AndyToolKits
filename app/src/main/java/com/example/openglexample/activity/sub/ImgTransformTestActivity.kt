package com.example.openglexample.activity.sub

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.example.openglexample.R

class ImgTransformTestActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imgview_transform_test)

        val canvas = Canvas()
        val paint = Paint()
        paint.color = Color.BLACK
        canvas.drawLine(0f, 0f, 100f, 100f, paint)

        findViewById<ImageView>(R.id.img_test)
    }
}