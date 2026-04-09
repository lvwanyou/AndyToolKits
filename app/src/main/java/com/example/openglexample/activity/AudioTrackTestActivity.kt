package com.example.openglexample.activity

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.example.openglexample.R
import com.example.openglexample.model.AudioConfigInfo

class AudioTrackTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AudioTrackTest"
    }

    private var mAudioTrack: AudioTrack? = null
    private val audioConfigInfo: AudioConfigInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audiotrack_test)

        testAudioTrackCreation()
//        val tempStr = "Button Clicked"
//        val tvLog = findViewById<TextView>(R.id.tv_log)
//
//        val bufsize = AudioTrack.getMinBufferSize(
//            audioConfigInfo.sampleRate,
//            if (audioConfigInfo.channelCount === 2) AudioFormat.CHANNEL_CONFIGURATION_STEREO else AudioFormat.CHANNEL_CONFIGURATION_MONO,
//            if (audioConfigInfo.bitWidth === 16) AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_8BIT
//        ) //一个采样点16比特-2个字节
//
//        mAudioTrack = AudioTrack(
//            AudioManager.STREAM_MUSIC, audioConfigInfo.sampleRate,
//            if (audioConfigInfo.channelCount === 2) AudioFormat.CHANNEL_CONFIGURATION_STEREO else AudioFormat.CHANNEL_CONFIGURATION_MONO,
//            if (audioConfigInfo.bitWidth === 16) AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_8BIT,
//            bufsize,
//            AudioTrack.MODE_STREAM
//        )
    }

    fun testAudioTrackCreation() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)    // 14144

        val audioTracks = arrayOfNulls<AudioTrack>(100)

        for (i in audioTracks.indices) {
            try {
                audioTracks[i] = AudioTrack(
                    AudioManager.STREAM_MUSIC, sampleRate, channelConfig,
                    audioFormat, bufferSize, AudioTrack.MODE_STATIC
                )

                if (audioTracks[i]?.state == AudioTrack.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioTrack $i initialized successfully")
                } else {
                    Log.e(TAG, "AudioTrack $i initialization failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating AudioTrack $i", e)
            }
        }

        // 释放资源
        for (audioTrack in audioTracks) {
            audioTrack?.release()
        }
    }
}