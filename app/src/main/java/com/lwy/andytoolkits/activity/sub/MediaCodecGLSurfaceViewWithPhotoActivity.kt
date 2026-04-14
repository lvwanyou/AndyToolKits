package com.lwy.andytoolkits.activity.sub

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.lwy.andytoolkits.R
import com.lwy.andytoolkits.widget.CaptureViewAnimator2
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MediaCodecGLSurfaceViewWithPhotoActivity : AppCompatActivity() {

    //    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var mCaptureViewAnimation: CaptureViewAnimator2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediacodec_glsv_photo)

//        glSurfaceView = findViewById(R.id.glSv)
//        glSurfaceView.setEGLContextClientVersion(2)
//        glSurfaceView.setRenderer(SimpleRenderer())
//        mCaptureViewAnimation = CaptureViewAnimator2(glSurfaceView)

        val imgView = findViewById<ImageView>(R.id.glSv)
        mCaptureViewAnimation = CaptureViewAnimator2(imgView)

        findViewById<TextView>(R.id.txtShow).setOnClickListener {
            // 开始动画
            mCaptureViewAnimation.rotate {
                // 动画结束回调
                Log.d("Animation", "Completed")
            }
        }
    }

    private inner class SimpleRenderer : GLSurfaceView.Renderer {
        private var textureId: Int = 0
        private lateinit var vertexBuffer: FloatBuffer
        private lateinit var textureBuffer: FloatBuffer
        private var shaderProgram: Int = 0
        private var positionHandle: Int = 0
        private var textureCoordHandle: Int = 0

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
            precision mediump float;
            varying vec2 textureCoordinate;
            uniform sampler2D s_texture;
            void main() {
                gl_FragColor = texture2D(s_texture, textureCoordinate);
            }
        """.trimIndent()

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glEnable(GLES20.GL_TEXTURE_2D)

            // 加载纹理
            textureId = loadTexture(R.drawable.aa)

            // 初始化顶点和纹理坐标缓冲区
            vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                    put(vertices)
                    position(0)
                }

            textureBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                    put(textureCoords)
                    position(0)
                }

            // 编译和链接着色器程序
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
            shaderProgram = GLES20.glCreateProgram().apply {
                GLES20.glAttachShader(this, vertexShader)
                GLES20.glAttachShader(this, fragmentShader)
                GLES20.glLinkProgram(this)
            }

            // 获取着色器程序中的属性位置
            positionHandle = GLES20.glGetAttribLocation(shaderProgram, "position")
            textureCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "inputTextureCoordinate")
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            // 使用着色器程序
            GLES20.glUseProgram(shaderProgram)

            // 绑定纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

            // 设置顶点坐标数据
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

            // 设置纹理坐标数据
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

        private fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }

        private fun loadTexture(resourceId: Int): Int {
            val textureHandle = IntArray(1)
            GLES20.glGenTextures(1, textureHandle, 0)

            if (textureHandle[0] != 0) {
                val options = BitmapFactory.Options().apply {
                    inScaled = false
                }

                val bitmap = BitmapFactory.decodeResource(resources, resourceId, options)

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR
                )
                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR
                )

                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    bitmap.width, bitmap.height, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(bitmapToByteArray(bitmap))
                )

                bitmap.recycle()
            }

            if (textureHandle[0] == 0) {
                throw RuntimeException("Error loading texture.")
            }

            return textureHandle[0]
        }

        private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
            val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            return byteBuffer.array()
        }
    }
}
