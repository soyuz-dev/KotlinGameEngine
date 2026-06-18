package org.soyuz.engine.render.text

import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.soyuz.engine.render.image.Texture
import org.soyuz.util.Color
import java.nio.ByteBuffer


class Font(val path: String) {
    private val fontData: ByteBuffer
    private val fontInfo: STBTTFontinfo

    init {
        val stream = Font::class.java.getResourceAsStream(path)
            ?: error("Font not found: $path")
        val bytes = stream.readAllBytes()
        fontData = ByteBuffer.allocateDirect(bytes.size)
        fontData.put(bytes)
        fontData.flip()

        fontInfo = STBTTFontinfo.create()
        STBTruetype.stbtt_InitFont(fontInfo, fontData)
    }

    fun rasterize(text: String, fontSize: Float, color: Color): Texture {

        if (text.isEmpty()) {
            // Return a 1x1 transparent texture as placeholder
            val empty = ByteBuffer.allocateDirect(4)
            empty.put(0); empty.put(0); empty.put(0); empty.put(0)
            empty.flip()
            return Texture(1, 1, empty)
        }

        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontSize)
        val ascent = IntArray(1)
        val descent = IntArray(1)
        val lineGap = IntArray(1)
        STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap)

        // First pass: calculate total width and height
        var totalWidth = 0f
        for (char in text) {
            val advanceWidth = IntArray(1)
            val leftBearing = IntArray(1)
            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, char.code, advanceWidth, leftBearing)
            totalWidth += advanceWidth[0] * scale
        }
        val totalHeight = (ascent[0] - descent[0]) * scale

        // Second pass: render each glyph into the assembled image
        val imageBuffer = ByteBuffer.allocateDirect((totalWidth * totalHeight * 4).toInt())
        for (i in 0 until imageBuffer.capacity()) {
            imageBuffer.put(i, 0.toByte())
        }
        var xPos = 0f
        for (char in text) {
            val width = IntArray(1); val height = IntArray(1)
            val xoff = IntArray(1); val yoff = IntArray(1)
            val bitmap = STBTruetype.stbtt_GetCodepointBitmap(
                fontInfo, scale, scale, char.code, width, height, xoff, yoff
            )

            if (bitmap != null) {
                val w = width[0]
                val h = height[0]
                val xo = xoff[0]
                val yo = yoff[0]

                for (row in 0 until h) {
                    for (col in 0 until w) {
                        val srcIdx = row * w + col
                        val alpha = bitmap.get(srcIdx).toInt() and 0xFF

                        val dstX = (xPos + xo + col).toInt()
                        val dstY = ((ascent[0] * scale) + yo + row).toInt()

                        if (dstX >= 0 && dstX < totalWidth.toInt() && dstY >= 0 && dstY < totalHeight.toInt()) {
                            val dstIdx = (dstY * totalWidth.toInt() + dstX) * 4
                            imageBuffer.put(dstIdx, color.r.toByte())
                            imageBuffer.put(dstIdx + 1, color.g.toByte())
                            imageBuffer.put(dstIdx + 2, color.b.toByte())
                            imageBuffer.put(dstIdx + 3, alpha.toByte())
                        }
                    }
                }
                STBTruetype.stbtt_FreeBitmap(bitmap, 0L)
            }

            // Advance x position (always, even for missing glyphs)
            val advanceWidth = IntArray(1)
            val leftBearing = IntArray(1)
            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, char.code, advanceWidth, leftBearing)
            xPos += advanceWidth[0] * scale
        }


        imageBuffer.flip()
        val texture = Texture(totalWidth.toInt(), totalHeight.toInt(), imageBuffer)
        return texture
    }

    fun cleanup() {
        // STBTruetype doesn't require cleanup for fontInfo,
        // but you can free fontData if needed
        fontData.clear()
    }
}