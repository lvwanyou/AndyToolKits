package com.example.openglexample.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.os.AsyncTask
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup

class CaptureViewAnimator1(private val targetView: View) {

    private var currentAnimator: AnimatorSet? = null

    fun rotate(endCallback: (() -> Unit)? = null) {
        BlurAnimationTask(targetView, endCallback).execute()
    }

    fun cancel() {
        currentAnimator?.cancel()
    }

    private class BlurAnimationTask(
        private val view: View,
        private val endCallback: (() -> Unit)?
    ) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            // 在后台线程准备模糊效果（如果需要复杂计算可以在这里处理）
            return null
        }

        override fun onPostExecute(result: Void?) {
            startBlurAnimation(view, endCallback)
        }

        private fun startBlurAnimation(view: View, endCallback: (() -> Unit)?) {
            val parent = view.parent as? ViewGroup ?: return

            // 添加模糊效果
            ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_SOFTWARE, null)
            ViewCompat.setLayerPaint(view, Paint().apply {
                maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            })
            // 创建动画组合
            val animatorSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, View.ROTATION_Y, 0f, 180f).apply {
                        duration = 500
                    },
                    ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0.5f).apply {
                        duration = 200
                    }
                )

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        removeBlurEffect(view)
                        endCallback?.invoke()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        removeBlurEffect(view)
                    }
                })
            }

            animatorSet.start()
        }

        private fun removeBlurEffect(view: View) {
            view.animate().alpha(1f).setDuration(200).withEndAction {
                ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_HARDWARE, null)
                ViewCompat.setLayerPaint(view, null)
            }.start()
        }
    }
}