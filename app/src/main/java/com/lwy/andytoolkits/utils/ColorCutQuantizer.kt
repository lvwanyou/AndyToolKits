package com.lwy.andytoolkits.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import java.util.Arrays
import java.util.PriorityQueue
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 详细说明：颜色切割量化器类，用于从位图中提取主要颜色。
 * <p>
 * 作用：该类通过颜色量化算法处理位图，将颜色空间划分为较小的颜色区域，
 * 并从中提取出主要颜色，以便于图像的颜色分析和处理。
 * <p>
 * 组件归属：peacockAI
 * <p>
 * 页面使用：发布页, 用于智能拼图封面提取主色
 *
 * @author lvwanyou
 */
class ColorCutQuantizer(bitmap: Bitmap?) {

    companion object {
        /**
         * 指定颜色量化后输出的颜色数目上限
         */
        private const val MAX_COLORS = 1
        private const val COMPONENT_RED = -3
        private const val COMPONENT_GREEN = -2
        private const val COMPONENT_BLUE = -1
        private const val QUANTIZE_WORD_WIDTH = 5
        private const val QUANTIZE_WORD_MASK = (1 shl QUANTIZE_WORD_WIDTH) - 1
        private const val QUANTIZE_WORD_BIT_COUNT = QUANTIZE_WORD_WIDTH * 3
        private val VBOX_COMPARATOR_VOLUME = java.util.Comparator<Vbox> { lhs, rhs -> rhs.volume - lhs.volume }

        private const val SCALE_SIZE_AREA = 25000

        /**
         * 从 RGB888 格式的颜色值中量化颜色。
         *
         *
         * 此方法将 RGB888 格式的颜色值转换为量化后的颜色值，以减少颜色的数量并简化图像处理。
         * 它通过调整颜色的位宽来实现颜色的量化。
         *
         * @param color RGB888 格式的颜色值。
         * @return 量化后的颜色值。
         */
        private fun quantizeFromRgb888(color: Int): Int {
            val r = modifyWordWidth(Color.red(color), 8, QUANTIZE_WORD_WIDTH)
            val g = modifyWordWidth(Color.green(color), 8, QUANTIZE_WORD_WIDTH)
            val b = modifyWordWidth(Color.blue(color), 8, QUANTIZE_WORD_WIDTH)
            return r shl QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH or (g shl QUANTIZE_WORD_WIDTH) or b
        }

        /**
         * 将量化后的颜色分量近似转换为 RGB888 格式的颜色值。
         *
         * @param r 量化后的红色分量
         * @param g 量化后的绿色分量
         * @param b 量化后的蓝色分量
         * @return 近似的 RGB888 格式颜色值
         */
        fun approximateToRgb888(r: Int, g: Int, b: Int): Int {
            return Color.rgb(modifyWordWidth(r, QUANTIZE_WORD_WIDTH, 8), modifyWordWidth(g, QUANTIZE_WORD_WIDTH, 8), modifyWordWidth(b, QUANTIZE_WORD_WIDTH, 8))
        }

        private fun approximateToRgb888(color: Int): Int {
            return approximateToRgb888(quantizedRed(color), quantizedGreen(color), quantizedBlue(color))
        }

        /**
         * 获取量化后的红色分量
         */
        private fun quantizedRed(color: Int): Int {
            return color shr 2 * QUANTIZE_WORD_WIDTH and QUANTIZE_WORD_MASK
        }

        /**
         * 获取量化后的绿色分量
         */
        private fun quantizedGreen(color: Int): Int {
            return color shr QUANTIZE_WORD_WIDTH and QUANTIZE_WORD_MASK
        }

        /**
         * 获取量化后的蓝色分量
         */
        private fun quantizedBlue(color: Int): Int {
            return color and QUANTIZE_WORD_MASK
        }

        /**
         * 修改颜色分量的位宽。
         *
         *
         * 此方法用于将颜色分量从当前位宽转换为目标位宽。如果目标位宽大于当前位宽，则通过左移位来增加位宽；
         * 如果目标位宽小于当前位宽，则通过右移位来减少位宽。转换后的值将被截断，以适应目标位宽的范围。
         *
         * @param value        需要修改位宽的颜色分量值。
         * @param currentWidth 当前的位宽。
         * @param targetWidth  目标的位宽。
         * @return 修改位宽后的颜色分量值。
         */
        private fun modifyWordWidth(value: Int, currentWidth: Int, targetWidth: Int): Int {
            val newValue: Int = if (targetWidth > currentWidth) {
                value shl targetWidth - currentWidth
            } else {
                value shr currentWidth - targetWidth
            }
            return newValue and (1 shl targetWidth) - 1
        }

        private val DEFAULT_FILTER: Filter = object : Filter {
            override fun isAllowed(rgb: Int, hsl: FloatArray): Boolean {
                return true
            }

            private fun isBlack(hslColor: FloatArray): Boolean {
                return hslColor[2] <= 0.05f
            }

            private fun isWhite(hslColor: FloatArray): Boolean {
                return hslColor[2] >= 0.95f
            }

            private fun isNearRedILine(hslColor: FloatArray): Boolean {
                return hslColor[0] in 10.0f..37.0f && hslColor[1] <= 0.82f
            }
        }
    }

    private var mColors: IntArray = IntArray(0)
    private var mHistogram: IntArray = IntArray(0)
    private var mQuantizedColors: MutableList<Swatch> = ArrayList()

    /**
     * 颜色过滤器
     */
    private val mFilters: Array<Filter> = arrayOf(DEFAULT_FILTER)

    private val mTempHsl = FloatArray(3)
    val quantizedColors: List<Swatch>
        /**
         * 获取量化后的颜色列表
         */
        get() = mQuantizedColors
    private val dominantSwatch: Swatch?
        /**
         * 获取主要颜色的 Swatch
         */
        get() {
            var dominantSwatch: Swatch? = null
            var maxPopulation = Int.MIN_VALUE
            for (swatch in mQuantizedColors) {
                if (swatch.population > maxPopulation) {
                    dominantSwatch = swatch
                    maxPopulation = swatch.population
                }
            }
            return dominantSwatch
        }


    /**
     * 构造函数，用于初始化颜色切割量化器。
     * 通过传入的位图对象，执行颜色量化处理，以提取主要颜色。
     */
    init {
        bitmap?.let {
            // 缩放位图以减少处理时间
            val scaledBitmap = scaleBitmapDown(it)
            // 从位图获取像素数组
            val pixels = getPixelsFromBitmap(scaledBitmap)
            val histogramSize = 1 shl QUANTIZE_WORD_BIT_COUNT
            mHistogram = IntArray(histogramSize)

            // 对像素数组中的颜色值进行量化: pixels 数组中的颜色值从 RGB888 格式量化, 以便减少颜色的数量以便于处理或存储。
            var color: Int
            var distinctColorCount = 0
            while (distinctColorCount < pixels.size) {
                color = quantizeFromRgb888(pixels[distinctColorCount])
                pixels[distinctColorCount] = color
                mHistogram[color]++
                ++distinctColorCount
            }

            // 颜色过滤
            distinctColorCount = 0
            color = 0
            while (color < mHistogram.size) {
                if (mHistogram[color] > 0 && this.shouldIgnoreColor(color)) {
                    mHistogram[color] = 0
                }
                if (mHistogram[color] > 0) {
                    ++distinctColorCount
                }
                ++color
            }
            mColors = IntArray(distinctColorCount)
            var distinctColorIndex = 0
            color = 0
            while (color < mHistogram.size) {
                if (mHistogram[color] > 0) {
                    mColors[distinctColorIndex++] = color
                }
                ++color
            }

            // 如果颜色数量小于或等于最大颜色数，直接使用这些颜色
            if (distinctColorCount <= MAX_COLORS) {
                mQuantizedColors = ArrayList()
                distinctColorIndex = 0
                while (distinctColorIndex < mColors.size) {
                    color = mColors[distinctColorIndex]
                    mQuantizedColors.add(Swatch(approximateToRgb888(color), mHistogram[color]))
                    ++distinctColorIndex
                }
            } else {
                // 否则，使用量化算法减少颜色数量
                mQuantizedColors = quantizePixels(MAX_COLORS)
            }

            // 回收临时创建的缩放位图资源
            if (scaledBitmap != it) {
                scaledBitmap.recycle()
            }
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap): Bitmap {
        var scaleRatio = -1.0
        val maxDimension: Int
        if (SCALE_SIZE_AREA > 0) {
            maxDimension = bitmap.getWidth() * bitmap.getHeight()
            if (maxDimension > SCALE_SIZE_AREA) {
                scaleRatio = sqrt(SCALE_SIZE_AREA.toDouble() / maxDimension.toDouble())
            }
        }
        return if (scaleRatio <= 0.0) bitmap else Bitmap.createScaledBitmap(bitmap, ceil(bitmap.getWidth().toDouble() * scaleRatio).toInt(), ceil(bitmap.getHeight().toDouble() * scaleRatio).toInt(), false)
    }

    private fun getPixelsFromBitmap(bitmap: Bitmap): IntArray {
        val bitmapWidth = bitmap.getWidth()
        val bitmapHeight = bitmap.getHeight()
        val pixels = IntArray(bitmapWidth * bitmapHeight)
        bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)
        return pixels
    }

    /**
     * 获取主要颜色的 RGB 值。
     * 如果存在主要颜色的 Swatch，则返回其 RGB 值；否则，返回默认颜色值。
     *
     * @param defaultColor 默认颜色值，当无法确定主要颜色时返回。
     * @return 主要颜色的 RGB 值，或默认颜色值。
     */
    @ColorInt
    fun getDominantColor(@ColorInt defaultColor: Int): Int {
        val mDominantSwatch = dominantSwatch
        return mDominantSwatch?.rgb ?: defaultColor
    }

    /**
     * 使用给定的颜色数量量化像素。
     *
     *
     * 该方法通过颜色量化算法减少颜色的数量，以便于图像的颜色分析和处理。
     * 它首先创建一个优先队列来存储颜色盒子，然后不断分割颜色盒子直到达到指定的颜色数量或无法进一步分割。
     * 最后，从每个颜色盒子中生成平均颜色，形成并返回量化后的颜色列表。
     *
     * @param maxColors 指定颜色量化后输出的颜色数目上限。
     * @return 量化后的颜色列表，每个颜色由一个 [Swatch] 对象表示。
     */
    private fun quantizePixels(maxColors: Int): MutableList<Swatch> {
        val pq = PriorityQueue(maxColors, VBOX_COMPARATOR_VOLUME)
        pq.offer(Vbox(0, mColors.size - 1))
        splitBoxes(pq, maxColors)
        return generateAverageColors(pq)
    }

    /**
     * 分割颜色盒子以减少颜色数量。
     *
     *
     * 该方法通过不断分割颜色盒子来减少颜色的数量，直到达到指定的最大颜色数或无法进一步分割为止。
     * 分割过程中，每次从优先队列中取出体积最大的颜色盒子进行分割，然后将分割后的盒子重新加入队列。
     * 这个过程一直持续，直到队列中的盒子数量达到最大颜色数或者盒子无法再分割。
     *
     * @param queue   优先队列，用于存储颜色盒子，按盒子的体积进行排序。
     * @param maxSize 指定颜色量化后输出的颜色数目上限。
     */
    private fun splitBoxes(queue: PriorityQueue<Vbox>, maxSize: Int) {
        while (true) {
            if (queue.size < maxSize) {
                val vbox = queue.poll() as Vbox
                if (vbox.canSplit()) {
                    queue.offer(vbox.splitBox())
                    queue.offer(vbox)
                    continue
                }
                return
            }
            return
        }
    }

    /**
     * 生成平均颜色列表。
     *
     *
     * 此方法遍历颜色盒子集合，从每个颜色盒子中计算平均颜色，并生成一个颜色样本列表。
     * 仅当颜色不应被忽略时，才将其添加到结果列表中。
     *
     * @param vBoxes 颜色盒子的集合，每个盒子代表一组颜色的范围。
     * @return 一个 [Swatch] 对象的列表，每个对象代表一个平均颜色样本。
     */
    private fun generateAverageColors(vBoxes: Collection<Vbox>): MutableList<Swatch> {
        val swatches = ArrayList<Swatch>(vBoxes.size)
        for (vbox in vBoxes) {
            val swatch: Swatch = vbox.averageColor
            if (!this.shouldIgnoreColor(swatch)) {
                swatches.add(swatch)
            }
        }
        return swatches
    }

    /**
     * 根据维度修改颜色以便重新排序
     */
    private fun modifySignificantOctet(dimension: Int, lower: Int, upper: Int) {
        var i: Int
        var color: Int
        when (dimension) {
            COMPONENT_BLUE -> {
                i = lower
                while (i <= upper) {
                    color = mColors[i]
                    mColors[i] = quantizedBlue(color) shl 2 * QUANTIZE_WORD_WIDTH or (quantizedGreen(color) shl QUANTIZE_WORD_WIDTH) or quantizedRed(color)
                    ++i
                }
                return
            }

            COMPONENT_GREEN -> {
                i = lower
                while (i <= upper) {
                    color = mColors[i]
                    mColors[i] = quantizedGreen(color) shl 2 * QUANTIZE_WORD_WIDTH or (quantizedRed(color) shl QUANTIZE_WORD_WIDTH) or quantizedBlue(color)
                    ++i
                }
                return
            }

            COMPONENT_RED -> {}
            else -> {}
        }
    }

    /**
     * 判断是否应该忽略某颜色
     */
    private fun shouldIgnoreColor(color565: Int): Boolean {
        val rgb = approximateToRgb888(color565)
        ColorUtils.colorToHSL(rgb, mTempHsl)
        return this.shouldIgnoreColor(rgb, mTempHsl)
    }

    /**
     * 判断是否应该忽略某 Swatch
     */
    private fun shouldIgnoreColor(swatch: Swatch): Boolean {
        return this.shouldIgnoreColor(swatch.rgb, swatch.hsl)
    }

    /**
     * 根据 RGB 和 HSL 判断是否应该忽略某颜色
     */
    private fun shouldIgnoreColor(rgb: Int, hsl: FloatArray): Boolean {
        if (mFilters.isNotEmpty()) {
            var i = 0
            val count = mFilters.size
            while (i < count) {
                if (!mFilters[i].isAllowed(rgb, hsl)) {
                    return true
                }
                ++i
            }
        }
        return false
    }

    /**
     * Vbox 类代表在颜色空间中的一个体积盒子，用于颜色量化过程中的颜色分割。
     * 它封装了颜色空间的一个子区域，提供了计算该区域颜色统计、体积、是否可分割等功能。
     * 通过对这些盒子的分割和颜色统计，可以有效地减少颜色的数量，实现颜色的量化。
     */
    private inner class Vbox(private val mLowerIndex: Int, private var mUpperIndex: Int) {
        private var mPopulation = 0
        private var mMinRed = 0
        private var mMaxRed = 0
        private var mMinGreen = 0
        private var mMaxGreen = 0
        private var mMinBlue = 0
        private var mMaxBlue = 0

        /**
         * 构造函数，初始化盒子的上下界
         */
        init {
            fitBox()
        }

        val volume: Int
            /**
             * 计算盒子的体积
             */
            get() = (mMaxRed - mMinRed + 1) * (mMaxGreen - mMinGreen + 1) * (mMaxBlue - mMinBlue + 1)

        /**
         * 判断盒子是否可以分割
         */
        fun canSplit(): Boolean {
            return this.colorCount > 1
        }

        /**
         * 获取盒子中的颜色数量
         */
        val colorCount: Int
            get() = 1 + mUpperIndex - mLowerIndex

        /**
         * 调整盒子以确保它包含其内所有颜色的最小和最大值
         */
        fun fitBox() {
            // 初始化红色、绿色和蓝色维度最小和最大值
            var minBlue = Int.MAX_VALUE
            var minGreen = Int.MAX_VALUE
            var minRed = Int.MAX_VALUE
            var maxBlue = Int.MIN_VALUE
            var maxGreen = Int.MIN_VALUE
            var maxRed = Int.MIN_VALUE
            var count = 0

            // 遍历盒子中的所有颜色，更新红色、绿色和蓝色维度最小和最大值
            for (i in mLowerIndex..mUpperIndex) {
                val color = mColors[i]
                count += mHistogram[color]
                val r = quantizedRed(color)
                val g = quantizedGreen(color)
                val b = quantizedBlue(color)
                if (r > maxRed) {
                    maxRed = r
                }
                if (r < minRed) {
                    minRed = r
                }
                if (g > maxGreen) {
                    maxGreen = g
                }
                if (g < minGreen) {
                    minGreen = g
                }
                if (b > maxBlue) {
                    maxBlue = b
                }
                if (b < minBlue) {
                    minBlue = b
                }
            }

            // 更新盒子的边界和颜色数量
            mMinRed = minRed
            mMaxRed = maxRed
            mMinGreen = minGreen
            mMaxGreen = maxGreen
            mMinBlue = minBlue
            mMaxBlue = maxBlue
            mPopulation = count
        }

        /**
         * 分割盒子
         */
        fun splitBox(): Vbox {
            return if (!canSplit()) {
                throw IllegalStateException("Can not split a box with only 1 color")
            } else {
                val splitPoint = findSplitPoint()
                val newBox = Vbox(splitPoint + 1, mUpperIndex)
                mUpperIndex = splitPoint
                fitBox()
                newBox
            }
        }

        /**
         * 获取最长的颜色维度
         */
        val longestColorDimension: Int
            get() {
                val redLength = mMaxRed - mMinRed
                val greenLength = mMaxGreen - mMinGreen
                val blueLength = mMaxBlue - mMinBlue
                return if (redLength >= greenLength && redLength >= blueLength) {
                    COMPONENT_RED
                } else {
                    if (greenLength >= redLength && greenLength >= blueLength) COMPONENT_GREEN else COMPONENT_BLUE
                }
            }

        /**
         * 找到分割点
         */
        fun findSplitPoint(): Int {
            val longestDimension: Int = this.longestColorDimension
            modifySignificantOctet(longestDimension, mLowerIndex, mUpperIndex)
            Arrays.sort(mColors, mLowerIndex, mUpperIndex + 1)
            modifySignificantOctet(longestDimension, mLowerIndex, mUpperIndex)
            val midPoint = mPopulation / 2
            var i = mLowerIndex
            var count = 0
            while (i <= mUpperIndex) {
                count += mHistogram[mColors[i]]
                if (count >= midPoint) {
                    return min(mUpperIndex - 1, i)
                }
                ++i
            }
            return mLowerIndex
        }

        val averageColor: Swatch
            /**
             * 获取盒子的平均颜色
             */
            get() {
                var redSum = 0
                var greenSum = 0
                var blueSum = 0
                var totalPopulation = 0
                var redMean: Int
                var greenMean: Int
                var blueMean: Int
                redMean = mLowerIndex
                while (redMean <= mUpperIndex) {
                    greenMean = mColors[redMean]
                    blueMean = mHistogram[greenMean]
                    totalPopulation += blueMean
                    redSum += blueMean * quantizedRed(greenMean)
                    greenSum += blueMean * quantizedGreen(greenMean)
                    blueSum += blueMean * quantizedBlue(greenMean)
                    ++redMean
                }
                redMean = Math.round(redSum.toFloat() / totalPopulation.toFloat())
                greenMean = Math.round(greenSum.toFloat() / totalPopulation.toFloat())
                blueMean = Math.round(blueSum.toFloat() / totalPopulation.toFloat())
                return Swatch(approximateToRgb888(redMean, greenMean, blueMean), totalPopulation)
            }
    }

    /**
     * Swatch 类表示一个颜色样本。
     * 它封装了颜色的RGB值、颜色在图片中的占比（直方图不同颜色的数量）以及其他颜色属性。
     * 可以通过RGB值、HSL值或直接通过颜色值来创建Swatch实例。
     */
    class Swatch {
        private val mRed: Int
        private val mGreen: Int
        private val mBlue: Int

        @get:ColorInt
        val rgb: Int
        val population: Int
        private var mGeneratedTextColors = false
        private var mTitleTextColor = 0
        private var mBodyTextColor = 0
        private var mHsl: FloatArray? = null

        constructor(@ColorInt color: Int, population: Int) {
            mRed = Color.red(color)
            mGreen = Color.green(color)
            mBlue = Color.blue(color)
            rgb = color
            this.population = population
        }

        internal constructor(red: Int, green: Int, blue: Int, population: Int) {
            mRed = red
            mGreen = green
            mBlue = blue
            rgb = Color.rgb(red, green, blue)
            this.population = population
        }

        internal constructor(hsl: FloatArray, population: Int) : this(ColorUtils.HSLToColor(hsl), population) {
            mHsl = hsl
        }

        val hsl: FloatArray
            get() {
                if (mHsl == null) {
                    mHsl = FloatArray(3)
                }
                ColorUtils.RGBToHSL(mRed, mGreen, mBlue, mHsl!!)
                return mHsl!!
            }

        @get:ColorInt
        val titleTextColor: Int
            get() {
                ensureTextColorsGenerated()
                return mTitleTextColor
            }

        @get:ColorInt
        val bodyTextColor: Int
            get() {
                ensureTextColorsGenerated()
                return mBodyTextColor
            }

        private fun ensureTextColorsGenerated() {
            if (!mGeneratedTextColors) {
                val lightBodyAlpha = ColorUtils.calculateMinimumAlpha(-1, rgb, 4.5f)
                val lightTitleAlpha = ColorUtils.calculateMinimumAlpha(-1, rgb, 3.0f)
                if (lightBodyAlpha != -1 && lightTitleAlpha != -1) {
                    mBodyTextColor = ColorUtils.setAlphaComponent(-1, lightBodyAlpha)
                    mTitleTextColor = ColorUtils.setAlphaComponent(-1, lightTitleAlpha)
                    mGeneratedTextColors = true
                    return
                }
                val darkBodyAlpha = ColorUtils.calculateMinimumAlpha(-16777216, rgb, 4.5f)
                val darkTitleAlpha = ColorUtils.calculateMinimumAlpha(-16777216, rgb, 3.0f)
                if (darkBodyAlpha != -1 && darkTitleAlpha != -1) {
                    mBodyTextColor = ColorUtils.setAlphaComponent(-16777216, darkBodyAlpha)
                    mTitleTextColor = ColorUtils.setAlphaComponent(-16777216, darkTitleAlpha)
                    mGeneratedTextColors = true
                    return
                }
                mBodyTextColor = if (lightBodyAlpha != -1) ColorUtils.setAlphaComponent(-1, lightBodyAlpha) else ColorUtils.setAlphaComponent(-16777216, darkBodyAlpha)
                mTitleTextColor = if (lightTitleAlpha != -1) ColorUtils.setAlphaComponent(-1, lightTitleAlpha) else ColorUtils.setAlphaComponent(-16777216, darkTitleAlpha)
                mGeneratedTextColors = true
            }
        }

        override fun toString(): String {
            return this.javaClass.getSimpleName() + " [RGB: #" + Integer.toHexString(this.rgb) + ']' + " [HSL: " + this.hsl.contentToString() + ']' + " [Population: " + population + ']' + " [Title Text: #" + Integer.toHexString(this.titleTextColor) + ']' + " [Body Text: #" + Integer.toHexString(this.bodyTextColor) + ']'
        }

        override fun equals(o: Any?): Boolean {
            return if (this === o) {
                true
            } else if (o != null && this.javaClass == o.javaClass) {
                val swatch = o as Swatch
                population == swatch.population && rgb == swatch.rgb
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return 31 * rgb + population
        }
    }

    interface Filter {
        fun isAllowed(@ColorInt rgb: Int, hsl: FloatArray): Boolean
    }
}