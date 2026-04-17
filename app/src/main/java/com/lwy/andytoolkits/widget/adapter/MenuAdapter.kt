package com.lwy.andytoolkits.widget.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lwy.andytoolkits.R
import com.lwy.andytoolkits.bean.Menu


class MenuAdapter(private val menuList: MutableList<Menu>) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {
    private var subMenuLayoutHeight: Int = 0

    private fun createExpandAnimation(holder: ViewHolder, targetHeight: Int): AnimatorSet {
        // 创建一个从0到height的动画
        val heightAnimator = ValueAnimator.ofInt(0, targetHeight)
        heightAnimator.addUpdateListener { animation ->
            holder.subItemsLayout.layoutParams.height = animation.animatedValue as Int
            holder.subItemsLayout.requestLayout()
        }
        // 创建一个alpha动画
        val alphaAnimator = ObjectAnimator.ofFloat(holder.subItemsLayout, "alpha", 0f, 1f)

        // 使用AnimatorSet同时执行这两个动画
        val animatorSet = AnimatorSet()
        animatorSet.duration = 300
        animatorSet.playTogether(heightAnimator, alphaAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                holder.subItemsLayout.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // 动画结束后，设置为wrap_content确保布局正确
                holder.subItemsLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                holder.subItemsLayout.requestLayout()
            }
        })
        return animatorSet
    }

    private fun createCollapseAnimation(holder: ViewHolder, targetHeight: Int): AnimatorSet {
        // 创建一个从startHeight到0的动画
        val heightAnimator = ValueAnimator.ofInt(targetHeight, 0)
        heightAnimator.addUpdateListener { animation ->
            holder.subItemsLayout.layoutParams.height = animation.animatedValue as Int
            holder.subItemsLayout.requestLayout()
        }
        // 创建一个alpha动画
        val alphaAnimator = ObjectAnimator.ofFloat(holder.subItemsLayout, "alpha", 1f, 0f)

        // 使用AnimatorSet同时执行这两个动画
        val animatorSet = AnimatorSet()
        animatorSet.duration = 300
        animatorSet.playTogether(heightAnimator, alphaAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                holder.subItemsLayout.visibility = View.GONE
            }
        })
        return animatorSet
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        subMenuLayoutHeight = parent.resources.getDimensionPixelSize(R.dimen.main_sub_menu_height)

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.main_menu_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("Recycle")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]
        holder.menuTitle.text = menu.mTitle
        menu.mIcon?.let { holder.menuIcon.setImageResource(it) }

        // 清除子项布局，然后添加新的子项
        holder.subItemsLayout.removeAllViews()
        menu.mSubItems?.forEach { (t, v) ->
            val view: View = LayoutInflater.from(holder.itemView.context).inflate(R.layout.main_sub_menu_item, holder.subItemsLayout, false)
            val textView = view.findViewById<TextView>(R.id.sub_item_text)
            textView.text = t
            view.setOnClickListener {
                v?.invoke(it)
            }
            holder.subItemsLayout.addView(view)
        }

        // 根据是否有子项显示/隐藏 chevron 图标
        if (menu.mSubItems.isNullOrEmpty()) {
            holder.chevronIcon.visibility = View.GONE
            holder.subItemsLayout.visibility = View.GONE
        } else {
            holder.chevronIcon.visibility = View.VISIBLE
            holder.chevronIcon.rotation = 0f
            // 默认隐藏子项布局
            holder.subItemsLayout.visibility = View.GONE
            holder.subItemsLayout.layoutParams.height = 0
            holder.subItemsLayout.layoutParams = holder.subItemsLayout.layoutParams
        }

        // 添加点击事件
        holder.itemView.setOnClickListener { v: View? ->
            run {
                if (!menu.mSubItems.isNullOrEmpty()) {
                    if (holder.subItemsLayout.visibility == View.GONE) {
                        // 展开前先测量目标高度
                        holder.subItemsLayout.visibility = View.VISIBLE
                        holder.subItemsLayout.measure(
                            View.MeasureSpec.makeMeasureSpec(holder.itemView.measuredWidth, View.MeasureSpec.AT_MOST),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        val targetHeight = holder.subItemsLayout.measuredHeight
                        // 先设置为0，再开始动画
                        holder.subItemsLayout.layoutParams.height = 0
                        holder.subItemsLayout.requestLayout()

                        val expandAnimator = createExpandAnimation(holder, targetHeight)
                        expandAnimator.start()

                        // 图标旋转180度向上
                        ObjectAnimator.ofFloat(holder.chevronIcon, "rotation", 0f, 180f).apply {
                            duration = 300
                            start()
                        }
                    } else {
                        // 收起前先测量当前高度
                        val targetHeight = holder.subItemsLayout.measuredHeight
                        val collapseAnimator = createCollapseAnimation(holder, targetHeight)
                        collapseAnimator.start()

                        // 图标旋转180度向下
                        ObjectAnimator.ofFloat(holder.chevronIcon, "rotation", 180f, 0f).apply {
                            duration = 300
                            start()
                        }
                    }
                }
                menu.mMenuClickListener?.invoke(v)
            }
        }

        // 添加动画效果
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_transition_animation))
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuTitle: TextView
        val menuIcon: ImageView
        val chevronIcon: ImageView
        val subItemsLayout: LinearLayout

        init {
            menuTitle = itemView.findViewById<TextView>(R.id.menu_title)
            menuIcon = itemView.findViewById<ImageView>(R.id.menu_icon)
            chevronIcon = itemView.findViewById<ImageView>(R.id.chevron_icon)
            subItemsLayout = itemView.findViewById<LinearLayout>(R.id.sub_items_layout)
        }
    }
}