package org.soyuz.engine.physics

import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.collision.CollisionSystem
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.events.CollisionEvent
import org.soyuz.engine.events.CollisionEventType
import org.soyuz.engine.events.EventBus
import org.soyuz.engine.physics.forcefields.EntityAwareForceField
import org.soyuz.engine.physics.joints.Joint
import org.soyuz.engine.physics.joints.PermissiveJoint
import org.soyuz.engine.physics.joints.StrictJoint
import org.soyuz.engine.scene.Scene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.ShapeQueries
import org.soyuz.util.math.Vector2D
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

class RuntimePhysicsSystem(
    private val collisionSystem: CollisionSystem,
    private val eventBus: EventBus,
) : PhysicsSystem {

    private val ccdContacts = mutableSetOf<Pair<String, String>>()
    private val previousContacts = mutableSetOf<Pair<String, String>>()

    private val bodies = mutableMapOf<String, PhysicsBody>()
    private val dynamicFields = mutableListOf<EntityAwareForceField>()

    private val joints = mutableListOf<Joint>()
    private val bodyToEntity = mutableMapOf<PhysicsBody, String>()

    override fun getBody(entityId: String): PhysicsBody? = bodies[entityId]

    override fun addJoint(joint: Joint) { joints.add(joint) }
    override fun removeJoint(joint: Joint) { joints.remove(joint) }

    override fun addDynamicField(field: EntityAwareForceField) { dynamicFields.add(field) }
    override fun removeDynamicField(field: EntityAwareForceField) { dynamicFields.remove(field) }

    override fun registerBody(entityId: String, body: PhysicsBody) {
        bodies[entityId] = body
        bodyToEntity[body] = entityId
    }

    override fun unregisterBody(entityId: String) {
        bodies.remove(entityId)?.let { bodyToEntity.remove(it) }
    }

    override fun step(scene: Scene, dt: Double) {
        if (dt <= 0.0) return

        // Phase 1: Accumulate forces from force fields
        for ((entityId, body) in bodies) {
            if (body is PointMass) {
                val entity = scene.findEntity(entityId) ?: continue
                body.accumulateForces(entity.transform.position)
            }
            if(body is RigidBody) {
                val entity = scene.findEntity(entityId) ?: continue
                body.accumulateForces(entity.transform.position)
            }
        }

        // Phase 1.5: Accumulate forces from permissive joints
        for (joint in joints) {
            if (joint is PermissiveJoint) {
                val entityA = scene.findEntity(bodyToEntity[joint.bodyA] ?: continue) ?: continue
                val entityB = scene.findEntity(bodyToEntity[joint.bodyB] ?: continue) ?: continue
                joint.accumulateForces(entityA.transform.position, entityB.transform.position)
            }
        }

        // Phase 2: Pre-calculate prospective full frame displacements
        val displacements = bodies.mapValues { (_, body) -> body.integratePosition(dt) }

        // Phase 3: Predictive CCD Substepping & Dynamic Bouncing Pass
        for ((entityId, body) in bodies) {
            val entity = scene.findEntity(entityId) ?: continue
            val oldPos = entity.transform.position
            val displacement = displacements[entityId] ?: Vector2D.ZERO

            val collider = collisionSystem.getCollider(entityId)
            val shape = entity.shape

            if (collider is CircleCollider && shape is CircleShape) {
                var currentPos = oldPos
                var currentDisplacement = displacement
                val maxIterations = 3

                for (iter in 0 until maxIterations) {
                    if (currentDisplacement.length() < 1e-6) break

                    var earliestHitTime: Double? = null
                    var hitNormal: Vector2D? = null
                    var earliestHitOtherId: String? = null

                    for ((otherId, _) in bodies) {
                        if (otherId == entityId) continue
                        val otherEntity = scene.findEntity(otherId) ?: continue
                        val otherCollider = collisionSystem.getCollider(otherId) ?: continue

                        if (otherCollider is RectangleCollider && otherEntity.shape is RectangleShape) {
                            val aabb = ShapeQueries.worldAabb(otherEntity.shape!!, otherEntity.transform)
                            val aabbMin = Vector2D(aabb.minX, aabb.minY)
                            val aabbMax = Vector2D(aabb.maxX, aabb.maxY)

                            val hit = sweptCircleVsAabb(currentPos, shape.radius, currentDisplacement, aabbMin, aabbMax)
                            if (hit != null) {
                                val (hitTime, normal) = hit
                                if (body.velocity.dot(normal) < -1e-5) {
                                    if (earliestHitTime == null || hitTime < earliestHitTime) {
                                        earliestHitTime = hitTime
                                        hitNormal = normal
                                        earliestHitOtherId = otherId
                                    }
                                }
                            }
                        }
                    }

                    if (earliestHitTime != null && hitNormal != null && earliestHitOtherId != null) {
                        currentPos += currentDisplacement * earliestHitTime
                        currentPos += hitNormal * 0.01 // Micro-nudge outward along normal

                        val vn = body.velocity.dot(hitNormal)
                        if (vn < 0) {
                            // BOUNCE THRESHOLD: Avoid energy generation loops at resting gravity speeds
                            val e = if (abs(vn) < 20.0) 0.0 else body.restitution
                            val impulseMagnitude = -(1.0 + e) * vn * body.mass
                            body.applyImpulse(hitNormal * impulseMagnitude, Vector2D.ZERO)

                            val pair = if (entityId < earliestHitOtherId) entityId to earliestHitOtherId else earliestHitOtherId to entityId
                            ccdContacts.add(pair)
                        }

                        val dtRemaining = dt * (1.0 - earliestHitTime)
                        currentDisplacement = body.velocity * dtRemaining
                    } else {
                        currentPos += currentDisplacement
                        break
                    }
                }

                entity.transform = entity.transform.copy(position = currentPos)
                if (body is RigidBody) {
                    entity.transform = entity.transform.copy(
                        rotationRadians = entity.transform.rotationRadians + body.angularVelocity * dt
                    )
                }
            } else {
                if (body is RigidBody) {
                    entity.transform = entity.transform.copy(
                        position = entity.transform.position + displacement,
                        rotationRadians = entity.transform.rotationRadians + body.angularVelocity * dt
                    )
                } else {
                    entity.transform = entity.transform.translated(displacement)
                }
            }
        }

        val contacts = collisionSystem.detect(scene)

        // Phase 4: Discrete Penetration Pass (Handles shallow resting contact overlaps safely)
        for (contact in contacts) {
            val bodyA = bodies[contact.entityA] ?: continue
            val bodyB = bodies[contact.entityB] ?: continue
            val entityA = scene.findEntity(contact.entityA) ?: continue
            val entityB = scene.findEntity(contact.entityB) ?: continue

            val invMassA = getInverseMass(bodyA)
            val invMassB = getInverseMass(bodyB)
            if (invMassA == 0.0 && invMassB == 0.0) continue

            val normal = contact.normal
            val relativeV = bodyB.velocity - bodyA.velocity
            val vAlongNormal = relativeV.dot(normal)

            if (vAlongNormal > 0.0) continue // Separating

            val approachSpeed = abs(vAlongNormal)
            val e = if (approachSpeed < 30.0) 0.0 else minOf(bodyA.restitution, bodyB.restitution)

            val rA = contact.point - entityA.transform.position
            val rB = contact.point - entityB.transform.position

            val rACrossN = rA.cross(normal)
            val rBCrossN = rB.cross(normal)

            val invMassEffectiveA = invMassA + if (bodyA is RigidBody) (rACrossN * rACrossN) * bodyA.inverseInertia else 0.0
            val invMassEffectiveB = invMassB + if (bodyB is RigidBody) (rBCrossN * rBCrossN) * bodyB.inverseInertia else 0.0

            val j = -(1.0 + e) * vAlongNormal / (invMassEffectiveA + invMassEffectiveB)
            val impulse = normal * j

            bodyA.applyImpulse(-impulse, rA)
            bodyB.applyImpulse(impulse, rB)

            // Resolve friction impulse
            applyFrictionImpulse(bodyA, bodyB, rA, rB, normal, j, invMassA, invMassB)

            // Positional correction to counter positional drifting/sinking
            val slop = 0.01
            val percent = 0.8
            val correctionMagnitude = maxOf(contact.depth - slop, 0.0) * percent / (invMassA + invMassB)
            val correction = normal * correctionMagnitude

            entityA.transform = entityA.transform.translated(-correction * invMassA)
            entityB.transform = entityB.transform.translated(correction * invMassB)
        }

        // Phase 4.5: Publish contacts to the event bus
        val currentContacts = mutableSetOf<Pair<String, String>>().apply { addAll(ccdContacts) }

        for (pair in ccdContacts) {
            if (pair !in previousContacts) {
                eventBus.publish(CollisionEvent(pair.first, pair.second, CollisionEventType.ENTER))
            }
        }
        ccdContacts.clear()

        for (contact in contacts) {
            val pair = if (contact.entityA < contact.entityB) contact.entityA to contact.entityB else contact.entityB to contact.entityA
            currentContacts.add(pair)

            if (pair !in previousContacts) {
                eventBus.publish(CollisionEvent(contact.entityA, contact.entityB, CollisionEventType.ENTER))
            }
        }

        for (pair in previousContacts) {
            if (pair !in currentContacts) {
                eventBus.publish(CollisionEvent(pair.first, pair.second, CollisionEventType.EXIT))
            }
        }

        previousContacts.clear()
        previousContacts.addAll(currentContacts)

        // Phase 5: Solve strict joints
        for (iter in 0 until 5) {
            for (joint in joints) {
                if (joint is StrictJoint) {
                    val entityA = scene.findEntity(bodyToEntity[joint.bodyA] ?: continue) ?: continue
                    val entityB = scene.findEntity(bodyToEntity[joint.bodyB] ?: continue) ?: continue
                    val (newPosA, newPosB) = joint.solvePositions(entityA.transform.position, entityB.transform.position, dt)
                    entityA.transform = entityA.transform.copy(position = newPosA)
                    entityB.transform = entityB.transform.copy(position = newPosB)
                }
            }
        }

        // Phase 6: Finalize Velocities
        bodies.values.forEach { it.integrateVelocity(dt) }

        // Phase 7: Update positions for dynamic force fields
        for (field in dynamicFields) {
            for ((entityId, _) in bodies) {
                val entity = scene.findEntity(entityId) ?: continue
                field.updatePosition(entityId, entity.transform.position)
            }
        }
    }

    private fun applyFrictionImpulse(
        bodyA: PhysicsBody, bodyB: PhysicsBody,
        rA: Vector2D, rB: Vector2D,
        normal: Vector2D, j: Double,
        invMassA: Double, invMassB: Double
    ) {
        val tangent = Vector2D(-normal.y, normal.x)
        val vA = bodyA.velocity + if (bodyA is RigidBody) Vector2D(-bodyA.angularVelocity * rA.y, bodyA.angularVelocity * rA.x) else Vector2D.ZERO
        val vB = bodyB.velocity + if (bodyB is RigidBody) Vector2D(-bodyB.angularVelocity * rB.y, bodyB.angularVelocity * rB.x) else Vector2D.ZERO

        val relVelAtContact = vB - vA
        val vAlongTangent = relVelAtContact.dot(tangent)

        if (abs(vAlongTangent) > 1e-5) {
            val rACrossT = rA.cross(tangent)
            val rBCrossT = rB.cross(tangent)

            val invMassEffectiveA_T = invMassA + (rACrossT * rACrossT) * if (bodyA is RigidBody) bodyA.inverseInertia else 0.0
            val invMassEffectiveB_T = invMassB + (rBCrossT * rBCrossT) * if (bodyB is RigidBody) bodyB.inverseInertia else 0.0

            val frictionCoeff = 0.3
            val frictionMagnitude = frictionCoeff * abs(j)
            val frictionImpulse = tangent * -sign(vAlongTangent) * frictionMagnitude

            bodyA.applyImpulse(-frictionImpulse, rA)
            bodyB.applyImpulse(frictionImpulse, rB)
        }
    }

    private fun getInverseMass(body: PhysicsBody): Double = when (body) {
        is RigidBody -> body.inverseMass
        is PointMass -> body.inverseMass
        else -> 0.0
    }

    private fun sweptCircleVsAabb(
        circleCenter: Vector2D, circleRadius: Double, displacement: Vector2D,
        aabbMin: Vector2D, aabbMax: Vector2D
    ): Pair<Double, Vector2D>? {
        val min = aabbMin - Vector2D(circleRadius, circleRadius)
        val max = aabbMax + Vector2D(circleRadius, circleRadius)

        if (displacement.length() < 1e-12) return null

        var tMin = Double.NEGATIVE_INFINITY
        var tMax = Double.POSITIVE_INFINITY

        val invX = if (abs(displacement.x) > 1e-12) 1.0 / displacement.x else 0.0
        val invY = if (abs(displacement.y) > 1e-12) 1.0 / displacement.y else 0.0

        // X Axis Slab Test
        if (invX == 0.0) {
            if (circleCenter.x <= min.x || circleCenter.x >= max.x) return null
        } else {
            var t1 = (min.x - circleCenter.x) * invX
            var t2 = (max.x - circleCenter.x) * invX
            if (t1 > t2) t1 = t2.also { t2 = t1 }
            tMin = maxOf(tMin, t1)
            tMax = minOf(tMax, t2)
            if (tMin > tMax) return null
        }

        // Y Axis Slab Test
        if (invY == 0.0) {
            if (circleCenter.y <= min.y || circleCenter.y >= max.y) return null
        } else {
            var t1 = (min.y - circleCenter.y) * invY
            var t2 = (max.y - circleCenter.y) * invY
            if (t1 > t2) t1 = t2.also { t2 = t1 }
            tMin = maxOf(tMin, t1)
            tMax = minOf(tMax, t2)
            if (tMin > tMax) return null
        }

        if (tMax < 0.0 || tMin > 1.0) return null

        val correctedTMin = maxOf(0.0, tMin)
        val hitPoint = circleCenter + (displacement * correctedTMin)

        val edgeX = when {
            hitPoint.x < aabbMin.x -> aabbMin.x
            hitPoint.x > aabbMax.x -> aabbMax.x
            else -> null
        }
        val edgeY = when {
            hitPoint.y < aabbMin.y -> aabbMin.y
            hitPoint.y > aabbMax.y -> aabbMax.y
            else -> null
        }

        if (edgeX != null && edgeY != null) {
            val corner = Vector2D(edgeX, edgeY)
            val tCorner = rayCastCircle(circleCenter, displacement, corner, circleRadius) ?: return null
            if (tCorner !in 0.0..1.0) return null

            val actualHitPoint = circleCenter + (displacement * tCorner)
            val normal = (actualHitPoint - corner).normalized()
            return tCorner to normal
        }

        return correctedTMin to computeSweptNormal(hitPoint, min, max)
    }

    private fun rayCastCircle(origin: Vector2D, dir: Vector2D, center: Vector2D, radius: Double): Double? {
        val m = origin - center
        val b = m.dot(dir)
        val c = m.dot(m) - (radius * radius)

        if (c > 0.0 && b > 0.0) return null
        val discr = b * b - dir.dot(dir) * c
        if (discr < 0.0) return null

        var t = -b - sqrt(discr)
        if (t < 0.0) t = 0.0
        return t / dir.dot(dir)
    }

    private fun computeSweptNormal(point: Vector2D, min: Vector2D, max: Vector2D): Vector2D {
        val eps = 1e-6
        val dxMin = abs(point.x - min.x)
        val dxMax = abs(point.x - max.x)
        val dyMin = abs(point.y - min.y)
        val dyMax = abs(point.y - max.y)

        val minDist = minOf(dxMin, dxMax, dyMin, dyMax)

        return when {
            abs(minDist - dxMin) < eps -> Vector2D(-1.0, 0.0)
            abs(minDist - dxMax) < eps -> Vector2D(1.0, 0.0)
            abs(minDist - dyMin) < eps -> Vector2D(0.0, -1.0)
            else -> Vector2D(0.0, 1.0)
        }
    }
}