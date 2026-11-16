// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.TypedValue
import custom.clockstyles.periodictable.ClockColors
import custom.clockstyles.periodictable.ClockReading
import custom.clockstyles.periodictable.Element
import custom.clockstyles.periodictable.FaceArea
import custom.clockstyles.periodictable.FaceDesign
import custom.clockstyles.periodictable.FaceView
import custom.clockstyles.periodictable.GlassStyle
import custom.clockstyles.periodictable.baselineBelow
import custom.clockstyles.periodictable.baselineCentredOn
import custom.clockstyles.periodictable.dp
import custom.clockstyles.periodictable.tileDepth
import custom.clockstyles.periodictable.withAlpha
import kotlin.math.min

/**
 * The small face of the Element clock: hour and minute as two cells of one glass row of the
 * periodic table, in the middle of the screen, with the day, the date and the next alarm in one row
 * below it.
 */
class ElementRowFaceView(context: Context, colors: ClockColors) :
    FaceView(context, design(context), colors) {
    private val number = textPaint(NUMBER_SIZE, weight = 300, Paint.Align.CENTER)
    private val symbol = textPaint(SYMBOL_SIZE, weight = 650, Paint.Align.RIGHT)
    private val name = textPaint(NAME_SIZE, weight = 500)
    private val weight = textPaint(WEIGHT_SIZE, weight = 400)
    private val bounds = RectF()

    private var hourCell = ElementContent.EMPTY
    private var minuteCell = ElementContent.EMPTY

    override fun onReading(reading: ClockReading) {
        hourCell = resources.elementContent(reading.hourText, Element.numbered(reading.hour))
        minuteCell = resources.elementContent(reading.minuteText, Element.numbered(reading.minute))
    }

    override fun draw(canvas: Canvas, reading: ClockReading, style: GlassStyle) {
        val top = ROW_TOP.px
        val bottom = (ROW_TOP + ROW_HEIGHT).px
        bounds.set(0f, top, ROW_WIDTH.px, bottom)
        painter.tile(canvas, style, bounds, radius = ROW_CORNER.px)
        val divider = CELL_WIDTH.px
        painter.divider(canvas, style, divider, top + DIVIDER_INSET.px, bottom - DIVIDER_INSET.px)
        cell(canvas, style, hourCell, left = 0f)
        cell(canvas, style, minuteCell, left = divider)
        canvas.drawDateRow(reading, style, centreX = ROW_WIDTH.px / 2, top = DATE_ROW_TOP.px)
    }

    private fun cell(canvas: Canvas, style: GlassStyle, content: ElementContent, left: Float) {
        val top = ROW_TOP.px
        val right = left + CELL_WIDTH.px
        val inset = CELL_INSET.px
        listOf(number, symbol, name).forEach { it.color = style.inkOnGlass }
        weight.color = style.inkOnGlass.withAlpha(0.8f)
        canvas.drawUpright(
            content.number,
            (left + right) / 2,
            number.baselineCentredOn(top + ROW_HEIGHT.px * NUMBER_MIDDLE),
            number,
        )
        content.symbol?.let {
            canvas.drawUpright(it, right - inset, symbol.baselineBelow(top + inset * 0.6f), symbol)
        }
        content.weight?.let {
            canvas.drawUpright(it, left + inset, weight.baselineBelow(top + inset * 0.8f), weight)
        }
        content.name?.let {
            val baseline = top + ROW_HEIGHT.px - inset * 0.7f - name.fontMetrics.descent
            val fit = min(1f, (CELL_WIDTH - CELL_INSET * 1.6f).px / name.measureText(it))
            canvas.save()
            canvas.scale(fit, fit, left + inset, baseline)
            canvas.drawUpright(it, left + inset, baseline, name)
            canvas.restore()
        }
    }

    private companion object {
        // The row is centred in the height SystemUI gives every small clock. The date row starts
        // below the thick side of the row.
        val TIME_HEIGHT = 114.dp
        const val DATE_ROW_BAND_SP = 22f
        val DATE_ROW_GAP = 8.dp

        val ROW_WIDTH = 232.dp
        val ROW_HEIGHT = 102.dp
        val ROW_TOP = (TIME_HEIGHT - ROW_HEIGHT) / 2f
        val DATE_ROW_TOP = ROW_TOP + ROW_HEIGHT + ROW_WIDTH.tileDepth + DATE_ROW_GAP
        val ROW_CORNER = 14.dp
        val CELL_WIDTH = ROW_WIDTH / 2f
        val CELL_INSET = 10.dp
        val DIVIDER_INSET = 10.dp

        val NUMBER_SIZE = 50.dp
        val SYMBOL_SIZE = 16.dp
        val NAME_SIZE = 10.5.dp
        val WEIGHT_SIZE = 9.5.dp

        // The number sits a little above the middle of its cell, between symbol and name.
        const val NUMBER_MIDDLE = 0.47f

        fun design(context: Context): FaceDesign {
            val metrics = context.resources.displayMetrics
            val band =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DATE_ROW_BAND_SP, metrics) /
                    metrics.density
            return FaceDesign(
                ROW_WIDTH,
                DATE_ROW_TOP + band.dp,
                FaceArea.CENTRED_IN_WIDTH,
                showsDate = true,
            )
        }
    }
}
