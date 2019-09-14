package com.hbl.cameraxapp.opengl

import android.opengl.GLES30

class ShaderUtils {
    companion object {
        fun compileShander(type: Int, shaderCode: String): Int {
            var glCreateShader = GLES30.glCreateShader(type)
            if (glCreateShader != 0) {
                GLES30.glShaderSource(glCreateShader, shaderCode)
                GLES30.glCompileShader(glCreateShader)
                val compileStatus = IntArray(1)
                GLES30.glGetShaderiv(glCreateShader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
                if (compileStatus[0] == 0) {
                    var logInfo = GLES30.glGetShaderInfoLog(glCreateShader)
                    println(logInfo)
                    GLES30.glDeleteShader(glCreateShader)
                    return 0
                }
                return glCreateShader
            } else {
                return 0
            }
        }

        fun linkProgram(vid: Int, fId: Int): Int {
            val programId = GLES30.glCreateProgram()
            if (programId != 0) {
                GLES30.glAttachShader(programId, vid)
                GLES30.glAttachShader(programId, fId)

                GLES30.glLinkProgram(programId)

                val linkStatus = IntArray(1)

                GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
                if (linkStatus[0] == 0) {
                    var logInfo = GLES30.glGetProgramInfoLog(programId)
                    println(logInfo)
                    GLES30.glDeleteProgram(programId)
                    return 0
                }
                return programId
            } else {
                return 0
            }
        }

        fun compileVertexShader(shaderCode: String): Int {
            return compileShander(GLES30.GL_VERTEX_SHADER, shaderCode)
        }

        fun compileFragmentShader(shaderCode: String): Int {
            return compileShander(GLES30.GL_FRAGMENT_SHADER, shaderCode)
        }
        fun validProgram(programId:Int):Boolean{
            GLES30.glValidateProgram(programId)
            val programStatus = IntArray(1)
            GLES30.glGetProgramiv(programId, GLES30.GL_VALIDATE_STATUS, programStatus, 0)
            return programStatus[0] != 0
        }
    }
}