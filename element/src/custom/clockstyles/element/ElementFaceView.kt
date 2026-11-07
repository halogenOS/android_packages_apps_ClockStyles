// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import custom.clockstyles.periodictable.AtomicWeight
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
import custom.clockstyles.periodictable.baselineBelow
import custom.clockstyles.periodictable.baselineCentredOn
import custom.clockstyles.periodictable.dp
import custom.clockstyles.periodictable.textMiddleOn
import custom.clockstyles.periodictable.withAlpha
import kotlin.math.min

/** Where a tile sits: its centre and how far it is tilted, in degrees clockwise. */
private data class TilePlacement(val centre: DpPoint, val tiltDegrees: Float)

/**
 * The tiles of a face: their edge, the size of the number on them and where each sits. A face that
 * [labels] the tiles writes name and atomic weight on each, and the date and next alarm below.
 */
private data class TileLayout(
    val design: FaceDesign,
    val tileEdge: Dp,
    val numberSize: Dp,
    val hour: TilePlacement,
    val minute: TilePlacement,
    val labels: Boolean,
)

private val FaceSize.tileLayout
    get() =
        when (this) {
            FaceSize.LARGE ->
                TileLayout(
                    design = FaceDesign(360.dp, 316.dp, FaceArea.FILLED_CENTRED, showsDate = true),
                    tileEdge = 162.dp,
                    numberSize = 92.dp,
                    hour = TilePlacement(DpPoint(103.dp, 93.dp), tiltDegrees = -5f),
                    minute = TilePlacement(DpPoint(257.dp, 177.dp), tiltDegrees = 4f),
                    labels = true,
                )
            FaceSize.SMALL ->
                TileLayout(
                    design = FaceDesign(132.dp, 114.dp, FaceArea.DESIGN, showsDate = false),
                    tileEdge = 60.dp,
                    numberSize = 32.dp,
                    hour = TilePlacement(DpPoint(32.dp, 52.dp), tiltDegrees = -5f),
                    minute = TilePlacement(DpPoint(94.dp, 62.dp), tiltDegrees = 4f),
                    labels = false,
                )
        }

/**
 * What one tile shows. A number without an element, such as 00, has a tile with only the number.
 */
private data class TileContent(
    val number: String,
    val symbol: String?,
    val name: String?,
    val weight: String?,
)

/** A face of the Element clock: the hour tile above at the start, the minute tile below it. */
class ElementFaceView
private constructor(context: Context, private val layout: TileLayout, colors: ClockColors) :
    FaceView(context, layout.design, colors) {
    constructor(
        context: Context,
        size: FaceSize,
        colors: ClockColors,
    ) : this(context, size.tileLayout, colors)

    private val number = textPaint(layout.numberSize, weight = 300, Paint.Align.CENTER)
    private val symbol = textPaint(layout.tileEdge * 0.15f, weight = 650, Paint.Align.RIGHT)
    private val name = textPaint(layout.tileEdge * 0.085f, weight = 500)
    private val weight = textPaint(layout.tileEdge * 0.07f, weight = 400)
    private val line = scaledTextPaint(17f, weight = 450)
    private val bounds = RectF()

    private var hourTile = TileContent("", null, null, null)
    private var minuteTile = TileContent("", null, null, null)

    override fun onReading(reading: ClockReading) {
        hourTile = content(reading.hourText, Element.numbered(reading.hour))
        minuteTile = content(reading.minuteText, Element.numbered(reading.minute))
    }

    override fun draw(canvas: Canvas, reading: ClockReading, style: GlassStyle) {
        tile(canvas, style, hourTile, layout.hour)
        tile(canvas, style, minuteTile, layout.minute)
        if (layout.labels) dateAndAlarm(canvas, reading, style)
    }

    private fun content(text: String, element: Element?) =
        TileContent(
            number = text,
            symbol = element?.symbol,
            name = element?.takeIf { layout.labels }?.let { resources.getString(it.nameRes) },
            weight = element?.takeIf { layout.labels }?.weight?.label,
        )

    private fun tile(
        canvas: Canvas,
        style: GlassStyle,
        content: TileContent,
        placement: TilePlacement,
    ) {
        val centre = placement.centre.px
        val half = layout.tileEdge.px / 2
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

    // The date and the next alarm in one line below the tiles. The line shrinks where a long date
    // in the locale would run past the end of the face.
    private fun dateAndAlarm(canvas: Canvas, reading: ClockReading, style: GlassStyle) {
        line.color = style.inkBeside
        val start = 4.dp.px
        val gap = 14.dp.px
        val alarm = reading.nextAlarm
        val alarmWidth = alarm?.let { gap + alarmIcon.width + gap / 2 + line.measureText(it) } ?: 0f
        val lineWidth = line.measureText(reading.shortDate) + alarmWidth
        val scale = min(1f, (layout.design.width.px - 2 * start) / lineWidth)
        val baseline = layout.design.height.px - line.fontMetrics.descent
        canvas.save()
        canvas.scale(scale, scale, start, baseline)
        canvas.drawUpright(reading.shortDate, start, baseline, line)
        if (alarm != null) {
            val iconStart = start + line.measureText(reading.shortDate) + gap
            val iconCentreX = iconStart + alarmIcon.width / 2
            alarmIcon.draw(canvas, iconCentreX, line.textMiddleOn(baseline), style.inkBeside)
            canvas.drawUpright(alarm, iconStart + alarmIcon.width + gap / 2, baseline, line)
        }
        canvas.restore()
    }
}

private val AtomicWeight.label
    get() =
        when (this) {
            is AtomicWeight.Standard -> value
            is AtomicWeight.LongestLivedIsotope -> "($massNumber)"
        }
