package com.example.openglexample.widget.rulerView.v2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Vibrator
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * Created by lvwanyou on 2024/2/28.
 * 详细说明
 *
 *
 * 作用:水平进度滚轮视图
 *
 *
 * 组件归属:
 *
 *
 * 页面使用:写笔记+写评价中的图片裁剪
 */
@SuppressLint("ClickableViewAccessibility")
class RulerViewReal @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    companion object {
        private const val TAG = "HorizontalProgressWheelView"
    }

    /**
     * 尺子画笔
     */
    private var mProgressSmallDialPaint: Paint? = null
    private var mProgressMediumDialPaint: Paint? = null

    /**
     * 刻度值画笔
     */
    private var mTextPaint: TextPaint? = null

    /**
     * 当前选中的真实刻度index
     */
    private var mSelectedIndex = 0

    /**
     * 数字的颜色
     */
    private var mTextColor = Color.WHITE

    /**
     * 尺子的高度
     */
    private var mRulerHeight = 0

    /**
     * 尺子的宽度
     */
    private var mRulerWidth = 0

    /**
     * 待展现的所有刻度值
     */
    private val mTextList: List<String>? = null

    /**
     * 能够滚动的最大距离（尺子（可见）的宽度的一半,in px）
     */
    private var mMaxOverScrollDistance = 0f

    /**
     * 能够滚动的最大刻度数量(尺子（可见）的刻度数量的一半)
     */
    private var mViewScopeSize = 0

    /**
     * 内容的长度（in px）
     */
    private var mContentLength = 0f

    /**
     * 是否快速滑动
     */
    private var mFling = false

    /**
     * 尺子的最大值与最小值
     */
    private var mMaxValue = 0f
    private var mMinValue = 0f

    /**
     * 相邻刻度间代表的刻度值
     */
    private var mIntervalValue = 1f

    /**
     * 相邻刻度间的距离(in px)
     */
    private var mIntervalDistance = 0f

    /**
     * 尺子上所有的刻度值的总数（包括暂时没有显示出来的）
     */
    private var mRulerCount = 0

    /**
     * 刻度值的字体大小
     */
    private var mTextSize = 0f

    /**
     * 刻度值小数点后保留多少位：支持0~3位
     */
    private var mRetainLength = 0

    /**
     * 是否单独处理整中刻度线的特殊刻度
     */
    private var mIsDivideByMediumDial = true

    /**
     * 中刻度线的特殊刻度的高度
     */
    private var mDivideByMediumDialHeight = 0f

    /**
     * 中刻度线的特殊刻度的宽度
     */
    private var mDivideByMediumDialWidth = 0f

    /**
     * 是否单独处理小刻度线的特殊刻度
     */
    private var mIsDivideBySmallDial = false

    /**
     * 小刻度线的特殊刻度的高度
     */
    private var mDivideBySmallDialHeight = 0f

    /**
     * 小刻度线的特殊刻度的宽度
     */
    private var mDivideBySmallDialWidth = 0f

    private var mDrawCenterY = 0f

    /**
     * 刻度值与尺子最长刻度之间的距离
     */
    private var mTextBaseLineDistance = 0f
    private var onValueChangeListener: OnValueChangeListener? = null
    private var mScroller: Scroller? = null

    //手势
    private var mGestureDetectorCompat: GestureDetectorCompat? = null

    // 震动器
    private var mVibrator: Vibrator? = null

    interface OnValueChangeListener {
        /**
         * 当水平进度滚轮视图的值发生变化时调用的方法。
         *
         * @param view 当前的水平进度滚轮视图
         * @param selectedValue 当前选中的值
         * @param deltaOffsetIndex 索引的偏移增量
         */
        fun onChange(view: RulerViewReal?, selectedValue: Int, deltaOffsetIndex: Float)
    }

    init {
        init()
    }

    /**
     * 初始化方法，用于初始化视图和画笔
     */
    private fun init() {
        val dm = resources.displayMetrics

        mDrawCenterY = dm.density * 8

        // 设置小刻度尺的的宽度、高度和颜色
        mDivideBySmallDialWidth = dm.density * 1
        mDivideBySmallDialHeight = dm.density * 7
        val mDivideBySmallDialColor = Color.parseColor("#7AFFFFFF")

        // 设置中刻度尺的的宽度、高度和颜色
        mDivideByMediumDialWidth = dm.density * 2
        mDivideByMediumDialHeight = dm.density * 10
        val mDivideByMediumDialColor = Color.parseColor("#B3FFFFFF")
        mIntervalDistance = dm.density * 8
        mTextSize = dm.scaledDensity * 12
        mTextColor = Color.parseColor("#FFFFFFFF")
        mIntervalValue = 1f
        mMaxValue = 45f
        mMinValue = -45f
        mRetainLength = 0
        mIsDivideBySmallDial = true
        mIsDivideByMediumDial = true
        mTextBaseLineDistance = dm.density * 5
        checkRulerLineParam()
        calculateTotal()
        mGestureDetectorCompat = GestureDetectorCompat(context, this)
        mScroller = Scroller(context)

        // 初始化中间线画笔
        mProgressSmallDialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressSmallDialPaint?.strokeCap = Paint.Cap.ROUND
        mProgressSmallDialPaint?.strokeWidth = mDivideBySmallDialWidth
        mProgressSmallDialPaint?.color = mDivideBySmallDialColor

        // 初始化中等刻度画笔
        mProgressMediumDialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressMediumDialPaint?.strokeCap = Paint.Cap.ROUND
        mProgressMediumDialPaint?.strokeWidth = mDivideByMediumDialWidth
        mProgressMediumDialPaint?.color = mDivideByMediumDialColor
        mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint?.textAlign = Paint.Align.CENTER
        mTextPaint?.textSize = mTextSize
        setOriginSelectedIndex(mRulerCount / 2)

        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    }

    private fun checkRulerLineParam() {
        val heights = floatArrayOf(mDivideBySmallDialHeight, mDivideByMediumDialHeight)
        val widths = floatArrayOf(mDivideBySmallDialWidth, mDivideByMediumDialWidth)
        //从小到大排序
        for (i in heights.indices) {
            var heightTemp: Float
            var weightTemp: Float
            for (j in 0 until heights.size - i - 1) {
                if (heights[j] > heights[j + 1]) {
                    heightTemp = heights[j]
                    heights[j] = heights[j + 1]
                    heights[j + 1] = heightTemp
                }
                if (widths[j] > widths[j + 1]) {
                    weightTemp = widths[j]
                    widths[j] = widths[j + 1]
                    widths[j + 1] = weightTemp
                }
            }
        }
        mDivideBySmallDialHeight = heights[0]
        mDivideByMediumDialHeight = heights[1]
        mDivideBySmallDialWidth = widths[0]
        mDivideByMediumDialWidth = widths[1]
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            measureWidth(widthMeasureSpec),
            measureHeight(heightMeasureSpec)
        )
    }

    //测量宽度：处理MeasureSpec.UNSPECIFIED的情况
    private fun measureWidth(widthMeasureSpec: Int): Int {
        val measureMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureSize = MeasureSpec.getSize(widthMeasureSpec)

        //View的最小值与背景最小值两者中的最大值（宽度）
        var result = suggestedMinimumWidth
        when (measureMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> result = measureSize
            MeasureSpec.UNSPECIFIED -> {}
            else -> {}
        }
        return result
    }

    //测量高度
    private fun measureHeight(heightMeasure: Int): Int {
        val measureMode = MeasureSpec.getMode(heightMeasure)
        val measureSize = MeasureSpec.getSize(heightMeasure)
        var result: Int
        result = mTextSize.toInt() * 4
        when (measureMode) {
            MeasureSpec.EXACTLY -> result = Math.max(result, measureSize)
            MeasureSpec.AT_MOST -> result = Math.min(result, measureSize)
            MeasureSpec.UNSPECIFIED -> {}
            else -> {}
        }
        return result
    }

    /**
     * 当 View 的大小改变时调用
     *
     * @param w    新的 view 宽度
     * @param h    新的 view 宽度
     * @param oldw 旧的view 宽度
     * @param oldh 旧的view 高度
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            mRulerHeight = h
            mRulerWidth = w
            mMaxOverScrollDistance = w / 2f
            mContentLength = ((mMaxValue - mMinValue) / mIntervalValue
                    * mIntervalDistance)
            mViewScopeSize = Math.ceil(
                (mMaxOverScrollDistance
                        / mIntervalDistance).toDouble()
            ).toInt()
        }
    }

    private var mTextRect: Rect? = null
    private var mDecimalFormat: DecimalFormat? = null

    /**
     * 绘制方法，用于绘制刻度和中间线
     *
     * @param canvas 画布对象
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDecimalFormat == null) {
            mDecimalFormat = DecimalFormat("##0")
        }
        var start = mSelectedIndex - mViewScopeSize
        var end = mSelectedIndex + mViewScopeSize
        if (mSelectedIndex.toFloat() == mMaxValue) {
            end += mViewScopeSize
        } else if (mSelectedIndex.toFloat() == mMinValue) {
            start -= mViewScopeSize
        }

        // 控制下各个刻度的宽度
        if (mDivideByMediumDialWidth >= mIntervalDistance) {
            mDivideBySmallDialWidth = mIntervalDistance / 3
            mDivideByMediumDialWidth = mIntervalDistance / 2
        }

        //水平方向
        var x = start * mIntervalDistance
        //刻度线的最大高度
        val markHeight = mRulerHeight - mTextSize
        //控制下各个刻度的高度
        if (mDivideByMediumDialHeight + mTextBaseLineDistance > markHeight) {
            mDivideBySmallDialHeight = markHeight * 3 / 4
            mDivideByMediumDialHeight = markHeight
            mTextBaseLineDistance = 0f
        }

        //start 可能小于0
        for (i in start until end) {
            if (mRulerCount > 0 && i >= 0 && i < mRulerCount) {
                // 画线
                val remainderBy3 = i % 3
                val remainderBy5 = i % 5

                // 绘制中等刻度, 被15整除的刻度线
                if (mIsDivideByMediumDial && remainderBy3 == 0 && remainderBy5 == 0) {
                    mProgressMediumDialPaint?.let {
                        canvas.drawLine(
                            x, mDrawCenterY - mDivideByMediumDialHeight / 2.0f, x, mDrawCenterY + mDivideByMediumDialHeight / 2.0f,
                            it
                        )
                    }
                }
                // 绘制小刻度,被3整除的刻度线
                else if (mIsDivideBySmallDial && remainderBy3 == 0 && remainderBy5 != 0) {
                    mProgressSmallDialPaint?.let {
                        canvas.drawLine(
                            x, mDrawCenterY - mDivideBySmallDialHeight / 2.0f, x,
                            mDrawCenterY + mDivideBySmallDialHeight / 2.0f, it
                        )
                    }
                }
                if (mTextRect == null) {
                    mTextRect = Rect()
                }
                mTextPaint?.color = mTextColor
                if (mIsDivideByMediumDial && remainderBy3 == 0 && remainderBy5 == 0) {
                    val text: String = if (mTextList?.isNotEmpty() == true) {
                        val index = i / 10
                        if (index < mTextList.size) {
                            mTextList[index]
                        } else {
                            ""
                        }
                    } else {
                        mDecimalFormat?.format((i * mIntervalValue + mMinValue).toDouble()).toString()
                    }
                    mTextRect?.let { outer ->
                        mTextPaint?.getTextBounds(text, 0, text.length, outer)
                        //文本的下边缘线中心位置
                        mTextPaint?.let {
                            canvas.drawText(text, 0, text.length, x, mDrawCenterY + mDivideByMediumDialHeight / 2.0f + outer.height() + mTextBaseLineDistance, it)
                        }
                    }
                }
            }
            x += mIntervalDistance
        }
    }

    /**
     * 格式化刻度值
     */
    private fun format(value: Float): String {
        return format(mRetainLength, value)
    }

    private fun format(patternType: Int, value: Float): String {
        return try {
            when (patternType) {
                0 -> DecimalFormat("##0").format(value.toDouble())
                1 -> DecimalFormat("##0.0").format(value.toDouble())
                2 -> DecimalFormat("##0.00").format(value.toDouble())
                3 -> DecimalFormat("##0.000").format(value.toDouble())
                else -> DecimalFormat("##0.0").format(value.toDouble())
            }
        } catch (e: Exception) {
            "0"
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var resolve = mGestureDetectorCompat?.onTouchEvent(event)
        if (!mFling && MotionEvent.ACTION_UP == event.action) {
            adjustPosition()
            resolve = true
        }
        return resolve == true || super.onTouchEvent(event)
    }

    /**
     * 滑动时调整尺子,使其指向整格
     */
    private fun adjustPosition() {
        val scroll: Int = scrollX
        val distance = (mSelectedIndex * mIntervalDistance - scroll
                - mMaxOverScrollDistance)
        if (distance == 0f) {
            return
        }
        mScroller?.startScroll(scroll, 0, distance.toInt(), 0)
        postInvalidate()
    }

    @SuppressLint("LongLogTag")
    override fun computeScroll() {
        super.computeScroll()
        mScroller?.let {
            if (it.computeScrollOffset()) {
                scrollTo(it.currX, it.currY)

                android.util.Log.e(TAG, "computeScroll: onValueChange" );
                onValueChange(true, 2)
                invalidate()
            } else {
                //滑动完成后，调整下刻度位置，使其指向整格
                if (mFling) {
                    mFling = false
                    adjustPosition()
                }
            }
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        mScroller?.let {
            if (!it.isFinished) {
                it.forceFinished(false)
            }
        }
        mFling = false
        if (null != parent) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return true
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent, e2: MotionEvent, distanceX: Float,
        distanceY: Float
    ): Boolean {
        val scroll: Float
        var distance: Float
        scroll = scrollX.toFloat()
        distance = distanceX

        //不要越界
        if (scroll + distance <= -mMaxOverScrollDistance) {
            distance = -(scroll + mMaxOverScrollDistance).toInt().toFloat()
        } else if (scroll + distance >= mContentLength - mMaxOverScrollDistance) {
            distance = (mContentLength - mMaxOverScrollDistance - scroll).toInt().toFloat()
        }
        if (distance == 0f) {
            return true
        }
        scrollBy(distance.toInt(), 0)

//        Log.e("----****", "onScroll: distance" + distance )
        if (onScrollingListener != null) {
            this.onScrollingListener!!.onScroll(distance / mIntervalDistance)
        }

        onValueChange(true, 8)
        return true
    }

    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {
        val scroll: Float
        val velocity: Float
        scroll = scrollX.toFloat()
        velocity = velocityX
        if (scroll < -mMaxOverScrollDistance
            || scroll > mContentLength - mMaxOverScrollDistance
        ) {
            return false
        }
        mFling = true
        fling(-velocity.toInt() / 2)
        return true
    }

    private fun fling(velocity: Int) {
        mScroller?.fling(scrollX, 0, velocity, 0, -mMaxOverScrollDistance.toInt(), (mContentLength - mMaxOverScrollDistance).toInt(), 0, 0)

        //触发computeScroll
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * 上次滑动的偏移量
     * 用于记录上次滑动的位置偏移量，初始值为最大滑动距离
     */
    private var lastScrollOffset: Int = 0
    /**
     * 当值发生改变时调用的方法
     * 该方法用于调用监听接口，通知监听器数值发生了改变
     *
     * @param needVibrate 是否需要震动
     * @param vibrateTime 震动时长, 单位 ms
     */
    private fun onValueChange(needVibrate: Boolean = false, vibrateTime: Long = 5) {
        val offset: Int = (scrollX + mMaxOverScrollDistance).toInt()

//        Log.e("----*****", "onValueChange: " + (((scrollX * 1f + mMaxOverScrollDistance) / mIntervalDistance) * mIntervalValue + mMinValue) )

        var tempIndex = (offset / mIntervalDistance).roundToInt()
        tempIndex = clampSelectedIndex(tempIndex)
        mSelectedIndex = tempIndex
        if (onValueChangeListener != null) {
            val str = format(
                mSelectedIndex * mIntervalValue
                        + mMinValue
            )
            val mValue = str.toInt()
            onValueChangeListener?.onChange(
                this, mValue,
                calculateDelta(offset)
            )

            // 滑动到15°的倍数（即0°，±15°，±30°，±45°），手机有震感反馈: 开始震动5ms
            vibrateIfNeeded(mValue, needVibrate, vibrateTime)
        }
    }

    private fun calculateDelta(offset: Int): Float {
        if (lastScrollOffset == 0) lastScrollOffset = offset
        val currentOffset = offset / mIntervalDistance + mMinValue
        val lastOffset = lastScrollOffset / mIntervalDistance + mMinValue
        lastScrollOffset = offset

        return try {
            format(1, currentOffset).toFloat() - format(1, lastOffset).toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    private val MIN_CLICK_INTERVAL: Long = 20 // 设置防止快速点击的最小间隔时间，例如600毫秒
    private val mHandler: Handler = Handler()

    private fun vibrateIfNeeded(value: Int, needVibrate: Boolean = false, vibrateTime: Long = 5) {
        if (needVibrate && value % 3 == 0) {
            mVibrator?.let {
                mHandler.postDelayed({
                    it.cancel()
                    if (it.hasVibrator()) {
                        it.vibrate(vibrateTime)
                    }
                }, MIN_CLICK_INTERVAL)
            }
        }
    }

    /**
     * 保证selectedIndex不越界
     */
    private fun clampSelectedIndex(selectedIndex: Int): Int {
        return selectedIndex.coerceIn(0, mRulerCount - 1)
    }

    /**
     * 滑动到指定刻度
     * selectedIndex：以最小刻度计算的索引
     */
    private fun setOriginSelectedIndex(selectedIndex: Int) {
        mSelectedIndex = clampSelectedIndex(selectedIndex)
        post {
            val position = (mSelectedIndex * mIntervalDistance - mMaxOverScrollDistance).toInt()
            scrollTo(position, 0)
            onValueChange()
            invalidate()
        }
    }

    /**
     * 计算刻度值的总数
     */
    private fun calculateTotal() {
        mRulerCount = ((mMaxValue - mMinValue) / mIntervalValue).toInt() + 1
    }

    fun setOnValueChangeListener(onValueChangeListener: OnValueChangeListener?) {
        this.onValueChangeListener = onValueChangeListener
    }

    private var onScrollingListener: ScrollingListener? = null

    fun setScrollingListener(scrollingListener: ScrollingListener?) {
        this.onScrollingListener = scrollingListener
    }

    interface ScrollingListener {
        fun onScroll(delta: Float)
    }
}