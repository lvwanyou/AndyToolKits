package com.dianping.video.view.gestureimgview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.support.annotation.IntRange
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 提供设置图片、通过矩阵（移动、缩放、旋转）变换图片以及获取当前矩阵状态的基本逻辑。
 *
 * Created by lvwanyou on 2024/7/3.
 */
open class DPTransformImageView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatImageView(context, attrs, defStyle) {

    companion object {
        private const val TAG = "TransformDPImageView"

        private const val RECT_CORNER_POINTS_COORDS = 8
        private const val RECT_CENTER_POINT_COORDS = 2
        private const val MATRIX_VALUES_COUNT = 9

        fun getCenterFromRect(r: RectF): FloatArray {
            return floatArrayOf(r.centerX(), r.centerY())
        }

        /**
         * 获取表示矩形角点的2D坐标的浮点数组。
         * 浮点数组中角点的顺序是：
         * 0------->1
         * ^        |
         * |        |
         * |        v
         * 3<-------2
         *
         * @param r 要获取角点的矩形
         * @return 角点的浮点数组（8个浮点数）
         */
        fun getCornersFromRect(r: RectF): FloatArray {
            return floatArrayOf(
                r.left, r.top,
                r.right, r.top,
                r.right, r.bottom,
                r.left, r.bottom
            )
        }
    }

    protected val mCurrentImageCorners: FloatArray = FloatArray(RECT_CORNER_POINTS_COORDS)
    protected val mCurrentImageCenter: FloatArray = FloatArray(RECT_CENTER_POINT_COORDS)

    private val mMatrixValues = FloatArray(MATRIX_VALUES_COUNT)

    protected var mCurrentImageMatrix: Matrix = Matrix()

    protected var mTransformImageListener: TransformImageListener? = null

    private var mInitialImageCorners: FloatArray? = null
    private var mInitialImageCenter: FloatArray? = null

    /**
     * 最小缩放值，用于控制图片的最小缩放比例。
     */
    protected val minScale: Float = Float.MIN_VALUE

    /**
     * 最大缩放值，用于控制图片的最大缩放比例。
     */
    protected val maxScale: Float = Float.MAX_VALUE

    /**
     * 旋转和缩放变化通知的接口。
     */
    interface TransformImageListener {
        fun onLoadComplete()

        fun onRotate(currentAngle: Float)

        fun onScale(currentScale: Float)

        fun onTranslate(currentTranslate: FloatArray?)
    }

    init {
        init()
    }

    protected fun init() {
        scaleType = ScaleType.MATRIX
    }

    fun setTransformImageListener(listener: TransformImageListener) {
        this.mTransformImageListener = listener
    }

    /**
     * 添加 bitmap 并且设置 bitmap 居中类型为 CENTER_CROP
     */
    fun setImageBitmapAndShowCenterCropWhenLayout(bm: Bitmap?) {
        bm?.let {
            mCurrentImageMatrix.reset()
            showImageWithCenterCropWhenLayout()
            setImageBitmap(it)
            onImageLaidOut()
        }
    }

    fun showImageWithCenterCropWhenLayout() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                showImageWithCenterCrop()
            }
        })
    }

    fun showImageWithCenterCrop(deltaWidth: Int, deltaHeight: Int) {
        if (drawable == null) {
            return
        }

        // 获取 ImageView 和 bitmap 的宽高
        val imageViewWidth = width
        val imageViewHeight = height
        val bitmapWidth = (drawable?.intrinsicWidth?.toFloat() ?: 0f)
        val bitmapHeight = (drawable?.intrinsicHeight?.toFloat() ?: 0f)

        val deltaBitmapWidth = if ((bitmapWidth - deltaWidth) > 0) bitmapWidth - deltaWidth else bitmapWidth
        val deltaBitmapHeight = if ((bitmapHeight - deltaHeight) > 0) bitmapHeight - deltaHeight else bitmapHeight

        // 计算缩放比例
        val scale: Float
        var dx = 0f
        var dy = 0f
        if (bitmapWidth * imageViewHeight > imageViewWidth * bitmapHeight) {
            scale = imageViewHeight.toFloat() / deltaBitmapHeight
        } else {
            scale = imageViewWidth.toFloat() / deltaBitmapWidth
        }
        dx = (imageViewWidth - bitmapWidth * scale) * 0.5f
        dy = (imageViewHeight - bitmapHeight * scale) * 0.5f

        // 设置Matrix进行缩放和平移
        mCurrentImageMatrix.setScale(scale, scale)
        mCurrentImageMatrix.postTranslate(dx, dy)
        imageMatrix = mCurrentImageMatrix
    }

    fun showImageWithCenterCrop() {
        showImageWithCenterCrop(0, 0)
    }

    override fun setImageMatrix(matrix: Matrix) {
        super.setImageMatrix(matrix)
        mCurrentImageMatrix.set(matrix)
        updateCurrentImagePoints()
    }

    val currentTranslate: FloatArray
        get() = getMatrixTranslate(mCurrentImageMatrix)

    /**
     * 计算给定Matrix对象的平移量。
     *
     * @param matrix 要计算平移量的Matrix对象。
     * @return 返回一个包含X轴和Y轴平移量的float数组，其中，数组的第0个元素是X轴平移量，第1个元素是Y轴平移量。
     */
    fun getMatrixTranslate(matrix: Matrix): FloatArray {
        val translateX = getMatrixValue(matrix, Matrix.MTRANS_X)
        val translateY = getMatrixValue(matrix, Matrix.MTRANS_Y)
        return floatArrayOf(translateX, translateY)
    }

    val currentScale: Float
        /**
         * 获取当前图片的缩放值。
         *
         *
         * 此方法用于获取当前图片相对于原始尺寸的缩放比例。返回的浮点数表示缩放比例，
         * 其中1.0f表示图片处于原始尺寸，2.0f表示图片被放大到200%的尺寸，以此类推。
         *
         * @return 当前图片的缩放比例。
         */
        get() = getMatrixScale(mCurrentImageMatrix)

    /**
     * 计算给定Matrix对象的缩放值。
     *
     *
     * 此方法通过计算Matrix对象的MSCALE_X和MSKEW_Y值的平方和的平方根来得到当前图片的缩放比例。
     * 缩放比例为图片相对于其原始尺寸被放大或缩小了多少。
     *
     * @param matrix 要计算缩放值的Matrix对象。
     * @return 当前图片的缩放比例。
     */
    private fun getMatrixScale(matrix: Matrix): Float {
        return sqrt(getMatrixValue(matrix, Matrix.MSCALE_X).pow(2f) + getMatrixValue(matrix, Matrix.MSKEW_Y).pow(2f))
    }

    val currentAngle: Float
        /**
         * 获取当前图片的旋转角度。
         *
         * @return 当前图片的旋转角度，以度为单位。
         */
        get() = getMatrixAngle(mCurrentImageMatrix)

    /**
     * 计算给定Matrix对象的旋转角度。
     *
     * @param matrix 要计算旋转角度的Matrix对象。
     * @return 当前图片的旋转角度，以度为单位。
     */
    private fun getMatrixAngle(matrix: Matrix): Float {
        return -(atan2(
            getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(),
            getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()
        ) * (180 / Math.PI)).toFloat()
    }

    /**
     * 对当前图片进行平移操作。
     *
     * @param deltaX - 水平移动的距离，正值向右移动，负值向左移动。
     * @param deltaY - 垂直移动的距离，正值向下移动，负值向上移动。
     */
    fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            mCurrentImageMatrix.postTranslate(deltaX, deltaY)
            imageMatrix = mCurrentImageMatrix
            mTransformImageListener?.onTranslate(getMatrixTranslate(mCurrentImageMatrix))
        }
    }

    /**
     * 对当前图片进行缩放操作。
     *
     * @param deltaScale - scale value
     * @param px         - scale center X
     * @param py         - scale center Y
     */
    fun postScale(deltaScale: Float, px: Float, py: Float) {
        if (deltaScale != 0f) {
            if ((deltaScale > 1 && currentScale * deltaScale <= maxScale) || (deltaScale < 1 && currentScale * deltaScale >= minScale)) {
                mCurrentImageMatrix.postScale(deltaScale, deltaScale, px, py)
                imageMatrix = mCurrentImageMatrix
                mTransformImageListener?.onScale(getMatrixScale(mCurrentImageMatrix))
            }
        }
    }

    /**
     * 对当前图片进行旋转操作。
     *
     * @param deltaAngle - rotation angle
     * @param px         - rotation center X
     * @param py         - rotation center Y
     */
    fun postRotate(deltaAngle: Float, px: Float, py: Float) {
        if (deltaAngle != 0f) {
            mCurrentImageMatrix.postRotate(deltaAngle, px, py)
            imageMatrix = mCurrentImageMatrix
            mTransformImageListener?.onRotate(getMatrixAngle(mCurrentImageMatrix))
        }
    }

    /**
     * 当图片布局完成时，必须设置 [.mInitialImageCenter] 和 [.mInitialImageCenter]。
     */
    protected fun onImageLaidOut() {
        val drawable = drawable ?: return

        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()

        Log.d(TAG, String.format("Image size: [%d:%d]", w.toInt(), h.toInt()))

        val initialImageRect = RectF(0f, 0f, w, h)
        mInitialImageCorners = getCornersFromRect(initialImageRect)
        mInitialImageCenter = getCenterFromRect(initialImageRect)

        mTransformImageListener?.onLoadComplete()
    }

    /**
     * 根据给定的索引返回Matrix的值。
     *
     * @param matrix     - 有效的Matrix对象
     * @param valueIndex - 所需值的索引。参见 [Matrix.MSCALE_X] 等。
     * @return - 索引对应的matrix值
     */
    private fun getMatrixValue(matrix: Matrix, @IntRange(from = 0, to = MATRIX_VALUES_COUNT.toLong()) valueIndex: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[valueIndex]
    }

    /**
     * 更新存储在 [.mCurrentImageCorners] 和 [.mCurrentImageCenter] 数组中的当前图片的角点和中心点。
     * 用于多种计算。
     */
    private fun updateCurrentImagePoints() {
        mInitialImageCorners?.let { mCurrentImageMatrix.mapPoints(mCurrentImageCorners, it) }
        mInitialImageCenter?.let { mCurrentImageMatrix.mapPoints(mCurrentImageCenter, it) }
    }

    fun getCurrentImageBitmapCenter(): FloatArray = mCurrentImageCenter

    /**
     * 记录给定矩阵的X、Y坐标、缩放值和角度值。
     * 用于调试。
     */
    protected fun printMatrix(logPrefix: String, matrix: Matrix) {
        val x = getMatrixValue(matrix, Matrix.MTRANS_X)
        val y = getMatrixValue(matrix, Matrix.MTRANS_Y)
        val rScale = getMatrixScale(matrix)
        val rAngle = getMatrixAngle(matrix)
        Log.d(TAG, "$logPrefix: matrix: { x: $x, y: $y, scale: $rScale, angle: $rAngle }")
    }
}
