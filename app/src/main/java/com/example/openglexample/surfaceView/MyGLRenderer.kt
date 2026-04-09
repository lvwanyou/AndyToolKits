package com.example.openglexample.surfaceView

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.example.openglexample.shape.Square2
import com.example.openglexample.shape.Triangle
import com.example.openglexample.utils.RendererHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(context: Context) : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mSquare2: Square2
    private val mContext: Context
    init {
        mContext = context
    }
    /**
     * vPMatrix（Model View Projection Matrix，模型视图投影矩阵）：这是一个16位的浮点数组，用于存储模型、视图和投影矩阵相乘的结果。在OpenGL ES中，这个矩阵用于将3D坐标转换为2D屏幕坐标，以便正确地在屏幕上渲染图形。
     * projectionMatrix（投影矩阵）：这也是一个16位的浮点数组，用于存储投影矩阵。投影矩阵定义了一个视锥体，它决定了哪些对象会被渲染到屏幕上。它可以是正交投影或透视投影，用于模拟相机的透视效果。
     * viewMatrix（视图矩阵）：这是另一个16位的浮点数组，用于存储视图矩阵。视图矩阵定义了观察场景的位置和方向，相当于设置相机的位置和朝向。它决定了哪个方向是上方，哪个方向是前方，从而影响渲染的结果。
     * rotationMatrix（旋转矩阵）：这同样是一个16位的浮点数组，用于存储旋转矩阵。旋转矩阵用于实现图形的旋转变换。通过改变旋转矩阵的值，可以控制图形绕某个轴旋转，从而实现动画效果或响应用户输入。
     */
    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var case = RendererHelper.TriangleRenderType.TOUCH_RENDER

    @Volatile
    var angle: Float = 0.0f

    /**
     * 系统会在创建 GLSurfaceView 时调用一次此方法。使用此方法可执行仅需发生一次的操作，例如设置 OpenGL 环境参数或初始化 OpenGL 图形对象。
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // initialize shapes
        mTriangle = Triangle(context = mContext)
        mSquare2 = Square2()
    }

    /**
     * 系统会在每次重新绘制 GLSurfaceView 时调用此方法。请将此方法作为绘制（和重新绘制）图形对象的主要执行点。
     * 通常情况下，每秒钟会调用 60 次 onDrawFrame() 方法，即 FPS 为 60。但是，实际的 FPS 受到多种因素的影响，例如设备性能、场景复杂度、渲染质量等等，因此实际的 FPS 可能会有所不同。
     */
    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        when (case) {
            RendererHelper.TriangleRenderType.VP_RENDER -> {
                // Set the camera position (View matrix)
                Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

                // Calculate the projection and view transformation
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

                // Draw shape
                mTriangle.draw(vPMatrix)
            }
            RendererHelper.TriangleRenderType.ROTATION_RENDER -> {
                val scratchMatrix = FloatArray(16)

                val time = SystemClock.uptimeMillis() % 4000L
                angle = 0.090f * time.toInt()
                Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

                // Set the camera position (View matrix)
                Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

                // Calculate the projection and view transformation
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

                Matrix.multiplyMM(scratchMatrix, 0, vPMatrix, 0, rotationMatrix, 0)

                // Draw shape
                mTriangle.draw(scratchMatrix)
            }
            RendererHelper.TriangleRenderType.TOUCH_RENDER -> {
                val scratchMatrix = FloatArray(16)

                Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

                // Set the camera position (View matrix)
                Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

                // Calculate the projection and view transformation
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

                Matrix.multiplyMM(scratchMatrix, 0, vPMatrix, 0, rotationMatrix, 0)

                // Draw shape
                mTriangle.draw(scratchMatrix)
            }
            else -> {
                mTriangle.draw()
            }
        }
    }

    /**
     * 系统会在 GLSurfaceView 几何图形发生变化（包括 GLSurfaceView 大小发生变化或设备屏幕方向发生变化）时调用此方法。例如，系统会在设备屏幕方向由纵向变为横向时调用此方法。使用此方法可响应 GLSurfaceView 容器中的更改。
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}