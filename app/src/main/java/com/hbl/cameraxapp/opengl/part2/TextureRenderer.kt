package com.hbl.cameraxapp.opengl.part2

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.hbl.cameraxapp.MyApplication
import com.hbl.cameraxapp.R
import com.hbl.cameraxapp.opengl.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TextureRenderer : GLSurfaceView.Renderer {
    val vertexBuffer: FloatBuffer
    val texVertexBuffer: FloatBuffer
    val vertexIndexBuffer: ShortBuffer

    /**
     * 顶点坐标
     * (x,y,z)
     */
    val POSITUIN_VERTEX = floatArrayOf(
        0f, 0f, 0f,
        1f, 1f, 0f,
        1f, 1f, 0f,
        1f, 1f, 0f,
        1f, 1f, 1f
    )

    /**
     * 纹理坐标
     * (s,t)
     */
    val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f, //纹理坐标V0
        1f, 0f,     //纹理坐标V1
        0f, 0f,     //纹理坐标V2
        0f, 1.0f,   //纹理坐标V3
        1f, 1.0f    //纹理坐标V4
    )
    val VERTEX_INDEX = shortArrayOf(
        0, 1, 2,
        0, 2, 3,
        0, 3, 4,
        0, 4, 1
    )

    init {
        vertexBuffer =
            ByteBuffer.allocateDirect(POSITUIN_VERTEX.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(POSITUIN_VERTEX)
                    position(0)
                }
        texVertexBuffer =
            ByteBuffer.allocateDirect(TEX_VERTEX.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(TEX_VERTEX)
                    position(0)
                }
        vertexIndexBuffer =
            ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2).order(ByteOrder.nativeOrder())
                .asShortBuffer().apply {
                    put(VERTEX_INDEX)
                    position(0)
                }

    }


    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(programID)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES30.GL_UNSIGNED_SHORT,
            vertexIndexBuffer
        )
    }

    val vShader = """
        #version 300 es
        layout(location=0) in vec4 vPosition;
        layout(location=1) in vec2 aTextureCoord;
        out vec2 vTexCoord;
        void main(){
            gl_Position=vPosition;
            gl_PointSize=10.0;
            vTexCoord=aTextureCoord;
        }
    """.trimIndent()

    val fShader = """
        #version 300 es
        precision mediump float;
        uniform sampler2D uTextureUnit;
        in vec2 vTexCoord;
        out vec4 vFragColor;
        void main(){
            vFragColor=texture(uTextureUnit,vTexCoord);
        }
    """.trimIndent()


    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        GLES30.glViewport(0, 0, p1, p2)
    }

    var programID: Int = 0
    var textureID = 0
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        val compileVertexShader = ShaderUtils.compileVertexShader(vShader)
        val compileFragmentShader = ShaderUtils.compileFragmentShader(fShader)
        programID = ShaderUtils.linkProgram(compileVertexShader, compileFragmentShader)
        textureID = TextureUtils.loadTexture(MyApplication.INSTANCE.applicationContext,R.drawable.main)
    }
}