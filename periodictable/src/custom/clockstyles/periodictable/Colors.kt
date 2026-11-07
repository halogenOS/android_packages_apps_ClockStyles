// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.graphics.Color

/** The colour [fraction] of the way from [from] to [to], channel by channel. */
internal fun blend(from: Int, to: Int, fraction: Float): Int {
    val t = fraction.coerceIn(0f, 1f)
    fun mix(a: Int, b: Int) = (a + (b - a) * t).toInt()
    return Color.argb(
        mix(Color.alpha(from), Color.alpha(to)),
        mix(Color.red(from), Color.red(to)),
        mix(Color.green(from), Color.green(to)),
        mix(Color.blue(from), Color.blue(to)),
    )
}

/** This colour with its alpha set to [fraction] of opaque. */
fun Int.withAlpha(fraction: Float): Int =
    Color.argb(
        (255 * fraction.coerceIn(0f, 1f)).toInt(),
        Color.red(this),
        Color.green(this),
        Color.blue(this),
    )
