package com.example.openglexample.unitTestDemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleMockTest {

    @Rule
    public TemporaryFolder rootFolder = new TemporaryFolder();

    SimpleMock simpleMock;
    Context mockContext;

    @Before
    public void setUp() throws IOException {
        simpleMock = new SimpleMock();
        mockContext = Mockito.mock(Context.class);
        Mockito.when(mockContext.getFilesDir()).thenReturn(rootFolder.newFolder());
    }

    @Test
    public void testSaveAndRead() throws IOException {
        simpleMock.saveConfig(mockContext, "醉里挑灯看剑");
        assertEquals("醉里挑灯看剑", simpleMock.readAndCheckData(mockContext));

        simpleMock.saveConfig(mockContext, "梦回吹角连营");
        File file = new File(mockContext.getFilesDir(), "test.dat");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write("4455".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
        assertNull(simpleMock.readAndCheckData(mockContext));
    }



}