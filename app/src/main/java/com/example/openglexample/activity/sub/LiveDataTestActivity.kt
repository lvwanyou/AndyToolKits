package com.example.openglexample.activity.sub

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.example.openglexample.R

class LiveDataTestActivity : AppCompatActivity() {

    private lateinit var viewModel: MyViewModel

    class MyViewModel() : ViewModel() {
        val liveData = MutableLiveData<String>()

        fun updateData(newValue: String) {
            liveData.value = newValue
        }
    }

    class MyViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
                return MyViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data_test)

        // 获取 ViewModel 实例
        // 创建 ViewModelFactory
        val factory = MyViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(MyViewModel::class.java)

        // 观察 LiveData
        val textView = findViewById<TextView>(R.id.txtLiveData)
        viewModel.liveData.observe(this, Observer { newValue ->
            // 更新UI
            textView.text = newValue
        })

        val button = findViewById<Button>(R.id.btnLiveData)
        // 更新 LiveData 的值
        button.setOnClickListener {
            val tempStr = "Button Clicked : " + System.currentTimeMillis()
            viewModel.updateData(tempStr)
        }
    }


    private val _livedata: MutableLiveData<String> = MutableLiveData()
    val liveData: LiveData<String> = _livedata
//    fun manager(x: T) {
//        _livedata.postValue(x)
//        _livedata.setValue(x)
//    }
//
//    fun observeX( observer: Observer<T>){
//        liveData.observe(this, Observer)
//    }
}