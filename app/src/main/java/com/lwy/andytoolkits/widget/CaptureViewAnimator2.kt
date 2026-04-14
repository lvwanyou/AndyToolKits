package com.lwy.andytoolkits.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.os.AsyncTask

class CaptureViewAnimator2(private val targetView: View) {

    private var blurImageView: ImageView? = null
    private var currentAnimator: AnimatorSet? = null

    fun rotate(endCallback: (() -> Unit)? = null) {
        if (targetView.width == 0 || targetView.height == 0) {
            endCallback?.invoke()
            return
        }

        BlurTask(targetView.context, targetView,
            onComplete = { blurredBitmap ->
                blurredBitmap?.let {
                    setupAndStartAnimation(it, endCallback)
                } ?: run {
                    endCallback?.invoke()
                }
            },
            onFailed = {
                endCallback?.invoke()
            }
        ).execute()
    }

    fun cancel() {
        currentAnimator?.cancel()
    }

    private fun setupAndStartAnimation(blurredBitmap: Bitmap, endCallback: (() -> Unit)?) {
        val parent = targetView.parent as? ViewGroup ?: run {
            endCallback?.invoke()
            return
        }

        blurImageView = ImageView(targetView.context).apply {
            setImageBitmap(blurredBitmap)
            alpha = 0f
            layoutParams = targetView.layoutParams
        }

        parent.addView(blurImageView)

        val fadeIn = ObjectAnimator.ofFloat(blurImageView, "alpha", 0f, 1f).apply {
            duration = 8000
        }

        val rotateAnim = ObjectAnimator.ofFloat(targetView, View.ROTATION_Y, 0f, 180f).apply {
            duration = 14000
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }

        currentAnimator = AnimatorSet().apply {
            playTogether(fadeIn, rotateAnim)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val fadeOut = ObjectAnimator.ofFloat(blurImageView, "alpha", 1f, 0f).apply {
                        duration = 7000
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                parent.removeView(blurImageView)
                                endCallback?.invoke()
                            }
                        })
                    }
                    fadeOut.start()
                }

                override fun onAnimationCancel(animation: Animator) {
//                    parent.removeView(blurImageView)
                }
            })
            start()
        }
    }

    private class BlurTask(
        context: Context,
        private val view: View,
        private val onComplete: (Bitmap?) -> Unit,
        private val onFailed: () -> Unit
    ) : AsyncTask<Void, Void, Bitmap?>() {

        private val contextRef = context.applicationContext

        private fun traditionalBlur(bitmap: Bitmap): Bitmap {
            // 优化步骤：先缩小图片再模糊以提高性能
            val scaleFactor = 8
            val scaledWidth = bitmap.width / scaleFactor
            val scaledHeight = bitmap.height / scaleFactor

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)

            try {
                val rs = RenderScript.create(contextRef)
                val input = Allocation.createFromBitmap(rs, scaledBitmap)
                val output = Allocation.createTyped(rs, input.type)
                val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

                script.apply {
                    setRadius(25f)
                    setInput(input)
                    forEach(output)
                }

                output.copyTo(scaledBitmap)
                rs.destroy()

                // 模糊后放大回原尺寸
                return Bitmap.createScaledBitmap(scaledBitmap, bitmap.width, bitmap.height, false)
            } catch (e: Exception) {
                return bitmap
            }
        }

        override fun doInBackground(vararg params: Void?): Bitmap? {
            return try {
                // 创建视图的截图
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                    Canvas(this).apply { view.draw(this) }
                }
                traditionalBlur(bitmap)
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                onComplete(result)
            } else {
                onFailed()
            }
        }
    }
}