package com.example.openglexample.widget.zoomImgView

import android.view.MotionEvent
import kotlin.math.atan2

/**
 * 旋转手势检测器类，用于检测和处理多点触控下的旋转手势。
 *
 * @param mListener 旋转手势监听器接口的实例，用于在检测到旋转手势时回调。
 */
class RotationGestureDetector(private val mListener: OnRotationGestureListener?) {

    companion object {
        private const val INVALID_POINTER_INDEX = -1
    }

    // fX, fY 表示第一个触摸点的初始位置坐标
    private var fX = 0f
    private var fY = 0f

    // sX, sY 表示第二个触摸点的初始位置坐标
    private var sX = 0f
    private var sY = 0f

    private var mPointerIndex1: Int
    private var mPointerIndex2: Int

    /**
     * 当前旋转的角度
     */
    var angle: Float = 0f
        private set // 外部只读，内部可写

    // 标记是否为第一次触摸，用于在计算旋转角度时进行初始化
    private var mIsFirstTouch = false

    init {
        mPointerIndex1 = INVALID_POINTER_INDEX
        mPointerIndex2 = INVALID_POINTER_INDEX
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                sX = event.x
                sY = event.y
                mPointerIndex1 = event.findPointerIndex(event.getPointerId(0))
                angle = 0f
                mIsFirstTouch = true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                fX = event.x
                fY = event.y
                mPointerIndex2 = event.findPointerIndex(event.getPointerId(event.actionIndex))
                angle = 0f
                mIsFirstTouch = true
            }

            MotionEvent.ACTION_MOVE -> if (mPointerIndex1 != INVALID_POINTER_INDEX && mPointerIndex2 != INVALID_POINTER_INDEX && event.pointerCount > mPointerIndex2) {
                val nsX = event.getX(mPointerIndex1)
                val nsY = event.getY(mPointerIndex1)
                val nfX = event.getX(mPointerIndex2)
                val nfY = event.getY(mPointerIndex2)

                if (mIsFirstTouch) {
                    angle = 0f
                    mIsFirstTouch = false
                } else {
                    calculateAngleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY)
                }

                mListener?.onRotation(this)
                fX = nfX
                fY = nfY
                sX = nsX
                sY = nsY
            }

            MotionEvent.ACTION_UP -> mPointerIndex1 = INVALID_POINTER_INDEX
            MotionEvent.ACTION_POINTER_UP -> mPointerIndex2 = INVALID_POINTER_INDEX
        }
        return true
    }

    /**
     * 计算两条线之间的角度差。
     *
     * @param fx1 第一条线的起点x坐标。
     * @param fy1 第一条线的起点y坐标。
     * @param fx2 第一条线的终点x坐标。
     * @param fy2 第一条线的终点y坐标。
     * @param sx1 第二条线的起点x坐标。
     * @param sy1 第二条线的起点y坐标。
     * @param sx2 第二条线的终点x坐标。
     * @param sy2 第二条线的终点y坐标。
     * @return 两条线之间的角度差。
     *
     * 使用atan2函数计算每条线的角度，然后调用calculateAngleDelta方法计算这两个角度之间的差值。
     * 这个差值表示了用户旋转手势的角度变化量，用于旋转图像或视图等。
     */
    private fun calculateAngleBetweenLines(
        fx1: Float, fy1: Float, fx2: Float, fy2: Float,
        sx1: Float, sy1: Float, sx2: Float, sy2: Float
    ): Float {
        return calculateAngleDelta(
            Math.toDegrees(atan2((fy1 - fy2).toDouble(), (fx1 - fx2).toDouble()).toFloat().toDouble()).toFloat(),
            Math.toDegrees(atan2((sy1 - sy2).toDouble(), (sx1 - sx2).toDouble()).toFloat().toDouble()).toFloat()
        )
    }

    /**
     * 计算两个角度之间的最小差值。
     *
     * @param angleFrom 初始角度。
     * @param angleTo 目标角度。
     * @return 返回从初始角度到目标角度的最短旋转距离（角度差），范围在-180到180之间。
     * 用于确定在旋转手势中，两次触摸事件之间角度的变化量。
     */
    private fun calculateAngleDelta(angleFrom: Float, angleTo: Float): Float {
        angle = angleTo % 360.0f - angleFrom % 360.0f

        if (angle < -180.0f) {
            angle += 360.0f
        } else if (angle > 180.0f) {
            angle -= 360.0f
        }

        return angle
    }

    open class SimpleOnRotationGestureListener : OnRotationGestureListener {
        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            return false
        }
    }

    interface OnRotationGestureListener {
        fun onRotation(rotationDetector: RotationGestureDetector): Boolean
    }
}