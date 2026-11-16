// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.annotation.ColorInt
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader

/**
 * How a face looks at one moment: its [colors] and how far into the always-on display it is.
 * [solidity] is 1 for filled glass on the lock screen and 0 for plain outlines on the always-on
 * display. The inks are the colours blended towards the always-on colour as the face dozes.
 */
data class GlassStyle(val colors: ClockColors, val dozeFraction: Float) {
    val solidity
        get() = 1f - dozeFraction

    /** Text on the glass. */
    @ColorInt val inkOnGlass = blend(NEAR_WHITE, colors.aod, dozeFraction)

    /** Text beside the glass. */
    @ColorInt val inkBeside = blend(colors.text, colors.aod, dozeFraction)

    /** [color] blended towards the always-on colour as the face dozes. */
    @ColorInt fun faded(@ColorInt color: Int) = blend(color, colors.aod, dozeFraction)

    private companion object {
        @ColorInt const val NEAR_WHITE = 0xFFF2FAFF.toInt()
    }
}

private const val TILE_DEPTH_PER_WIDTH = 0.035f

/** How far the thick side of a tile of this width shows below and beside it. */
val Dp.tileDepth
    get() = this * TILE_DEPTH_PER_WIDTH

/** Draws thick glass in a [GlassStyle]. Lengths are in pixels. */
class GlassPainter(private val density: Float) {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val shaders = ShaderCache()

    /** A rounded tile with its thick side showing towards the bottom right. */
    fun tile(canvas: Canvas, style: GlassStyle, bounds: RectF, radius: Float) {
        val glass = style.colors.glass
        val depth = bounds.width() * TILE_DEPTH_PER_WIDTH
        if (style.solidity > 0f) {
            fill.shader = null
            fill.color = glass.side.withAlpha(style.solidity)
            fill.setShadowLayer(
                GLOW * density,
                0f,
                0f,
                glass.middle.withAlpha(0.45f * style.solidity),
            )
            canvas.save()
            canvas.translate(depth * 0.7f, depth)
            canvas.drawRoundRect(bounds, radius, radius, fill)
            canvas.restore()
            fill.clearShadowLayer()
            fill.shader = shaders.linear(bounds, glass)
            fill.alpha = (255 * style.solidity).toInt()
            canvas.drawRoundRect(bounds, radius, radius, fill)
            fill.shader = null
        }
        val width = (if (style.solidity > 0f) RIM else OUTLINE) * density
        line.strokeWidth = width
        line.color = blend(glass.light.withAlpha(0.85f), style.colors.aod, style.dozeFraction)
        val inset = width / 2
        canvas.drawRoundRect(
            bounds.left + inset,
            bounds.top + inset,
            bounds.right - inset,
            bounds.bottom - inset,
            radius,
            radius,
            line,
        )
    }

    /** A line in the colour of the rim of the glass at [x], from [top] to [bottom]. */
    fun divider(canvas: Canvas, style: GlassStyle, x: Float, top: Float, bottom: Float) {
        line.strokeWidth = (if (style.solidity > 0f) RIM else OUTLINE) * density
        line.color =
            blend(style.colors.glass.light.withAlpha(0.7f), style.colors.aod, style.dozeFraction)
        canvas.drawLine(x, top, x, bottom, line)
    }

    /** A sphere lit from the top left, around [centre]. */
    fun sphere(canvas: Canvas, style: GlassStyle, centre: PointF, radius: Float) {
        val glass = style.colors.glass
        if (style.solidity > 0f) {
            fill.shader = shaders.radial(centre, radius, glass)
            fill.alpha = (255 * style.solidity).toInt()
            fill.setShadowLayer(
                GLOW * 0.6f * density,
                0f,
                0f,
                glass.top.withAlpha(0.8f * style.solidity),
            )
            canvas.drawCircle(centre.x, centre.y, radius, fill)
            fill.clearShadowLayer()
            fill.shader = null
        }
        if (style.dozeFraction > 0f) {
            line.strokeWidth = OUTLINE * density
            line.color = style.colors.aod.withAlpha(style.dozeFraction)
            canvas.drawCircle(centre.x, centre.y, radius, line)
        }
    }

    /** A thin glowing ring, as an electron shell. */
    fun shell(canvas: Canvas, style: GlassStyle, centre: PointF, radius: Float) {
        val glass = style.colors.glass
        line.strokeWidth = OUTLINE * density
        line.color = style.faded(glass.top).withAlpha(0.6f)
        line.setShadowLayer(
            GLOW * 0.4f * density * style.solidity,
            0f,
            0f,
            glass.middle.withAlpha(0.4f * style.solidity),
        )
        canvas.drawCircle(centre.x, centre.y, radius, line)
        line.clearShadowLayer()
    }

    private companion object {
        const val GLOW = 14f
        const val RIM = 1.2f
        const val OUTLINE = 1.5f
    }
}

/**
 * The gradients of the glass for one set of shades, drawn over the unit square and the unit circle
 * and placed on a tile or sphere with a local matrix, so that moving glass needs no new gradient.
 */
private class ShaderCache {
    private var shades: GlassShades? = null
    private lateinit var linear: Shader
    private lateinit var radial: Shader
    private val placement = Matrix()

    fun linear(bounds: RectF, glass: GlassShades): Shader {
        use(glass)
        placement.setScale(bounds.width(), bounds.height())
        placement.postTranslate(bounds.left, bounds.top)
        return linear.apply { setLocalMatrix(placement) }
    }

    fun radial(centre: PointF, radius: Float, glass: GlassShades): Shader {
        use(glass)
        placement.setScale(radius, radius)
        placement.postTranslate(centre.x, centre.y)
        return radial.apply { setLocalMatrix(placement) }
    }

    private fun use(glass: GlassShades) {
        if (glass == shades) return
        shades = glass
        linear =
            LinearGradient(
                0f,
                0f,
                1f,
                1f,
                intArrayOf(glass.top, glass.middle, glass.deep),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP,
            )
        // Lit from the top left: the highlight sits up and to the left of the centre.
        radial =
            RadialGradient(
                -0.3f,
                -0.35f,
                1.3f,
                intArrayOf(glass.light, glass.top, glass.deep),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP,
            )
    }
}
