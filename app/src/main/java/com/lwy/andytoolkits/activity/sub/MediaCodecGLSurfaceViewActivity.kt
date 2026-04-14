package com.lwy.andytoolkits.activity.sub

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import com.lwy.andytoolkits.R
import com.lwy.andytoolkits.decoder.SimpleVideoDecoder
import com.lwy.andytoolkits.utils.AppFileUtils
import com.lwy.andytoolkits.utils.AssertsUtils
import com.lwy.andytoolkits.utils.OpenGlUtils
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MediaCodecGLSurfaceViewActivity : AppCompatActivity() {
    private lateinit var surfaceView: GLSurfaceView
    private var surface: Surface? = null
    private var decoder: SimpleVideoDecoder? = null

    @SuppressLint("MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediacodec_glsv_test)

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
        val photoPath = videoRootPath + File.separator + "cover.jpg"

        surfaceView = findViewById(R.id.glSv)
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setRenderer(object : GLSurfaceView.Renderer {
            private var surfaceTexture: SurfaceTexture? = null
            private var textureId: Int = 0

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                // 创建一个 OpenGL 纹理
                val textures = IntArray(1)
                GLES20.glGenTextures(1, textures, 0)
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
                GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST.toFloat()
                )
                GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR.toFloat()
                )
                GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE
                )
                GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE
                )
                textureId = textures[0]

                // 创建 SurfaceTexture 并将其与 OpenGL 纹理绑定
                surfaceTexture = SurfaceTexture(textureId)
                surface = Surface(surfaceTexture)

                // 初始化解码器
                decoder = SimpleVideoDecoder(videoPath, surface!!)

                // 在一个新线程中运行解码过程
                Thread {
                    decoder?.decode()
                }.start()
            }

            override fun onDrawFrame(gl: GL10?) {
                // 更新 SurfaceTexture 并绘制
                surfaceTexture?.updateTexImage()

                // 清除屏幕并绘制纹理
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                OpenGlUtils.convertTexture2Bitmap(1080, 1440, textureId)


                drawTexture(textureId)
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

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        decoder?.release()
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