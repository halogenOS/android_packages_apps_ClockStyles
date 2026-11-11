/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import com.android.systemui.customization.clocks.DigitalDateFormatter

/** The date as the date row of a face writes it: the weekday and the day of the month. */
internal class DateRowText(
    private val weekday: DigitalDateFormatter,
    private val monthDay: DigitalDateFormatter,
) {
    val text: String
        get() = listOf(weekday.getText(), monthDay.getText()).joinToString(SEPARATOR)

    /** The date in words for a screen reader, or null when [weekday] writes none. */
    val contentDescription: String?
        get() = weekday.getContentDescription()

    private companion object {
        // The look of the row, a design choice of the clock style.
        const val SEPARATOR = " · "
    }
}
