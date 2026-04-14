package com.lwy.andytoolkits.unitTestDemo;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// 最常用的文件自定义格式持久化和读取
public class SimpleMock {

    /**
     * 读取文本内容
     * @return 本地配置内容
     */
    public String readAndCheckData(Context context) {
        File file = new File(context.getFilesDir(), "/test.dat");
        if (file.exists()) {
            try(FileInputStream inputStream = new FileInputStream(file)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                if (line != null && line.startsWith("magic")) {
                    return line.substring("magic".length());
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void saveConfig(Context context, String line) {
        File file = new File(context.getFilesDir(), "/test.dat");
        if (!file.exists()) {
            File parent = file.getParentFile();
            //noinspection ConstantConditions
            if (!parent.exists()) {
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(("magic" + line).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}