// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import kotlin.math.min

/**
 * How much room a face takes: the size of its design, or the whole area SystemUI gives it. SystemUI
 * centres a face in its area, so a face that fills the area draws its design at the top of it,
 * either at the start or centred across it.
 */
enum class FaceArea {
    DESIGN,
    FILLED_FROM_START,
    FILLED_CENTRED,
}

/**
 * The size a face is designed for, and the room it takes. A face is drawn at that size where it
 * fits and scaled down as a whole where it does not. A face that [showsDate] writes the date
 * itself, and SystemUI leaves out its own date beside it.
 */
data class FaceDesign(val width: Dp, val height: Dp, val area: FaceArea, val showsDate: Boolean)

/**
 * The view of one face. It draws its [reading] in the [GlassStyle] of its colours and doze state,
 * and draws again whenever one of them changes. Faces draw in dp of their design, mirrored for
 * right-to-left layouts.
 */
abstract class FaceView(context: Context, val design: FaceDesign, colors: ClockColors) :
    View(context) {
    protected val density = resources.displayMetrics.density
    protected val painter = GlassPainter(density)
    protected val alarmIcon = AlarmIcon(density)

    protected var style = GlassStyle(colors, dozeFraction = 0f)
        private set

    var reading: ClockReading? = null
        set(value) {
            if (value == field) return
            field = value
            contentDescription = value?.description
            value?.let(::onReading)
            invalidate()
        }

    var colors: ClockColors
        get() = style.colors
        set(value) {
            style = style.copy(colors = value)
            invalidate()
        }

    /** How far into the always-on display the device is, from 0 to 1. */
    var dozeFraction: Float
        get() = style.dozeFraction
        set(value) {
            style = style.copy(dozeFraction = value)
            invalidate()
        }

    private var scale = 1f

    /** Prepares what does not change until the next reading, such as names and labels. */
    protected open fun onReading(reading: ClockReading) {}

    /** Draws [reading] in [style], in px of the design at full size. */
    protected abstract fun draw(canvas: Canvas, reading: ClockReading, style: GlassStyle)

    final override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val designWidth = design.width.px
        val designHeight = design.height.px
        val availableWidth = available(widthMeasureSpec) ?: designWidth
        val availableHeight = available(heightMeasureSpec) ?: designHeight
        scale = min(1f, min(availableWidth / designWidth, availableHeight / designHeight))
        val (width, height) =
            when (design.area) {
                FaceArea.FILLED_FROM_START,
                FaceArea.FILLED_CENTRED -> availableWidth to availableHeight
                FaceArea.DESIGN -> designWidth * scale to designHeight * scale
            }
        setMeasuredDimension(
            resolveSize(width.toInt(), widthMeasureSpec),
            resolveSize(height.toInt(), heightMeasureSpec),
        )
    }

    final override fun onDraw(canvas: Canvas) {
        val reading = reading ?: return
        canvas.save()
        if (isRtl) canvas.scale(-1f, 1f, width / 2f, 0f)
        canvas.translate(designStart, 0f)
        canvas.scale(scale, scale)
        draw(canvas, reading, style)
        canvas.restore()
    }

    protected val Dp.px
        get() = toPx(density)

    protected val DpPoint.px
        get() = PointF(x.px, y.px)

    /**
     * Draws [text] at [x] and baseline [y] the right way round, also where the face is mirrored for
     * a right-to-left layout. The alignment of [paint] is then mirrored with it.
     */
    protected fun Canvas.drawUpright(text: String, x: Float, y: Float, paint: Paint) {
        if (!isRtl) {
            drawText(text, x, y, paint)
            return
        }
        val align = paint.textAlign
        paint.textAlign = align.mirrored
        save()
        scale(-1f, 1f, x, 0f)
        drawText(text, x, y, paint)
        restore()
        paint.textAlign = align
    }

    /** A paint for text of [size] in the system font with [weight] and tabular figures. */
    protected fun textPaint(size: Dp, weight: Int, align: Paint.Align = Paint.Align.LEFT) =
        textPaint(size.px, weight, align)

    /** A paint for text of [sizeSp] that grows with the font size picked by the user. */
    protected fun scaledTextPaint(
        sizeSp: Float,
        weight: Int,
        align: Paint.Align = Paint.Align.LEFT,
    ) =
        textPaint(
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, resources.displayMetrics),
            weight,
            align,
        )

    private fun textPaint(sizePx: Float, weight: Int, align: Paint.Align) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizePx
            textAlign = align
            typeface = Typeface.create(Typeface.DEFAULT, weight, /* italic= */ false)
            fontFeatureSettings = "tnum"
        }

    private val isRtl
        get() = layoutDirection == LAYOUT_DIRECTION_RTL

    private val designStart
        get() =
            when (design.area) {
                FaceArea.FILLED_CENTRED -> (width - design.width.px * scale) / 2
                FaceArea.FILLED_FROM_START,
                FaceArea.DESIGN -> 0f
            }

    private fun available(measureSpec: Int): Float? =
        MeasureSpec.getSize(measureSpec).toFloat().takeIf {
            MeasureSpec.getMode(measureSpec) != MeasureSpec.UNSPECIFIED
        }
}

/** The baseline that centres the text of this paint on [y]. */
fun Paint.baselineCentredOn(y: Float) = y - (fontMetrics.ascent + fontMetrics.descent) / 2

/** The middle of the text of this paint written on [baseline], to centre an icon beside it. */
fun Paint.textMiddleOn(baseline: Float) = baseline + (fontMetrics.ascent + fontMetrics.descent) / 2

/** The baseline that puts the top of the text of this paint at [top]. */
fun Paint.baselineBelow(top: Float) = top - fontMetrics.ascent

private val Paint.Align.mirrored
    get() =
        when (this) {
            Paint.Align.LEFT -> Paint.Align.RIGHT
            Paint.Align.RIGHT -> Paint.Align.LEFT
            Paint.Align.CENTER -> Paint.Align.CENTER
        }
