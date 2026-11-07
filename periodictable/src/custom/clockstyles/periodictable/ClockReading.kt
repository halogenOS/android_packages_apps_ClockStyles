// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.text.format.DateFormat
import com.android.systemui.plugins.keyguard.ui.clocks.TimeFormatKind
import java.util.Date
import java.util.Locale

/**
 * What a clock shows at one minute. [hour] and [minute] are the numbers, [hourText] and
 * [minuteText] how the clock writes them. [description] is the whole reading as one phrase for
 * accessibility.
 */
data class ClockReading(
    val hour: Int,
    val minute: Int,
    val hourText: String,
    val minuteText: String,
    val weekday: String,
    val monthDay: String,
    val shortDate: String,
    val nextAlarm: String?,
    val description: String,
) {
    val time
        get() = "$hourText:$minuteText"
}

/**
 * The date and time formats of a clock in [locale], [timeZone] and [formatKind]. They are built
 * once for each change of these.
 */
class ClockFormats(
    private val locale: Locale,
    private val timeZone: TimeZone,
    private val formatKind: TimeFormatKind,
) {
    private val hourSkeleton = if (formatKind == TimeFormatKind.FULL_DAY) "HH" else "hh"
    private val hourDigits = formatter(if (formatKind == TimeFormatKind.FULL_DAY) "HH" else "hh")
    private val minuteDigits = formatter("mm")
    private val weekday = bestFormatter("EEEE")
    private val monthDay = bestFormatter("MMMd")
    private val shortDate = bestFormatter("EEEMMMd")
    private val alarm = bestFormatter("EEE${hourSkeleton}mm")
    private val description = bestFormatter("EEEEMMMMd${hourSkeleton}mm")

    fun read(now: Date, nextAlarm: Date?): ClockReading {
        val calendar = Calendar.getInstance(timeZone, locale).apply { time = now }
        return ClockReading(
            hour = hourOf(calendar),
            minute = calendar[Calendar.MINUTE],
            hourText = hourDigits.format(now),
            minuteText = minuteDigits.format(now),
            weekday = weekday.format(now),
            monthDay = monthDay.format(now),
            shortDate = shortDate.format(now),
            nextAlarm = nextAlarm?.let(alarm::format),
            description = description.format(now),
        )
    }

    // The hour as the clock shows it: 0 to 23, or 1 to 12 on a half-day clock.
    private fun hourOf(calendar: Calendar) =
        when (formatKind) {
            TimeFormatKind.FULL_DAY -> calendar[Calendar.HOUR_OF_DAY]
            TimeFormatKind.HALF_DAY -> calendar[Calendar.HOUR].takeIf { it != 0 } ?: 12
        }

    // The best pattern of the locale for the fields in [skeleton].
    private fun bestFormatter(skeleton: String) =
        formatter(DateFormat.getBestDateTimePattern(locale, skeleton))

    // Exactly [pattern], as the best pattern for "hh" would add the AM/PM marker.
    private fun formatter(pattern: String) =
        SimpleDateFormat(pattern, locale).also { it.timeZone = timeZone }
}
