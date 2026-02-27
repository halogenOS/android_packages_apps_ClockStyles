/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.content.Context
import com.android.internal.annotations.Keep
import com.android.systemui.customization.clocks.TimeKeeperImpl
import com.android.systemui.log.LogcatOnlyMessageBuffer
import com.android.systemui.log.core.LogLevel
import com.android.systemui.plugins.annotations.Requires
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMessageBuffers
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMetadata
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPickerConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockProviderPlugin
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings

@Keep
@Requires(target = ClockProviderPlugin::class, version = ClockProviderPlugin.VERSION)
class SymmetraClockProvider : ClockProviderPlugin {
    private lateinit var hostCtx: Context
    private lateinit var pluginCtx: Context
    private lateinit var messageBuffers: ClockMessageBuffers

    override fun onCreate(hostCtx: Context, pluginCtx: Context) {
        this.hostCtx = hostCtx
        this.pluginCtx = pluginCtx
    }

    override fun initialize(buffers: ClockMessageBuffers?) {
        this.messageBuffers =
            buffers ?: ClockMessageBuffers(LogcatOnlyMessageBuffer(LogLevel.DEBUG))
    }

    override fun getClocks(): List<ClockMetadata> {
        return listOf(ClockMetadata(CLOCK_ID))
    }

    override fun createClock(ctx: Context, settings: ClockSettings): ClockController {
        if (settings.clockId != CLOCK_ID)
            throw IllegalArgumentException("${settings.clockId} unsupported by this provider")
        return SymmetraClockController(ctx, pluginCtx, settings, messageBuffers, TimeKeeperImpl())
    }

    override fun getClockPickerConfig(settings: ClockSettings): ClockPickerConfig {
        if (settings.clockId != CLOCK_ID)
            throw IllegalArgumentException("${settings.clockId} unsupported by this provider")
        return ClockPickerConfig(
            CLOCK_ID,
            pluginCtx.resources.getString(R.string.symmetra_clock_name),
            pluginCtx.resources.getString(R.string.symmetra_clock_description),
            pluginCtx.resources.getDrawable(R.drawable.symmetra_clock_thumbnail, null),
        )
    }

    companion object {
        const val CLOCK_ID = "SYMMETRA"
    }
}
