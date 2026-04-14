package com.lwy.andytoolkits.widget.gestureimgview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet


class GradientImageView : AppCompatImageView {
    private var paint: Paint? = null
    private var bitmap: Bitmap? = null
    private var shader: BitmapShader? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    init {
        init()
    }

    private fun init() {
        scaleType = ScaleType.MATRIX
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
    }

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        shader = BitmapShader(bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint!!.setShader(shader)
        invalidate()
    }

    fun setMatrix(matrix: Matrix?) {
        this.imageMatrix?.set(matrix)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (bitmap == null) return

        // Save the canvas state
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // Draw the bitmap with the matrix transformation
        canvas.drawBitmap(bitmap!!, matrix!!, null)

        // Create a gradient for the fade effect
        val gradient = LinearGradient(
            0f, (height - 200).toFloat(), 0f, height.toFloat(),
            -0x1, 0x00FFFFFF, Shader.TileMode.CLAMP
        )
        paint!!.setShader(gradient)

        // Draw the gradient on top of the bitmap
        canvas.drawRect(0f, (height - 200).toFloat(), width.toFloat(), height.toFloat(), paint!!)

        // Restore the canvas state
        canvas.restoreToCount(saveCount)
    }
}
