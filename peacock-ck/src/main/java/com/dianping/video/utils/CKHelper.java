package com.dianping.video.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dianping.codelog.NovaCodeLog;

public class CKHelper {
    public static void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static void novaCodeLogE(Class clazz, String subTag, String message) {
        if (Const.isDebug) {
            Log.e(subTag, message);
        }
        NovaCodeLog.e(clazz, subTag, message);
    }

    public static void novaCodeLogI(Class clazz, String subTag, String message) {
        if (Const.isDebug) {
            Log.e(subTag, message);
        }
        NovaCodeLog.i(clazz, subTag, message);
    }
}
