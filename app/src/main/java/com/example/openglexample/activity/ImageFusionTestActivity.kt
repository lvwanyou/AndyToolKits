package com.example.openglexample.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.dianping.video.view.gestureimgview.BlendGestureImageView1
import com.dianping.video.view.gestureimgview.BlendGestureImageView2
import com.dianping.video.view.gestureimgview.DPTransformImageView
import com.example.openglexample.R
import com.example.openglexample.utils.PictUtils


class ImageFusionTestActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_GALLERY: Int = 1
    }

    private lateinit var img1: BlendGestureImageView1
    private lateinit var img2: BlendGestureImageView2
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_fusion_test)
        initViews()
        openGallery()
    }

    private fun initViews() {
        img1 = findViewById<BlendGestureImageView1>(R.id.zoomRotateImg1)
        img2 = findViewById<BlendGestureImageView2>(R.id.zoomRotateImg2)
        seekBar = findViewById<SeekBar>(R.id.intensityBar)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                img1.blendIntensity = progress
                img2.blendIntensity = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                // 多选图片
                val clipData = data.clipData
                if (clipData!!.itemCount == 1) {
                    val imageUri = clipData.getItemAt(0).uri
//                    val filePath: String = PictUtils.getPathFromUri(this, imageUri)!!
                    setUriForDPGestureImageView(img1, imageUri)
                } else {
                    val images: MutableList<Uri> = mutableListOf()
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        // 在这里处理选中的图片 Uri
                        images.add(imageUri!!)
                    }

                    setUriForDPGestureImageView(img1, images[0])
                    setUriForDPGestureImageView(img2, images[1])
                }
            } else if (data.data != null) {
                // 单选图片
                val imageUri = data.data
                // 在这里处理选中的图片 Uri
                setUriForDPGestureImageView(img1, imageUri!!)
            }
        }
    }

    private fun setUriForDPGestureImageView(dpGestureImageView: DPTransformImageView, fileUri: Uri) {
        dpGestureImageView.post {

            val bitmap = PictUtils.getBitmapFromUri(contentResolver, fileUri)
            val scaleBitmap = PictUtils.scaleBitmap(bitmap, dpGestureImageView.width, dpGestureImageView.height)

//            val blurBitmap = PictUtils.blurBitmap(this, scaleBitmap, 20f)
            val blurBitmap = this.applyBlurEdges(scaleBitmap)
            dpGestureImageView.setImageBitmapAndShowCenterCropWhenLayout(blurBitmap)

            dpGestureImageView.invalidate()
        }
    }

    fun applyBlurEdges(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height

        // 创建一个新的 Bitmap，用于绘制最终结果
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 绘制原始图像
        canvas.drawBitmap(original, 0f, 0f, null)

        // 创建一个用于模糊边缘的 Paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))

        val edgeWidth = 30f

        // 上下渐变
        //intArrayOf(0x00FFFFFF, 0x),
        val topBottomGradient = LinearGradient(
            0f, 0f, 0f, edgeWidth,
            intArrayOf(Color.argb(0, 255, 255, 255) , -0x1),
            null,
            Shader.TileMode.CLAMP
        )
        paint.setShader(topBottomGradient)
        canvas.drawRect(RectF(0f, 0f, width.toFloat(), edgeWidth), paint)

        val bottomTopGradient = LinearGradient(
            0f, height - edgeWidth, 0f, height.toFloat(),
            intArrayOf(-0x1, Color.argb(0, 255, 255, 255) ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.setShader(bottomTopGradient)
        canvas.drawRect(RectF(0f, height - edgeWidth, width.toFloat(), height.toFloat()), paint)

        // 左右渐变
        val leftRightGradient = LinearGradient(
            0f, 0f, edgeWidth, 0f,
            intArrayOf(Color.argb(0, 255, 255, 255) , -0x1),
            null,
            Shader.TileMode.CLAMP
        )
        paint.setShader(leftRightGradient)
        canvas.drawRect(RectF(0f, 0f, edgeWidth, height.toFloat()), paint)

        val rightLeftGradient = LinearGradient(
            width - edgeWidth, 0f, width.toFloat(), 0f,
            intArrayOf(-0x1, Color.argb(0, 255, 255, 255) ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.setShader(rightLeftGradient)
        canvas.drawRect(RectF(width - edgeWidth, 0f, width.toFloat(), height.toFloat()), paint)

        // 创建一个径向渐变，用于模糊边缘
//        效果不好
//        val gradient: RadialGradient = RadialGradient(
//            width / 2f, height / 2f, (max(width.toDouble(), height.toDouble()) / 2f).toFloat(),
//            intArrayOf(-0x1, 0x00FFFFFF),
//            floatArrayOf(0.8f, 1.0f),
//            Shader.TileMode.CLAMP
//        )
//        paint.setShader(gradient)
// 绘制渐变蒙版到 Canvas
//        canvas.drawRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), paint)


        return result
    }

    private fun isNetPhoto(photoPath: String): Boolean {
        return !TextUtils.isEmpty(photoPath) &&
                (photoPath.startsWith("http://") || photoPath.startsWith("https://"))
    }
}