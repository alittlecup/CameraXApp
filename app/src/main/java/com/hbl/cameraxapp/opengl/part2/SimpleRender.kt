package com.hbl.cameraxapp.opengl.part2

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.hbl.cameraxapp.opengl.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender : GLSurfaceView.Renderer {
    private val vertexPoints = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )
    private var byteBuffer: FloatBuffer
    private var colorBuffer: FloatBuffer

    private val colors = floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f
    )

    init {
        byteBuffer =
            ByteBuffer.allocateDirect(vertexPoints.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        byteBuffer.put(vertexPoints)
        byteBuffer.position(0)

        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        colorBuffer.put(colors)
        colorBuffer.position(0)


    }

    private val vertexShader =
        """
            #version 300 es
            layout (location=0) in vec4 vPosition;
            layout (location=1) in vec4 aColor;
            out vec4 vColor;
            void main(){
                gl_Position=vPosition;
                gl_PointSize= 10.0;
                vColor=aColor;
            }
        """.trimIndent()

    private val fragmentShader =
        """
            #version 300 es
            precision mediump float;
            in vec4 vColor;
            out vec4 fragColor;
            void main(){
                fragColor=vColor;
            }
        
        """.trimIndent()


    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, byteBuffer)

        GLES30.glEnableVertexAttribArray(0)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, colorBuffer)


        GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, 3)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)

    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        GLES30.glViewport(0, 0, p1, p2)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)

        val compileShader = ShaderUtils.compileVertexShader(vertexShader)

        val compileFragmentShader = ShaderUtils.compileFragmentShader(fragmentShader)

        val linkProgram = ShaderUtils.linkProgram(compileShader, compileFragmentShader)

        GLES30.glUseProgram(linkProgram)
    }


}


























