package com.lwy.andytoolkits.shape

import android.content.Context
import android.opengl.GLES20
import com.lwy.andytoolkits.R
import com.lwy.andytoolkits.utils.FileUtils
import com.lwy.andytoolkits.utils.RendererHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class Triangle(context: Context) {
    companion object {
        // number of coordinates per vertex in this array: (x, y, z)
        const val COORDS_PER_VERTEX = 3
        var triangleCoords = floatArrayOf(     // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f,      // top
            -0.5f, -0.311004243f, 0.0f,    // bottom left
            0.5f, -0.311004243f, 0.0f      // bottom right
        )
    }

    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0

    private var mProgram: Int
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX

    // 4 bytes per vertex； 感觉这里写错了， 每个 vertex 对应3个 float，是 12 bytes
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    // 使用 VBO 机制将顶点存到 GPU 内存中
    private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
        order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(triangleCoords)
            position(0)
        }
    }

    init {
        val vertexShader: Int = RendererHelper.loadShader(GLES20.GL_VERTEX_SHADER, FileUtils.readRawTextFileWithoutExplanation(context = context, resId = R.raw.triangle_vertex_shader))
        val fragmentShader: Int = RendererHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, FileUtils.readRawTextFileWithoutExplanation(context = context, resId = R.raw.triangle_fragment_shader))

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {
            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray? = null) { // pass in the calculated transformation matrix
        GLES20.glUseProgram(mProgram)

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            // Set color for drawing the triangle
            GLES20.glUniform4fv(it, 1, color, 0)
        }

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            // enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(it, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        }

        if (mvpMatrix != null) {
            // get handle to shape's transformation matrix
            vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMVPMatrix").also {
                // Pass the projection and view transformation to the shader
                GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
            }
        }

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}