package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.soyuz.engine.collision.TriangleCollider
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.TriangleShape
import org.soyuz.engine.ui.Interactive
import org.soyuz.engine.ui.draggable
import org.soyuz.input.Input
import org.soyuz.input.KeyListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.Vector2D
import kotlin.math.sin
import kotlin.math.cos

fun main() {
    val width = 800
    val height = 600

    val camera = Camera()
    val engine = RuntimeEngine(
        title = "Bump - Triangle Test",
        windowWidth = width,
        windowHeight = height,
        physicsSystem = null,
        camera = camera
    )
    engine.init()
    engine.shader = Assets.shader("default")
    engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))

    val scene = RuntimeScene("triangle_test")

    // --- Static colorful triangles in a pattern ---
    val colors = listOf(
        Color(255, 80, 80),   // red
        Color(80, 255, 80),   // green
        Color(80, 80, 255),   // blue
        Color(255, 255, 80),  // yellow
        Color(255, 80, 255),  // magenta
        Color(80, 255, 255),  // cyan
    )

    for (i in 0..5) {
        val angle = i * Math.PI / 3
        val cx = width / 2.0 + cos(angle) * 180.0
        val cy = height / 2.0 + sin(angle) * 180.0
        val size = 60.0

        val tri = DefaultGameEntity("tri_$i")
        tri.position = Vector2D(cx, cy)
        tri.rotation = angle
        tri.shape = TriangleShape.equilateral(size)
        tri.painter = SolidColor(colors[i])
        tri.collider = TriangleCollider(tri.shape as TriangleShape)
        scene.addEntity(tri)
    }

    // --- Draggable triangle ---
    val dragTri = DefaultGameEntity("drag_tri")
    dragTri.position = Vector2D(width / 2.0, height / 2.0)
    val dragSize = 80.0
    dragTri.shape = TriangleShape.equilateral(dragSize)
    dragTri.painter = SolidColor(Color(255, 200, 50, 220))
    dragTri.collider = TriangleCollider(dragTri.shape as TriangleShape)
    dragTri.interactive = Interactive
        .empty { dragTri.collider!!.containsPoint(it, dragTri.transform) }
        .draggable(onDrag = { _, pos -> dragTri.position = pos })
    scene.addEntity(dragTri)

    // --- Pulsing triangle ---
    val pulseTri = DefaultGameEntity("pulse_tri")
    pulseTri.position = Vector2D(width / 2.0, height - 100.0)
    val pulseBaseSize = 50.0
    pulseTri.shape = TriangleShape.equilateral(pulseBaseSize)
    pulseTri.painter = SolidColor(Color(150, 100, 255, 200))
    pulseTri.collider = TriangleCollider(pulseTri.shape as TriangleShape)
    scene.addEntity(pulseTri)

    // --- Rotating triangle ---
    val rotTri = DefaultGameEntity("rot_tri")
    rotTri.position = Vector2D(150.0, height / 2.0)
    val rotSize = 55.0
    rotTri.shape = TriangleShape.equilateral(rotSize)
    rotTri.painter = SolidColor(Color(100, 255, 150, 220))
    rotTri.collider = TriangleCollider(rotTri.shape as TriangleShape)
    scene.addEntity(rotTri)

    // --- Instructions ---
    val font = Assets.font("roboto")
    val instrLabel = org.soyuz.engine.ui.UI.label(
        "instr", width / 2.0, 20.0,
        "6 static triangles | 1 draggable (center) | 1 pulsing (bottom) | 1 rotating (left) | ESC to exit",
        font, 12f, Color(180, 180, 180)
    )
    scene.addEntity(instrLabel)

    var time = 0f

    engine.forever { dt ->
        time += dt.toFloat()

        // Pulse the bottom triangle
        val pulse = 1.0 + sin(time * 4.0) * 0.3
        pulseTri.shape = TriangleShape.equilateral(pulse * pulseBaseSize)
        pulseTri.painter = SolidColor(Color(
            (150 + sin(time * 2.0) * 80).toInt(),
            (100 + sin(time * 2.5) * 80).toInt(),
            (255 + sin(time * 3.0) * 0).toInt(),
            200
        ))

        // Rotate the left triangle continuously
        rotTri.rotation += dt * 2.0

        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(engine.window, true)
        }
    }

    engine.loadScene(scene)
    engine.run()
}