// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

/** A length in density-independent pixels. */
@JvmInline
value class Dp(val value: Float) : Comparable<Dp> {
    operator fun plus(other: Dp) = Dp(value + other.value)

    operator fun minus(other: Dp) = Dp(value - other.value)

    operator fun times(factor: Float) = Dp(value * factor)

    operator fun div(factor: Float) = Dp(value / factor)

    override fun compareTo(other: Dp) = value.compareTo(other.value)

    fun toPx(density: Float) = value * density
}

val Number.dp
    get() = Dp(toFloat())

/** A point given in dp from the top left of a face. */
data class DpPoint(val x: Dp, val y: Dp)

/** An area given in dp from the top left of a face. */
data class DpRect(val left: Dp, val top: Dp, val right: Dp, val bottom: Dp) {
    val width
        get() = right - left

    val height
        get() = bottom - top
}
