package com.lwy.andytoolkits.activity.sub

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.lwy.andytoolkits.R

class CodecTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codec_test)
        initViews()
    }

    private fun initViews() {
        val button1 = findViewById<Button>(R.id.decode_720)
        val button2 = findViewById<Button>(R.id.decode_1080)
        val button3 = findViewById<Button>(R.id.encode_720)
        val button4 = findViewById<Button>(R.id.encode_1080)
        button1.setOnClickListener { createMediaDecoder(720) }
        button2.setOnClickListener { createMediaEncoder(720) }
        button3.setOnClickListener { createMediaDecoder(1080) }
        button4.setOnClickListener { createMediaEncoder(1080) }
    }

    private fun createMediaDecoder(width: Int): String {
        val format = MediaFormat.createVideoFormat("video/avc", width, width * 4 / 3)
        var name = ""
        var decoder: MediaCodec? = null
        try {
            decoder = MediaCodec.createDecoderByType("video/avc")
            name = decoder.name
            decoder.configure(format, null, null, 0)
            decoder.start()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                decoder?.release()
            } catch (_: Exception) {

            }
            return e.message ?: ("$name:false")
        }
        return "$name:true"
    }

    private fun createMediaEncoder(width: Int): String {
        val format = MediaFormat.createVideoFormat("video/avc", width, width * 4 / 3)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1000 * 1024)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        var name = ""
        var encoder: MediaCodec? = null
        try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            name = codecList.findEncoderForFormat(format)
            encoder = MediaCodec.createByCodecName(name)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                encoder?.release()
            } catch (e: Exception) {

            }
            return e.message ?: ("$name:false")
        }
        return "$name:true"
    }
}