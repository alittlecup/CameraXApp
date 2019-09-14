package com.hbl.cameraxapp.opengl.part2

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.hbl.cameraxapp.opengl.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RectangleRenderer : GLSurfaceView.Renderer {
    private val mMatrix: FloatArray = FloatArray(16)
    private var uMatrixLocation: Int = 0

    private val vShader = """
        #version 300 es
        layout(location=0) in vec4 vPosition;
        layout(location=1) in vec4 aColor;
        uniform mat4 u_Matrix;
        out vec4 vColor;
        void main(){
            gl_Position= u_Matrix*vPosition;
            gl_PointSize= 10.0;
            vColor=aColor;
        }
    """.trimIndent()

    private val fShaderUtils = """
        #version 300 es
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        
        void main(){
            fragColor= vColor;
        }
    """.trimIndent()

    private val vertexPositions = floatArrayOf(
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
        -0.5f, -0.5f, 1.0f, 1.0f, 1.0f,
        0.5f, -0.5f, 1.0f, 1.0f, 1.0f,
        0.5f, 0.5f, 1.0f, 1.0f, 1.0f,
        -0.5f, 0.5f, 1.0f, 1.0f, 1.0f,
        -0.5f, -0.5f, 1.0f, 1.0f, 1.0f,

        0.0f, 0.25f, 1f, 0.0f, 0.0f,
        0.0f, -0.25f, 0.0f, 0.0f, 0.1f
    )
    private val vertexBuffer: FloatBuffer

    companion object {
        private val POSITION_COMPONENT_COUNT = 2
        private val COLOR_COMPONENT_COUNT = 3

        private val BYTES_PER_FLOAT = 4

        private val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

    init {
        vertexBuffer =
            ByteBuffer.allocateDirect(vertexPositions.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexBuffer.put(vertexPositions)
        vertexBuffer.position(0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 6)

        GLES30.glDrawArrays(GLES30.GL_POINTS, 6, 2)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val aspectRatio = if (width > height) {
            width.toFloat() / height.toFloat()
        } else {
            height.toFloat() / width.toFloat()
        }

        if (width > height) {
            //横屏
            Matrix.orthoM(mMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            //竖屏
            Matrix.orthoM(mMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        val compileVertexShader = ShaderUtils.compileVertexShader(vShader)
        val compileFragmentShader = ShaderUtils.compileFragmentShader(fShaderUtils)

        val linkProgram = ShaderUtils.linkProgram(compileVertexShader, compileFragmentShader)

        GLES30.glUseProgram(linkProgram)

        uMatrixLocation = GLES30.glGetUniformLocation(linkProgram, "u_Matrix")

        var glVPosition = GLES30.glGetAttribLocation(linkProgram, "vPosition")

        var glAColor = GLES30.glGetAttribLocation(linkProgram, "aColor")

        vertexBuffer.position(0)
        GLES30.glVertexAttribPointer(
            glVPosition,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            STRIDE,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(glVPosition)

        vertexBuffer.position(POSITION_COMPONENT_COUNT)

        GLES30.glVertexAttribPointer(
            glAColor,
            COLOR_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            STRIDE,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(glAColor)

    }

}