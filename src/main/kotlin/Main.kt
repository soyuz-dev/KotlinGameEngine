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

        val wallBody = PointMass(mass = 0.0, restitution = 1.0)
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

    val body = PointMass(mass = 1.0, restitution = 1.0)
    body.addField(ConstantForceField(Vector2D(0.0, 500.0)))
    body.velocity = Vector2D(.0, 100.0)

    val ballCollider = CircleCollider(CircleShape(10.0))

    physicsSystem.registerBody(ball.id, body)
    collisionSystem.registerCollider(ball.id, ballCollider)
    scene.addEntity(ball)

    // Ball 2
    val ball2 = DefaultGameEntity("ball2")
    ball2.goto(position = Vector2D(2 * width / 3.0, 300.0))
    ball2.shape = CircleShape(30.0)

    val body2 = PointMass(mass = 2.0, restitution = 1.0)
    body2.addField(ConstantForceField(Vector2D(0.0, 500.0)))
    body2.velocity = Vector2D(-200.0, 0.0)

    val ballCollider2 = CircleCollider(CircleShape(30.0))

    physicsSystem.registerBody(ball2.id, body2)
    collisionSystem.registerCollider(ball2.id, ballCollider2)
    scene.addEntity(ball2)

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

        // Ball 1 - orange
        glColor3f(1f, 0.5f, 0.2f)
        glPointSize(15f)
        glBegin(GL_POINTS)
        glVertex2d(ball.transform.position.x, ball.transform.position.y)
        glEnd()

        // Ball 2 - cyan
        glColor3f(0.2f, 0.8f, 1f)
        glPointSize(15f)
        glBegin(GL_POINTS)
        glVertex2d(ball2.transform.position.x, ball2.transform.position.y)
        glEnd()


        glfwSwapBuffers(window)
        glfwPollEvents()

        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}