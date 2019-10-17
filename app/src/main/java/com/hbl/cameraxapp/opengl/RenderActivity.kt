package com.hbl.cameraxapp.opengl

import android.opengl.GLSurfaceView
import com.hbl.cameraxapp.opengl.part2.TextureRenderer

class RenderActivity : AbsGLSurfaceActivity() {
    override fun bindRender(): GLSurfaceView.Renderer =TextureRenderer()


}
