package com.example.openglexample.activity.sub

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.example.openglexample.R
import com.example.openglexample.widget.rulerView.v2.RulerViewReal
import com.example.openglexample.widget.rulerView.v3.RulerWheel

class RulerViewTestActivity : AppCompatActivity(), RulerViewReal.OnValueChangeListener {
    private var mTextView: TextView? = null
    private var mTextViewReal: TextView? = null

    private var rulerView: RulerWheel? = null
    private var tvCurValue: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruler_view_test)
        mTextView = findViewById<View>(R.id.text) as TextView

        (findViewById<View>(R.id.height_ruler_real) as RulerViewReal).setOnValueChangeListener(this)
        mTextViewReal = findViewById<View>(R.id.text_real) as TextView

        val list = mutableListOf<String>()
        for (i in 30 until 150) {
            list.add(i.toString())
            for (j in 1 until 10) {
                list.add("$i.$j")
            }
        }
        tvCurValue = findViewById<View>(R.id.curValue_tv) as TextView
        rulerView = findViewById<View>(R.id.ruler_view) as RulerWheel
        rulerView!!.setData(list)
        rulerView!!.setScrollingListener(object : RulerWheel.OnWheelScrollListener<String?> {
            override fun onChanged(wheel: RulerWheel?, oldValue: String?, newValue: String?) {
                tvCurValue!!.text = newValue + ""
            }

            override fun onScrollingFinished(wheel: RulerWheel?) {
            }

            override fun onScrollingStarted(wheel: RulerWheel?) {
            }
        })

    }

    override fun onChange(view: RulerViewReal?, selectedValue: Int, deltaOffsetIndex: Float) {
        when (view!!.id) {
            R.id.height_ruler_real -> mTextViewReal!!.text = "Your height is $selectedValue meters"
        }
    }
}