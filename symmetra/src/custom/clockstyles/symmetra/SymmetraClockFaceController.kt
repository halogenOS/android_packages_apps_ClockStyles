/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.android.systemui.customization.clocks.DigitalDateFormatter
import com.android.systemui.customization.clocks.TimeKeeper
import com.android.systemui.log.core.Logger
import com.android.systemui.log.core.MessageBuffer
import com.android.systemui.plugins.keyguard.VRect
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAnimations
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAxisStyle
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPositionAnimationArgs
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.plugins.keyguard.ui.clocks.ClockStyleSlot
import com.android.systemui.plugins.keyguard.ui.clocks.ThemeConfig

internal class SymmetraClockFaceController(
    private val hostCtx: Context,
    private val pluginCtx: Context,
    private val settings: ClockSettings,
    private val face: SymmetraFace,
    private val timeKeeper: TimeKeeper,
    weekdayFormatter: DigitalDateFormatter,
    monthDayFormatter: DigitalDateFormatter,
    private val messageBuffer: MessageBuffer,
) : ClockFaceController {

    private val logger = Logger(messageBuffer, this::class.simpleName!!)

    override val config = ClockFaceConfig()
    override var theme = ThemeConfig(isDarkTheme = true, settings.seedColor)

    private val timeView: TextView =
        TextView(pluginCtx).apply {
            id = face.viewId

            visibility = View.VISIBLE
            gravity = Gravity.CENTER
            alpha = 1f
            letterSpacing = 0.02f
            fontFeatureSettings = "tnum"

            face.style(this)
            text = face.timeText()
        }

    private val dateRow = SymmetraDateRow(weekdayFormatter, monthDayFormatter, timeView)

    override val clockStyleSlot: ClockStyleSlot
        get() = dateRow

    @Deprecated("Prefer use of layout")
    override val view = timeView

    override val layout = face.createLayout(timeView)

    override val events =
        object : ClockFaceEvents {
            override fun onTimeTick() {
                timeKeeper.updateTime()
                timeView.text = face.timeText()
                logger.i("Updated rendered time to '${timeView.text}'")
                dateRow.update()
            }

            override fun onThemeChanged(theme: ThemeConfig) {
                this@SymmetraClockFaceController.theme = theme
                timeView.setTextColor(theme.getDefaultColor(hostCtx))
                dateRow.update()
            }

            override fun onFontSettingChanged(fontSizePx: Float) {
                timeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx)
            }

            override fun onTargetRegionChanged(targetRegion: VRect) {}

            override fun onSecondaryDisplayChanged(onSecondaryDisplay: Boolean) {}
        }

    override val animations =
        object : ClockAnimations {
            override fun enter() {}

            override fun doze(fraction: Float) {
                timeView.alpha = if (fraction > 0.5f) 0.5f else 1f
                dateRow.update()
            }

            override fun fold(fraction: Float) {}

            override fun charge() {}

            override fun onPickerCarouselSwiping(swipingFraction: Float) {}

            override fun onPositionAnimated(anim: ClockPositionAnimationArgs) {}

            override fun onFidgetTap(x: Float, y: Float) {}

            override fun onFontAxesChanged(style: ClockAxisStyle) {}
        }
}
