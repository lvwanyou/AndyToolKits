package com.dianping.video.view.gestureimgview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.lwy.andytoolkits.activity.sub.ImageFusionTest3Activity.Companion.MAX_SEEK_VALUE
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * 自定义手势操作图片视图类，继承自TransformDPImageView。
 * 支持通过手势进行图片的缩放、旋转和平移操作。
 *
 * Created by lvwanyou on 2024/7/3.
 */
class BlendGestureImageView5 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : DPTransformImageView(context, attrs, defStyle) {
    companion object {
        // 定义误差容忍度
        const val EPSILON: Float = 1e-3f

        // 定义平移到边界吸附的距离阈值, 单位: px
        private const val BOUNDS_EPSILON = 10

        // 定义震动时长, 单位: ms
        const val VIBRATE_TIME: Long = 50

        // 定义吸附的角度阈值
        const val SNAP_ANGLE_THRESHOLD: Int = 3

        /**
         * 接受表示矩形四个角的二维坐标数组，并返回包含这些角坐标的最小矩形。
         *
         * @param array 二维坐标数组
         * @return 包含坐标的最小矩形
         */
        private fun trapToRect(array: FloatArray): RectF {
            val r = RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
            var i = 1
            while (i < array.size) {
                val x = (array[i - 1] * 10).roundToInt() / 10f
                val y = (array[i] * 10).roundToInt() / 10f
                r.left = if (x < r.left) x else r.left
                r.top = if (y < r.top) y else r.top
                r.right = if (x > r.right) x else r.right
                r.bottom = if (y > r.bottom) y else r.bottom
                i += 2
            }

            // 对矩形的边界进行排序，确保矩形 r 的左边界小于等于右边界，上边界小于等于下边界
            r.sort()
            return r
        }
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
    var isRotateEnabled: Boolean = true

    // 是否启用缩放手势功能，默认为true
    var isScaleEnabled: Boolean = true

    // 是否启用通用手势功能（如平移、双击等），默认为true
    var isGestureEnabled: Boolean = true

    // 震动器
    private var mVibrator: Vibrator? = null
    var isDragging: Boolean = false
    private var drawFilter: PaintFlagsDrawFilter? = null

    init {
        setupGestureListeners()
//        setBackgroundColor(Color.TRANSPARENT) // 设置背景为透明

//        // 旋转和缩放手势在外部响应。ps.因为组件可能会存在较小的宽高，内部响应复杂手势效果不好
//        isScaleEnabled = false
//        isRotateEnabled = false

//        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        mVibrator = null
        drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2
            mMidPntY = (event.getY(0) + event.getY(1)) / 2
        }
//        if (event.getActionMasked() == MotionEvent.ACTION_UP && isDragging) {
//            mOuterGestureListener?.onDragEnd(event)
//        }

        if (isGestureEnabled) {
            mGestureDetector?.onTouchEvent(event)
        }

        if (isScaleEnabled) {
            mScaleDetector?.onTouchEvent(event)
        }

        if (isRotateEnabled) {
            mRotateDetector?.onTouchEvent(event)
        }

        return true
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val density = resources.displayMetrics.density

    private var baseHeight: Int = 0
    private var baseMarginTop: Int = 0
    private var baseWidth: Int = 0
    private var baseMarginLeft: Int = 0

    val max_value = 200

    var blendIntensity: Int = 0
        set(value) {
            field = value
            //  此处不能简写：视图并不会自动监听 LayoutParams 对象的改变，为了让视图知道 LayoutParams 对象的属性已经改变，需要调用 view.setLayoutParams(layoutParams) 方法，这个方法会让视图重新读取 LayoutParams 对象的所有属性，并根据这些属性来更新视图的显示
            val layoutParams = this.layoutParams
            layoutParams.height = baseHeight + field
            (layoutParams as ViewGroup.MarginLayoutParams).topMargin = baseMarginTop - blendIntensity
            layoutParams.width = baseWidth + field
            (layoutParams as ViewGroup.MarginLayoutParams).leftMargin = baseMarginLeft - blendIntensity
            this.layoutParams = layoutParams
            this.showImageWithCenterCrop(10 + blendIntensity / 2, 10 + blendIntensity / 2)
        }

    init {
        super.init()
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                this@BlendGestureImageView5.baseHeight = height
                this@BlendGestureImageView5.baseMarginTop = (this@BlendGestureImageView5.layoutParams as ViewGroup.MarginLayoutParams).topMargin

                this@BlendGestureImageView5.baseWidth = width
                this@BlendGestureImageView5.baseMarginLeft = (this@BlendGestureImageView5.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
            }
        })
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
//        // ps: 这样的效果是加到了 bitmap 上而不是 组件的底部
//        val drawable = drawable ?: return
//        val saveCount = canvas?.save()
//        canvas?.concat(imageMatrix)
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val tempCanvas = Canvas(bitmap)
//        drawable.setBounds(0, 0, width, height)
//        drawable.draw(tempCanvas)
//        val gradientHeight = 50 * density
//        // 创建一个线性渐变
//        val shader = LinearGradient(
//            0f, 0f, // 起始点
//            0f, gradientHeight, // 终止点（从底部到顶部50dp）
//            Color.TRANSPARENT, // 起始颜色（透明）
//            Color.WHITE, // 终止颜色（黑色）
//            Shader.TileMode.CLAMP
//        )
//
//        // 应用渐变到画笔
//        gradientPaint.shader = shader
//        // 绘制渐变矩形到临时画布上
//        tempCanvas.drawRect(0f, gradientHeight, width.toFloat(), 0.toFloat(), gradientPaint)
//        // 将临时位图绘制到实际画布上
//        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
//        // 恢复画布状态
//        canvas?.restoreToCount(saveCount!!)


        //        val saveCount = canvas?.save()
        // 计算终点透明度
        val gradientHeight = blendIntensity.toFloat()

        val maxGradientHeight = MAX_SEEK_VALUE // 假设100为gradientHeight的最大值，可根据实际情况调整
        val minAlpha = (255 * 0).toInt() // 设置最小透明度为128，即50%的透明度，防止太透明
        val alpha = ((1 - (gradientHeight / maxGradientHeight.toFloat())) * (255 - minAlpha) + minAlpha).toInt().coerceIn(minAlpha, 255)
        val startColor = Color.argb(alpha, 255, 255, 255) // 根据计算的透明度创建起始颜色
        val endColor = Color.argb(0, 255, 255, 255) // 根据计算的透明度和白色值创建结束颜色

        canvas?.drawFilter = drawFilter
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap)

        super.onDraw(tempCanvas)
        // 创建一个线性渐变
        val shader = LinearGradient(
            0f, gradientHeight.toFloat(), // 起始点
            0f, 0f, // 终止点（从底部到顶部50dp）
            Color.WHITE, // 起始颜色
            endColor, // 终止颜色
            Shader.TileMode.CLAMP
        )
        // 应用渐变到画笔
        gradientPaint.shader = shader
        // 绘制渐变矩形到临时画布上
//        tempCanvas.drawRect(0f, gradientHeight.toFloat(), width.toFloat(), 0f, gradientPaint)
        tempCanvas.drawRect(0f, 0f, width.toFloat(), gradientHeight.toFloat(), gradientPaint)

        val shader2 = LinearGradient(
            width - gradientHeight.toFloat(), 0f, // 起始点
            width.toFloat(), 0f, // 终止点（从底部到顶部50dp）
            Color.WHITE, // 起始颜色
            endColor, // 终止颜色
            Shader.TileMode.CLAMP
        )
        // 应用渐变到画笔
        gradientPaint.shader = shader2
        // 绘制渐变矩形到临时画布上
        tempCanvas.drawRect(width - gradientHeight.toFloat(), 0f, width.toFloat(), height.toFloat(), gradientPaint)

        // 将临时位图绘制到实际画布上‘
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)

//        canvas?.restoreToCount(saveCount!!)
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 处理缩放手势
            postScale(detector.scaleFactor, mMidPntX, mMidPntY)
//            postScale(detector.scaleFactor, mCurrentImageCenter[0], mCurrentImageCenter[1])
            return true
        }
    }

    private enum class Axis {
        X, Y
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        // 定义一个布尔数组，用于标记X轴和Y轴是否已经触发过平移吸附震动，初始值为false，表示未触发
        private var hasVibratedTranslate: BooleanArray = booleanArrayOf(false, false)

        // 定义一个浮点数组，用于记录X轴和Y轴的总位移量，初始值为0f，用于处理平移操作时的累积位移
        private var totalDeltaTranslate: FloatArray = floatArrayOf(0f, 0f)

        /**
         * 根据指定的轴（X轴或Y轴），在必要时触发震动反馈。
         * 当用户的平移操作使得图片接近边界时，此方法会调用震动器以提供物理反馈，增强用户体验。
         *
         * @param mAxis 指定的轴（X轴或Y轴），用于判断是哪个方向的平移操作。
         */
        private fun vibrateIfNeeded(mAxis: Axis) {
            val axis = mAxis.ordinal
            if (axis < hasVibratedTranslate.size && !hasVibratedTranslate[axis]) {
                mVibrator?.vibrate(VIBRATE_TIME)
                hasVibratedTranslate[axis] = true
            }
        }

        /**
         * 处理轴向平移操作。
         * 根据传入的轴（X轴或Y轴）和平移距离，执行相应的平移操作; 拖动图片调整位置时，靠近边界增加震动吸附
         *
         * @param mAxis 指定的轴（X轴或Y轴），表示平移操作的方向。
         * @param distance 平移的距离，正值表示向右或向下平移，负值表示向左或向上平移。
         */
        private fun handleAxisTranslation(mAxis: Axis, distance: Float) {
            val axis = mAxis.ordinal
            // 从边界吸附状态平移到其他位置，累计的平移距离超过阈值时，解除吸附并继续平移
            if (axis < hasVibratedTranslate.size && hasVibratedTranslate[axis]) {
                totalDeltaTranslate[axis] += distance
                if (abs(totalDeltaTranslate[axis]) > BOUNDS_EPSILON) {
                    hasVibratedTranslate[axis] = false

                    isDragging = true
                    postTranslateWthAxis(mAxis, -totalDeltaTranslate[axis])
                    totalDeltaTranslate[axis] = 0f // 重置累计平移距离
                }
            } else {
                isDragging = true
                postTranslateWthAxis(mAxis, -distance)

                // 判断当前角度是否接近90度的整数倍，用于确定是否为水平或垂直方向。若当前角度与最近的90度整数倍的差值小于误差容忍度，则认为是水平或垂直方向。
                val isAngleCloseToOrthogonal = abs(currentAngle - 90.0 * round(currentAngle / 90.0)) < EPSILON
                if (isAngleCloseToOrthogonal) {
                    // 根据轴向，获取图片和视图的尺寸，以及当前的平移值
                    var imageViewValue = 0
                    val currentBitmapRect: RectF = trapToRect(mCurrentImageCorners)
                    if (currentBitmapRect.left == Float.POSITIVE_INFINITY && currentBitmapRect.top == Float.POSITIVE_INFINITY) {
                        return
                    }
                    var bitmapStartValue: Float = BOUNDS_EPSILON * 1f
                    var bitmapEndValue: Float = BOUNDS_EPSILON * 1f
                    when (mAxis) {
                        Axis.X -> {
                            imageViewValue = this@BlendGestureImageView5.width
                            bitmapStartValue = currentBitmapRect.left
                            bitmapEndValue = currentBitmapRect.right
                        }

                        Axis.Y -> {
                            imageViewValue = this@BlendGestureImageView5.height
                            bitmapStartValue = currentBitmapRect.top
                            bitmapEndValue = currentBitmapRect.bottom
                        }
                    }

                    // 判断平移是否接近边界，如果是，则触发震动反馈，并进行相应的平移调整
                    when {
                        bitmapStartValue == Float.POSITIVE_INFINITY && bitmapEndValue == Float.POSITIVE_INFINITY -> return

                        abs(bitmapStartValue) < BOUNDS_EPSILON -> {
                            vibrateIfNeeded(mAxis)
                            totalDeltaTranslate[mAxis.ordinal] = 0f
                            postTranslateWthAxis(mAxis, -bitmapStartValue)
                        }

                        abs(bitmapEndValue - imageViewValue) < BOUNDS_EPSILON -> {
                            vibrateIfNeeded(mAxis)
                            totalDeltaTranslate[mAxis.ordinal] = 0f
                            postTranslateWthAxis(mAxis, -(bitmapEndValue - imageViewValue))
                        }
                    }
                }
            }
        }

        private fun postTranslateWthAxis(mAxis: Axis, deltaDistance: Float) {
            when (mAxis) {
                Axis.X -> postTranslate(deltaDistance, 0f)
                Axis.Y -> postTranslate(0f, deltaDistance)
            }
        }

        /**
         * e1和e2分别代表滑动动作的起始事件和当前事件。e1是手指刚接触屏幕时的事件，e2是手指当前的位置事件。
         * distanceX 和 distanceY 分别表示从上次滑动到这次滑动，X轴和Y轴上的距离差。
         */
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // 判断滑动是否超过了当前组件
            val x = e2.x
            val y = e2.y
            if (x < 0 || x > width || y < 0 || y > height) {
                isDragging = false
                mOuterGestureListener?.onScrollBeyondBorder(e1, e2, distanceX, distanceY)
            }

            handleAxisTranslation(Axis.X, distanceX)
            handleAxisTranslation(Axis.Y, distanceY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            mOuterGestureListener?.onClick(e)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            // 处理长按手势
            mOuterGestureListener?.onLongPress(e)
        }
    }

    private inner class RotateListener : RotationGestureDetector.SimpleOnRotationGestureListener() {
        private var totalDeltaAngle = 0f
        private val threshold = SNAP_ANGLE_THRESHOLD  // 设置吸附的角度阈值

        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            // 处理旋转手势
            var deltaAngle = rotationDetector.angle

            // 从水平或垂直线旋转到其他角度的时候时，累计的旋转角度超过阈值时，解除吸附并继续旋转
            if (abs(currentAngle - 90.0 * round(currentAngle / 90.0)) < EPSILON) {
                totalDeltaAngle += deltaAngle
                if (abs(totalDeltaAngle) > threshold) {
                    deltaAngle = totalDeltaAngle

                    postRotate(deltaAngle, mMidPntX, mMidPntY)
//                    postRotate(deltaAngle, mCurrentImageCenter[0], mCurrentImageCenter[1])
                }
            } else {
                // 旋转至水平或垂直线时，设置吸附和震动效果
                val normalizedAngle = (((currentAngle + deltaAngle) % 360) + 360) % 360  // Normalize to [0,360)
                val deltaMultipleOf90 = normalizedAngle - 90.0 * round(normalizedAngle / 90.0)
                val isMultipleOf90 = abs(deltaMultipleOf90) < threshold
                if (isMultipleOf90) {
                    totalDeltaAngle = 0f
                    // 将角度调整到最近的90度的倍数
                    deltaAngle -= deltaMultipleOf90.toFloat()

                    // 震动
                    mVibrator?.vibrate(VIBRATE_TIME)
                }

                postRotate(deltaAngle, mMidPntX, mMidPntY)
//                postRotate(deltaAngle, mCurrentImageCenter[0], mCurrentImageCenter[1])
            }
            return true
        }
    }

    var mOuterGestureListener: OuterGestureListener? = null

    /**
     * 滑动手势变化通知的接口。
     */
    interface OuterGestureListener {
        /**
         * 处理滑动手势事件：滑动超出控件边界时，该方法被调用。
         *
         * @param e1 滑动开始时的事件对象。
         * @param e2 当前滑动事件的对象。
         * @param distanceX 水平方向的滑动距离。
         * @param distanceY 垂直方向的滑动距离。
         */
        fun onScrollBeyondBorder(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float)

        /**
         * 处理长按手势
         */
        fun onLongPress(e: MotionEvent)

        /**
         *  处理点击事件
         */
        fun onClick(e: MotionEvent)

        /**
         *  处理平移结束
         */
        fun onDragEnd(e: MotionEvent)
    }
}