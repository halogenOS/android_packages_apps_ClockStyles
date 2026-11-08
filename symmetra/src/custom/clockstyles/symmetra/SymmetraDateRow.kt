/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.android.systemui.customization.clocks.DigitalDateFormatter
import com.android.systemui.plugins.keyguard.ui.clocks.ClockStyleSlot

/**
 * The centered date row of one clock face, shown by the host in its date slot. The row takes its
 * color and transparency from the time view of the face.
 */
class SymmetraDateRow(
    private val weekdayFormatter: DigitalDateFormatter,
    private val monthDayFormatter: DigitalDateFormatter,
    private val timeView: TextView,
) : ClockStyleSlot {
    private val attachedViews = mutableSetOf<TextView>()

    override fun createView(context: Context): View {
        checkMainThread()
        val view =
            TextView(context).apply {
                gravity = Gravity.CENTER
                setSingleLine(true)
                isAllCaps = true
                letterSpacing = LETTER_SPACING_EM
                setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP)
            }
        view.addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    attachedViews += view
                    update(view)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    attachedViews -= view
                }
            }
        )
        return view
    }

    /** Shows the date, color and transparency of the face in every attached view. */
    fun update() {
        checkMainThread()
        attachedViews.forEach(::update)
    }

    private fun update(view: TextView) {
        view.text =
            listOf(weekdayFormatter.getText(), monthDayFormatter.getText()).joinToString(SEPARATOR)
        view.contentDescription = weekdayFormatter.getContentDescription()
        view.setTextColor(timeView.currentTextColor)
        view.alpha = timeView.alpha
    }

    private fun checkMainThread() =
        check(Looper.getMainLooper().isCurrentThread) { "The date row runs on the main thread" }

    private companion object {
        // The look of the row, a design choice of the clock style.
        const val LETTER_SPACING_EM = 0.15f
        const val TEXT_SIZE_SP = 14f
        const val SEPARATOR = " · "
    }
}
