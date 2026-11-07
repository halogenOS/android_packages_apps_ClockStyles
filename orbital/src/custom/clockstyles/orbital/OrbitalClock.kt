// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.orbital

import android.content.Context
import com.android.internal.annotations.Keep
import com.android.systemui.plugins.annotations.Requires
import com.android.systemui.plugins.keyguard.ui.clocks.ClockProviderPlugin
import custom.clockstyles.periodictable.ClockColors
import custom.clockstyles.periodictable.FaceSize
import custom.clockstyles.periodictable.PeriodicTableClockProvider
import custom.clockstyles.periodictable.PeriodicTableClockStyle

/** Hour and minute as two electrons on the shells around a nucleus that shows the time. */
object OrbitalClock : PeriodicTableClockStyle {
    override val clockId = "ORBITAL"
    override val nameRes = R.string.orbital_clock_name
    override val descriptionRes = R.string.orbital_clock_description
    override val thumbnailRes = R.drawable.orbital_clock_thumbnail

    override fun createFace(context: Context, size: FaceSize, colors: ClockColors) =
        OrbitalFaceView(context, size, colors)
}

@Keep
@Requires(target = ClockProviderPlugin::class, version = ClockProviderPlugin.VERSION)
class OrbitalClockProvider : PeriodicTableClockProvider(OrbitalClock)
