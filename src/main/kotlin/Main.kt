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
import org.soyuz.engine.physics.forcefields.ConstantForceField
import org.soyuz.engine.physics.joints.RodJoint
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
import org.lwjgl.opengl.GL30.*
import org.lwjgl.BufferUtils
import org.soyuz.util.Transform

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
    glfwSwapInterval(0)

    println("OpenGL ${glGetString(GL_VERSION)}")

    // --- Renderer setup ---
    val shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
    val camera = Camera()
    camera.setOrtho(width.toFloat(), height.toFloat())
    val quadMesh = Mesh.quad()
    val circleMesh = Mesh.circle(32)

    // Line mesh for rods (VAO/VBO that we update each frame)
    val lineVao = glGenVertexArrays()
    val lineVbo = glGenBuffers()
    glBindVertexArray(lineVao)
    glBindBuffer(GL_ARRAY_BUFFER, lineVbo)
    glBufferData(GL_ARRAY_BUFFER, 100 * 2 * 4, GL_DYNAMIC_DRAW) // 4 vertices * 2 floats * 4 bytes
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
    glEnableVertexAttribArray(0)
    glBindVertexArray(0)

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
    val gravity = ConstantForceField(Vector2D(0.0, 981.0))

    eventBus.subscribe(CollisionEvent::class.java) { event ->
        println("Bump! ${event.sourceEntityId} hit ${event.otherEntityId}")
    }
    var ballCount = 0

    fun makeBall(x: Double, y: Double, vx: Double, vy: Double, mass: Double = 1.0, radius: Double = 10.0, restitution: Double = 0.8) {
        val id = "ball_${ballCount++}"
        val ball = DefaultGameEntity(id)
        ball.goto(position = Vector2D(x, y))
        ball.shape = CircleShape(radius)
        ball.painter = SolidColor(Math.random(), Math.random(), Math.random(), 1.0)
        val body = PointMass(mass = mass, restitution = restitution)
        body.addField(gravity)
        val collider = CircleCollider(CircleShape(radius))
        physicsSystem.registerBody(id, body)
        collisionSystem.registerCollider(id, collider)
        scene.addEntity(ball)
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
    createWall("left",   -wallThickness / 2, height / 2.0, wallThickness * 1.5, height.toDouble())
    createWall("right",   width + wallThickness / 2, height / 2.0, wallThickness * 1.5, height.toDouble())
    createWall("top",     width / 2.0, -wallThickness / 2, width.toDouble(), wallThickness * 1.5)
    createWall("bottom",  width / 2.0, height + wallThickness / 2, width.toDouble(), wallThickness * 1.5)

    // Double pendulum setup
    val anchor = DefaultGameEntity("anchor")
    anchor.goto(Vector2D(width / 2.0, height / 3.0))
    anchor.shape = CircleShape(6.0)
    anchor.painter = SolidColor(1.0, 1.0, 1.0, 1.0)
    val anchorBody = PointMass(mass = 0.0)
    val anchorCollider = CircleCollider(CircleShape(6.0))
    physicsSystem.registerBody("anchor", anchorBody)
    collisionSystem.registerCollider("anchor", anchorCollider)
    scene.addEntity(anchor)

    makeBall(width / 2.0, height / 2.0, 200.0, 0.0, mass = 1.0, radius = 10.0)
    val bob1Body = physicsSystem.getBody("ball_0")!!

    makeBall(width / 2.0 + 150.0, height / 2.0, -200.0, 0.0, mass = 1.0, radius = 10.0)
    val bob2Body = physicsSystem.getBody("ball_1")!!

    makeBall(width/2.0 + 200.0, height / 2.0, -250.0, 0.0, mass = 1.0, radius = 10.0)
    val bob3Body = physicsSystem.getBody("ball_2")!!

    val rod1 = RodJoint(anchorBody, bob1Body, restLength = 150.0)
    val rod2 = RodJoint(bob1Body, bob2Body, restLength = 200.0)
    val rod3 = RodJoint(bob2Body, bob3Body, restLength = 50.0)
    physicsSystem.addJoint(rod1)
    physicsSystem.addJoint(rod2)
    physicsSystem.addJoint(rod3)

    // --- Main loop ---
    var lastTime = glfwGetTime()

    while (!glfwWindowShouldClose(window)) {
        val pos = MouseListener.getPos()
        val currentTime = glfwGetTime()
        val rawDt = currentTime - lastTime
        lastTime = currentTime
        val dt = minOf(rawDt, 0.02)

        glfwSetWindowTitle(window, "Bump! | Mouse: (${"%.0f".format(pos.x)}, ${"%.0f".format(pos.y)}) | fps: ${"%.0f".format(1.0 / dt)}")

        physicsSystem.step(scene, dt)
        glfwPollEvents()

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        shader.bind()
        shader.setProjection(camera.getProjection())

        // Draw rods
        val anchorPos = scene.findEntity("anchor")?.transform?.position ?: Vector2D.ZERO
        val bob0Pos = scene.findEntity("ball_0")?.transform?.position ?: Vector2D.ZERO
        val bob1Pos = scene.findEntity("ball_1")?.transform?.position ?: Vector2D.ZERO
        val bob2Pos = scene.findEntity("ball_2")?.transform?.position ?: Vector2D.ZERO

        val lineVerts = floatArrayOf(
            anchorPos.x.toFloat(), anchorPos.y.toFloat(),
            bob0Pos.x.toFloat(), bob0Pos.y.toFloat(),
            bob0Pos.x.toFloat(), bob0Pos.y.toFloat(),
            bob1Pos.x.toFloat(), bob1Pos.y.toFloat(),
            bob1Pos.x.toFloat(), bob1Pos.y.toFloat(),
            bob2Pos.x.toFloat(), bob2Pos.y.toFloat()
        )

        glBindBuffer(GL_ARRAY_BUFFER, lineVbo)
        glBufferSubData(GL_ARRAY_BUFFER, 0, lineVerts)
        glBindVertexArray(lineVao)
        shader.setModel(MathUtil.modelMatrix(Transform())) // identity model
        shader.setColor(1f, 1f, 1f, 0.8f)
        glDrawArrays(GL_LINES, 0, 6)

        // Draw all entities
        for (entity in scene.allEntities()) {
            when (entity.shape) {
                is CircleShape -> {
                    shader.setModel(MathUtil.modelMatrix(entity.transform.copy(
                        scale = Vector2D((entity.shape as CircleShape).radius * 2, (entity.shape as CircleShape).radius * 2)
                    )))
                    entity.painter?.bind(shader) ?: shader.setColor(1f, 0f, 1f, 1f)
                    circleMesh.draw()
                }
                is RectangleShape -> {
                    shader.setModel(MathUtil.modelMatrix(entity.transform.copy(
                        scale = Vector2D((entity.shape as RectangleShape).width, (entity.shape as RectangleShape).height)
                    )))
                    entity.painter?.bind(shader) ?: shader.setColor(0.2f, 0.2f, 0.3f, 1f)
                    quadMesh.draw()
                }

                else -> {}
            }
        }



        glfwSwapBuffers(window)
        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    glDeleteVertexArrays(lineVao)
    glDeleteBuffers(lineVbo)
    glfwDestroyWindow(window)
    glfwTerminate()
}