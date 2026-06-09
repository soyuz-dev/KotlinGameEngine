package org.soyuz.engine.render

import org.lwjgl.opengl.GL20.GL_COMPILE_STATUS
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_LINK_STATUS
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCompileShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.glCreateShader
import org.lwjgl.opengl.GL20.glDeleteProgram
import org.lwjgl.opengl.GL20.glDeleteShader
import org.lwjgl.opengl.GL20.glGetProgrami
import org.lwjgl.opengl.GL20.glGetShaderi
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glShaderSource
import org.lwjgl.opengl.GL20.glUniform4f
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL20.glUseProgram
import java.io.FileNotFoundException

class Shader (
    val vertex :String,
    val fragment:String,

) {
    private val colorLoc: Int
    private val projLoc: Int
    private val modelLoc: Int
    private val program : Int
    init {
        val vertexHandle = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexHandle, vertex)
        glCompileShader(vertexHandle)
        var success = glGetShaderi(vertexHandle, GL_COMPILE_STATUS)
        if(success == 0) {System.err.println("ERROR: Failed to compile vertex shader: $vertex")}

        val fragmentHandle = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentHandle, fragment)
        glCompileShader(fragmentHandle)
        success = glGetShaderi(fragmentHandle, GL_COMPILE_STATUS)
        if(success == 0) {System.err.println("ERROR: Failed to compile fragment shader: $fragment")}
        program = glCreateProgram()
        glAttachShader(program, vertexHandle)
        glAttachShader(program, fragmentHandle)
        glLinkProgram(program)
        success = glGetProgrami(program, GL_LINK_STATUS)
        if(success == 0) {System.err.println("ERROR: Failed to link program: $program")}
        glDeleteShader(vertexHandle)
        glDeleteShader(fragmentHandle)

        colorLoc = glGetUniformLocation(program, "uColor")
        projLoc = glGetUniformLocation(program, "uProjection")
        modelLoc = glGetUniformLocation(program, "uModel")
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        glUniform4f(colorLoc, r, g, b, a)
    }

    fun setProjection(matrix: FloatArray) {
        glUniformMatrix4fv(projLoc, false, matrix)
    }

    fun setModel(matrix: FloatArray) {
        glUniformMatrix4fv(modelLoc, false, matrix)
    }

    fun bind() {
        glUseProgram(program)
    }

    fun cleanup() {
        glDeleteProgram(program)
    }

    companion object {
        fun fromResource(vertexPath : String, fragmentPath : String): Shader {
            val vertexResource = Shader::class.java.getResourceAsStream(vertexPath)
                ?.bufferedReader()?.readText()
                ?: throw FileNotFoundException("vertex shader $vertexPath not found")
            val fragmentResource = Shader::class.java.getResourceAsStream(fragmentPath)
                ?.bufferedReader()?.readText()
                ?: throw FileNotFoundException("fragment shader $fragmentPath not found")

            return Shader(vertexResource, fragmentResource)
        }
    }

    fun setUniform4f(name:String, r: Float, g: Float, b: Float, a: Float) {
        val location = glGetUniformLocation(program, name)
        glUniform4f(location, r, g, b, a)
    }
}