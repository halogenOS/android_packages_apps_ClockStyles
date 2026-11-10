// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.orbital

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import custom.clockstyles.periodictable.ClockColors
import custom.clockstyles.periodictable.ClockReading
import custom.clockstyles.periodictable.Dp
import custom.clockstyles.periodictable.DpPoint
import custom.clockstyles.periodictable.Element
import custom.clockstyles.periodictable.FaceArea
import custom.clockstyles.periodictable.FaceDesign
import custom.clockstyles.periodictable.FaceSize
import custom.clockstyles.periodictable.FaceView
import custom.clockstyles.periodictable.GlassStyle
import custom.clockstyles.periodictable.baselineCentredOn
import custom.clockstyles.periodictable.dp
import custom.clockstyles.periodictable.withAlpha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * An electron shell: its radius, the number of places on it, every how many places a major mark
 * sits, and the radius of the electron on it.
 */
private data class Shell(val radius: Dp, val places: Int, val majorEvery: Int, val electron: Dp)

/** Where a face writes the time: in the glass nucleus, or beside the atom. */
private sealed interface TimePlacement {
    data class InNucleus(val radius: Dp, val textSize: Dp) : TimePlacement

    data class BesideAtom(val gap: Dp, val textSize: Dp) : TimePlacement
}

/**
 * The atom of a face around [centre]. A face with [marks] shows the places on its shells, and one
 * with [details] the day, date, next alarm and the two elements beside the atom.
 */
private data class AtomLayout(
    val design: FaceDesign,
    val centre: DpPoint,
    val hours: Shell,
    val minutes: Shell,
    val time: TimePlacement,
    val marks: Boolean,
    val details: Boolean,
)

private val FaceSize.atomLayout
    get() =
        when (this) {
            FaceSize.LARGE ->
                AtomLayout(
                    design =
                        FaceDesign(380.dp, 240.dp, FaceArea.FILLED_FROM_START, showsDate = true),
                    centre = DpPoint(128.dp, 120.dp),
                    hours = Shell(62.dp, places = 12, majorEvery = 1, electron = 11.dp),
                    minutes = Shell(104.dp, places = 60, majorEvery = 5, electron = 8.8.dp),
                    time = TimePlacement.InNucleus(radius = 40.dp, textSize = 21.dp),
                    marks = true,
                    details = true,
                )
            FaceSize.SMALL ->
                AtomLayout(
                    design = FaceDesign(190.dp, 114.dp, FaceArea.DESIGN, showsDate = false),
                    centre = DpPoint(26.dp, 57.dp),
                    hours = Shell(14.dp, places = 12, majorEvery = 1, electron = 2.6.dp),
                    minutes = Shell(24.dp, places = 60, majorEvery = 5, electron = 2.dp),
                    time = TimePlacement.BesideAtom(gap = 10.dp, textSize = 46.dp),
                    marks = false,
                    details = false,
                )
        }

/** A face of the Orbital clock: the hour electron on the inner shell, the minute one outside. */
class OrbitalFaceView
private constructor(context: Context, private val layout: AtomLayout, colors: ClockColors) :
    FaceView(context, layout.design, colors) {
    constructor(
        context: Context,
        size: FaceSize,
        colors: ClockColors,
    ) : this(context, size.atomLayout, colors)

    private val mark = Paint(Paint.ANTI_ALIAS_FLAG)
    private val time =
        when (val placement = layout.time) {
            is TimePlacement.InNucleus ->
                textPaint(placement.textSize, weight = 450, Paint.Align.CENTER)
            is TimePlacement.BesideAtom -> textPaint(placement.textSize, weight = 300)
        }
    private val alarm = scaledTextPaint(16f, weight = 400)
    private val chip = textPaint(13.dp, weight = 650, Paint.Align.CENTER)
    private val chipBounds = RectF()
    private val place = PointF()

    override fun draw(canvas: Canvas, reading: ClockReading, style: GlassStyle) {
        val centre = layout.centre.px
        shell(canvas, style, centre, layout.hours)
        shell(canvas, style, centre, layout.minutes)
        val hourTurn = ((reading.hour % 12) + reading.minute / 60f) / 12f
        electron(canvas, style, centre, layout.hours, hourTurn)
        electron(canvas, style, centre, layout.minutes, reading.minute / 60f)
        when (val placement = layout.time) {
            is TimePlacement.InNucleus -> {
                painter.sphere(canvas, style, centre, placement.radius.px)
                time.color = style.inkOnGlass
                canvas.drawUpright(reading.time, centre.x, time.baselineCentredOn(centre.y), time)
            }
            is TimePlacement.BesideAtom -> {
                time.color = style.inkBeside
                val start = centre.x + (layout.minutes.radius + placement.gap).px
                canvas.drawUpright(reading.time, start, time.baselineCentredOn(centre.y), time)
            }
        }
        if (layout.details) details(canvas, reading, style, centre)
    }

    private fun shell(canvas: Canvas, style: GlassStyle, centre: PointF, shell: Shell) {
        painter.shell(canvas, style, centre, shell.radius.px)
        if (!layout.marks) return
        mark.color = style.faded(style.colors.glass.light).withAlpha(0.45f)
        repeat(shell.places) { i ->
            placeOnShell(centre, shell.radius.px, i.toFloat() / shell.places)
            val radius = if (i % shell.majorEvery == 0) 1.6.dp else 0.8.dp
            canvas.drawCircle(place.x, place.y, radius.px, mark)
        }
    }

    private fun electron(
        canvas: Canvas,
        style: GlassStyle,
        centre: PointF,
        shell: Shell,
        turn: Float,
    ) {
        placeOnShell(centre, shell.radius.px, turn)
        painter.sphere(canvas, style, place, shell.electron.px)
    }

    // Day, date, next alarm and the elements for the hour and minute, stacked beside the atom. They
    // shrink together where a long date in the locale would run past the end of the face.
    private fun details(canvas: Canvas, reading: ClockReading, style: GlassStyle, centre: PointF) {
        val start = centre.x + (layout.minutes.radius + 20.dp).px
        val top = 30.dp.px
        val room = layout.design.width.px - start - 12.dp.px
        val scale = min(1f, room / dayAndDateWidth(reading)).coerceAtLeast(0f)
        canvas.save()
        canvas.scale(scale, scale, start, top)
        canvas.drawDayAndDate(reading, style, start, top)
        var baseline = dateBaselineBelow(top)
        reading.nextAlarm?.let {
            baseline += alarm.fontSpacing * 1.4f
            canvas.drawAlarm(it, style, alarm, start, baseline)
        }
        baseline += alarm.fontSpacing * 0.6f
        var x = start
        listOf(reading.hourText to reading.hour, reading.minuteText to reading.minute).forEach {
            (text, number) ->
            // A number without an element, such as 00, gets a chip with only the number.
            val label = Element.numbered(number)?.symbol ?: text
            val width = chip.measureText(label) + 14.dp.px
            chipBounds.set(x, baseline, x + width, baseline + 22.dp.px)
            painter.tile(canvas, style, chipBounds, radius = 5.dp.px)
            chip.color = style.inkOnGlass
            canvas.drawUpright(
                label,
                chipBounds.centerX(),
                chip.baselineCentredOn(chipBounds.centerY()),
                chip,
            )
            x += width + 6.dp.px
        }
        canvas.restore()
    }

    // Moves [place] to the point [turn] of a full turn round the shell, clockwise from the top.
    private fun placeOnShell(centre: PointF, radius: Float, turn: Float) {
        val angle = 2 * PI * turn
        place.set(
            centre.x + radius * sin(angle).toFloat(),
            centre.y - radius * cos(angle).toFloat(),
        )
    }
}
