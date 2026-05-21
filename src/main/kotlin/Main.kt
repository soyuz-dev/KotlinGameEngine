package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.collision.RuntimeCollisionSystem
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.physics.*
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Vector2D

fun main() {
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    var width = 800
    var height = 800

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
    val collisionSystem = RuntimeCollisionSystem()
    val physicsSystem = RuntimePhysicsSystem(collisionSystem)
    val scene = RuntimeScene("main")

    fun createWall(id: String, x: Double, y: Double, w: Double, h: Double) {
        val wall = DefaultGameEntity(id)
        wall.goto(Vector2D(x, y))
        wall.shape = RectangleShape(w, h)

        val wallBody = PointMass(mass = 0.0, restitution = 0.5)
        val wallCollider = RectangleCollider(RectangleShape(w, h))

        physicsSystem.registerBody(id, wallBody)
        collisionSystem.registerCollider(id, wallCollider)
        scene.addEntity(wall)
    }

    val wallThickness = 100.0
    createWall("left",   -wallThickness / 2, height / 2.0, wallThickness, height.toDouble())
    createWall("right",   width + wallThickness / 2, height / 2.0, wallThickness, height.toDouble())
    createWall("top",     width / 2.0, -wallThickness / 2, width.toDouble(), wallThickness)
    createWall("bottom",  width / 2.0, height + wallThickness / 2, width.toDouble(), wallThickness)

    val ball = DefaultGameEntity("ball")
    ball.goto(position = Vector2D(width / 2.0, 300.0))
    ball.shape = CircleShape(10.0)

    val body = PointMass(mass = 1.0, restitution = 0.7)
    body.addField(ConstantForceField(Vector2D(0.0, 500.0)))
    body.velocity = Vector2D(30000.0, 15000.0)

    val ballCollider = CircleCollider(CircleShape(10.0))

    physicsSystem.registerBody(ball.id, body)
    collisionSystem.registerCollider(ball.id, ballCollider)
    scene.addEntity(ball)

    // --- Main loop ---
    var lastTime = glfwGetTime()

    while (!glfwWindowShouldClose(window)) {
        val pos = MouseListener.getPos()
        val currentTime = glfwGetTime()
        val rawDt = currentTime - lastTime
        lastTime = currentTime

        val dt = minOf(rawDt, 0.02)

        glfwSetWindowTitle(window, "Bump | Mouse: (${"%.0f".format(pos.x)}, ${"%.0f".format(pos.y)}) | fps: ${"%.0f".format(1.0 / dt)}")

        // Physics step
        physicsSystem.step(scene, dt)

        if (ball.transform.position.y > height + 100) println("BALL ESCAPED: ${ball.transform.position}")

        // Render
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glColor3f(1f, 0.5f, 0.2f)
        glPointSize(10f)
        glBegin(GL_POINTS)
        glVertex2d(ball.transform.position.x, ball.transform.position.y)
        glEnd()

        // Draw walls as lines for debugging
        glColor3f(0.3f, 0.3f, 0.5f)
        glBegin(GL_LINES)
        // top
        glVertex2d(0.0, 0.0); glVertex2d(width.toDouble(), 0.0)
        // bottom
        glVertex2d(0.0, height.toDouble()); glVertex2d(width.toDouble(), height.toDouble())
        // left
        glVertex2d(0.0, 0.0); glVertex2d(0.0, height.toDouble())
        // right
        glVertex2d(width.toDouble(), 0.0); glVertex2d(width.toDouble(), height.toDouble())
        glEnd()

        glfwSwapBuffers(window)
        glfwPollEvents()

        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}