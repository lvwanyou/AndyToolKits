package com.lwy.andytoolkits.unitTestDemo;

import android.annotation.SuppressLint;
import android.os.Process;
import android.os.SystemClock;

@SuppressLint("NewApi")
public class StaticMock {

    private long startMs;

    public boolean findOnePiece() {
        if (startMs == 0) {
            startMs = Process.getStartElapsedRealtime();
            return false;
        }
        long delta = SystemClock.elapsedRealtime() - startMs;
        if (delta < 60 * 60 * 1000 * 24) {
            return false;
        }
        if (delta > 365L * 60 * 60 * 1000 * 24 * 10) {
            throw new RuntimeException("十年终成正果");
        }
        return true;
    }

    public void testException() {
        throw new RuntimeException("Test");
    }

    private String privateMethod() {
        return "Test";
    }
}
