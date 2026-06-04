package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.collision.RuntimeCollisionSystem
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.events.CollisionEvent
import org.soyuz.engine.events.RuntimeEventBus
import org.soyuz.engine.physics.*
import org.soyuz.engine.physics.forcefields.ConstantAccelerationField
import org.soyuz.engine.physics.forcefields.ConstantForceField
import org.soyuz.engine.physics.forcefields.GravityField
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.MathUtil
import org.soyuz.util.Vector2D

fun main() {
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    var width = 800
    var height = 800

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(width, height, "Bump", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(1)

    println("OpenGL ${glGetString(GL_VERSION)}")

    // --- Renderer setup (after physics setup, before main loop) ---
    val shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
    val camera = Camera()
    camera.setOrtho(width.toFloat(), height.toFloat())
    val quadMesh = Mesh.quad()
    val circleMesh = Mesh.circle(32)

    // --- Framebuffer size callback ---
    glfwSetFramebufferSizeCallback(window) { _, w, h ->
        width = w
        height = h
        glViewport(0, 0, width, height)
        camera.setOrtho(width.toFloat(), height.toFloat())
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

    // --- Physics setup ---
    val collisionSystem = RuntimeCollisionSystem()
    val eventBus = RuntimeEventBus()
    val physicsSystem = RuntimePhysicsSystem(collisionSystem, eventBus)
    val scene = RuntimeScene("main")
    val gravityField = GravityField()
    physicsSystem.addDynamicField(gravityField)

    eventBus.subscribe(CollisionEvent::class.java) { event ->
        println("Bump! ${event.sourceEntityId} hit ${event.otherEntityId}")
    }
    var ballCount = 0

    fun makeBall(x: Double, y: Double, vx: Double, vy: Double, mass: Double = 1.0, radius: Double = 10.0, restitution: Double = 0.8) {
        val id = "ball_${ballCount++}"
        val ball = DefaultGameEntity(id)
        ball.goto(position = Vector2D(x, y))
        ball.shape = CircleShape(radius)
        ball.painter = SolidColor(
            Math.random(),
            Math.random(),
            Math.random(),
            1.0
        )

        val body = PointMass(mass = mass, restitution = restitution)
        //body.velocity = Vector2D(vx, vy)
        gravityField.registerBody(id, body, ball.transform.position)

        val collider = CircleCollider(CircleShape(radius))

        physicsSystem.registerBody(id, body)
        collisionSystem.registerCollider(id, collider)
        scene.addEntity(ball)
        gravityField.registerBody(id, body, ball.transform.position)
    }

    fun createWall(id: String, x: Double, y: Double, w: Double, h: Double) {
        val wall = DefaultGameEntity(id)
        wall.goto(Vector2D(x, y))
        wall.shape = RectangleShape(w, h)
        wall.painter = SolidColor(0.2, 0.2, 0.3, 1.0)

        val wallBody = PointMass(mass = 0.0, restitution = 1.0)
        val wallCollider = RectangleCollider(RectangleShape(w, h))

        physicsSystem.registerBody(id, wallBody)
        collisionSystem.registerCollider(id, wallCollider)
        scene.addEntity(wall)
    }

    val wallThickness = 100.0
    createWall("left",   -wallThickness / 2, height / 2.0, wallThickness*1.5, height.toDouble())
    createWall("right",   width + wallThickness / 2, height / 2.0, wallThickness*1.5, height.toDouble())
    createWall("top",     width / 2.0, -wallThickness / 2, width.toDouble(), wallThickness*1.5)
    createWall("bottom",  width / 2.0, height + wallThickness / 2, width.toDouble(), wallThickness*1.5)

// Spawn a couple starter balls
    makeBall(width / 3.0, height / 2.0, 200.0, -100.0, mass = 1.0, radius = 12.0)

    // --- Main loop ---
    var lastTime = glfwGetTime()

    while (!glfwWindowShouldClose(window)) {
        val pos = MouseListener.getPos()
        val currentTime = glfwGetTime()
        val rawDt = currentTime - lastTime
        lastTime = currentTime

        val dt = minOf(rawDt, 0.02)

        glfwSetWindowTitle(window, "Bump! | Mouse: (${"%.0f".format(pos.x)}, ${"%.0f".format(pos.y)}) | fps: ${"%.0f".format(1.0 / dt)}")

        // Physics step
        physicsSystem.step(scene, dt)

        // Event Polling
        glfwPollEvents()


        // Spawn on click
        if (MouseListener.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            val mPos = MouseListener.getPos()
            val r = 5.0 + Math.random() * 20.0
            val vx = (Math.random() - 0.5) * 120.0
            val vy = (Math.random() - 0.5) * 120.0
            val m = 0.5 + Math.random() * 2.0
            makeBall(mPos.x, mPos.y, vx, vy, mass = m, radius = r)
            println("Total balls: ${scene.allEntities().count { it.id.startsWith("ball_") }}")  // debug
        }
        // Render
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        shader.bind()
        shader.setProjection(camera.getProjection())

        for (entity in scene.allEntities()) {
            if (entity.shape is CircleShape && entity.id.startsWith("ball_")) {
                shader.setModel(MathUtil.modelMatrix(entity.transform.copy(scale = Vector2D((entity.shape as CircleShape).radius * 2, (entity.shape as CircleShape).radius * 2))))
                entity.painter?.bind(shader) ?: shader.setColor(1f, 0f, 1f, 1f)
                circleMesh.draw()
            }
            if (entity.shape is RectangleShape) {
                shader.setModel(MathUtil.modelMatrix(entity.transform.copy(
                    scale = Vector2D((entity.shape as RectangleShape).width, (entity.shape as RectangleShape).height)
                )))
                entity.painter?.bind(shader) ?: shader.setColor(0.2f, 0.2f, 0.3f, 1f)
                quadMesh.draw()
            }
        }

        glfwSwapBuffers(window)

        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}