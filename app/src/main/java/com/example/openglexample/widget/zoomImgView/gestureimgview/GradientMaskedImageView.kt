package com.dianping.video.view.gestureimgview


import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class GradientMaskedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var gradientBitmap: Bitmap? = null
    private var gradientCanvas: Canvas? = null

    init {
        // 初始化渐变画笔
        gradientPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return
        val bitmap = (drawable as BitmapDrawable).bitmap

        // 创建一个临时位图和画布
        if (gradientBitmap == null || gradientBitmap!!.width != width || gradientBitmap!!.height != height) {
            gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            gradientCanvas = Canvas(gradientBitmap!!)
        }

        // 清空临时画布
        gradientCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // 绘制原始位图到临时画布上
        gradientCanvas!!.drawBitmap(bitmap, null, Rect(0, 0, width, height), paint)

        // 创建一个线性渐变
        val shader = LinearGradient(
            0f, height.toFloat(), // 起始点
            0f, height - 200f, // 终止点（从底部到顶部200dp）
            Color.TRANSPARENT, // 起始颜色（透明）
            Color.BLACK, // 终止颜色（黑色）
            Shader.TileMode.CLAMP
        )

        // 应用渐变到画笔
        gradientPaint.shader = shader

        // 绘制渐变到临时画布上
        gradientCanvas!!.drawRect(0f, height - 200f, width.toFloat(), height.toFloat(), gradientPaint)

        // 绘制临时位图到实际画布上
        canvas.drawBitmap(gradientBitmap!!, 0f, 0f, null)
    }
}
