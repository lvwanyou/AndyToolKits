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
    private lateinit var appearAlphaAnimator: AnimatorSet
    private lateinit var disappearAlphaAnimator: AnimatorSet

    private fun initAppearAlphaAnimator(holder: ViewHolder) {
        // 创建一个从0到height的动画
        val heightAnimator = ValueAnimator.ofInt(0, subMenuLayoutHeight)
        heightAnimator.addUpdateListener { animation ->
            // 在动画过程中，更新subItemsLayout的高度
            val currentHeight = animation.animatedValue as Int
            val params = holder.subItemsLayout.layoutParams
            params.height = currentHeight
            holder.subItemsLayout.layoutParams = params
        }
        // 创建一个alpha动画
        val alphaAnimator = ObjectAnimator.ofFloat(holder.subItemsLayout, "alpha", 0f, 1f)

        // 使用AnimatorSet同时执行这两个动画
        appearAlphaAnimator = AnimatorSet()
        appearAlphaAnimator.duration = 300
        appearAlphaAnimator.playTogether(heightAnimator, alphaAnimator)
        appearAlphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                holder.subItemsLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun initDisappearAlphaAnimator(holder: ViewHolder) {
        // 创建一个从startHeight到0的动画
        val heightAnimator = ValueAnimator.ofInt(subMenuLayoutHeight, 0)
        heightAnimator.addUpdateListener { animation ->
            // 在动画过程中，更新subItemsLayout的高度
            val currentHeight = animation.animatedValue as Int
            val params = holder.subItemsLayout.layoutParams
            params.height = currentHeight
            holder.subItemsLayout.layoutParams = params
        }
        // 创建一个alpha动画
        val alphaAnimator = ObjectAnimator.ofFloat(holder.subItemsLayout, "alpha", 1f, 0f)

        // 使用AnimatorSet同时执行这两个动画
        disappearAlphaAnimator = AnimatorSet()
        disappearAlphaAnimator.duration = 300
        disappearAlphaAnimator.playTogether(heightAnimator, alphaAnimator)
        disappearAlphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                holder.subItemsLayout.visibility = View.GONE
            }
        })
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
        // 默认隐藏子项布局
        holder.subItemsLayout.visibility = View.GONE

        // 添加点击事件
        holder.itemView.setOnClickListener { v: View? ->
            run {
                initAppearAlphaAnimator(holder)
                initDisappearAlphaAnimator(holder)
                if (!menu.mSubItems.isNullOrEmpty()) {
                    if (holder.subItemsLayout.visibility == View.GONE) {
                        // 如果子项布局是隐藏的，那么展开它
                        // 开始动画
                        appearAlphaAnimator.start()
                    } else {
                        // 如果子项布局是可见的，那么折叠它
                        // 开始动画
                        disappearAlphaAnimator.start()
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
        val subItemsLayout: LinearLayout

        init {
            menuTitle = itemView.findViewById<TextView>(R.id.menu_title)
            menuIcon = itemView.findViewById<ImageView>(R.id.menu_icon)
            subItemsLayout = itemView.findViewById<LinearLayout>(R.id.sub_items_layout)
        }
    }
}