package com.hbl.cameraxapp.opengl.part1

import android.graphics.Color
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ColorRender(val color: Int) : GLSurfaceView.Renderer {
    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        GLES30.glViewport(0, 0, p1, p2)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        val redF = Color.red(color) / 255f
        val greenF = Color.green(color) / 255f
        val blueF = Color.blue(color) / 255f
        val alphaF = Color.alpha(color) / 255f

        GLES30.glClearColor(redF, greenF, blueF, alphaF)

    }
}