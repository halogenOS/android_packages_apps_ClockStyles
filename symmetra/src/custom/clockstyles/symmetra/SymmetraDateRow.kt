/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/** The date row of one clock face, shown under the time of the face. */
internal class SymmetraDateRow(context: Context, init: SymmetraDateRow.() -> Unit) :
    TextView(context) {
    init {
        gravity = Gravity.CENTER
        setSingleLine(true)
        // A date wider than the screen ends in an ellipsis, which keeps its text centered.
        ellipsize = TextUtils.TruncateAt.END
        isAllCaps = true
        letterSpacing = LETTER_SPACING_EM
        setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP)
        init()
    }

    /** Shows [date] in the row. */
    fun show(date: DateRowText) {
        text = date.text
        contentDescription = date.contentDescription
    }

    private companion object {
        // The look of the row, a design choice of the clock style.
        const val LETTER_SPACING_EM = 0.15f
        const val TEXT_SIZE_SP = 14f
    }
}
