/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.android.systemui.customization.clocks.DefaultClockFaceLayout
import com.android.systemui.customization.clocks.DigitalTimeFormatter
import com.android.systemui.customization.clocks.DigitalTimespec
import com.android.systemui.customization.clocks.DigitalTimespecHandler
import com.android.systemui.log.core.Logger
import com.android.systemui.log.core.MessageBuffer
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAnimations
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAxisStyle
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPositionAnimationArgs
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds
import com.android.systemui.plugins.keyguard.ui.clocks.ThemeConfig

class SymmetraClockFaceController(
    private val hostCtx: Context,
    private val pluginCtx: Context,
    private val settings: ClockSettings,
    private val timeFormatter: DigitalTimeFormatter,
    private val messageBuffer: MessageBuffer,
    private val isLargeClock: Boolean,
    private val minuteFormatter: DigitalTimeFormatter? = null,
) : ClockFaceController {

    val logger = Logger(messageBuffer, this::class.simpleName!!)
    val timespecHandler = DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, timeFormatter)
    val minuteTimespecHandler = minuteFormatter?.let {
        DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, it)
    }

    override val config = ClockFaceConfig()
    override var theme = ThemeConfig(isDarkTheme = true, settings.seedColor)

    override val view: TextView =
        TextView(pluginCtx).apply {
            id =
                if (isLargeClock) ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE
                else ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL

            visibility = View.VISIBLE
            gravity = Gravity.CENTER
            alpha = 1f
            typeface = Typeface.create(
                hostCtx.resources.getString(com.android.internal.R.string.config_clockFontFamily),
                Typeface.NORMAL,
            )
            letterSpacing = 0.02f
            fontFeatureSettings = "tnum"

            if (isLargeClock) {
                setSingleLine(false)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 150f)
                setLineSpacing(0f, 0.7f)
                text = "${timespecHandler.getText()}\n${minuteTimespecHandler?.getText() ?: ""}"
            } else {
                setSingleLine(true)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                text = timespecHandler.getText()
            }
        }

    override val layout =
        if (isLargeClock) DefaultClockFaceLayout(view)
        else SymmetraClockFaceLayout(view, hostCtx)

    override val events =
        object : ClockFaceEvents {
            override fun onTimeTick() {
                timeFormatter.timeKeeper.updateTime()
                view.text =
                    if (isLargeClock) {
                        "${timespecHandler.getText()}\n${minuteTimespecHandler?.getText() ?: ""}"
                    } else {
                        timespecHandler.getText()
                    }
                logger.i("Updated rendered time to '${view.text}'")
            }

            override fun onThemeChanged(theme: ThemeConfig) {
                this@SymmetraClockFaceController.theme = theme
                view.setTextColor(theme.getDefaultColor(hostCtx))
            }

            override fun onFontSettingChanged(fontSizePx: Float) {
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx)
            }

            override fun onTargetRegionChanged(targetRegion: Rect?) {}

            override fun onSecondaryDisplayChanged(onSecondaryDisplay: Boolean) {}
        }

    override val animations =
        object : ClockAnimations {
            override fun enter() {}

            override fun doze(fraction: Float) {
                view.alpha = if (fraction > 0.5f) 0.5f else 1f
            }

            override fun fold(fraction: Float) {}

            override fun charge() {}

            override fun onPickerCarouselSwiping(swipingFraction: Float) {}

            override fun onPositionAnimated(args: ClockPositionAnimationArgs) {}

            override fun onFidgetTap(x: Float, y: Float) {}

            override fun onFontAxesChanged(style: ClockAxisStyle) {}
        }
}
