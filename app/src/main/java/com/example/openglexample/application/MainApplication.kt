package com.example.openglexample.application

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 监听整个应用中各个 Activity 的生命周期事件
        registerActivityLifecycleCallbacks(AppLifecycleCallbacks())

        var startWorkTimeMillis = 0L
        Looper.getMainLooper().setMessageLogging {
            if (it.startsWith(">>>>> Dispatching to Handler")) {
                startWorkTimeMillis = System.currentTimeMillis()
            } else if (it.startsWith("<<<<< Finished to Handler")) {
                val duration = System.currentTimeMillis() - startWorkTimeMillis
                if (duration > 100) {
                    Log.e("主线程执行耗时过长", "$duration 毫秒，$it")
                }
            }
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    // TODO 主线程崩溃，自行上报崩溃信息
                    if (e.message != null && e.message!!.startsWith("Unable to start activity")) {
                        android.os.Process.killProcess(android.os.Process.myPid())
                        break
                    }
                    e.printStackTrace()
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            e.printStackTrace()
            // TODO 异步线程崩溃，自行上报崩溃信息
        }
    }

}