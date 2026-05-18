package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.physics.*
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Vector2D

fun main() {
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    var width = 1920
    var height = 1080

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)

    val window = glfwCreateWindow(width, height, "Bump", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(1)

    println("OpenGL ${glGetString(GL_VERSION)}")

    // --- Framebuffer size callback ---
    glfwSetFramebufferSizeCallback(window) { _, w, h ->
        width = w
        height = h
        glViewport(0, 0, width, height)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
    }

    // --- Input callbacks ---
    glfwSetKeyCallback(window) { _, key, _, action, _ ->
        KeyListener.keyCallback(0, key, 0, action, 0)
    }

    glfwSetMouseButtonCallback(window) { _, button, action, _ ->
        MouseListener.mouseButtonCallback(0, button, action, 0)
    }

    glfwSetCursorPosCallback(window) { _, x, y ->
        MouseListener.mousePosCallback(0, x, y)
    }

    glfwSetScrollCallback(window) { _, x, y ->
        MouseListener.mouseScrollCallback(0, x, y)
    }

    // --- Initial projection setup ---
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()

    // --- Physics setup ---
    val physicsSystem = RuntimePhysicsSystem()
    val scene = RuntimeScene("main")

    val ball = DefaultGameEntity("ball")
    ball.transform = ball.transform.copy(position = Vector2D(width / 2.0, 1000.0)) // top-center

    val body = PointMass(mass = 1.0)
    body.addField(ConstantForceField(Vector2D(0.0, 500.0))) // gravity (screen-down)
    body.velocity = Vector2D(200.0, -400.0) // initial toss up-right

    physicsSystem.registerBody(ball.id, body)
    scene.addEntity(ball)

    // --- Main loop ---
    var lastTime = glfwGetTime()

    while (!glfwWindowShouldClose(window)) {
        val pos = MouseListener.getPos()
        val currentTime = glfwGetTime()
        val dt = currentTime - lastTime
        lastTime = currentTime
        glfwSetWindowTitle(window, "Bump | Mouse: (${"%.0f".format(pos.x)}, ${"%.0f".format(pos.y)}) | fps: ${"%.0f".format(1.0 / dt)}")

        // Physics step
        physicsSystem.step(scene, dt)

        // Bounce off screen edges
        val p = ball.transform.position
        if (p.x < 0 || p.x > width) {
            body.velocity = Vector2D(-body.velocity.x, body.velocity.y)

            ball.transform = ball.transform.copy(position = Vector2D(
                p.x.coerceIn(0.0, width.toDouble()),
                p.y
            ))
        }
        if (p.y > height) {
            body.velocity = Vector2D(body.velocity.x, -body.velocity.y)
            ball.transform = ball.transform.copy(position = Vector2D(p.x, height.toDouble()))
        }
        if (p.y < 0) {
            body.velocity = Vector2D(body.velocity.x, -body.velocity.y)
            ball.transform = ball.transform.copy(position = Vector2D(p.x, 0.0))
        }

        // Render
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glColor3f(1f, 0.5f, 0.2f) // orange
        glPointSize(10f)
        glBegin(GL_POINTS)
        glVertex2d(ball.transform.position.x, ball.transform.position.y)
        glEnd()

        glfwSwapBuffers(window)
        glfwPollEvents()

        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}