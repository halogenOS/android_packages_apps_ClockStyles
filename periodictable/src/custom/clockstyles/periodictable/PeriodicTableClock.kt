// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.annotation.DrawableRes
import android.annotation.StringRes
import android.content.Context
import android.icu.util.TimeZone
import com.android.systemui.customization.clocks.TimeKeeper
import com.android.systemui.customization.clocks.TimeKeeperImpl
import com.android.systemui.customization.clocks.view.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.VRect
import com.android.systemui.plugins.keyguard.data.model.AlarmData
import com.android.systemui.plugins.keyguard.data.model.WeatherData
import com.android.systemui.plugins.keyguard.data.model.ZenData
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAnimations
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAxisStyle
import com.android.systemui.plugins.keyguard.ui.clocks.ClockConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockEventListeners
import com.android.systemui.plugins.keyguard.ui.clocks.ClockEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMessageBuffers
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMetadata
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPickerConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPositionAnimationArgs
import com.android.systemui.plugins.keyguard.ui.clocks.ClockProviderPlugin
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds
import com.android.systemui.plugins.keyguard.ui.clocks.ThemeConfig
import com.android.systemui.plugins.keyguard.ui.clocks.TimeFormatKind
import java.io.PrintWriter
import java.util.Date
import java.util.Locale

/**
 * The two faces of a clock: large without notifications, small above them. SystemUI looks the faces
 * up by [viewId].
 */
enum class FaceSize(val viewId: Int) {
    LARGE(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE),
    SMALL(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL),
}

/**
 * A clock style built on this library: its id, its name, description and thumbnail in the clock
 * picker, from the resources of the plugin that contains it, and the views that draw its faces.
 */
interface PeriodicTableClockStyle {
    val clockId: String
    @get:StringRes val nameRes: Int
    @get:StringRes val descriptionRes: Int
    @get:DrawableRes val thumbnailRes: Int

    fun createFace(context: Context, size: FaceSize, colors: ClockColors): FaceView
}

/**
 * The clock provider of a plugin with one [style]. A plugin subclasses it, so that the class has
 * the annotations read by the plugin loader.
 */
abstract class PeriodicTableClockProvider(private val style: PeriodicTableClockStyle) :
    ClockProviderPlugin {
    private lateinit var pluginContext: Context

    override fun onCreate(hostContext: Context, pluginContext: Context) {
        this.pluginContext = pluginContext
    }

    override fun initialize(buffers: ClockMessageBuffers?) {}

    override fun getClocks() = listOf(ClockMetadata(style.clockId))

    override fun createClock(ctx: Context, settings: ClockSettings): ClockController {
        requireOwnClock(settings)
        return PeriodicTableClockController(ctx, pluginContext, settings, style)
    }

    override fun getClockPickerConfig(settings: ClockSettings): ClockPickerConfig {
        requireOwnClock(settings)
        return pluginContext.resources.run {
            ClockPickerConfig(
                style.clockId,
                getString(style.nameRes),
                getString(style.descriptionRes),
                getDrawable(style.thumbnailRes, null),
            )
        }
    }

    private fun requireOwnClock(settings: ClockSettings) =
        require(settings.clockId == style.clockId) {
            "${settings.clockId} unsupported by this provider"
        }
}

/** Reads the time, the date and the next alarm, and shows them on both faces of [style]. */
class PeriodicTableClockController(
    private val hostContext: Context,
    pluginContext: Context,
    settings: ClockSettings,
    style: PeriodicTableClockStyle,
    private val timeKeeper: TimeKeeper = TimeKeeperImpl(),
) : ClockController {
    private var locale: Locale = Locale.getDefault()
    private var formatKind = TimeFormatKind.HALF_DAY
    private var formats = ClockFormats(locale, timeKeeper.timeZone, formatKind)
    private var nextAlarm: Date? = null

    private val theme = ThemeConfig(isDarkTheme = true, settings.seedColor)

    override val smallClock = face(style, pluginContext, FaceSize.SMALL)

    override val largeClock = face(style, pluginContext, FaceSize.LARGE)

    override val config =
        pluginContext.resources.run {
            ClockConfig(style.clockId, getString(style.nameRes), getString(style.descriptionRes))
        }

    override val eventListeners = ClockEventListeners()

    override val events =
        object : ClockEvents {
            override fun onTimeZoneChanged(timeZone: TimeZone) {
                timeKeeper.timeZone = timeZone
                refresh()
            }

            override fun onTimeFormatChanged(formatKind: TimeFormatKind) {
                this@PeriodicTableClockController.formatKind = formatKind
                refresh()
            }

            override fun onLocaleChanged(locale: Locale) {
                this@PeriodicTableClockController.locale = locale
                refresh()
            }

            override fun onAlarmDataChanged(data: AlarmData) {
                nextAlarm = data.nextAlarmMillis?.let(::Date)
                refresh()
            }

            override fun onWeatherDataChanged(data: WeatherData) {}

            override fun onZenDataChanged(data: ZenData) {}
        }

    override fun initialize(isDarkTheme: Boolean, dozeFraction: Float, foldFraction: Float) {
        listOf(smallClock, largeClock).forEach { face ->
            face.events.onThemeChanged(face.theme.copy(isDarkTheme = isDarkTheme))
            face.animations.doze(dozeFraction)
            face.events.onTimeTick()
        }
    }

    override fun dump(pw: PrintWriter) {
        pw.println("small=${smallClock.faceView.reading}")
        pw.println("large=${largeClock.faceView.reading}")
    }

    private fun face(style: PeriodicTableClockStyle, pluginContext: Context, size: FaceSize) =
        PeriodicTableFaceController(
            hostContext,
            theme,
            style.createFace(pluginContext, size, theme.toClockColors(hostContext)).apply {
                id = size.viewId
            },
            ::read,
        )

    // Builds the formats again for the current locale, time zone and time format, then shows the
    // time on both faces.
    private fun refresh() {
        formats = ClockFormats(locale, timeKeeper.timeZone, formatKind)
        smallClock.events.onTimeTick()
        largeClock.events.onTimeTick()
    }

    private fun read(): ClockReading {
        timeKeeper.updateTime()
        return formats.read(timeKeeper.time, nextAlarm)
    }
}

/** One face of a clock: [faceView], which shows what [read] returns on every tick. */
class PeriodicTableFaceController(
    private val hostContext: Context,
    override var theme: ThemeConfig,
    val faceView: FaceView,
    private val read: () -> ClockReading,
) : ClockFaceController {
    override val config = ClockFaceConfig(hasCustomWeatherDataDisplay = faceView.design.showsDate)

    @Deprecated("Use layout instead") override val view = faceView

    override val layout = DefaultClockFaceLayout(faceView)

    override val events =
        object : ClockFaceEvents {
            override fun onTimeTick() {
                faceView.reading = read()
            }

            override fun onThemeChanged(theme: ThemeConfig) {
                this@PeriodicTableFaceController.theme = theme
                faceView.colors = theme.toClockColors(hostContext)
            }

            override fun onFontSettingChanged(fontSizePx: Float) {}

            override fun onTargetRegionChanged(targetRegion: VRect) {}

            override fun onSecondaryDisplayChanged(onSecondaryDisplay: Boolean) {}
        }

    override val animations =
        object : ClockAnimations {
            override fun enter() {}

            override fun doze(fraction: Float) {
                faceView.dozeFraction = fraction
            }

            override fun fold(fraction: Float) {}

            override fun charge() {}

            override fun onPickerCarouselSwiping(swipingFraction: Float) {}

            override fun onPositionAnimated(anim: ClockPositionAnimationArgs) {}

            override fun onFidgetTap(x: Float, y: Float) {}

            override fun onFontAxesChanged(style: ClockAxisStyle) {}
        }
}
