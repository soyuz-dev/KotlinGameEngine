package org.soyuz.windowing

import org.lwjgl.glfw.GLFW

class TransparentWindow(
    title: String = "Bump",
    initialWidth: Int = 800,
    initialHeight: Int = 600,
    shareContext: Long = 0L
) : Window(title, initialWidth, initialHeight, shareContext) {

    override fun configureHints() {
        GLFW.glfwWindowHint(
            GLFW.GLFW_DECORATED,
            GLFW.GLFW_FALSE
        )

        GLFW.glfwWindowHint(
            GLFW.GLFW_TRANSPARENT_FRAMEBUFFER,
            GLFW.GLFW_TRUE
        )

        GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, 8)
        GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, 8)
        GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, 8)
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, 8)
    }

    init {
        clearColor = floatArrayOf(
            0f,
            0f,
            0f,
            0f
        )
    }
}