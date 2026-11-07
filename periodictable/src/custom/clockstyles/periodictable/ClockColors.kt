// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.annotation.ColorInt
import android.content.Context
import com.android.systemui.monet.ColorScheme
import com.android.systemui.plugins.keyguard.ui.clocks.ThemeConfig

/** The shades of the glass, from light to deep, and its thick side. */
data class GlassShades(
    @get:ColorInt val light: Int,
    @get:ColorInt val top: Int,
    @get:ColorInt val middle: Int,
    @get:ColorInt val deep: Int,
    @get:ColorInt val side: Int,
)

/**
 * The colours of a face: the [glass], [text] outside the glass and [aod], the colour of everything
 * on the always-on display.
 */
data class ClockColors(
    val glass: GlassShades,
    @get:ColorInt val text: Int,
    @get:ColorInt val aod: Int,
)

/**
 * The colours for this theme. The glass is generated from the seed colour picked by the user for
 * the clock, or taken from the system accent palette when there is none.
 */
fun ThemeConfig.toClockColors(hostContext: Context): ClockColors {
    val glass =
        seedColor?.let { seed ->
            ColorScheme(seed, /* darkTheme= */ false).accent1.run {
                GlassShades(s100, s300, s500, s700, s900)
            }
        }
            ?: hostContext.resources.run {
                GlassShades(
                    getColor(android.R.color.system_accent1_100, null),
                    getColor(android.R.color.system_accent1_300, null),
                    getColor(android.R.color.system_accent1_500, null),
                    getColor(android.R.color.system_accent1_700, null),
                    getColor(android.R.color.system_accent1_900, null),
                )
            }
    return ClockColors(glass, text = getDefaultColor(hostContext), aod = getAodColor(hostContext))
}
