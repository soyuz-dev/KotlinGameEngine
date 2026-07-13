package org.soyuz

import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.ui.Interactive
import org.soyuz.engine.ui.UI
import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.ui.draggable
import org.soyuz.engine.ui.hoverable
import org.soyuz.input.KeyListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.Dynamic
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D

fun main() {
    val width = 800
    val height = 600

    val camera = Camera()
    val engine = RuntimeEngine(
        title = "Bump - Dragging Test",
        windowWidth = width,
        windowHeight = height,
        physicsSystem = null,
        camera = camera
    )
    engine.init()
    engine.shader = Assets.shader("default")
    engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))

    val scene = RuntimeScene("drag_test")
    val font = Assets.font("roboto")

    // Volume label
    val volumeLabel = UI.label("volume_label", width / 2.0, height - 130.0, "Volume: 50%", font, 16f, Color(200, 200, 200))
    scene.addEntity(volumeLabel)
    // --- Slider ---
    val (sliderTrack, sliderThumb) = UI.slider(
        "volume", width / 2.0, height - 100.0, 300.0, 30.0,
        min = 0.0, max = 100.0, initial = 50.0
    ) { value ->
        println("Volume: ${value.toInt()}%")
        volumeLabel.painter?.let {
            (it as TextPainter).text = "Volume: ${value.toInt()}%"
        }
    }
    scene.addEntity(sliderTrack)
    scene.addEntity(sliderThumb)
    // --- Draggable circle ---
    val circle = DefaultGameEntity("drag_circle")
    circle.transform = Transform(position = Vector2D(150.0, 150.0))
    circle.shape = CircleShape(40.0)
    circle.painter = SolidColor(Color(200, 80, 60, 200))
    circle.collider = CircleCollider(CircleShape(40.0))
    circle.interactive = Interactive
        .clickable(containsPoint = { circle.collider!!.containsPoint(it, circle.transform) }) { }
        .draggable(
            onDrag = { _, currentPos -> circle.position = currentPos },
        )
        .hoverable(
            onHoverEnter = { circle.painter = SolidColor(Color(255, 120, 90, 220)) },
            onHoverExit = { circle.painter = SolidColor(Color(200, 80, 60, 200)) }
        )
    scene.addEntity(circle)
    // Circle label
    val circleLabel = UI.label("circle_label", 150.0, 80.0, "Drag the circle too!", font, 14f, Color(200, 200, 200))
    scene.addEntity(circleLabel)
    // Instructions
    val instrLabel = UI.label("instr", width / 2.0, 20.0, "Drag the circle, and slider. And also type in your name!", font, 14f, Color(150, 150, 150))
    scene.addEntity(instrLabel)


    circle.onPositionChanged { pos ->
        circleLabel.position = pos + Vector2D(0.0, -50.0)
    }

    val nameLabel = UI.label("namelabel", width / 2.0, 600.0, "where", font, 14f, Color(150, 150, 150))
    scene.addEntity(instrLabel)


    val (nameInput, nameInputBG) = UI.textInput("name", 400.0, 300.0, font = font, placeholder = "Enter name...", background = SolidColor(Color(60, 10, 30))) { text ->
        (nameLabel.painter as? TextPainter)?.text = text
        (nameLabel.painter as? Dynamic)?.update(.0f)
    }

    scene.addEntity(nameInputBG)
    scene.addEntity(nameInput)

    engine.forEvery(200.0) {
        nameInput.toggleCursor()
    }

    engine.everyFrame {
        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(engine.window, true)
        }
    }

    engine.loadScene(scene)
    engine.run()
}