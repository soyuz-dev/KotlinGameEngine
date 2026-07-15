package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.soyuz.engine.audio.AudioSource
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
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.math.Transform
import org.soyuz.util.math.Vector2D
import kotlin.math.roundToInt
import kotlin.math.sin

fun main() {
    val width = 800
    val height = 600

    // --- Systems & Engine Setup --//
    val camera = Camera()

    // The RuntimeEngine now handles all GLFW init, window hints, GL capabilities,
    // audio initialization, and the camera's initial setOrtho projection.
    val engine = RuntimeEngine(
        title = "Bump - AV Test v3",
        windowWidth = width,
        windowHeight = height,
        physicsSystem = null,
        camera = camera
    )


    engine.init()


    engine.shader = Assets.shader("default")
    engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))

    // --- Scene Setup ---
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
        AudioSource().play(Assets.audio("meow"))
    }
    cat.collider = org.soyuz.engine.collision.RectangleCollider(
        RectangleShape(180.0, 180.0)
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
        AudioSource().play(Assets.audio("bark"))
    }
    dog.collider = org.soyuz.engine.collision.RectangleCollider(
        RectangleShape(180.0, 180.0)
    )
    scene.addEntity(dog)

    // FPS counter
    val fpsLabel = UI.label("fps", width / 2.0, 10.0, "FPS: 0", font, 20f, Color(255, 255, 255))
    scene.addEntity(fpsLabel)

    // Title
    val title = UI.label("title", width / 2.0, 60.0, "Bump AV Test v2", font, 40f, Color(255, 200, 255))
    scene.addEntity(title)

    // Pulsing circle
    val circle = DefaultGameEntity("circle")
    circle.transform = Transform(position = Vector2D(width / 2.0, height / 2.0))
    circle.shape = CircleShape(60.0)
    circle.painter = SolidColor(Color(255, 100, 50, 100))
    scene.addEntity(circle)

    // Play button
    val playBtn = UI.button("play", width / 2.0, height - 80.0, 200.0, 50.0) {
        println("Play clicked!")

        // Camera shake using the `during` interval logic
        engine.during(500.0) { progress ->
            val intensity = (1.0 - progress) * 20.0  // decay from 20px to 0
            val offsetX = (Math.random() - 0.5) * intensity
            val offsetY = (Math.random() - 0.5) * intensity * 0.2

            // Note: utilizing engine.width/height dynamically bounds logic
            // safely in case the window gets resized during the shake
            camera.setOrtho(
                (engine.width + offsetX).toFloat(),
                (engine.height + offsetY).toFloat()
            )
        }
        // Reset camera exactly after shake
        engine.after(500.0) {
            camera.setOrtho(engine.width.toFloat(), engine.height.toFloat())
        }
    }
    scene.addEntity(playBtn)

    // --- Logic Hooks ---
    engine.loadScene(scene)

    engine.forEvery(500.0) {
        AudioSource().play(Assets.audio("click"))
    }
    engine.after(1000.0) {
        AudioSource().play(Assets.audio("meow"))
    }

    // Replace the manual `while` loop using `during` running infinitely
    var lastTime = glfwGetTime()
    var time = 0f

    val fpsHistory = mutableListOf<Float>()
    var fpsAccumulator = 0f
    var fpsFrameCount = 0

    engine.everyFrame {
        val currentTime = glfwGetTime()
        val dt = (currentTime - lastTime).toFloat()
        lastTime = currentTime
        time += dt

        // Rolling FPS average over 1 second
        fpsAccumulator += dt
        fpsFrameCount++
        if (fpsAccumulator >= 1f) {
            val avgFps = fpsFrameCount / fpsAccumulator
            fpsHistory.add(avgFps)
            if (fpsHistory.size > 10) fpsHistory.removeAt(0) // keep last 10 samples
            fpsAccumulator = 0f
            fpsFrameCount = 0
        }

        val displayFps = if (fpsHistory.isNotEmpty()) {
            fpsHistory.average().toFloat()
        } else {
            if (dt > 0) (1f / dt) else 0f
        }

        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            engine.quit()
        }

        val fpsPainter = fpsLabel.painter as TextPainter
        fpsPainter.text = "FPS: ${displayFps.toInt()}"
        fpsLabel.shape = RectangleShape(
            fpsPainter.texture?.width?.toDouble() ?: 100.0,
            fpsPainter.texture?.height?.toDouble() ?: 30.0
        )

        val pulse = sin(time * 3.0)
        circle.shape = CircleShape(60.0 * pulse + 80.0)
        engine.width = (600 + 200 * pulse).roundToInt()
    }

    // --- Run Loop ---
    // Start game execution. It automatically blocks, polls, updates,
    // renders, handles timers, and then cleans up everything.
    engine.run()
}