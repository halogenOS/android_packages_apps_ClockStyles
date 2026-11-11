/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.icu.util.TimeZone
import com.android.systemui.customization.clocks.DigitalDateFormatter
import com.android.systemui.customization.clocks.DigitalTimeFormatter
import com.android.systemui.customization.clocks.TimeKeeper
import com.android.systemui.log.core.MessageBuffer
import com.android.systemui.plugins.keyguard.data.model.AlarmData
import com.android.systemui.plugins.keyguard.data.model.WeatherData
import com.android.systemui.plugins.keyguard.data.model.ZenData
import com.android.systemui.plugins.keyguard.ui.clocks.ClockConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockEventListeners
import com.android.systemui.plugins.keyguard.ui.clocks.ClockEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMessageBuffers
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.plugins.keyguard.ui.clocks.TimeFormatKind
import java.io.PrintWriter
import java.util.Locale

internal class SymmetraClockController(
    private val hostCtx: Context,
    private val pluginCtx: Context,
    private val settings: ClockSettings,
    private val messageBuffers: ClockMessageBuffers,
    private val timeKeeper: TimeKeeper,
) : ClockController {
    val timeFormatter = DigitalTimeFormatter("hh:mm", timeKeeper)
    val hourFormatter = DigitalTimeFormatter("hh", timeKeeper)
    val minuteFormatter = DigitalTimeFormatter("mm", timeKeeper)
    val weekdayFormatter = DigitalDateFormatter("EEEE", timeKeeper, enableContentDescription = true)
    val monthDayFormatter = DigitalDateFormatter("MMM d", timeKeeper)

    override val smallClock =
        createFace(SymmetraFace.Small(timeFormatter), messageBuffers.smallClockMessageBuffer)

    override val largeClock =
        createFace(
            SymmetraFace.Large(hourFormatter, minuteFormatter),
            messageBuffers.largeClockMessageBuffer,
        )

    override val config =
        ClockConfig(
            SymmetraClockProvider.CLOCK_ID,
            pluginCtx.resources.getString(R.string.symmetra_clock_name),
            pluginCtx.resources.getString(R.string.symmetra_clock_description),
        )

    override val eventListeners = ClockEventListeners()

    override val events =
        object : ClockEvents {
            override fun onTimeZoneChanged(timeZone: TimeZone) {
                timeFormatter.timeKeeper.timeZone = timeZone
                smallClock.events.onTimeTick()
                largeClock.events.onTimeTick()
            }

            override fun onTimeFormatChanged(formatKind: TimeFormatKind) {
                timeFormatter.formatKind = formatKind
                hourFormatter.formatKind = formatKind
                minuteFormatter.formatKind = formatKind
                smallClock.events.onTimeTick()
                largeClock.events.onTimeTick()
            }

            override fun onLocaleChanged(locale: Locale) {
                timeFormatter.locale = locale
                hourFormatter.locale = locale
                minuteFormatter.locale = locale
                weekdayFormatter.locale = locale
                monthDayFormatter.locale = locale
                smallClock.events.onTimeTick()
                largeClock.events.onTimeTick()
            }

            override fun onWeatherDataChanged(data: WeatherData) {}

            override fun onAlarmDataChanged(data: AlarmData) {}

            override fun onZenDataChanged(data: ZenData) {}
        }

    override fun initialize(isDarkTheme: Boolean, dozeFraction: Float, foldFraction: Float) {
        smallClock.run {
            events.onThemeChanged(theme.copy(isDarkTheme = isDarkTheme))
            animations.doze(dozeFraction)
            animations.fold(foldFraction)
            events.onTimeTick()
        }

        largeClock.run {
            events.onThemeChanged(theme.copy(isDarkTheme = isDarkTheme))
            animations.doze(dozeFraction)
            animations.fold(foldFraction)
            events.onTimeTick()
        }
    }

    override fun dump(pw: PrintWriter) {}

    private fun createFace(face: SymmetraFace, messageBuffer: MessageBuffer) =
        SymmetraClockFaceController(
            hostCtx = hostCtx,
            pluginCtx = pluginCtx,
            settings = settings,
            face = face,
            timeKeeper = timeKeeper,
            weekdayFormatter = weekdayFormatter,
            monthDayFormatter = monthDayFormatter,
            messageBuffer = messageBuffer,
        )
}
