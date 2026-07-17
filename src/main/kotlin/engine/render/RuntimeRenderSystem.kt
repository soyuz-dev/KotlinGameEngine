package org.soyuz.engine.render

import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glClearColor
import org.soyuz.engine.scene.Scene
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.engine.shape.TriangleShape
import org.soyuz.util.math.MathUtil
import org.soyuz.util.math.Vector2D

class RuntimeRenderSystem(
    private val quadMesh: Mesh,
    private val circleMesh: Mesh,
    private val lineVao: Int? = null,
    private val lineVbo: Int? = null
) : RenderSystem {
    override fun render(scene: Scene, camera: Camera, shader: Shader) {
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        shader.bind()
        shader.setProjection(camera.getProjection())

        for (entity in scene.allEntities()) {
            when (entity.shape) {
                is CircleShape -> {
                    shader.setModel(
                        MathUtil.modelMatrix(entity.transform.copy(
                        scale = Vector2D(
                            (entity.shape as CircleShape).radius * 2,
                            (entity.shape as CircleShape).radius * 2
                        )
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
                is TriangleShape -> {
                    shader.setModel(MathUtil.modelMatrix(entity.transform))
                    entity.painter?.bind(shader) ?: shader.setColor(1f, 0f, 1f, 1f)
                    (entity.shape as TriangleShape).mesh.draw()
                }
                else -> {}
            }
        }
    }

    override fun cleanup() {
        quadMesh.cleanup()
        circleMesh.cleanup()
    }
}
