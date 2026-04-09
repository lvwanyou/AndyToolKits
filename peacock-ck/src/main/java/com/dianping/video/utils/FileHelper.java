package com.dianping.video.utils;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {

    private static void copyAssetToDst(Context context, String assetName, String dstFilePath) {
        File outFile = new File(dstFilePath);
        if (outFile.exists()) { // 已存在则不复制
            android.util.Log.e("APP", "dstFilePath is exists");
            return;
        }
        InputStream inputStream = null;
        OutputStream outStream = null;
        try {
            inputStream = context.getAssets().open(assetName);
            outStream = new FileOutputStream(outFile);
            byte[] data = new byte[128];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                outStream.write(data, 0, nRead);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyAssetsToDst(Context context, String srcPath, String dstFilePath) {
        File outPath = new File(dstFilePath);
        if (outPath.exists()) { // 已存在则不复制
            android.util.Log.e("APP", "dstFilePath is exists");
            return;
        }

        try {
            String fileNames[] = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(dstFilePath);
                if (!file.exists()) file.mkdirs();
                for (String fileName : fileNames) {
                    if (!srcPath.equals("")) { // assets 文件夹下的目录
                        copyAssetsToDst(context, srcPath + File.separator + fileName, dstFilePath + File.separator + fileName);
                    } else { // assets 文件夹
                        copyAssetsToDst(context, fileName, dstFilePath + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(dstFilePath);
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 2      * 根据byte数组，生成文件
     * 3
     */
    public static void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {//判断文件目录是否存在
                boolean mkdirs = dir.mkdirs();
                if (!mkdirs)
                    return;
            }
            file = new File(dir, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
