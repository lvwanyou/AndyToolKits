package com.example.openglexample.activity

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import com.example.openglexample.utils.AppFileUtils
import com.example.openglexample.utils.AssertsUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MediaCodecGLSurfaceView2Activity : AppCompatActivity() {

//    private lateinit var glSurfaceView: GLSurfaceView
    private var videoPath: String = "path_to_your_video_file"

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//        glSurfaceView = findViewById(R.id.glSv)
//        glSurfaceView.setEGLContextClientVersion(2)
//        glSurfaceView.setRenderer(VideoRenderer(videoPath))
//    }
    private var glSurfaceView: VideoGLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glSurfaceView = VideoGLSurfaceView(this)
        setContentView(glSurfaceView)

        var videoRootPath = "decoder"
        try {
            val templateFiles = assets.list(videoRootPath)
            for (templateFile in templateFiles!!) {
                AssertsUtils.writeToStorage(this, "$videoRootPath/$templateFile", true)
            }

            videoRootPath = AppFileUtils.getCache(this) + videoRootPath
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val videoPath = videoRootPath + File.separator + "7260802466709635107.MP4"

        // 设置视频路径
        glSurfaceView!!.setVideoPath(videoPath)
        glSurfaceView!!.playVideo()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView!!.onPause()
        glSurfaceView!!.stopVideo()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView!!.onResume()
        glSurfaceView!!.playVideo()
    }

    class VideoGLSurfaceView(context: Context?) : GLSurfaceView(context) {
        private val renderer: VideoRenderer

        init {
            // 设置OpenGL ES版本
            setEGLContextClientVersion(2)

            // 创建渲染器
            renderer = VideoRenderer(context)
            setRenderer(renderer)
        }

        fun setVideoPath(path: String?) {
            renderer.setVideoPath(path)
        }

        fun playVideo() {
            renderer.playVideo()
        }

        fun stopVideo() {
            renderer.stopVideo()
        }
    }

    class VideoRenderer(private val context: Context?) : GLSurfaceView.Renderer, OnFrameAvailableListener {
        private var surfaceTexture: SurfaceTexture? = null
        private var textureId = 0
        private var mediaCodec: MediaCodec? = null
        private var videoPath: String? = null
        private var updateSurface = false
        private var surface: Surface? = null

        fun setVideoPath(path: String?) {
            this.videoPath = path
        }

        fun playVideo() {
            if (mediaCodec != null) {
                mediaCodec!!.start()
            }
        }

        fun stopVideo() {
            if (mediaCodec != null) {
                mediaCodec!!.stop()
            }
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            // 创建纹理
            textureId = createTexture()

            // 创建SurfaceTexture
            surfaceTexture = SurfaceTexture(textureId)
            surfaceTexture!!.setOnFrameAvailableListener(this)

            // 创建Surface
            surface = Surface(surfaceTexture)

            // 初始化MediaCodec
            initMediaCodec()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            synchronized(this) {
                if (updateSurface) {
                    surfaceTexture!!.updateTexImage()
                    updateSurface = false
                }
            }

            // 清除屏幕
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            // 渲染SurfaceTexture
            // 这里你需要实现纹理的绘制，通常是通过一个简单的着色器程序
            // 例如：将纹理绘制到全屏四边形上
            drawTexture(textureId)
        }

        override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
            synchronized(this) {
                updateSurface = true
            }
        }

        private fun createTexture(): Int {
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            return textures[0]
        }

        private fun initMediaCodec() {
            try {
                mediaCodec = MediaCodec.createDecoderByType("video/avc")
                val format = MediaFormat.createVideoFormat("video/avc", 1280, 720)
                mediaCodec!!.configure(format, surface, null, 0)

                // 设置视频源
                Thread {
                    try {
                        val fis = FileInputStream(videoPath)
                        val fileChannel = fis.channel
                        val inputBuffers = mediaCodec!!.inputBuffers
                        var sampleSize: Int
                        var inputBufferIndex: Int

                        while (true) {
                            inputBufferIndex = mediaCodec!!.dequeueInputBuffer(-1)
                            if (inputBufferIndex >= 0) {
                                val inputBuffer = inputBuffers[inputBufferIndex]
                                inputBuffer.clear()
                                sampleSize = fileChannel.read(inputBuffer)
                                if (sampleSize < 0) {
                                    // End of stream
                                    mediaCodec!!.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                    break
                                } else {
                                    mediaCodec!!.queueInputBuffer(inputBufferIndex, 0, sampleSize, 0, 0)
                                }
                            }
                        }


                        fileChannel.close()
                        fis.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }.start()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }


        private fun drawTexture(textureId: Int) {
            // 这里应该添加实际的绘制逻辑，例如使用一个简单的着色器程序绘制纹理
            // 下面是一个简单的示例，使用一个基本的着色器程序绘制纹理

            // 使用你的着色器程序
            GLES20.glUseProgram(shaderProgram)

            // 绑定纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

            // 设置顶点和纹理坐标
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            GLES20.glEnableVertexAttribArray(textureCoordHandle)
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

            // 绘制
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            // 禁用顶点数组
            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(textureCoordHandle)
        }

        private val vertexShaderCode = """
    attribute vec4 position;
    attribute vec2 inputTextureCoordinate;
    varying vec2 textureCoordinate;
    void main() {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate;
    }
""".trimIndent()

        private val fragmentShaderCode = """
    #extension GL_OES_EGL_image_external : require
    precision mediump float;
    varying vec2 textureCoordinate;
    uniform samplerExternalOES s_texture;
    void main() {
        gl_FragColor = texture2D(s_texture, textureCoordinate);
    }
""".trimIndent()

        private val vertices = floatArrayOf(
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
        )

        private val textureCoords = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )

        private val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(vertices)
                position(0)
            }

        private val textureBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(textureCoords)
                position(0)
            }


        private val shaderProgram = GLES20.glCreateProgram().also {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        private val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "position")
        private val textureCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "inputTextureCoordinate")

        private fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}
