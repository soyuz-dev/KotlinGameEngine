package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.audio.AudioSource
import org.soyuz.engine.audio.AudioSystem
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.render.image.ImagePainter
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.MathUtil
import org.soyuz.util.Vector2D
import kotlin.math.roundToInt
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
    val scene = RuntimeScene("av_test")

    // Cat
    val cat = DefaultGameEntity("cat")
    cat.goto(Vector2D(width / 3.0, height / 2.0))
    cat.shape = RectangleShape(200.0, 200.0)
    cat.painter = ImagePainter(Assets.texture("cat"))
    scene.addEntity(cat)

    // Dog
    val dog = DefaultGameEntity("dog")
    dog.goto(Vector2D(2 * width / 3.0, height / 2.0))
    dog.shape = RectangleShape(200.0, 200.0)
    dog.painter = ImagePainter(Assets.texture("dog"))
    scene.addEntity(dog)

    // FPS Counter
    val font = Assets.font("roboto")
    val fpsPainter = TextPainter(font)
    fpsPainter.fontSize = 24f
    fpsPainter.text = "FPS: 0"
    fpsPainter.color = Color(255, 255, 255)

    val fpsEntity = DefaultGameEntity("fps")
    fpsEntity.goto(Vector2D(width/2.0, 10.0))
    fpsEntity.shape = RectangleShape(200.0, 30.0)
    fpsEntity.painter = fpsPainter
    scene.addEntity(fpsEntity)

    // Title
    val titlePainter = TextPainter(font)
    titlePainter.fontSize = 48f
    titlePainter.text = "Bump AV Test"
    titlePainter.color = Color(255, 200, 50)

    val titleEntity = DefaultGameEntity("title")
    titleEntity.goto(Vector2D(width / 2.0 - 150.0, 50.0))
    titleEntity.shape = RectangleShape(400.0, 60.0)
    titleEntity.painter = titlePainter
    scene.addEntity(titleEntity)

    // Instructions
    val instrPainter = TextPainter(font)
    instrPainter.fontSize = 18f
    instrPainter.text = "Click on cat or dog for sound | Press ESC to exit"
    instrPainter.color = Color(180, 180, 180)

    val instrEntity = DefaultGameEntity("instructions")
    instrEntity.goto(Vector2D(width / 2.0 - 200.0, height - 40.0))
    instrEntity.shape = RectangleShape(500.0, 25.0)
    instrEntity.painter = instrPainter
    scene.addEntity(instrEntity)

    // Decorative circle
    val circle = DefaultGameEntity("circle")
    circle.goto(Vector2D(width / 2.0, height / 2.0))
    circle.shape = CircleShape(80.0)
    circle.painter = SolidColor(Color(255, 100, 50, 100))
    scene.addEntity(circle)

    // --- Main loop ---
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

        val fps = (1f / dt).roundToInt()
        fpsPainter.text = "FPS: $fps"

        // Pulse circle
        val pulse = 1.0 + sin(time * 3.0) * 0.3
        circle.shape = CircleShape(80.0 * pulse)
        (circle.painter as SolidColor).let {
            val hue = (sin(time * 0.5) * 0.5 + 0.5)
            // Can't modify SolidColor easily — skip for now or reconstruct
        }

        // Click detection
        if (MouseListener.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            val mPos = MouseListener.getPos()
            val catBounds = cat.shape as RectangleShape
            val dogBounds = dog.shape as RectangleShape

            if (mPos.x in cat.transform.position.x - catBounds.width/2 .. cat.transform.position.x + catBounds.width/2 &&
                mPos.y in cat.transform.position.y - catBounds.height/2 .. cat.transform.position.y + catBounds.height/2) {
                val meow = AudioSource()
                meow.play(Assets.audio("meow"))
            }

            if (mPos.x in dog.transform.position.x - dogBounds.width/2 .. dog.transform.position.x + dogBounds.width/2 &&
                mPos.y in dog.transform.position.y - dogBounds.height/2 .. dog.transform.position.y + dogBounds.height/2) {
                val bark = AudioSource()
                bark.play(Assets.audio("bark"))
            }
        }

        // Update dynamic painters
        fpsPainter.update(dt)
        titlePainter.update(dt)
        instrPainter.update(dt)

        // Resize text entities to match textures
        for (painter in listOf(fpsPainter, titlePainter, instrPainter)) {
            painter.texture?.let { tex ->
                val entity = when (painter) {
                    fpsPainter -> fpsEntity
                    titlePainter -> titleEntity
                    instrPainter -> instrEntity
                    else -> null
                }
                entity?.shape = RectangleShape(tex.width.toDouble(), tex.height.toDouble())
            }
        }

        AudioSystem.update(0f, 0f, 0f, 0f)

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