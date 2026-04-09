package com.example.openglexample.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.openglexample.R
import com.example.openglexample.utils.PictUtils
import java.io.IOException
import java.util.Random
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread


class BitmapMainColorActivity : Activity(), View.OnClickListener {
    companion object {
        private const val REQUEST_PERMISSION = 123
        private const val REQUEST_CODE_SELECT_IMAGE = 1

        private const val PALETTE_ALGORITHM = "Palette"
        private const val K_MEANS_ALGORITHM = "k_means"
        private const val HISTOGRAM_ALGORITHM = "histogram"
        private const val K_MEANS_PLUS_ALGORITHM = "k_means++"
        private const val SELF_PALETTE_ALGORITHM = "self-Palette"

        private const val MSG_SELECT_IMAGE = 1
        private const val MSG_SET_COLOR = 2
    }

    private lateinit var ivBitmap: ImageView
    private lateinit var tvMainColor1: TextView
    private lateinit var tvMainColor2: TextView
    private lateinit var tvMainColor3: TextView
    private lateinit var tvMainColor4: TextView
    private lateinit var tvMainColor5: TextView

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_SELECT_IMAGE -> {
                    if (msg.obj is Pair<*, *>) {
                        val imageInfo: Pair<Uri, String> = msg.obj as Pair<Uri, String>
                        try {
                            val bitmap = PictUtils.getBitmapFromUri(contentResolver, imageInfo.first)
                            // 使用获取到的 Bitmap
                            ivBitmap.setImageBitmap(bitmap)
                            getDominantColor(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                MSG_SET_COLOR -> {
                    // 更新 UI
                    val colorIntPair = msg.obj as Pair<String, Int>
                    val colorInt = colorIntPair.second
                    when (colorIntPair.first) {
                        PALETTE_ALGORITHM -> {
                            val tvBg = tvMainColor1.background as? GradientDrawable
                            tvBg?.setColor(colorInt)
                            val formattedColorStr = "#${Integer.toHexString(colorInt).padStart(8, '0')}" // 补齐到8位
                            tvMainColor1.text = formattedColorStr
                        }

                        K_MEANS_ALGORITHM -> {
                            val tvBg = tvMainColor2.background as? GradientDrawable
                            tvBg?.setColor(colorInt)
                            val formattedColorStr = "#${Integer.toHexString(colorInt).padStart(8, '0')}" // 补齐到8位
                            tvMainColor2.text = formattedColorStr
                        }

                        HISTOGRAM_ALGORITHM -> {
                            val tvBg = tvMainColor3.background as? GradientDrawable
                            tvBg?.setColor(colorInt)
                            val formattedColorStr = "#${Integer.toHexString(colorInt).padStart(8, '0')}" // 补齐到8位
                            tvMainColor3.text = formattedColorStr
                        }

                        K_MEANS_PLUS_ALGORITHM -> {
                            val tvBg = tvMainColor4.background as? GradientDrawable
                            tvBg?.setColor(colorInt)
                            val formattedColorStr = "#${Integer.toHexString(colorInt).padStart(8, '0')}" // 补齐到8位
                            tvMainColor4.text = formattedColorStr
                        }

                        SELF_PALETTE_ALGORITHM -> {
                            val tvBg = tvMainColor5.background as? GradientDrawable
                            tvBg?.setColor(colorInt)
                            val adjustColorInt = adjustBrightness(colorInt) // adjustColorIfNeeded(colorInt)
                            val formattedColorStr = "#${Integer.toHexString(colorInt).padStart(8, '0')}" // 补齐到8位
                            val formattedAdjustColorStr = "#${Integer.toHexString(adjustColorInt).padStart(8, '0')}" // 补齐到8位
                            tvMainColor5.text = formattedColorStr + "\n" + formattedAdjustColorStr
                        }
                    }
                }
            }
        }
    }.also { mHandler = it }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitmap_main_color_view)
        ivBitmap = findViewById<ImageView>(R.id.img_bitmap)
        findViewById<Button>(R.id.btn_chose_img).setOnClickListener(this)
        tvMainColor1 = findViewById<TextView>(R.id.tv_main_color1)
        tvMainColor2 = findViewById<TextView>(R.id.tv_main_color2)
        tvMainColor3 = findViewById<TextView>(R.id.tv_main_color3)
        tvMainColor4 = findViewById<TextView>(R.id.tv_main_color4)
        tvMainColor5 = findViewById<TextView>(R.id.tv_main_color5)

        ivBitmap.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onGlobalLayout() {
                // 确保这段代码只执行一次，移除监听器
                ivBitmap.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // 获取 Drawable 并检查是否为 BitmapDrawable
                val drawable = ivBitmap.drawable
                if (drawable is AdaptiveIconDrawable) {
                    // 创建一个空的 Bitmap，其大小与 AdaptiveIconDrawable 相同
                    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

                    // 创建一个 Canvas 来绘制 Bitmap
                    val canvas = Canvas(bitmap)

                    // 将 AdaptiveIconDrawable 绘制到 Bitmap 上
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    if (bitmap != null) {
                        getDominantColor(bitmap)
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageFromAlbum()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_chose_img -> {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
                } else {
                    chooseImageFromAlbum()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImageUri!!, projection, null, null, null)
            val imagePath: String
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                imagePath = cursor.getString(columnIndex)
                cursor.close()
            } else {
                imagePath = PictUtils.getPathFromUri(this, selectedImageUri)!!
            }

            val msg: Message = mHandler.obtainMessage(MSG_SELECT_IMAGE)
            msg.obj = Pair(selectedImageUri, imagePath)
            mHandler.sendMessage(msg)
        }
    }

    private fun chooseImageFromAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    private val threadMap = HashMap<String, Thread>()

    private fun startNamedThread(name: String, task: () -> Unit) {
        // 先停止具有相同名称的线程
        threadMap[name]?.interrupt()
        threadMap[name]?.join() // 确保线程已经停止

        // 启动新线程
        val newThread = thread(start = true, name = name) {
            try {
                task()
            } catch (e: InterruptedException) {
                println("$name was interrupted")
            }
        }

        // 更新线程映射
        threadMap[name] = newThread
    }

    private fun getDominantColor(bitmap: Bitmap?) {
        bitmap?.let {
            val downSampleBitmap = PictUtils.scaleBitmapDown(it)
            startNamedThread(PALETTE_ALGORITHM) {
                getDominantColorByPalette(downSampleBitmap)
            }
            startNamedThread(K_MEANS_ALGORITHM) {
                getDominantColorBykMeans(downSampleBitmap)
            }
            startNamedThread(HISTOGRAM_ALGORITHM) {
                getDominantColorByHistogram(downSampleBitmap)
            }
            startNamedThread(K_MEANS_PLUS_ALGORITHM) {
                getDominantColorBykMeansParallel(downSampleBitmap)
            }
            startNamedThread(SELF_PALETTE_ALGORITHM) {
                val dominantColor = com.example.openglexample.utils.ColorCutQuantizer(downSampleBitmap).getDominantColor(Color.TRANSPARENT)
                // 使用主色 (dominantColor)
                val msg: Message = mHandler.obtainMessage(MSG_SET_COLOR)
                msg.obj = Pair(SELF_PALETTE_ALGORITHM, dominantColor)
                mHandler.sendMessage(msg)
            }

            // todo: 在任务结束的时候，需要释放临时生成的 bitmap
//            if (downSampleBitmap != bitmap) {
//                downSampleBitmap.recycle()
//            }
//            if (!bitmap.isRecycled) {
//                bitmap.recycle()
//            }
        }
    }

    /**
     * 判断颜色是否浅色，并且加深浅色
     * @param color 原始颜色
     * @return 处理后的颜色
     */
    fun adjustColorIfNeeded(color: Int): Int {
        // 将 RGB 颜色转换为 HSL 颜色
        val hsl = FloatArray(3)
        Color.colorToHSV(color, hsl)
        val lightness = hsl[2]

        // 如果颜色比较浅（定义 lightness 阈值为 0.7），则加深颜色
        if (lightness > 0.5f) {
            // 将 lightness 值减少 20%
            hsl[2] = hsl[2] * 0.8f
        }

        // 将 HSL 颜色转换回 RGB 颜色
        return Color.HSVToColor(hsl)
    }

    fun adjustBrightness(colorInt: Int): Int {
        // 将RGB转换为HSB
        val hsb = FloatArray(3)
        Color.RGBToHSV(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt), hsb)

        // 调整亮度B, 若大于50则设置为50
        if (hsb[2] * 100 > 50) {
            hsb[2] = 0.5f  // 设置亮度为50
        }

        // 将HSB转换回RGB
        return Color.HSVToColor(hsb)
    }

    private fun getDominantColorByPalette(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            // 获取主色
            val dominantColor: Int = palette?.getDominantColor(Color.TRANSPARENT) ?: Color.TRANSPARENT // Color.TRANSPARENT is default color if no dominant color found
            // 使用主色 (dominantColor)
            val msg: Message = mHandler.obtainMessage(MSG_SET_COLOR)
            msg.obj = Pair(PALETTE_ALGORITHM, dominantColor)
            mHandler.sendMessage(msg)
        }
    }

    private fun getDominantColorBykMeans(bitmap: Bitmap) {
        val K = 3 // 聚类数
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()
        val colors: MutableList<IntArray> = ArrayList()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.alpha(pixel) == 0) {
                    // 跳过透明像素
                    continue
                }
                val rgb = intArrayOf(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
                colors.add(rgb)
            }
        }

        // 初始化中心点
        val random = Random()
        val centers = Array(K) { IntArray(3) }
        for (i in 0 until K) {
            centers[i] = colors[random.nextInt(colors.size)]
        }
        var changed = true
        val labels = IntArray(colors.size)
        val counts = IntArray(K)

        while (changed) {
            changed = false
            // 分类
            for (i in colors.indices) {
                var minDist = Int.MAX_VALUE
                for (j in 0 until K) {
                    val dist: Int = distance(colors[i], centers[j])
                    if (dist < minDist) {
                        minDist = dist
                        labels[i] = j
                    }
                }
            }

            // 更新中心点
            val newCenters = Array(K) { IntArray(3) }
            counts.fill(0)
            for (i in colors.indices) {
                val label = labels[i]
                newCenters[label][0] += colors[i][0]
                newCenters[label][1] += colors[i][1]
                newCenters[label][2] += colors[i][2]
                counts[label]++
            }
            for (j in 0 until K) {
                if (counts[j] > 0) {
                    newCenters[j][0] /= counts[j]
                    newCenters[j][1] /= counts[j]
                    newCenters[j][2] /= counts[j]
                }
            }
            for (j in 0 until K) {
                if (centers[j][0] != newCenters[j][0] || centers[j][1] != newCenters[j][1] || centers[j][2] != newCenters[j][2]) {
                    changed = true
                    centers[j] = newCenters[j]
                }
            }
        }

        // 计算并返回主色
        var maxCount = 0
        var dominantColor = Color.rgb(centers[0][0], centers[0][1], centers[0][2])
        for (i in 0 until K) {
            if (counts[i] > maxCount) {
                maxCount = counts[i]
                dominantColor = Color.rgb(centers[i][0], centers[i][1], centers[i][2])
            }
        }
        // 使用主色 (dominantColor)
        val msg: Message = mHandler.obtainMessage(MSG_SET_COLOR)
        msg.obj = Pair(K_MEANS_ALGORITHM, dominantColor)
        mHandler.sendMessage(msg)
    }

    private fun distance(color1: IntArray, color2: IntArray): Int {
        val redDiff = color1[0] - color2[0]
        val greenDiff = color1[1] - color2[1]
        val blueDiff = color1[2] - color2[2]
        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff
    }

    private fun getDominantColorByHistogram(bitmap: Bitmap) {
        val colorCountMap: MutableMap<Int, Int> = HashMap()
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()
        val totalPixels = width * height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = bitmap.getPixel(x, y)
                if (Color.alpha(color) == 0) {
                    // 跳过透明像素
                    continue
                }
                if (colorCountMap.containsKey(color)) {
                    colorCountMap[color] = colorCountMap[color]!! + 1
                } else {
                    colorCountMap[color] = 1
                }
            }
        }
        var dominantColor = Color.BLACK
        var maxCount = 0
        for ((key, value) in colorCountMap) {
            if (value > maxCount) {
                dominantColor = key
                maxCount = value
            }
        }

        // 使用主色 (dominantColor)
        val msg: Message = mHandler.obtainMessage(MSG_SET_COLOR)
        msg.obj = Pair(HISTOGRAM_ALGORITHM, dominantColor)
        mHandler.sendMessage(msg)
    }


    private fun initializeCentersPlusPlus(colors: List<IntArray>, K: Int): Array<IntArray> {
        val random = Random()
        val centers = Array(K) { IntArray(3) }
        centers[0] = colors[random.nextInt(colors.size)]
        for (i in 1 until K) {
            val distances = DoubleArray(colors.size)
            var totalDistance = 0.0
            for (j in colors.indices) {
                distances[j] = Double.MAX_VALUE
                for (m in 0 until i) {
                    val dist = distance(colors[j], centers[m]).toDouble()
                    if (dist < distances[j]) {
                        distances[j] = dist
                    }
                }
                totalDistance += distances[j]
            }
            var rand = random.nextDouble() * totalDistance
            for (j in colors.indices) {
                rand -= distances[j]
                if (rand <= 0) {
                    centers[i] = colors[j]
                    break
                }
            }
        }
        return centers
    }

    private fun parallelClassifyColors(colors: List<IntArray>, centers: Array<IntArray>): IntArray {
        val labels = IntArray(colors.size)
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val futures: MutableList<Future<Void?>> = ArrayList<Future<Void?>>()

        for (i in colors.indices) {
            futures.add(executor.submit<Void?> {
                var minDist = Int.MAX_VALUE
                for (j in centers.indices) {
                    val dist = distance(colors[i], centers[j])
                    if (dist < minDist) {
                        minDist = dist
                        labels[i] = j
                    }
                }
                null
            })
        }
        for (future in futures) {
            try {
                future.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        executor.shutdown()
        return labels
    }

    private fun getDominantColorBykMeansParallel(bitmap: Bitmap) {
        val K = 3 // 聚类数
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()
        val colors: MutableList<IntArray> = ArrayList()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.alpha(pixel) == 0) {
                    // 跳过透明像素
                    continue
                }
                val rgb = intArrayOf(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
                colors.add(rgb)
            }
        }

        // 初始化中心点
        val centers = initializeCentersPlusPlus(colors, K)

        var changed = true
        var labels = IntArray(colors.size)
        val counts = IntArray(K)

        while (changed) {
            changed = false

//            // 并行分类
//            labels = parallelClassifyColors(colors, centers)
            // 分类
            for (i in colors.indices) {
                var minDist = Int.MAX_VALUE
                for (j in 0 until K) {
                    val dist: Int = distance(colors[i], centers[j])
                    if (dist < minDist) {
                        minDist = dist
                        labels[i] = j
                    }
                }
            }

            // 更新中心点
            val newCenters = Array(K) { IntArray(3) }
            counts.fill(0)
            for (i in colors.indices) {
                val label = labels[i]
                newCenters[label][0] += colors[i][0]
                newCenters[label][1] += colors[i][1]
                newCenters[label][2] += colors[i][2]
                counts[label]++
            }
            for (j in 0 until K) {
                if (counts[j] > 0) {
                    newCenters[j][0] /= counts[j]
                    newCenters[j][1] /= counts[j]
                    newCenters[j][2] /= counts[j]
                }
            }
            for (j in 0 until K) {
                if (centers[j][0] != newCenters[j][0] || centers[j][1] != newCenters[j][1] || centers[j][2] != newCenters[j][2]) {
                    changed = true
                    centers[j] = newCenters[j]
                }
            }
        }

        // 计算并返回主色
        var maxCount = 0
        var dominantColor = Color.rgb(centers[0][0], centers[0][1], centers[0][2])
        for (i in 0 until K) {
            if (counts[i] > maxCount) {
                maxCount = counts[i]
                dominantColor = Color.rgb(centers[i][0], centers[i][1], centers[i][2])
            }
        }
        // 使用主色 (dominantColor)
        val msg: Message = mHandler.obtainMessage(MSG_SET_COLOR)
        msg.obj = Pair(K_MEANS_PLUS_ALGORITHM, dominantColor)
        mHandler.sendMessage(msg)
    }


}

