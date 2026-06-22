package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.audio.AudioSource
import org.soyuz.engine.audio.AudioSystem
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.render.image.ImagePainter
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.engine.ui.Interactive
import org.soyuz.engine.ui.UI
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D
import kotlin.math.sin

fun main() {
    // --- Window ---
    if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")
    var width = 800; var height = 600
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    val window = glfwCreateWindow(width, height, "Bump - AV Test v2", NULL, NULL)
    if (window == NULL) throw RuntimeException("Failed to create window")
    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(0)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    AudioSystem.init()

    // --- Systems ---
    val shader = Assets.shader("default")
    val camera = Camera()
    camera.setOrtho(width.toFloat(), height.toFloat())
    val renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    val engine = RuntimeEngine(
        physicsSystem = null,  // no physics for this demo
        renderSystem = renderSystem,
        shader = shader,
        camera = camera
    )

    // --- Callbacks ---
    glfwSetFramebufferSizeCallback(window) { _, w, h ->
        width = w; height = h
        glViewport(0, 0, width, height)
        camera.setOrtho(width.toFloat(), height.toFloat())
    }
    glfwSetKeyCallback(window) { _, key, _, action, _ -> KeyListener.keyCallback(0, key, 0, action, 0) }
    glfwSetMouseButtonCallback(window) { _, button, action, _ -> MouseListener.mouseButtonCallback(0, button, action, 0) }
    glfwSetCursorPosCallback(window) { _, x, y -> MouseListener.mousePosCallback(0, x, y) }
    glfwSetScrollCallback(window) { _, x, y -> MouseListener.mouseScrollCallback(0, x, y) }

    // --- Scene ---
    val scene = RuntimeScene("av_test")
    val font = Assets.font("roboto")

    // Cat
    val cat = DefaultGameEntity("cat")
    cat.transform = Transform(position = Vector2D(width / 4.0, height / 2.0))
    cat.shape = RectangleShape(180.0, 180.0)
    cat.painter = ImagePainter(Assets.texture("cat"))
    cat.interactive = Interactive.clickable(
        containsPoint = { cat.collider?.containsPoint(it, cat.transform) ?: false }
    ) {
        val meow = AudioSource()
        meow.play(Assets.audio("meow"))
    }
    cat.collider = org.soyuz.engine.collision.RectangleCollider(
        org.soyuz.engine.shape.RectangleShape(180.0, 180.0)
    )
    scene.addEntity(cat)

    // Dog
    val dog = DefaultGameEntity("dog")
    dog.transform = Transform(position = Vector2D(3 * width / 4.0, height / 2.0))
    dog.shape = RectangleShape(180.0, 180.0)
    dog.painter = ImagePainter(Assets.texture("dog"))
    dog.interactive = Interactive.clickable(
        containsPoint = { dog.collider?.containsPoint(it, dog.transform) ?: false }
    ) {
        val bark = AudioSource()
        bark.play(Assets.audio("bark"))
    }
    dog.collider = org.soyuz.engine.collision.RectangleCollider(
        org.soyuz.engine.shape.RectangleShape(180.0, 180.0)
    )
    scene.addEntity(dog)

    // FPS counter
    val fpsLabel = UI.label("fps", width/2.0, 10.0, "FPS: 0", font, 20f, Color(255, 255, 255))
    scene.addEntity(fpsLabel)

    // Title
    val title = UI.label("title", width / 2.0 - 150.0, 40.0, "Bump AV Test v2", font, 40f, Color(255, 200, 50))
    scene.addEntity(title)

    // Pulsing circle
    val circle = DefaultGameEntity("circle")
    circle.transform = Transform(position = Vector2D(width / 2.0, height / 2.0))
    circle.shape = CircleShape(60.0)
    circle.painter = SolidColor(Color(255, 100, 50, 100))
    scene.addEntity(circle)

    // Play button
    val playBtn = UI.button("play", width / 2.0, height - 80.0, 200.0, 50.0) {
        val click = AudioSource()
        click.play(Assets.audio("click"))
        println("Play clicked!")
    }
    scene.addEntity(playBtn)

    // --- Run ---
    engine.loadScene(scene)
    engine.start()

    var lastTime = glfwGetTime()
    var time = 0f

    while (!glfwWindowShouldClose(window)) {
        val currentTime = glfwGetTime()
        val dt = (currentTime - lastTime).toFloat()
        lastTime = currentTime
        time += dt

        glfwPollEvents()

        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(window, true)
        }

        // Update FPS text
        val fps = (1f / dt).toInt()
        val fpsPainter = fpsLabel.painter as TextPainter
        fpsPainter.text = "FPS: $fps"
        fpsLabel.shape = RectangleShape(fpsPainter.texture?.width?.toDouble() ?: 100.0,
            fpsPainter.texture?.height?.toDouble() ?: 30.0)

        // Pulse circle
        val pulse = 1.0 + sin(time * 3.0) * 0.3
        circle.shape = CircleShape(60.0 * pulse)

        engine.update(dt)
        engine.render()

        glfwSwapBuffers(window)
        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    engine.stop()
    AudioSystem.cleanup()
    Assets.cleanup()
    glfwDestroyWindow(window)
    glfwTerminate()
}