package org.soyuz
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

fun main() {
    // Initialize GLFW
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    // Configure the window
    val window = glfwCreateWindow(800, 600, "Kotlin LWJGL", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities() // Connects LWJGL to the GPU's OpenGL context

    // Main Loop
    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Render stuff here!

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}