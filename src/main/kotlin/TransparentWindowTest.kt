package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.soyuz.engine.collision.Collider
import org.soyuz.engine.core.Application
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.engine.ui.Interactive
import org.soyuz.engine.ui.clickable
import org.soyuz.engine.ui.draggable
import org.soyuz.engine.ui.hoverable
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.Debug
import org.soyuz.util.math.Easing
import org.soyuz.util.math.Vector2D
import org.soyuz.windowing.TransparentWindow
import kotlin.math.roundToInt

fun main() {
    val width = 800
    val height = 500
    Application.init()

    val camera = Camera()
    val window = TransparentWindow("Faux Window", width, height)
    val engine = RuntimeEngine(window, physicsSystem = null, camera)

    window.x = 200
    window.y = 200

    var dragStartWindowX = 0
    var dragStartWindowY = 0
    var dragStartMouseX = 0.0
    var dragStartMouseY = 0.0

    Application.windows.add(window, engine) {
        engine.shader = Assets.shader("default")
        engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }

    val scene = RuntimeScene("faux_window")
    val font = Assets.font("roboto")

    // --- Title bar background ---
    val titleBar = DefaultGameEntity("titlebar")
    titleBar.position = Vector2D(width / 2.0, 15.0)
    titleBar.shape = RectangleShape(width.toDouble(), 30.0)
    titleBar.painter = SolidColor(Color(40, 40, 50))


    // --- Window title ---
    val titleLabel = engine.ui.label("title", .0, 15.0, "Faux Window", font, 16f, Color(220, 220, 220))
    titleLabel.position = titleLabel.position.copy(x = (titleLabel.shape as RectangleShape).width/2 + 15)


    // --- Close button ---
    val closeBtn = DefaultGameEntity("close")
    closeBtn.position = Vector2D(width - 20.0, 15.0)
    closeBtn.shape = CircleShape(10.0)
    closeBtn.painter = SolidColor(Color(220, 60, 60))
    closeBtn.collider = Collider(CircleShape(10.0))
    closeBtn.interactive = Interactive {
        val result = closeBtn.collider!!.containsPoint(it, closeBtn.transform)
        if (result) println("HIT closeBtn at mouse=$it, btn=${closeBtn.position}")
        result
    }
        .clickable {
            println("Close clicked! Window: ${window.x}, ${window.y}, ${window.width}x${window.height}")
            engine.quit()
        }
        .hoverable(
            onHoverEnter = { closeBtn.painter = SolidColor(Color(255, 80, 80)) },
            onHoverExit = { closeBtn.painter = SolidColor(Color(220, 60, 60)) }
        )


    // --- Minimize button ---
    val minBtn = DefaultGameEntity("minimize")
    minBtn.position = Vector2D(width - 50.0, 15.0)
    minBtn.shape = CircleShape(10.0)
    minBtn.painter = SolidColor(Color(220, 180, 60))
    minBtn.collider = Collider(CircleShape(10.0))
    minBtn.interactive = Interactive { minBtn.collider!!.containsPoint(it, minBtn.transform) }
        .clickable { window.minimize() }
        .hoverable(
            onHoverEnter = { minBtn.painter = SolidColor(Color(255, 210, 80)) },
            onHoverExit = { minBtn.painter = SolidColor(Color(220, 180, 60)) }
        )


    // --- Window body ---
    val body = DefaultGameEntity("body")
    body.position = Vector2D(width / 2.0, height / 2.0 + 15.0)
    body.shape = RectangleShape(width.toDouble(), (height - 30).toDouble())
    body.painter = SolidColor(Color(30, 30, 40, 230))


    // --- Content ---
    val contentLabel = engine.ui.label("content", width / 2.0, height / 2.0 + 5.0,
        "This is a custom window. No OS chrome — all custom.", font, 18f, Color(200, 200, 200))

    val fpsLabel = engine.ui.label("fps", width / 2.0, height / 2.0 + 20.0,
        "FPS:", font, 18f, Color(200, 200, 200))


    // --- Resize handle ---
    val resizeHandle = DefaultGameEntity("resize")
    resizeHandle.position = Vector2D(width - 15.0, height - 15.0)
    resizeHandle.shape = RectangleShape(30.0, 30.0)
    resizeHandle.painter = SolidColor(Color(0, 0, 0, 0)) // invisible, just for dragging
    resizeHandle.collider = Collider(RectangleShape(30.0, 30.0))
    resizeHandle.interactive = Interactive { resizeHandle.collider!!.containsPoint(it, resizeHandle.transform) }
        .draggable(
            onDrag = { _, pos ->
                window.width = pos.x.toInt().coerceIn(300, 1920)
                window.height = pos.y.toInt().coerceIn(200, 1080)
            }
        )


    // --- Title bar drag ---
    val titleDrag = DefaultGameEntity("title_drag")
    titleDrag.position = Vector2D(width / 2.0, 15.0)
    titleDrag.shape = RectangleShape(width.toDouble(), 30.0)
    titleDrag.painter = SolidColor(Color(0, 0, 0, 0)) // invisible overlay
    titleDrag.collider = Collider(RectangleShape(width.toDouble(), 30.0))
    titleDrag.interactive = Interactive { titleDrag.collider!!.containsPoint(it, titleDrag.transform) }
        .draggable(
            onDragStart = { _, _ ->
                dragStartWindowX = window.x
                dragStartWindowY = window.y
                val (sx, sy) = MouseListener.getDesktopPos()
                dragStartMouseX = sx
                dragStartMouseY = sy
            },
            onDrag = { _, _ ->
                val s = MouseListener.getDesktopPos()
                window.x = dragStartWindowX + s.x.toInt() - dragStartMouseX.toInt()
                window.y = dragStartWindowY + s.y.toInt() - dragStartMouseY.toInt()
            }
        )
    scene.addEntity(body)
    scene.addEntity(contentLabel)
    scene.addEntity(fpsLabel)
    scene.addEntity(titleBar)
    scene.addEntity(titleLabel)

    scene.addEntity(titleDrag)
    scene.addEntity(resizeHandle)
    scene.addEntity(minBtn)
    scene.addEntity(closeBtn)

    // --- Pulsing circle (just for fun) ---
    val circle = DefaultGameEntity("circle")
    circle.position = Vector2D(width / 2.0, height / 2.0 + 80.0)
    circle.shape = CircleShape(40.0)
    circle.painter = SolidColor(Color(100, 180, 255, 150))
    scene.addEntity(circle)

    engine.loadScene(scene)

    window.onResize { w, h ->
        println("resize fired: $w, $h")
        resizeHandle.position = Vector2D(w - 15.0, h - 15.0)
        body.shape = RectangleShape(w.toDouble(), (h - 30).toDouble())
        body.position = Vector2D(w / 2.0, h / 2.0 + 15.0)
        titleBar.shape = RectangleShape(w.toDouble(), 30.0)
        titleBar.position = Vector2D(w / 2.0, 15.0)
        closeBtn.position = Vector2D(w - 20.0, 15.0)
        minBtn.position = Vector2D(w - 50.0, 15.0)
        titleDrag.shape = RectangleShape(w.toDouble(), 30.0)
        titleDrag.position = Vector2D(w / 2.0, 15.0)
        titleDrag.collider = Collider(RectangleShape(w.toDouble(), 30.0))
    }

    var time = 0f
    engine { dt ->
        if (window.isMinimized()) return@engine
        time += dt.toFloat()

        if (KeyListener.isKeyJustPressed(window.handle, GLFW_KEY_ESCAPE)) {
            engine.quit()
        }

        val raw = time % 2.0
        val t = if (raw < 1.0) raw else 2.0 - raw
        val pulse = Easing.cubicInOut(t)
        circle.shape = CircleShape(40.0 * (0.5 + pulse * 0.5))

        val fpsPainter = fpsLabel.painter as? TextPainter
        fpsPainter?.text = "FPS: ${(1/dt).roundToInt()}"
        fpsPainter?.update(dt.toFloat())
        fpsLabel.shape = RectangleShape(
            fpsPainter?.texture?.width?.toDouble() ?: 100.0,
            fpsPainter?.texture?.height?.toDouble() ?: 30.0
        )
        println("closeBtn pos: ${closeBtn.position}, window: ${window.width}x${window.height}")
    }

    Application.run()
}