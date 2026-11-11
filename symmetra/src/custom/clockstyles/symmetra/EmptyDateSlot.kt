/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.view.View
import android.widget.Space
import com.android.systemui.plugins.keyguard.ui.clocks.ClockStyleSlot

/**
 * The date slot of a face that shows its date itself. With it, the host shows neither a date nor an
 * alarm of its own for the face.
 */
internal object EmptyDateSlot : ClockStyleSlot {
    override fun createView(context: Context): View = Space(context)
}
