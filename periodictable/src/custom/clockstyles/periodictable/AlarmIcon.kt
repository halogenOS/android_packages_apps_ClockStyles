// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.annotation.ColorInt
import android.graphics.Canvas
import android.graphics.Paint

/**
 * A small alarm clock drawn in lines: the dial with its hands, and a bell on each side at the top.
 */
class AlarmIcon(private val density: Float) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    /** The width of the icon in pixels. */
    val width
        get() = 2 * RADIUS * 1.15f * density

    fun draw(canvas: Canvas, cx: Float, cy: Float, @ColorInt color: Int) {
        paint.color = color
        paint.strokeWidth = STROKE * density
        val r = RADIUS * density
        canvas.drawCircle(cx, cy, r, paint)
        canvas.drawLine(cx, cy, cx, cy - r * 0.6f, paint)
        canvas.drawLine(cx, cy, cx + r * 0.45f, cy + r * 0.3f, paint)
        canvas.drawLine(cx - r * 1.15f, cy - r * 0.6f, cx - r * 0.6f, cy - r * 1.15f, paint)
        canvas.drawLine(cx + r * 1.15f, cy - r * 0.6f, cx + r * 0.6f, cy - r * 1.15f, paint)
    }

    private companion object {
        const val RADIUS = 6.5f
        const val STROKE = 1.6f
    }
}
