package org.soyuz

import org.lwjgl.glfw.GLFW.*
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
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.math.Vector2D

fun main() {
    // Window 1
    val window1 = Window("Bump - Window 1", 800, 600)
    window1.x = 100; window1.y = 100

    val camera1 = Camera()
    val engine1 = RuntimeEngine(window1, null, camera1)
    engine1.init()
    engine1.shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
    engine1.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))

    val scene1 = RuntimeScene("scene1")
    val circle1 = DefaultGameEntity("circle1")
    circle1.position = Vector2D(400.0, 300.0)
    circle1.shape = CircleShape(60.0)
    circle1.painter = SolidColor(Color(255, 100, 80))
    scene1.addEntity(circle1)

    val rect1 = DefaultGameEntity("rect1")
    rect1.position = Vector2D(200.0, 200.0)
    rect1.shape = RectangleShape(100.0, 80.0)
    rect1.painter = SolidColor(Color(80, 180, 255))
    scene1.addEntity(rect1)

    engine1.loadScene(scene1)

    // Window 2
    val window2 = Window("Bump - Window 2", 600, 400)
    window2.x = 950; window2.y = 100

    val camera2 = Camera()
    val engine2 = RuntimeEngine(window2, null, camera2)
    engine2.init()
    engine2.shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
    engine2.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))

    val scene2 = RuntimeScene("scene2")
    val circle2 = DefaultGameEntity("circle2")
    circle2.position = Vector2D(300.0, 200.0)
    circle2.shape = CircleShape(50.0)
    circle2.painter = SolidColor(Color(100, 255, 130))
    scene2.addEntity(circle2)

    val rect2 = DefaultGameEntity("rect2")
    rect2.position = Vector2D(450.0, 300.0)
    rect2.shape = RectangleShape(120.0, 60.0)
    rect2.painter = SolidColor(Color(255, 220, 80))
    scene2.addEntity(rect2)

    engine2.loadScene(scene2)

    engine1.start()
    engine2.start()

    var lastTime = glfwGetTime()
    while (!(window1.shouldClose() && !window2.shouldClose())) {
        val currentTime = glfwGetTime()
        val dt = (currentTime - lastTime).toFloat().coerceIn(0f, 0.25f)
        lastTime = currentTime

        if (!window1.shouldClose()) {
            window1.makeContextCurrent()
            window1.pollEvents()
            engine1.update(dt)
            engine1.render()
            window1.swapBuffers()
        }

        if (!window2.shouldClose()) {
            window2.makeContextCurrent()
            window2.pollEvents()
            engine2.update(dt)
            engine2.render()
            window2.swapBuffers()
        }

        KeyListener.endFrame()
        MouseListener.endFrame()
    }

    engine1.stop()
    engine2.stop()
    window1.destroy()
    window2.destroy()
    glfwTerminate()
}