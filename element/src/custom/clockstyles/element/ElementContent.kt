// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.element

import android.content.res.Resources
import custom.clockstyles.periodictable.AtomicWeight
import custom.clockstyles.periodictable.Element

/**
 * What one tile or cell shows for an hour or a minute. A number without an element, such as 00,
 * shows only the number.
 */
internal data class ElementContent(
    val number: String,
    val symbol: String?,
    val name: String?,
    val weight: String?,
) {
    companion object {
        val EMPTY = ElementContent("", null, null, null)
    }
}

/** The content for the number written as [text], showing [element] when there is one. */
internal fun Resources.elementContent(text: String, element: Element?) =
    ElementContent(
        number = text,
        symbol = element?.symbol,
        name = element?.let { getString(it.nameRes) },
        weight = element?.weight?.label,
    )

private val AtomicWeight.label
    get() =
        when (this) {
            is AtomicWeight.Standard -> value
            is AtomicWeight.LongestLivedIsotope -> "($massNumber)"
        }
