package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.soyuz.engine.core.Application
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.windowing.Window
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.TriangleShape
import org.soyuz.input.KeyListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.math.Transform
import org.soyuz.util.math.Vector2D
import kotlin.math.sin

fun main() {
    val application = Application()

    // --- Window 1: Rotating Triangle ---
    val window1 = Window("Bump - Rotating Triangle", 600, 500)
    window1.x = 100; window1.y = 100

    val camera1 = Camera()
    val engine1 = RuntimeEngine(window1, null, camera1)

    application.windows.add(window1, engine1) {
        engine1.shader = Assets.shader("default")
        engine1.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }

    val scene1 = RuntimeScene("triangle_scene")

    val triangle = DefaultGameEntity("rotating_tri")
    triangle.position = Vector2D(300.0, 250.0)
    triangle.shape = TriangleShape.isosceles(60.0, 80.0)
    triangle.painter = SolidColor(Color(100, 200, 255))
    scene1.addEntity(triangle)

    var triTime = 0f
    engine1.everyFrame { dt ->
        triTime += dt.toFloat()
        triangle.rotation += dt * 3.0
        // Pulsing color
        val r = (150 + sin(triTime * 2.0) * 100).toInt()
        val g = (180 + sin(triTime * 2.5) * 70).toInt()
        val b = (220 + sin(triTime * 3.0) * 35).toInt()
        triangle.painter = SolidColor(Color(r, g, b))
        engine1.title = "Triangle | FPS: ${(1.0 / dt).toInt()}"
        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) engine1.quit()
    }

    engine1.loadScene(scene1)

    // --- Window 2: Pulsing Circle ---
    val window2 = Window("Bump - Pulsing Circle", 600, 500)
    window2.x = 750; window2.y = 100

    val camera2 = Camera()
    val engine2 = RuntimeEngine(window2, null, camera2)

    application.windows.add(window2, engine2) {
        engine2.shader = Assets.shader("default")
        engine2.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }

    val scene2 = RuntimeScene("circle_scene")

    val circle = DefaultGameEntity("pulsing_circle")
    circle.position = Vector2D(300.0, 250.0)
    circle.shape = CircleShape(80.0)
    circle.painter = SolidColor(Color(2, 130, 80))
    scene2.addEntity(circle)

    var circleTime = 0f
    engine2.everyFrame { dt ->
        circleTime += dt.toFloat()
        val pulse = 1.0 + sin(circleTime * 4.0) * 0.4
        circle.shape = CircleShape(80.0 * pulse)
        val r = (255 + sin(circleTime * 1.5) * 0).toInt()
        val g = (130 + sin(circleTime * 2.0) * 80).toInt()
        val b = (80 + sin(circleTime * 2.5) * 100).toInt()
        circle.painter = SolidColor(Color(r, g, b))
        engine2.title = "Circle | FPS: ${(1.0 / dt).toInt()}"
        if (KeyListener.isKeyJustPressed(GLFW_KEY_ESCAPE)) engine2.quit()
    }

    engine2.loadScene(scene2)

    application.run()
}