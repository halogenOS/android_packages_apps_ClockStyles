// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.element

import android.content.Context
import com.android.internal.annotations.Keep
import com.android.systemui.plugins.annotations.Requires
import com.android.systemui.plugins.keyguard.ui.clocks.ClockProviderPlugin
import custom.clockstyles.periodictable.ClockColors
import custom.clockstyles.periodictable.FaceSize
import custom.clockstyles.periodictable.PeriodicTableClockProvider
import custom.clockstyles.periodictable.PeriodicTableClockStyle

/** Hour and minute as two glass tiles showing the elements with those atomic numbers. */
object ElementClock : PeriodicTableClockStyle {
    override val clockId = "ELEMENT"
    override val nameRes = R.string.element_clock_name
    override val descriptionRes = R.string.element_clock_description
    override val thumbnailRes = R.drawable.element_clock_thumbnail

    override fun createFace(context: Context, size: FaceSize, colors: ClockColors) =
        ElementFaceView(context, size, colors)
}

@Keep
@Requires(target = ClockProviderPlugin::class, version = ClockProviderPlugin.VERSION)
class ElementClockProvider : PeriodicTableClockProvider(ElementClock)
