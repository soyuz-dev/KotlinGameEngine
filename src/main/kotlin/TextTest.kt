package org.soyuz

import org.soyuz.engine.render.text.TextPainter
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.Shader
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.MathUtil
import org.soyuz.util.Vector2D
import kotlin.math.roundToInt

fun main() {
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

    var width = 800
    var height = 600

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(width, height, "Bump - Text Test", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(0)

    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    // --- Renderer setup ---
    val shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
    val camera = Camera()
    camera.setOrtho(width.toFloat(), height.toFloat())
    val quadMesh = Mesh.quad()

    // --- Framebuffer size callback ---
    glfwSetFramebufferSizeCallback(window) { _, w, h ->
        width = w; height = h
        glViewport(0, 0, width, height)
        camera.setOrtho(width.toFloat(), height.toFloat())
    }

    glfwSetKeyCallback(window) { _, key, _, action, _ ->
        KeyListener.keyCallback(0, key, 0, action, 0)
    }

    // --- Scene setup ---
    val scene = RuntimeScene("text_test")
    val font = Assets.font("roboto") // drop roboto.ttf in resources/fonts/
    val textPainter = TextPainter(font)
    textPainter.fontSize = 30f
    textPainter.text = "FPS: "

    val textEntity = DefaultGameEntity("text")
    textEntity.goto(Vector2D(width / 2.0, height / 2.0))
    textEntity.shape = RectangleShape(400.0, 60.0)
    textEntity.painter = textPainter
    scene.addEntity(textEntity)

    // --- Main loop ---
    var lastTime = glfwGetTime()

    var delta = 0f

    while (!glfwWindowShouldClose(window)) {
        val currentTime = glfwGetTime()
        val dt = (currentTime - lastTime).toFloat()
        lastTime = currentTime


        delta += dt

        glfwPollEvents()
        val fps = (1/dt).roundToInt()

        textPainter.text = "FPS: $fps"

        if(delta > 1f) {
            textPainter.color = Color.random()
            delta = 0f
        }

        textPainter.update(dt)
        // In TextTest.kt, after textPainter.update(dt):
        val tex = textPainter.texture
        if (tex != null) {
            textEntity.shape = RectangleShape(tex.width.toDouble(), tex.height.toDouble())
        }

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        shader.bind()
        shader.setProjection(camera.getProjection())

        for (entity in scene.allEntities()) {
            when (entity.shape) {
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

    Assets.cleanup()
    glfwDestroyWindow(window)
    glfwTerminate()
}