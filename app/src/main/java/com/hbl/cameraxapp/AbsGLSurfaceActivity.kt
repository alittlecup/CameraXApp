package com.hbl.cameraxapp

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class AbsGLSurfaceActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    abstract fun bindRender(): GLSurfaceView.Renderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpView()
    }

    private fun setUpView() {
        glSurfaceView = GLSurfaceView(this)
        setContentView(glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setRenderer(bindRender())
    }
}