package org.soyuz.engine.ui

import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.render.text.Font
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.Color
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D

object UI {
    fun button(
        id: String,
        x: Double, y: Double,
        width: Double, height: Double,
        onClick: () -> Unit
    ): DefaultGameEntity {
        val btn = DefaultGameEntity(id)
        btn.transform = Transform(position = Vector2D(x, y))
        btn.shape = RectangleShape(width, height)
        btn.collider = RectangleCollider(RectangleShape(width, height))

        val normal = SolidColor(Color(60, 120, 200))
        val hover = SolidColor(Color(80, 150, 240))
        val press = SolidColor(Color(40, 80, 160))
        btn.painter = normal

        btn.interactive = Interactive
            .clickable(containsPoint = { btn.collider!!.containsPoint(it, btn.transform) }) { onClick() }
            .hoverable(onHoverEnter = { btn.painter = hover }, onHoverExit = { btn.painter = normal })
            .let { base -> object : InteractiveDecorator(base) {
                override fun onPress(button: Int) { btn.painter = press; base.onPress(button) }
                override fun onRelease(button: Int) {
                    btn.painter = if (UISystem.getState(id).isHovered) hover else normal
                    base.onRelease(button)
                }
            }}

        return btn
    }

    fun label(id: String, x: Double, y: Double, text: String, font: Font, fontSize: Float, color: Color): DefaultGameEntity {
        val label = DefaultGameEntity(id)
        label.transform = Transform(position = Vector2D(x, y))
        val painter = TextPainter(font)
        painter.fontSize = fontSize
        painter.text = text
        painter.color = color
        label.painter = painter
        // shape set after first rasterize
        return label
    }
}