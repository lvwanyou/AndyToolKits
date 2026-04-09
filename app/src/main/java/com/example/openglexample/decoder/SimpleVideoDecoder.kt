package com.example.openglexample.decoder

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException


class SimpleVideoDecoder(videoPath: String, private val outputSurface: Surface) {
    private var extractor: MediaExtractor? = null
    private var decoder: MediaCodec? = null
    private var mHeight: Int = 0
    private var mWidth: Int = 0

    init {
        setupDecoder(videoPath)
    }

    private fun setupDecoder(videoPath: String) {
        try {
            extractor = MediaExtractor()
            extractor!!.setDataSource(videoPath)

            // 选择视频轨道
            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime!!.startsWith("video/")) {
                    extractor!!.selectTrack(i)
                    decoder = MediaCodec.createDecoderByType(mime)
                    decoder!!.configure(format, outputSurface, null, 0)
                    mWidth = format.getInteger(MediaFormat.KEY_WIDTH)
                    mHeight = format.getInteger(MediaFormat.KEY_HEIGHT)
                    break
                }
            }

            if (decoder == null) {
                throw RuntimeException("Unable to find video track")
            }

            decoder!!.start()
        } catch (e: IOException) {
            Log.e(TAG, "Error setting up decoder", e)
        }
    }

    fun decode() {
        val inputBuffers = decoder!!.inputBuffers
        var outputBuffers = decoder!!.outputBuffers
        val info = MediaCodec.BufferInfo()
        var isEOS = false
        val startMs = System.currentTimeMillis()

        loop@ while (!Thread.interrupted()) {
            if (!isEOS) {
                // 获取可用的输入缓冲区索引
                val inIndex = decoder!!.dequeueInputBuffer(TIMEOUT_US)
                if (inIndex >= 0) {
                    val buffer = inputBuffers[inIndex]

                    // 从 MediaExtractor 读取数据到 buffer 中
                    val sampleSize = extractor!!.readSampleData(buffer, 0)
                    if (sampleSize < 0) {
                        // We have reached the end of the stream
                        decoder!!.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEOS = true
                    } else {
                        // 将数据提交给解码器，包括时间戳
                        decoder!!.queueInputBuffer(inIndex, 0, sampleSize, extractor!!.sampleTime, 0)
                        // 移动到下一帧
                        extractor!!.advance()
                    }
                }
            }

            // 处理输出
            val outIndex = decoder!!.dequeueOutputBuffer(info, TIMEOUT_US)
            when (outIndex) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> outputBuffers = decoder!!.outputBuffers
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                else -> if (outIndex >= 0) {
                    // 计算解码时间戳和当前时间的差异，进行适当的延迟
                    val presentationTimeUs = info.presentationTimeUs
                    val delayMs = (presentationTimeUs / 1000) - (System.currentTimeMillis() - startMs)
                    if (delayMs > 0) {
                        Thread.sleep(delayMs)
                    }

                    val buffer = outputBuffers[outIndex]
                    // 创建一个与视频帧大小相匹配的空Bitmap
//                    val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
//                    // 将ByteBuffer的数据复制到Bitmap中
//                    buffer.rewind() // 确保ByteBuffer的position是0
//                    bitmap.copyPixelsFromBuffer(buffer)

                    // 如果使用 Surface 进行输出，不需要手动处理 ByteBuffer
                    // 否则，这里可以处理解码后的数据
                    val doRender = (info.size != 0)
                    // 释放指定索引的输出缓冲区。index为缓冲区索引，render为是否渲染该缓冲区。
                    decoder!!.releaseOutputBuffer(outIndex, doRender)

                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                        break@loop
                    }
                }
            }
            if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "Decoding completed")
                break
            }
        }

        val endMs = System.currentTimeMillis()
        Log.d(TAG, "Decoding took " + (endMs - startMs) + "ms")

        release()
    }

    fun release() {
        if (decoder != null) {
            decoder!!.stop()
            decoder!!.release()
            decoder = null
        }
        if (extractor != null) {
            extractor!!.release()
            extractor = null
        }
    }

    companion object {
        private const val TAG = "SimpleVideoDecoder"
        private const val TIMEOUT_US: Long = 10000
    }
}