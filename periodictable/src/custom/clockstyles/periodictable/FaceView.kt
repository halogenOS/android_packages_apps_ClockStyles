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
 * How much room a face takes: the size of its design, or the width or the whole area SystemUI gives
 * it. SystemUI centres a face in its area, so a face that fills the area draws its design at the
 * top of it, either at the start or centred across it.
 *
 * @property fillsWidth the face is as wide as the area, else as wide as its design
 * @property fillsHeight the face is as high as the area, else as high as its design
 * @property centresDesign the design is in the middle of the width of the face, else at its start
 * @property spansHostWidth the face sets its own constraints to span the width of the screen, in
 *   place of the place and the fixed height SystemUI gives every small clock
 */
enum class FaceArea(
    val fillsWidth: Boolean,
    val fillsHeight: Boolean,
    val centresDesign: Boolean,
    val spansHostWidth: Boolean,
) {
    DESIGN(fillsWidth = false, fillsHeight = false, centresDesign = false, spansHostWidth = false),
    FILLED_FROM_START(
        fillsWidth = true,
        fillsHeight = true,
        centresDesign = false,
        spansHostWidth = false,
    ),
    FILLED_CENTRED(
        fillsWidth = true,
        fillsHeight = true,
        centresDesign = true,
        spansHostWidth = false,
    ),
    CENTRED_IN_WIDTH(
        fillsWidth = true,
        fillsHeight = false,
        centresDesign = true,
        spansHostWidth = true,
    ),
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
    private val alarmIcon = AlarmIcon(density)
    private val weekday by lazy { scaledTextPaint(18f, weight = 400) }
    private val monthDay by lazy { textPaint(46.dp, weight = 300) }
    private val dateRow by lazy {
        scaledTextPaint(DATE_ROW_SIZE_SP, weight = 450).apply {
            letterSpacing = DATE_ROW_LETTER_SPACING
        }
    }

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
        val width = if (design.area.fillsWidth) availableWidth else designWidth * scale
        val height = if (design.area.fillsHeight) availableHeight else designHeight * scale
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

    /** How far content of the given size in px shrinks to fit into this area: 1 where it fits. */
    protected fun DpRect.scaleToFit(contentWidth: Float, contentHeight: Float) =
        min(1f, min(width.px / contentWidth, height.px / contentHeight))

    /** The width of the day and date of [reading] as [drawDayAndDate] writes them. */
    protected fun dayAndDateWidth(reading: ClockReading) =
        maxOf(monthDay.measureText(reading.monthDay), weekday.measureText(reading.weekday))

    /** The height of the day and date as [drawDayAndDate] writes them. */
    protected val dayAndDateHeight
        get() = dateBaselineBelow(0f) + monthDay.fontMetrics.descent

    /** The baseline of the date written by [drawDayAndDate] with the top of the day at [top]. */
    protected fun dateBaselineBelow(top: Float) = weekday.baselineBelow(top) + monthDay.textSize

    /**
     * Writes the day of the week and, in large light type below it, the date, from [start] with the
     * top of the day at [top].
     */
    protected fun Canvas.drawDayAndDate(
        reading: ClockReading,
        style: GlassStyle,
        start: Float,
        top: Float,
    ) {
        weekday.color = style.inkBeside.withAlpha(0.8f)
        monthDay.color = style.inkBeside
        drawUpright(reading.weekday, start, weekday.baselineBelow(top), weekday)
        drawUpright(reading.monthDay, start, dateBaselineBelow(top), monthDay)
    }

    /**
     * Writes the day of the week, the date and the next alarm of [reading] in one row of capitals,
     * as the date row of the Symmetra clock, centred on [centreX] with its top at [top]. A row
     * wider than the face shrinks to its width.
     */
    protected fun Canvas.drawDateRow(
        reading: ClockReading,
        style: GlassStyle,
        centreX: Float,
        top: Float,
    ) {
        val locale = resources.configuration.locales[0]
        val date = "${reading.weekday}$DATE_ROW_SEPARATOR${reading.monthDay}".uppercase(locale)
        val alarm = reading.nextAlarm?.uppercase(locale)
        val dateWidth = dateRow.measureText(date)
        val separatorWidth = dateRow.measureText(DATE_ROW_SEPARATOR)
        val rowWidth = dateWidth + (alarm?.let { separatorWidth + alarmWidth(it, dateRow) } ?: 0f)
        val fit = min(1f, this@FaceView.width / scale / rowWidth)
        val baseline = dateRow.baselineBelow(top)
        val start = centreX - rowWidth / 2
        dateRow.color = style.inkBeside
        save()
        scale(fit, fit, centreX, top)
        drawUpright(date, start, baseline, dateRow)
        alarm?.let {
            drawUpright(DATE_ROW_SEPARATOR, start + dateWidth, baseline, dateRow)
            drawAlarm(it, style, dateRow, start + dateWidth + separatorWidth, baseline)
        }
        restore()
    }

    /** The width of the alarm icon and [time] in [paint] as [drawAlarm] draws them. */
    protected fun alarmWidth(time: String, paint: Paint) =
        alarmIcon.width + ALARM_ICON_GAP.px + paint.measureText(time)

    /** Draws the alarm icon and, after it, [time] in [paint] on [baseline], from [start]. */
    protected fun Canvas.drawAlarm(
        time: String,
        style: GlassStyle,
        paint: Paint,
        start: Float,
        baseline: Float,
    ) {
        paint.color = style.inkBeside
        alarmIcon.draw(
            this,
            start + alarmIcon.width / 2,
            paint.textMiddleOn(baseline),
            style.inkBeside,
        )
        drawUpright(time, start + alarmIcon.width + ALARM_ICON_GAP.px, baseline, paint)
    }

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
        get() = if (design.area.centresDesign) (width - design.width.px * scale) / 2 else 0f

    private fun available(measureSpec: Int): Float? =
        MeasureSpec.getSize(measureSpec).toFloat().takeIf {
            MeasureSpec.getMode(measureSpec) != MeasureSpec.UNSPECIFIED
        }

    private companion object {
        val ALARM_ICON_GAP = 6.dp

        // The look of the date row, the same as on the Symmetra clock.
        const val DATE_ROW_SIZE_SP = 14f
        const val DATE_ROW_LETTER_SPACING = 0.15f
        const val DATE_ROW_SEPARATOR = " · "
    }
}

/** The baseline that centres the text of this paint on [y]. */
fun Paint.baselineCentredOn(y: Float) = y - (fontMetrics.ascent + fontMetrics.descent) / 2

/** The middle of the text of this paint written on [baseline], to centre an icon beside it. */
fun Paint.textMiddleOn(baseline: Float) = baseline + (fontMetrics.ascent + fontMetrics.descent) / 2

/** The baseline that puts the top of the text of this paint at [top]. */
fun Paint.baselineBelow(top: Float) = top - fontMetrics.ascent

/** The height of a line of text of this paint, from the top of the text to its bottom. */
val Paint.textHeight
    get() = fontMetrics.run { descent - ascent }

private val Paint.Align.mirrored
    get() =
        when (this) {
            Paint.Align.LEFT -> Paint.Align.RIGHT
            Paint.Align.RIGHT -> Paint.Align.LEFT
            Paint.Align.CENTER -> Paint.Align.CENTER
        }
