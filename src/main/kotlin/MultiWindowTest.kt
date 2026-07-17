package org.soyuz

import org.soyuz.engine.core.Application
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.Color
import org.soyuz.util.math.Vector2D
import org.soyuz.windowing.Window

fun main() {
    val application = Application()

    val window1 = Window("Bump - Window 1", 800, 600)
    window1.x = 100
    window1.y = 100

    val engine1 = RuntimeEngine(window1, null, Camera())
    engine1.loadScene(firstScene())
    application.windows.add(window1, engine1) {
        engine1.shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
        engine1.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }

    val window2 = Window("Bump - Window 2", 600, 400)
    window2.x = 950
    window2.y = 100

    val engine2 = RuntimeEngine(window2, null, Camera())
    engine2.loadScene(secondScene())
    application.windows.add(window2, engine2) {
        engine2.shader = Shader.fromResource("/shaders/default.vert", "/shaders/default.frag")
        engine2.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }

    application.run()
}

private fun firstScene(): RuntimeScene {
    val scene = RuntimeScene("scene1")

    val circle = DefaultGameEntity("circle1")
    circle.position = Vector2D(400.0, 300.0)
    circle.shape = CircleShape(60.0)
    circle.painter = SolidColor(Color(255, 100, 80))
    scene.addEntity(circle)

    val rectangle = DefaultGameEntity("rect1")
    rectangle.position = Vector2D(200.0, 200.0)
    rectangle.shape = RectangleShape(100.0, 80.0)
    rectangle.painter = SolidColor(Color(80, 180, 255))
    scene.addEntity(rectangle)

    return scene
}

private fun secondScene(): RuntimeScene {
    val scene = RuntimeScene("scene2")

    val circle = DefaultGameEntity("circle2")
    circle.position = Vector2D(300.0, 200.0)
    circle.shape = CircleShape(50.0)
    circle.painter = SolidColor(Color(100, 255, 130))
    scene.addEntity(circle)

    val rectangle = DefaultGameEntity("rect2")
    rectangle.position = Vector2D(450.0, 300.0)
    rectangle.shape = RectangleShape(120.0, 60.0)
    rectangle.painter = SolidColor(Color(255, 220, 80))
    scene.addEntity(rectangle)

    return scene
}
