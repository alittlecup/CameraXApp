package com.hbl.cameraxapp.opengl

import android.graphics.Color
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hbl.cameraxapp.AbsGLSurfaceActivity
import com.hbl.cameraxapp.opengl.part1.NativeColorRender
import com.hbl.cameraxapp.opengl.part2.RectangleRenderer
import com.hbl.cameraxapp.opengl.part2.SimpleRender

class RenderActivity : AbsGLSurfaceActivity() {
    override fun bindRender(): GLSurfaceView.Renderer = RectangleRenderer()


}
