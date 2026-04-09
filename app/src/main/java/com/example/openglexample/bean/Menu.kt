package com.example.openglexample.bean

import android.view.View

class Menu(
    private val title: String?, private val icon: Int?, private var menuClickListener: ((v: View?) -> Unit)?, private val subItems: HashMap<String, ((v: View?) -> Unit)?>? = null
) {
    var mTitle: String? = null
    var mIcon: Int? = null
    var mMenuClickListener: ((v: View?) -> Unit)? = null
    var mSubItems: HashMap<String, ((v: View?) -> Unit)?>? = null

    init {
        mTitle = title
        mIcon = icon
        mSubItems = subItems
        mMenuClickListener = menuClickListener
    }
}