package org.soyuz

import org.lwjgl.glfw.GLFW.*
import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.collision.RuntimeCollisionSystem
import org.soyuz.engine.collision.TriangleCollider
import org.soyuz.engine.core.Application
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.windowing.Window
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.entity.GameEntity
import org.soyuz.engine.events.CollisionEvent
import org.soyuz.engine.events.RuntimeEventBus
import org.soyuz.engine.physics.PointMass
import org.soyuz.engine.physics.RuntimePhysicsSystem
import org.soyuz.engine.physics.forcefields.VelocityForceField
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.Mesh
import org.soyuz.engine.render.RuntimeRenderSystem
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.scene.RuntimeScene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.TriangleShape
import org.soyuz.input.KeyListener
import org.soyuz.util.Assets
import org.soyuz.util.Color
import org.soyuz.util.math.Polynomial
import org.soyuz.util.math.Vector2D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    val width = 800
    val height = 600
    val camera = Camera()
    val collisionSystem = RuntimeCollisionSystem()
    val eventBus = RuntimeEventBus()
    val physicsSystem = RuntimePhysicsSystem(collisionSystem, eventBus)

    val window = Window(
        title = "Bump - Asteroids",
        initialWidth = width,
        initialHeight = height,
    )
    val engine = RuntimeEngine(
        window = window,
        physicsSystem = physicsSystem,
        camera = camera
    )
    Application.windows.add(window, engine) {
        engine.shader = Assets.shader("default")
        engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }

    val scene = RuntimeScene("asteroids")
    var entityCount = 0

    fun wrapEntity(entity: GameEntity) {
        var pos = entity.transform.position
        if (pos.x < -10) pos = Vector2D(width.toDouble(), pos.y)
        if (pos.x > width+10) pos = Vector2D(0.0, pos.y)
        if (pos.y < -10) pos = Vector2D(pos.x, height.toDouble())
        if (pos.y > height+10) pos = Vector2D(pos.x, 0.0)
        entity.transform = entity.transform.copy(position = pos)
    }

    // --- Ship ---
    val ship = DefaultGameEntity("ship")
    ship.position = Vector2D(width / 2.0, height / 2.0)
    ship.shape = TriangleShape.isosceles(24.0, 35.0)
    ship.painter = SolidColor(Color(200, 200, 255))
    ship.collider = TriangleCollider(ship.shape as TriangleShape)
    val shipBody = PointMass(mass = 1.0)
    shipBody.addField(VelocityForceField(Polynomial(0.0, 2.5))) // thrust
    physicsSystem.registerBody("ship", shipBody)
    collisionSystem.registerCollider("ship", ship.collider!!)
    scene.addEntity(ship)

    var canShoot = true
    var bulletCount = 0
    var asteroidCount = 0
    var score = 0

    fun spawnAsteroid(x: Double, y: Double, size: Int, vx: Double = 0.0, vy: Double = 0.0) {
        val radius = when (size) {
            2 -> 50.0  // large
            1 -> 35.0  // medium
            else -> 25.0 // small
        }
        val speed = when (size) {
            2 -> 80.0
            1 -> 120.0
            else -> 160.0
        }

        val id = "asteroid_${asteroidCount++}"
        val asteroid = DefaultGameEntity(id)
        asteroid.position = Vector2D(x, y)
        asteroid.shape = CircleShape(radius)
        asteroid.painter = SolidColor(Color(150, 150, 150))
        asteroid.collider = CircleCollider(CircleShape(radius))

        val body = PointMass(mass = radius / 10.0)
        body.velocity = if (vx == 0.0 && vy == 0.0) {
            val angle = Math.random() * 2 * PI
            Vector2D(cos(angle) * speed, sin(angle) * speed)
        } else {
            Vector2D(vx, vy)
        }

        physicsSystem.registerBody(id, body)
        collisionSystem.registerCollider(id, asteroid.collider!!)
        scene.addEntity(asteroid)
    }

    // Spawn initial asteroids
    for (i in 0..2) {
        val edge = (Math.random() * 4).toInt()
        val (x, y) = when (edge) {
            0 -> 0.0 to Math.random() * height
            1 -> width.toDouble() to Math.random() * height
            2 -> Math.random() * width to 0.0
            else -> Math.random() * width to height.toDouble()
        }
        spawnAsteroid(x, y, 2)
    }

    fun spawnBullet(x: Double, y: Double, angle: Double, shipVel: Vector2D) {

        if (!canShoot) return
        canShoot = false

        val speed = 600.0
        val vx = cos(angle) * speed + shipVel.x
        val vy = sin(angle) * speed + shipVel.y

        val id = "bullet_${bulletCount++}"
        val bullet = DefaultGameEntity(id)
        bullet.position = Vector2D(x, y)
        bullet.shape = CircleShape(3.0)
        bullet.painter = SolidColor(Color(255, 255, 100))
        val bulletBody = PointMass(mass = 0.1)
        bulletBody.velocity = Vector2D(vx, vy)
        val bulletCollider = CircleCollider(CircleShape(3.0))
        physicsSystem.registerBody(id, bulletBody)
        collisionSystem.registerCollider(id, bulletCollider)
        scene.addEntity(bullet)

        engine.after(200.0) { canShoot = true }

        engine.after(500.0) {
            physicsSystem.unregisterBody(id)
            collisionSystem.unregisterCollider(id)
            scene.removeEntity(id)
        }
    }


    fun handleBulletAsteroidCollision(bulletId: String, asteroidId: String) {
        // Get asteroid size from its shape radius
        val asteroidEntity = scene.findEntity(asteroidId) ?: return
        val radius = (asteroidEntity.shape as CircleShape).radius
        val size = when {
            radius >= 45 -> 2  // large (50)
            radius >= 30 -> 1  // medium (35)
            else -> 0          // small (25)
        }

        // Remove bullet
        physicsSystem.unregisterBody(bulletId)
        collisionSystem.unregisterCollider(bulletId)
        scene.removeEntity(bulletId)

        // Remove asteroid
        physicsSystem.unregisterBody(asteroidId)
        collisionSystem.unregisterCollider(asteroidId)
        scene.removeEntity(asteroidId)

        // Score
        score += when (size) { 2 -> 20; 1 -> 50; else -> 100 }

        // Split or destroy
        if (size > 0) {
            val pos = asteroidEntity.transform.position
            for (i in 0..1) {
                val angle = Math.random() * 2 * PI
                val speed = 100.0 + Math.random() * 100.0
                spawnAsteroid(pos.x, pos.y, size - 1,
                    cos(angle) * speed, sin(angle) * speed)
            }
        }
    }

    eventBus.subscribe(CollisionEvent::class.java) { event ->
        val a = event.sourceEntityId
        val b = event.otherEntityId

        if (a.startsWith("bullet") && b.startsWith("asteroid")) {
            handleBulletAsteroidCollision(a, b)
        } else if (b.startsWith("bullet") && a.startsWith("asteroid")) {
            handleBulletAsteroidCollision(b, a)
        }
    }

    // --- Main loop ---
    engine { dt ->
        // Ship rotation
        if (KeyListener.isKeyDown(window.handle, GLFW_KEY_LEFT) || KeyListener.isKeyDown(window.handle, GLFW_KEY_A)) {
            ship.rotation -= 5.0 * dt
        }
        if (KeyListener.isKeyDown(window.handle, GLFW_KEY_RIGHT) || KeyListener.isKeyDown(window.handle, GLFW_KEY_D)) {
            ship.rotation += 5.0 * dt
        }

        // Thrust
        val thrustAngle = ship.rotation + PI / 2 // ship points up by default
        if (KeyListener.isKeyDown(window.handle, GLFW_KEY_UP) || KeyListener.isKeyDown(window.handle, GLFW_KEY_W)) {
            val thrustForce = Vector2D(cos(thrustAngle), sin(thrustAngle)) * 50.0
            shipBody.applyForce(thrustForce)
        }

        // Shoot
        if (KeyListener.isKeyDown(window.handle, GLFW_KEY_J)) {
            val noseDist = 18.0
            val noseX = ship.position.x + cos(thrustAngle) * noseDist
            val noseY = ship.position.y + sin(thrustAngle) * noseDist
            spawnBullet(noseX, noseY, thrustAngle, shipBody.velocity)
        }

        // Wrap all
        scene.allEntities().forEach { wrapEntity(it) }

        // Quit
        if (KeyListener.isKeyJustPressed(window.handle, GLFW_KEY_ESCAPE)) {
            engine.quit()
        }
    }

    engine.loadScene(scene)
    Application.run()
}
