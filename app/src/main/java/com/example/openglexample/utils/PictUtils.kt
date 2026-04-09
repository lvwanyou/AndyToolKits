package com.example.openglexample.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.sqrt

object PictUtils {
    const val TAG = "NoteTitleRecommendManager"
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    @SuppressLint("LongLogTag")
    fun getPathFromUri(context: Context, uri: Uri): String? {
        var path: String? = null
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // 如果是Document类型的URI，则通过Document ID来进行解析
                val documentId = DocumentsContract.getDocumentId(uri)
                if (isExternalStorageDocument(uri)) {
                    // 如果是外部存储器的URI，则获取SD卡的路径
                    val split = documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (split.size >= 2) {
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        }
                    }
                } else if (isDownloadsDocument(uri)) {
                    // 如果是Downloads文件夹的URI，则获取Downloads文件夹的路径
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), documentId.toLong())
                    path = getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    // 如果是Media类型的URI，则获取Media文件的路径
                    val split = documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (split.size >= 2) {
                        val type = split[0]
                        var contentUri: Uri? = null
                        if ("image" == type) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == type) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == type) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        path = contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
                    }
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                // 如果是Content类型的URI，则直接通过ContentResolver进行解析
                path = getDataColumn(context, uri, null, null)
                if (path == null && uri.path!!.endsWith(".zip")) {
                    path = uri.path
                }
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                // 如果是File类型的URI，则直接获取文件路径
                path = uri.path
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPathFromUri error, and " + e.message)
        }
        return path
    }

    @SuppressLint("LongLogTag")
    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            Log.d(TAG, "getDataColumn uri: $uri")
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        } catch (exception: IllegalArgumentException) {
            Log.e(TAG, "_data is not exist, and " + exception.message)
        } finally {
            cursor?.close()
        }
        return path
    }

    @Throws(IOException::class)
    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        return bitmap
    }


    // 降采样方案一
    fun downSampleToScreenSize(bitmap: Bitmap, resources: Resources): Bitmap {
        val screenWidth = resources.displayMetrics.widthPixels / 2
        val screenHeight = resources.displayMetrics.heightPixels / 2

        val imgWidth = bitmap.getWidth()
        val imgHeight = bitmap.getHeight()
        if (imgWidth <= screenWidth && imgHeight <= screenHeight) {
            return bitmap // 如果图片大小未超过屏幕大小，则不变
        }
        var scale = screenWidth.toFloat() / imgWidth
        if (imgHeight * scale > screenHeight) {
            scale = screenHeight.toFloat() / imgHeight
        }
        val newWidth = Math.round(imgWidth * scale)
        val newHeight = Math.round(imgHeight * scale)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private const val SCALE_SIZE_AREA = 25000
    fun scaleBitmapDown(bitmap: Bitmap): Bitmap {
        var scaleRatio = -1.0
        val maxDimension: Int
        if (SCALE_SIZE_AREA > 0) {
            maxDimension = bitmap.getWidth() * bitmap.getHeight()
            if (maxDimension > SCALE_SIZE_AREA) {
                scaleRatio = sqrt(SCALE_SIZE_AREA.toDouble() / maxDimension.toDouble())
            }
        }
        return if (scaleRatio <= 0.0) bitmap else Bitmap.createScaledBitmap(bitmap, ceil(bitmap.getWidth().toDouble() * scaleRatio).toInt(), ceil(bitmap.getHeight().toDouble() * scaleRatio).toInt(), false)
    }

    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        // 获取原始宽高
        val width = bitmap.width
        val height = bitmap.height

        // 计算等比缩放的目标尺寸
        val aspectRatio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxWidth
            targetHeight = (maxWidth / aspectRatio).toInt()
        } else {
            targetHeight = maxHeight
            targetWidth = (maxHeight * aspectRatio).toInt()
        }

        // 调整Bitmap大小
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

        return scaledBitmap
    }

    /**
     * 模糊图片
     * blurRadius 可以设置的范围是从 1.0（最小模糊）到 25.0（最大模糊）
     */
    fun blurBitmap(context: Context, image: Bitmap, blurRadius: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(image)
        val renderScript = RenderScript.create(context)
        val tmpIn = Allocation.createFromBitmap(renderScript, image)
        val tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)
        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.setRadius(blurRadius)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }
}