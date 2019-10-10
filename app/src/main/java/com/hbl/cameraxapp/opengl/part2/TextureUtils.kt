package com.hbl.cameraxapp.opengl.part2

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log

class TextureUtils {
    companion object {
        val TAG = "TextureUtils"
        fun loadTexture(context: Context, resourceId: Int): Int {
            val textureIds = IntArray(1)
            GLES30.glGenTextures(1, textureIds, 0)
            if (textureIds[0] == 0) {
                Log.d("TAG", "Could: not")
                return 0
            }
            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap =
                BitmapFactory.decodeResource(context.resources, resourceId, options)
            if (bitmap == null) {
                Log.d("TAG", "error: ")
                GLES30.glDeleteTextures(1, textureIds, 0)
                return 0
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR_MIPMAP_LINEAR
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR
            )
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            bitmap.recycle()
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            return textureIds[0]
        }
    }
}