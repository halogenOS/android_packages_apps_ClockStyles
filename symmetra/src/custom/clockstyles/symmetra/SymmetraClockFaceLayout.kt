/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import com.android.systemui.customization.clocks.view.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPreviewConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds

internal class SymmetraClockFaceLayout(view: View) : DefaultClockFaceLayout(view) {

    override fun applyConstraints(constraints: ConstraintSet): ConstraintSet {
        super.applyConstraints(constraints)
        constraints.wrapAndCenterSmallFace()

        return constraints
    }

    override fun applyPreviewConstraints(
        clockPreviewConfig: ClockPreviewConfig,
        constraints: ConstraintSet,
    ): ConstraintSet {
        super.applyPreviewConstraints(clockPreviewConfig, constraints)
        constraints.wrapAndCenterSmallFace()

        return constraints
    }

    // A host gives every small clock one fixed height, which has no room for the date row. The
    // small face is as high as its time and its row, and in the middle of the width of the host.
    private fun ConstraintSet.wrapAndCenterSmallFace() {
        constrainWidth(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, WRAP_CONTENT)
        constrainHeight(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, WRAP_CONTENT)
        connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, START, PARENT_ID, START, 0)
        connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, END, PARENT_ID, END, 0)
    }
}
