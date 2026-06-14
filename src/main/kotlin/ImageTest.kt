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
import org.soyuz.engine.audio.AudioSource
import org.soyuz.engine.audio.AudioSystem
import org.soyuz.engine.physics.joints.RopeJoint
import org.soyuz.engine.physics.joints.SpringJoint
import org.soyuz.engine.render.image.ImagePainter
import org.soyuz.util.Assets
import org.soyuz.util.Transform
import javax.swing.Spring

fun main() {
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    var width = 1920
    var height = 1080

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(width, height, "Bump", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(0)

    println("OpenGL ${glGetString(GL_VERSION)}")

    // --- Audio setup ---
    AudioSystem.init()

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
    val scene = RuntimeScene("main")

    // Add a textured entity
    val texturedBox = DefaultGameEntity("textured_box")
    texturedBox.goto(Vector2D(width / 2.0, height / 2.0))
    texturedBox.shape = RectangleShape(128.0, 128.0)
    texturedBox.painter = ImagePainter(Assets.texture("cat")) // needs image in resources/textures/
    scene.addEntity(texturedBox)


    val texturedBox1 = DefaultGameEntity("textured_box1")
    texturedBox1.goto(Vector2D(width / 1.5, height / 3.0))
    texturedBox1.turnTo(1.0)
    texturedBox1.shape = RectangleShape(128.0, 128.0)
    texturedBox1.painter = ImagePainter(Assets.texture("dog")) // needs image in resources/textures/
    scene.addEntity(texturedBox1)

    // --- Main loop ---
    var lastTime = glfwGetTime()

    while (!glfwWindowShouldClose(window)) {
        val pos = MouseListener.getPos()
        val currentTime = glfwGetTime()
        val rawDt = currentTime - lastTime
        lastTime = currentTime
        val dt = minOf(rawDt, 0.02)

        glfwSetWindowTitle(window, "Bump! | Mouse: (${"%.0f".format(pos.x)}, ${"%.0f".format(pos.y)}) | fps: ${"%.0f".format(1.0 / dt)}")

        glfwPollEvents()

        AudioSystem.update(
            listenerX = 0f,  // or camera position if you had one
            listenerY = 0f,
            vX = 0f,
            vY = 0f
        )

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        shader.bind()
        shader.setProjection(camera.getProjection())

        if (MouseListener.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            val source = AudioSource();
            source.play(Assets.audio("meow"))
        }

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

    shader.cleanup()
    AudioSystem.cleanup()
    glDeleteVertexArrays(lineVao)
    glDeleteBuffers(lineVbo)
    glfwDestroyWindow(window)
    glfwTerminate()
}