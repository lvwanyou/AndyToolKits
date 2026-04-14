package com.lwy.andytoolkits.unitTestDemo;

import android.os.Process;
import android.os.SystemClock;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Process.class, SystemClock.class, StaticMock.class})
public class StaticMockTest {

    @Test
    public void tooYoungTooSimple() {
        StaticMock mock = new StaticMock();
        PowerMockito.mockStatic(Process.class);
        PowerMockito.when(Process.getStartElapsedRealtime()).thenReturn(200L);
        PowerMockito.mockStatic(SystemClock.class);
        PowerMockito.when(SystemClock.elapsedRealtime()).thenReturn(300L);
        Assert.assertFalse(mock.findOnePiece());
    }

    @Test(expected = RuntimeException.class)
    public void bombPlan() {
        StaticMock mock = new StaticMock();
        PowerMockito.mockStatic(Process.class);
        PowerMockito.when(Process.getStartElapsedRealtime()).thenReturn(200L);
        PowerMockito.mockStatic(SystemClock.class);
        PowerMockito.when(SystemClock.elapsedRealtime()).thenReturn(11L * 365 * 24 * 60 * 60 * 1000);
        mock.testException();
    }

    @Test
    public void testPrivateMethod() throws Exception {
        StaticMock mock = new StaticMock();
        String result = Whitebox.invokeMethod(mock, "privateMethod");

        // Assert
        Assert.assertEquals("Test", result);
    }

}