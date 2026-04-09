package com.example.openglexample.unitTestDemo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

public class SimpleSpyTest {

    @Test
    public void catAllDiedOrAlive() {
        SimpleSpy spy = Mockito.spy(new SimpleSpy());
        Mockito.doReturn("died").when(spy).magicBox();
        assertFalse(spy.isSchrodingerRight());
        Mockito.doReturn("alive").when(spy).magicBox();
        assertFalse(spy.isSchrodingerRight());
    }

    @Test
    public void catRandomAlive() {
        SimpleSpy spy = Mockito.spy(new SimpleSpy());
        Random random = new Random();
        Mockito.doAnswer(invocation -> random.nextBoolean() ? "died" : "alive")
                .when(spy).magicBox();
        assertTrue(spy.isSchrodingerRight());
    }
}