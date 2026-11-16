// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import custom.clockstyles.periodictable.ClockColors
import custom.clockstyles.periodictable.ClockReading
import custom.clockstyles.periodictable.Dp
import custom.clockstyles.periodictable.DpPoint
import custom.clockstyles.periodictable.DpRect
import custom.clockstyles.periodictable.Element
import custom.clockstyles.periodictable.FaceArea
import custom.clockstyles.periodictable.FaceDesign
import custom.clockstyles.periodictable.FaceView
import custom.clockstyles.periodictable.GlassStyle
import custom.clockstyles.periodictable.baselineBelow
import custom.clockstyles.periodictable.baselineCentredOn
import custom.clockstyles.periodictable.dp
import custom.clockstyles.periodictable.textHeight
import custom.clockstyles.periodictable.withAlpha

/** Where a tile sits: its centre and how far it is tilted, in degrees clockwise. */
private data class TilePlacement(val centre: DpPoint, val tiltDegrees: Float)

/**
 * The two corners of the face left free by its tiles: [date] beside the hour tile, for the day and
 * the date, and [alarm] under it, for the next alarm. Text too large for its corner shrinks.
 */
private data class Corners(val date: DpRect, val alarm: DpRect)

/** The tiles of the face: their edge, the size of the number on them and where each sits. */
private data class TileLayout(
    val width: Dp,
    val height: Dp,
    val tileEdge: Dp,
    val numberSize: Dp,
    val hour: TilePlacement,
    val minute: TilePlacement,
    val corners: Corners,
) {
    val design = FaceDesign(width, height, FaceArea.FILLED_CENTRED, showsDate = true)
}

private val LAYOUT =
    TileLayout(
        width = 360.dp,
        height = 316.dp,
        tileEdge = 162.dp,
        numberSize = 92.dp,
        hour = TilePlacement(DpPoint(103.dp, 93.dp), tiltDegrees = -5f),
        minute = TilePlacement(DpPoint(257.dp, 177.dp), tiltDegrees = 4f),
        corners =
            Corners(
                date = DpRect(204.dp, 7.dp, 356.dp, 84.dp),
                // From the bottom left corner of the tilted hour tile, so the alarm ends far from
                // the minute tile.
                alarm = DpRect(29.dp, 196.dp, 160.dp, 240.dp),
            ),
    )

/**
 * The large face of the Element clock: the hour tile above at the start, the minute tile below it,
 * with name and atomic weight on each tile, and the date and the next alarm in the free corners.
 */
class ElementTilesFaceView(context: Context, colors: ClockColors) :
    FaceView(context, LAYOUT.design, colors) {
    private val number = textPaint(LAYOUT.numberSize, weight = 300, Paint.Align.CENTER)
    private val symbol = textPaint(LAYOUT.tileEdge * 0.15f, weight = 650, Paint.Align.RIGHT)
    private val name = textPaint(LAYOUT.tileEdge * 0.085f, weight = 500)
    private val weight = textPaint(LAYOUT.tileEdge * 0.07f, weight = 400)
    private val alarm = scaledTextPaint(17f, weight = 450)
    private val bounds = RectF()

    private var hourTile = ElementContent.EMPTY
    private var minuteTile = ElementContent.EMPTY
    private var dateScale = 1f
    private var nextAlarmScale = 1f

    override fun onReading(reading: ClockReading) {
        hourTile = resources.elementContent(reading.hourText, Element.numbered(reading.hour))
        minuteTile = resources.elementContent(reading.minuteText, Element.numbered(reading.minute))
        val corners = LAYOUT.corners
        dateScale = corners.date.scaleToFit(dayAndDateWidth(reading), dayAndDateHeight)
        reading.nextAlarm?.let {
            nextAlarmScale = corners.alarm.scaleToFit(alarmWidth(it, alarm), alarm.textHeight)
        }
    }

    override fun draw(canvas: Canvas, reading: ClockReading, style: GlassStyle) {
        tile(canvas, style, hourTile, LAYOUT.hour)
        tile(canvas, style, minuteTile, LAYOUT.minute)
        date(canvas, reading, style, LAYOUT.corners.date)
        reading.nextAlarm?.let { nextAlarm(canvas, it, style, LAYOUT.corners.alarm) }
    }

    private fun tile(
        canvas: Canvas,
        style: GlassStyle,
        content: ElementContent,
        placement: TilePlacement,
    ) {
        val centre = placement.centre.px
        val half = LAYOUT.tileEdge.px / 2
        bounds.set(centre.x - half, centre.y - half, centre.x + half, centre.y + half)
        canvas.save()
        canvas.rotate(placement.tiltDegrees, centre.x, centre.y)
        painter.tile(canvas, style, bounds, radius = bounds.width() * 0.086f)
        listOf(number, symbol, name, weight).forEach { it.color = style.inkOnGlass }
        canvas.drawUpright(content.number, centre.x, number.baselineCentredOn(centre.y), number)
        val inset = bounds.width() * 0.09f
        content.symbol?.let {
            canvas.drawUpright(
                it,
                bounds.right - inset,
                symbol.baselineBelow(bounds.top + inset),
                symbol,
            )
        }
        content.weight?.let {
            weight.color = style.inkOnGlass.withAlpha(0.8f)
            canvas.drawUpright(
                it,
                bounds.left + inset,
                weight.baselineBelow(bounds.top + inset),
                weight,
            )
        }
        content.name?.let {
            canvas.drawUpright(
                it,
                bounds.left + inset,
                bounds.bottom - inset - name.fontMetrics.descent,
                name,
            )
        }
        canvas.restore()
    }

    private fun date(canvas: Canvas, reading: ClockReading, style: GlassStyle, corner: DpRect) {
        val start = corner.left.px
        val top = corner.top.px
        canvas.save()
        canvas.scale(dateScale, dateScale, start, top)
        canvas.drawDayAndDate(reading, style, start, top)
        canvas.restore()
    }

    private fun nextAlarm(canvas: Canvas, time: String, style: GlassStyle, corner: DpRect) {
        val start = corner.left.px
        val top = corner.top.px
        canvas.save()
        canvas.scale(nextAlarmScale, nextAlarmScale, start, top)
        canvas.drawAlarm(time, style, alarm, start, alarm.baselineBelow(top))
        canvas.restore()
    }
}
