package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.audio.AudioSource
import org.soyuz.engine.audio.AudioSystem
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.engine.ui.Interactive
import org.soyuz.engine.ui.InteractiveDecorator
import org.soyuz.engine.ui.UI
import org.soyuz.engine.ui.UISystem
import org.soyuz.engine.ui.hoverable
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.*
import kotlin.math.sin

fun main() {
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    var width = 800
    var height = 600

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(width, height, "Bump - AV Test", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(0)

    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    AudioSystem.init()

    // --- Renderer setup ---
    val shader = Assets.shader("default")
    val camera = Camera()
    camera.setOrtho(width.toFloat(), height.toFloat())
    val quadMesh = Mesh.quad()
    val circleMesh = Mesh.circle(32)

    glfwSetFramebufferSizeCallback(window) { _, w, h ->
        width = w; height = h
        glViewport(0, 0, width, height)
        camera.setOrtho(width.toFloat(), height.toFloat())
    }

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

    // --- Scene setup ---
    val scene = RuntimeScene("ui_test")

    val btn = UI.button("Test", width/2.0, height/2.0, 50.0, 40.0) {
        println("HI")
    }

    scene.addEntity(btn)


    // --- Main loop ---
    var lastTime = glfwGetTime()
    var time = 0f

    while (!glfwWindowShouldClose(window)) {
        val currentTime = glfwGetTime()
        val dt = (currentTime - lastTime).toFloat()
        lastTime = currentTime
        time += dt

        glfwPollEvents()

        UISystem.update(listOf(btn))

        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(window, true)
        }

        // Render
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        shader.bind()
        shader.setProjection(camera.getProjection())

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
                    entity.painter?.bind(shader) ?: shader.setColor(1f, 0f, 1f, 1f)
                    quadMesh.draw()
                }
                else -> {}
            }
        }

        glfwSwapBuffers(window)
        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    AudioSystem.cleanup()
    Assets.cleanup()
    glfwDestroyWindow(window)
    glfwTerminate()
}