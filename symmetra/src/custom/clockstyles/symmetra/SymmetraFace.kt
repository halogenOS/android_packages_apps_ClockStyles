/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.android.systemui.customization.clocks.DigitalTimeFormatter
import com.android.systemui.customization.clocks.DigitalTimespec
import com.android.systemui.customization.clocks.DigitalTimespecHandler
import com.android.systemui.customization.clocks.view.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds

/** One of the two faces of the clock, with everything that differs between them. */
internal sealed interface SymmetraFace {
    /** The id by which a host finds the view of the face. */
    val viewId: Int

    /** The time as the face writes it. */
    fun timeText(): String

    /** Gives [time] the look of the face. */
    fun style(time: TextView)

    /** Creates the constraints of the face around its [view]. */
    fun createLayout(view: View): ClockFaceLayout

    /** The small face: hours and minutes in one line. */
    class Small(timeFormatter: DigitalTimeFormatter) : SymmetraFace {
        private val timespec =
            DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, timeFormatter)

        override val viewId = ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL

        override fun timeText() = timespec.getText()

        override fun style(time: TextView) =
            time.run {
                setSingleLine(true)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP)
            }

        override fun createLayout(view: View) = SymmetraClockFaceLayout(view)

        private companion object {
            // The size of the time until a host sets the one it has for a small clock.
            const val TEXT_SIZE_SP = 28f
        }
    }

    /** The large face: the hours above the minutes. */
    class Large(hourFormatter: DigitalTimeFormatter, minuteFormatter: DigitalTimeFormatter) :
        SymmetraFace {
        private val hours = DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, hourFormatter)
        private val minutes =
            DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, minuteFormatter)

        override val viewId = ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE

        override fun timeText() = "${hours.getText()}\n${minutes.getText()}"

        override fun style(time: TextView) =
            time.run {
                setSingleLine(false)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DP)
                setLineSpacing(0f, LINE_SPACING)
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
