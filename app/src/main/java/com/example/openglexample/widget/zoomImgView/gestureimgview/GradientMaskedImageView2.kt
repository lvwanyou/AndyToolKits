package com.example.openglexample.widget.zoomImgView

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class GradientMaskedImageView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return

        // 保存画布状态
        val saveCount = canvas.save()

        // 应用 ImageView 的矩阵变换
        canvas.concat(imageMatrix)

        // 创建一个渐变遮罩的位图
        val gradientBitmap = createGradientBitmap(width, height)

        // 创建一个临时位图和画布
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap)

        // 绘制原始位图
        drawable.draw(tempCanvas)

        // 将渐变遮罩绘制到画布上
        canvas.drawBitmap(gradientBitmap, 0f, 0f, gradientPaint)

        // 恢复画布状态
        canvas.restoreToCount(saveCount)
    }

    private fun createGradientBitmap(width: Int, height: Int): Bitmap {
        val gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gradientBitmap)

        // 创建一个线性渐变
        val shader = LinearGradient(
            0f, height.toFloat(), // 起始点
            0f, height - 200f, // 终止点（从底部到顶部200dp）
            Color.TRANSPARENT, // 起始颜色（透明）
            Color.BLACK, // 终止颜色（黑色）
            Shader.TileMode.CLAMP
        )

        // 应用渐变到画笔
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        }

        // 绘制渐变到临时画布上
        canvas.drawRect(0f, height - 200f, width.toFloat(), height.toFloat(), paint)

        return gradientBitmap
    }
}
