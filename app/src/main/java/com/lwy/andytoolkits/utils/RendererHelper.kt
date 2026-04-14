package com.lwy.andytoolkits.utils

import android.opengl.GLES20


object RendererHelper {
    enum class TriangleRenderType {
        DEFAULT,    // 默认形状，会存在形变
        VP_RENDER,      // 相机视图
        ROTATION_RENDER,     // 相机视图，添加动画
        TOUCH_RENDER    // 相机视图，响应轻触事件
    }

    /**
     * 着色程序包含 OpenGL 着色语言 (GLSL) 代码，必须先对其进行编译，然后才能在 OpenGL ES 环境中使用。ps
     *
     * ps. 就 CPU 周期和处理时间而言，编译 OpenGL ES 着色程序及关联程序的成本非常高，因此应避免多次执行该操作。如果您在运行时不清楚着色程序的内容，则应编译代码，使其仅被创建一次，然后缓存以备后用。
     */
    fun loadShader(type: Int, shaderCode: String): Int {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}