package com.hbl.cameraxapp.opengl.part1

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NativeColorRender(val color: Int) : GLSurfaceView.Renderer {
    override fun onDrawFrame(p0: GL10?) {
        onDrawFrame()
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        surfaceChanged(p1, p2)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        surfaceCreated(color)
    }

    companion object {
        init {
            System.loadLibrary("native-color")
        }
    }


    external fun surfaceCreated(color: Int)
    external fun surfaceChanged(width: Int, height: Int)
    external fun onDrawFrame()
}
