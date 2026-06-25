package org.soyuz

import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.collision.RuntimeCollisionSystem
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.events.RuntimeEventBus
import org.soyuz.engine.physics.*
import org.soyuz.engine.physics.forcefields.ConstantAccelerationField
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D

fun main() {
    val width = 800
    val height = 800

    val camera = Camera()
    val collisionSystem = RuntimeCollisionSystem()
    val eventBus = RuntimeEventBus()
    val physicsSystem = RuntimePhysicsSystem(collisionSystem, eventBus)

    val engine = RuntimeEngine(
        title = "Bump - BrickPit",
        windowWidth = width,
        windowHeight = height,
        physicsSystem = physicsSystem,
        camera = camera
    )
    engine.init()
    engine.shader = Assets.shader("default")
    engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))

    val scene = RuntimeScene("brickpit")
    val gravity = ConstantAccelerationField(Vector2D(0.0, 981.0))
    var entityCount = 0

    fun makeBall(x: Double, y: Double, vx: Double, vy: Double, mass: Double = 1.0, radius: Double = 10.0, restitution: Double = 0.8) {
        val id = "ball_${entityCount++}"
        val ball = DefaultGameEntity(id)
        ball.position = Vector2D(x, y)
        ball.shape = CircleShape(radius)
        ball.painter = SolidColor(Math.random(), Math.random(), Math.random(), 1.0)
        val body = PointMass(mass = mass, restitution = restitution)
        body.addField(gravity)
        body.velocity = Vector2D(vx, vy)
        val collider = CircleCollider(CircleShape(radius))
        physicsSystem.registerBody(id, body)
        collisionSystem.registerCollider(id, collider)
        scene.addEntity(ball)
    }

    fun makeBrick(x: Double, y: Double, vx: Double, vy: Double, w: Double = 40.0, h: Double = 30.0, mass: Double = 1.0, angVel: Double = 0.0, restitution: Double = 0.5) {
        val id = "brick_${entityCount++}"
        val brick = DefaultGameEntity(id)
        brick.position = Vector2D(x, y)
        brick.shape = RectangleShape(w, h)
        brick.painter = SolidColor(0.8, 0.4, 0.1, 1.0)
        val body = RigidBody(mass = mass, restitution = restitution, friction = 0.4, width = w, height = h)
        body.addField(gravity)
        body.velocity = Vector2D(vx, vy)
        body.angularVelocity = angVel
        val collider = RectangleCollider(RectangleShape(w, h))
        physicsSystem.registerBody(id, body)
        collisionSystem.registerCollider(id, collider)
        scene.addEntity(brick)
    }

    fun createWall(id: String, x: Double, y: Double, w: Double, h: Double) {
        val wall = DefaultGameEntity(id)
        wall.position = Vector2D(x, y)
        wall.shape = RectangleShape(w, h)
        wall.painter = SolidColor(0.2, 0.2, 0.3, 1.0)
        val wallBody = PointMass(mass = 0.0, restitution = 0.6)
        val wallCollider = RectangleCollider(RectangleShape(w, h))
        physicsSystem.registerBody(id, wallBody)
        collisionSystem.registerCollider(id, wallCollider)
        scene.addEntity(wall)
    }

    val wallThickness = 100.0
    createWall("left",   -wallThickness / 2, height / 2.0, wallThickness * 1.5, height.toDouble())
    createWall("right",   width + wallThickness / 2, height / 2.0, wallThickness * 1.5, height.toDouble())
    createWall("top",     width / 2.0, -wallThickness / 2, width.toDouble(), wallThickness * 1.5)
    createWall("bottom",  width / 2.0, height + wallThickness / 2, width.toDouble(), wallThickness * 1.5)

    makeBall(width / 3.0, height / 2.0, 200.0, -500.0, mass = 0.5, radius = 8.0)
    makeBall(2 * width / 3.0, height / 2.0, -200.0, -300.0, mass = 0.5, radius = 8.0)

    engine.forever {
        if (MouseListener.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            val mPos = MouseListener.getPos()
            if (Math.random() < 0.5) {
                val r = 5.0 + Math.random() * 15.0
                val vx = (Math.random() - 0.5) * 600.0
                val vy = (Math.random() - 0.5) * 600.0 - 300.0
                makeBall(mPos.x, mPos.y, vx, vy, mass = 0.5 + Math.random() * 2.0, radius = r)
            } else {
                val w = 20.0 + Math.random() * 60.0
                val h = 15.0 + Math.random() * 40.0
                val vx = (Math.random() - 0.5) * 400.0
                val vy = (Math.random() - 0.5) * 400.0 - 200.0
                val angVel = (Math.random() - 0.5) * 10.0
                makeBrick(mPos.x, mPos.y, vx, vy, w = w, h = h, mass = 0.5 + Math.random() * 2.0, angVel = angVel)
            }
        }
    }

    engine.loadScene(scene)
    engine.run()
}