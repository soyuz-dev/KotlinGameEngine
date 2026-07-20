package org.soyuz.engine.ui

import org.soyuz.engine.collision.CircleCollider
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.Painter
import org.soyuz.engine.render.SolidColor
import org.soyuz.engine.render.text.Font
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.Color
import org.soyuz.util.math.Transform
import org.soyuz.util.math.Vector2D

class UI(private val uiSystem: UISystem) {
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
                    btn.painter = if (uiSystem.getState(id).isHovered) hover else normal
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
        painter.update(0f)  // force initial rasterize
        label.shape = RectangleShape(
            painter.texture?.width?.toDouble() ?: 100.0,
            painter.texture?.height?.toDouble() ?: 30.0
        )
        return label
        // shape set after first rasterize
    }

    fun slider(
        id: String,
        x: Double, y: Double,
        width: Double, height: Double,
        min: Double = 0.0, max: Double = 1.0,
        initial: Double = 0.5,
        onValueChanged: (Double) -> Unit = {}
    ): Pair<DefaultGameEntity, DefaultGameEntity> {
        // Track
        val track = DefaultGameEntity("${id}_track")
        track.transform = Transform(position = Vector2D(x, y))
        track.shape = RectangleShape(width, height)
        track.painter = SolidColor(Color(80, 80, 80))

        // Thumb
        val thumbRadius = height * 0.6
        val thumbX = x - width / 2 + (initial / (max - min)) * width
        val thumb = DefaultGameEntity("${id}_thumb")
        thumb.transform = Transform(position = Vector2D(thumbX, y))
        thumb.shape = CircleShape(thumbRadius)
        thumb.painter = SolidColor(Color(200, 200, 200))
        thumb.collider = CircleCollider(CircleShape(thumbRadius))

        // Draggable logic
        var currentValue = initial
        thumb.interactive = Interactive
            .clickable(containsPoint = { thumb.collider!!.containsPoint(it, thumb.transform) }) { }
            .draggable(
                onDrag = { _, currentPos ->
                    val clampedX = currentPos.x.coerceIn(x - width / 2, x + width / 2)
                    thumb.position = Vector2D(clampedX, y)
                    currentValue = min + (clampedX - (x - width / 2)) / width * (max - min)
                    onValueChanged(currentValue)
                }
            )

        return track to thumb
    }

    fun draggablePanel(
        id: String,
        x: Double, y: Double,
        width: Double, height: Double,
        painter: Painter
    ): DefaultGameEntity {
        val panel = DefaultGameEntity(id)
        panel.transform = Transform(position = Vector2D(x, y))
        panel.shape = RectangleShape(width, height)
        panel.painter = painter
        panel.collider = RectangleCollider(RectangleShape(width, height))

        panel.interactive = Interactive
            .clickable(containsPoint = { panel.collider!!.containsPoint(it, panel.transform) }) { }
            .draggable(
                onDrag = { _, currentPos ->
                    panel.transform = panel.transform.copy(position = currentPos)
                }
            )

        return panel
    }

    fun textInput(
        id: String, x: Double, y: Double,
        width: Double = 200.0,
        font: Font, fontSize: Float = 18f,
        background: Painter = SolidColor(Color(40, 40, 50)),
        placeholder: String = "",
        onSubmit: (String) -> Unit = {},
        onTextChanged: (String) -> Unit = {}
    ): Pair<TextInputEntity, DefaultGameEntity> {
        val input = TextInputEntity(id, width, font, fontSize, placeholder, onSubmit, onTextChanged)
        input.position = Vector2D(x, y)
        val entity = DefaultGameEntity("bg_$id", shape = input.shape, painter = background)
        input.onPositionChanged {
            entity.position = it
        }
        input.onShapeChanged {
            entity.shape = it
        }

        return input to entity
    }
}