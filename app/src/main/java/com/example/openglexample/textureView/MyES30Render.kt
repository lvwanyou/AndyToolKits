package com.example.openglexample.textureView

import android.opengl.GLES30
import android.util.Log
import android.view.TextureView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig

class MyES30Renderer : EGL14TextureView.Renderer {
    private var glVersion: String? = null
    private var program = 0
    private var positionHandle = 0
    private var offsetHandle = 0

    private var vertexBuffer: FloatBuffer? = null
    private var offsetBuffer: FloatBuffer? = null

    fun onSurfaceCreated(config: EGLConfig?) {
        // 获取 OpenGL ES 版本
        glVersion = GLES30.glGetString(GLES30.GL_VERSION)
        Log.d("MyRenderer", "OpenGL ES 版本: $glVersion")

        // 获取更多详细信息
        val vendor = GLES30.glGetString(GLES30.GL_VENDOR)
        val renderer = GLES30.glGetString(GLES30.GL_RENDERER)
        val extensions = GLES30.glGetString(GLES30.GL_EXTENSIONS)

        Log.d("MyRenderer", "供应商: $vendor")
        Log.d("MyRenderer", "渲染器: $renderer")
        Log.d("MyRenderer", "扩展: $extensions")

        // 设置清除颜色
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 编译着色器并链接程序
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        // 获取属性位置
        positionHandle = GLES30.glGetAttribLocation(program, "vPosition")
        offsetHandle = GLES30.glGetAttribLocation(program, "vOffset")

        // 初始化顶点缓冲区
        vertexBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer?.put(triangleCoords)?.position(0)

        // 初始化偏移缓冲区
        offsetBuffer = ByteBuffer.allocateDirect(offsets.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        offsetBuffer!!.put(offsets).position(0)
    }

    override fun onSurfaceCreated(config: android.opengl.EGLConfig?) {
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        // 设置视口
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        // 清除颜色缓冲区
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 使用程序
        GLES30.glUseProgram(program)

        // 启用顶点属性数组
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(
            positionHandle, 3,
            GLES30.GL_FLOAT, false,
            12, vertexBuffer
        )

        // 启用实例属性数组
        GLES30.glEnableVertexAttribArray(offsetHandle)
        GLES30.glVertexAttribPointer(
            offsetHandle, 2,
            GLES30.GL_FLOAT, false,
            8, offsetBuffer
        )
        // 设置实例属性除数
        GLES30.glVertexAttribDivisor(offsetHandle, 1)

        // 绘制实例化的三角形
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 3, 4)

        // 禁用顶点属性数组
        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(offsetHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        // 检查编译状态
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e("MyRenderer", "着色器编译失败: " + GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    companion object {
        private const val vertexShaderCode = "#version 300 es\n" +
                "layout (location = 0) in vec4 vPosition;\n" +
                "layout (location = 1) in vec2 vOffset;\n" +
                "out vec4 vColor;\n" +
                "void main() {\n" +
                "    gl_Position = vPosition + vec4(vOffset, 0.0, 0.0);\n" +
                "    vColor = vec4(vOffset * 0.5 + 0.5, 0.0, 1.0);\n" +
                "}"

        private const val fragmentShaderCode = "#version 300 es\n" +
                "precision mediump float;\n" +
                "in vec4 vColor;\n" +
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "    fragColor = vColor;\n" +
                "}"

        private val triangleCoords = floatArrayOf(
            0.0f, 0.1f, 0.0f,
            -0.1f, -0.1f, 0.0f,
            0.1f, -0.1f, 0.0f
        )

        private val offsets = floatArrayOf(
            -0.5f, -0.5f,
            0.5f, -0.5f,
            -0.5f, 0.5f,
            0.5f, 0.5f
        )
    }
}