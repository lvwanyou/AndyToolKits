package com.example.openglexample.utils

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object FileUtils {
    fun readRawTextFile(context: Context, resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun readRawTextFileWithoutExplanation(context: Context, resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val commentStartIndex = line!!.indexOf("//")
                if (commentStartIndex != -1) {
                    // 如果该行包含注释，则只添加注释之前的部分
                    line = line?.substring(0, commentStartIndex)
                }
                // 如果该行不为空，则添加到StringBuilder中
                if (line?.isNotBlank() == true) {
                    sb.append(line).append("\n")
                }
            }
            reader.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}