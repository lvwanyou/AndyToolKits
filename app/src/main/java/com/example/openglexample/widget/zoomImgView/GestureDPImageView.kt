package com.example.openglexample.widget.zoomImgView

import android.content.Context
import android.graphics.Color
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import kotlin.math.abs
import kotlin.math.round

/**
 * 自定义手势操作图片视图类，继承自TransformDPImageView。
 * 支持通过手势进行图片的缩放、旋转和平移操作。
 */
class GestureDPImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : TransformDPImageView(context, attrs, defStyle) {
    companion object {
        // 定义误差容忍度
        private const val EPSILON: Float = 1e-3f
    }

    // 缩放手势检测器，用于处理用户的缩放手势
    private var mScaleDetector: ScaleGestureDetector? = null

    // 旋转手势检测器，用于处理用户的旋转手势
    private var mRotateDetector: RotationGestureDetector? = null

    // 通用手势检测器，用于处理除缩放和旋转之外的其他手势，如平移、双击等
    private var mGestureDetector: GestureDetector? = null

    // 用于记录两指触控时，触点中心的X和Y坐标。
    private var mMidPntX: Float = 0f
    private var mMidPntY: Float = 0f

    // 是否启用旋转手势功能，默认为true
    private var mIsRotateEnabled: Boolean = true

    // 是否启用缩放手势功能，默认为true
    private var mIsScaleEnabled: Boolean = true

    // 是否启用通用手势功能（如平移、双击等），默认为true
    private var mIsGestureEnabled: Boolean = true

    // 震动器
    private var mVibrator: Vibrator? = null

    init {
        setupGestureListeners()
        setBackgroundColor(Color.TRANSPARENT) // 设置背景为透明
        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        // todo 添加 bitmap 并且设置 bitmap 居中处理
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2
            mMidPntY = (event.getY(0) + event.getY(1)) / 2
        }

        if (mIsGestureEnabled) {
            mGestureDetector?.onTouchEvent(event)
        }

        if (mIsScaleEnabled) {
            mScaleDetector?.onTouchEvent(event)
        }

        if (mIsRotateEnabled) {
            mRotateDetector?.onTouchEvent(event)
        }

//        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
//            setImageToWrapCropBounds()
//        }
        return true
    }

    /**
     * 初始化手势监听器。
     * 负责创建和配置用于检测缩放、旋转和通用手势（如平移、双击等）的监听器。
     * 确保手势操作的功能能够正常工作。
     */
    private fun setupGestureListeners() {
        // 初始化手势检测器
        mGestureDetector = GestureDetector(context, GestureListener(), null, true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mRotateDetector = RotationGestureDetector(RotateListener())
    }

    fun setScaleEnabled(scaleEnabled: Boolean) {
        mIsScaleEnabled = scaleEnabled
    }

    fun isScaleEnabled(): Boolean {
        return mIsScaleEnabled
    }

    fun setRotateEnabled(rotateEnabled: Boolean) {
        mIsRotateEnabled = rotateEnabled
    }

    fun isRotateEnabled(): Boolean {
        return mIsRotateEnabled
    }

    fun setGestureEnabled(gestureEnabled: Boolean) {
        mIsGestureEnabled = gestureEnabled
    }

    fun isGestureEnabled(): Boolean {
        return mIsGestureEnabled
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 处理缩放手势
            postScale(detector.scaleFactor, mMidPntX, mMidPntY)
            return true
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // 处理双击手势
            return super.onDoubleTap(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // 处理滑动手势
            postTranslate(-distanceX, -distanceY)
            return true
        }
    }

    private inner class RotateListener : RotationGestureDetector.SimpleOnRotationGestureListener() {
        private var totalDeltaAngle = 0f
        private val threshold = 5  // 设置吸附的角度阈值

        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            // 处理旋转手势
            var deltaAngle = rotationDetector.angle

            // 【旋转】至水平线时，设置吸附和震动效果
            if (abs(currentAngle - 90.0 * round(currentAngle / 90.0)) < EPSILON) {
                totalDeltaAngle += deltaAngle
                if (abs(totalDeltaAngle) > threshold) {
                    deltaAngle = totalDeltaAngle

                    postRotate(deltaAngle, mMidPntX, mMidPntY)
                }
            } else {
                // 从水平线旋转到其他角度的时候时，累计的旋转角度超过阈值时，解除吸附并继续旋转
                val normalizedAngle = (((currentAngle + deltaAngle) % 360) + 360) % 360  // Normalize to [0,360)
                val deltaMultipleOf90 = normalizedAngle - 90.0 * round(normalizedAngle / 90.0)
                val isMultipleOf90 = abs(deltaMultipleOf90) < threshold
                if (isMultipleOf90) {
                    totalDeltaAngle = 0f
                    // 将角度调整到最近的90度的倍数
                    deltaAngle -= deltaMultipleOf90.toFloat()

                    // 震动
                    mVibrator?.vibrate(100)
                }

                postRotate(deltaAngle, mMidPntX, mMidPntY)
            }
            return true
        }
    }
}