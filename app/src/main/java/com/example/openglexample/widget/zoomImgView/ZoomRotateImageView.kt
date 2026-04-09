package com.example.openglexample.widget.zoomImgView

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewTreeObserver
import kotlin.math.sqrt

class ZoomRotateImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : android.support.v7.widget.AppCompatImageView(context, attrs, defStyle) {

    private val mImageMatrix = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val mMatrixValues = FloatArray(9)

    private var mMidPntX = 0f
    private var mMidPntY = 0f

    private val lastTouch = PointF()
    private var startRotation = 0f
    private val minScale = 0.5f
    private val maxScale = 5f

    // 震动器
    private var mVibrator: Vibrator? = null

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = mImageMatrix
        setBackgroundColor(Color.BLUE) // 设置背景为白色
//        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 确保只调用一次
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                // 在这里设置需要的属性
                setInitialProperties()
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var sumX = 0f
        var sumY = 0f
        for (i in 0 until event.pointerCount) {
            sumX += event.getX(i)
            sumY += event.getY(i)
        }
        // 得到多个触摸点的x与y均值
        mMidPntX = sumX / event.pointerCount
        mMidPntY = sumY / event.pointerCount

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mImageMatrix.getValues(mMatrixValues)
                lastTouch.set(event.x, event.y)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                startRotation = rotation(event)
            }

            MotionEvent.ACTION_MOVE -> {
//                if (event.pointerCount == 1) {
//                    val dx = event.x - lastTouch.x
//                    val dy = event.y - lastTouch.y
//                    mImageMatrix.postTranslate(dx, dy)
//                    lastTouch.set(event.x, event.y)
//                } else
                    if (event.pointerCount == 2) {
                    val rotation = rotation(event) - startRotation
                    mImageMatrix.postRotate(rotation, width / 2f, height / 2f)
                    startRotation = rotation(event) // 更新起始旋转角度

//                        checkRotateSnapToHorizontal(rotation) // 检查是否需要吸附到水平线
                }
                imageMatrix = mImageMatrix
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                // 事件结束后重新约束图像到边界
//                    constrainToBounds()
            }
        }
            scaleDetector.onTouchEvent(event)
//            constrainToBounds()
        return true
    }

    private fun setInitialProperties() {
        // 设置首次布局完成后的属性
        // 例如：设置图片居中显示
        showBitmapInCenter()
    }

    private fun showBitmapInCenter() {
        // 拿到图片的宽和高
        val dw = drawable?.intrinsicWidth?.toFloat() ?: 0f
        val dh = drawable?.intrinsicHeight?.toFloat() ?: 0f

        // 图片移动至屏幕中心
        val deltaX = (width - dw) / 2
        val deltaY = (height - dh) / 2

        mImageMatrix.postTranslate(deltaX, deltaY)
        imageMatrix = mImageMatrix
    }

    private fun constrainToBounds() {
//        imgMatrix.getValues(matrixValues)
//        val currentScale = matrixValues[Matrix.MSCALE_X]
//
//        // 限制缩放范围
//        if (currentScale < minScale) {
//            imgMatrix.setScale(minScale, minScale)
//        } else if (currentScale > maxScale) {
//            imgMatrix.setScale(maxScale, maxScale)
//        }
//
//        val transX = matrixValues[Matrix.MTRANS_X]
//        val transY = matrixValues[Matrix.MTRANS_Y]
//
//        val viewWidth = width.toFloat()
//        val viewHeight = height.toFloat()
//
//        val drawableWidth = drawable?.intrinsicWidth?.toFloat() ?: 0f
//        val drawableHeight = drawable?.intrinsicHeight?.toFloat() ?: 0f
//
//        val currentWidth = drawableWidth * currentScale
//        val currentHeight = drawableHeight * currentScale
//
//        var deltaX = 0f
//        var deltaY = 0f
//
//        if (currentWidth > viewWidth) {
//            if (transX > 0) deltaX = -transX
//            else if (transX + currentWidth < viewWidth) deltaX = viewWidth - (transX + currentWidth)
//        } else {
//            deltaX = (viewWidth - currentWidth) / 2 - transX
//        }
//
//        if (currentHeight > viewHeight) {
//            if (transY > 0) deltaY = -transY
//            else if (transY + currentHeight < viewHeight) deltaY = viewHeight - (transY + currentHeight)
//        } else {
//            deltaY = (viewHeight - currentHeight) / 2 - transY
//        }
//
//        imgMatrix.postTranslate(deltaX, deltaY)
//        imageMatrix = imgMatrix
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        return Math.toDegrees(Math.atan2(deltaY, deltaX)).toFloat()
    }

    private fun checkRotateSnapToHorizontal(rotation: Float) {
        val degrees = rotation % 360
        val snapThreshold = 5

        if (kotlin.math.abs(degrees) < snapThreshold ||
            kotlin.math.abs(degrees - 90) < snapThreshold ||
            kotlin.math.abs(degrees - 180) < snapThreshold ||
            kotlin.math.abs(degrees - 270) < snapThreshold ||
            kotlin.math.abs(degrees - 360) < snapThreshold
        ) {

            mVibrator?.let {
                it.cancel()
                if (it.hasVibrator()) {
                    it.vibrate(50)
                }
            }

            val snappedAngle = when {
                kotlin.math.abs(degrees) < snapThreshold -> 0f
                kotlin.math.abs(degrees - 90) < snapThreshold -> 90f
                kotlin.math.abs(degrees - 180) < snapThreshold -> 180f
                kotlin.math.abs(degrees - 270) < snapThreshold -> 270f
                kotlin.math.abs(degrees - 360) < snapThreshold -> 360f
                else -> degrees
            }

            mImageMatrix.postRotate(snappedAngle - degrees, mMidPntX, mMidPntY)
            invalidate()
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val currentScale = getScale()
            if (currentScale * scaleFactor > maxScale) {
                scaleFactor = maxScale / currentScale
            }

            mImageMatrix.postScale(detector.scaleFactor, detector.scaleFactor, mMidPntX, mMidPntX)
            constrainToBounds()
            return true
        }
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    fun getScale(): Float {
        mImageMatrix.getValues(mMatrixValues)
        // calculate real scale
        val scalex: Float = mMatrixValues[Matrix.MSCALE_X]
        val skewy: Float = mMatrixValues[Matrix.MSKEW_Y]
        val rScale = sqrt((scalex * scalex + skewy * skewy).toDouble()).toFloat()
        return rScale
    }
}
