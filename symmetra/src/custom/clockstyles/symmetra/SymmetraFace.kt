/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.android.systemui.customization.clocks.DigitalTimeFormatter
import com.android.systemui.customization.clocks.DigitalTimespec
import com.android.systemui.customization.clocks.DigitalTimespecHandler
import com.android.systemui.customization.clocks.R as clocksR
import com.android.systemui.customization.clocks.view.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds

/** One of the two faces of the clock, with everything that differs between them. */
internal sealed interface SymmetraFace {
    /** The time as the face writes it. */
    fun timeText(): String

    /** Gives [time] the look of the face. */
    fun style(time: TextView)

    /** Creates the view of the face: [time] with [dateRow] centered under it. */
    fun createView(context: Context, time: View, dateRow: View): View

    /** Creates the constraints of the face around its [view]. */
    fun createLayout(view: View): ClockFaceLayout

    /** The small face: hours and minutes in one line, and the date row right below them. */
    class Small(timeFormatter: DigitalTimeFormatter) : SymmetraFace {
        private val timespec =
            DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, timeFormatter)

        override fun timeText() = timespec.getText()

        override fun style(time: TextView) =
            time.run {
                setSingleLine(true)
                // A host makes a small clock that is a text view read from left to right. The
                // time is inside the view of the face, so the face does that.
                textDirection = View.TEXT_DIRECTION_LTR
                setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP)
            }

        override fun createView(context: Context, time: View, dateRow: View) =
            SymmetraFaceView(context) {
                id = ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL
                // The height a host gives every small clock, from the clock library that a host
                // and this module both build in. The row then starts where a host shows its date
                // below a small clock.
                addView(
                    time,
                    WRAP_CONTENT,
                    resources.getDimensionPixelSize(clocksR.dimen.small_clock_height),
                )
                addView(dateRow, WRAP_CONTENT, WRAP_CONTENT)
            }

        override fun createLayout(view: View) = SymmetraClockFaceLayout(view)

        private companion object {
            // The size of the time until a host sets the one it has for a small clock.
            const val TEXT_SIZE_SP = 28f
        }
    }

    /**
     * The large face: the hours above the minutes, in the middle of the view of the face, and the
     * date row below them.
     */
    class Large(hourFormatter: DigitalTimeFormatter, minuteFormatter: DigitalTimeFormatter) :
        SymmetraFace {
        private val hours = DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, hourFormatter)
        private val minutes =
            DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, minuteFormatter)

        override fun timeText() = "${hours.getText()}\n${minutes.getText()}"

        override fun style(time: TextView) =
            time.run {
                setSingleLine(false)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DP)
                setLineSpacing(0f, LINE_SPACING)
            }

        override fun createView(context: Context, time: View, dateRow: View) =
            TimeCenteredFaceView(context) {
                id = ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE
                addView(time, WRAP_CONTENT, WRAP_CONTENT)
                addView(
                    dateRow,
                    LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                        topMargin =
                            resources.getDimensionPixelSize(R.dimen.symmetra_large_date_row_gap)
                    },
                )
            }

        override fun createLayout(view: View) = DefaultClockFaceLayout(view)

        private companion object {
            // The size of the time until a host sets the one it has for a large clock.
            const val TEXT_SIZE_DP = 150f

            // The minutes are closer to the hours than a line of text is to the line above it, a
            // design choice of the clock style.
            const val LINE_SPACING = 0.7f
        }
    }
}
