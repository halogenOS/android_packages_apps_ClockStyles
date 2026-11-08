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
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import com.android.systemui.customization.clocks.R as clocksR
import com.android.systemui.customization.clocks.view.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPreviewConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds

class SymmetraClockFaceLayout(view: View) : DefaultClockFaceLayout(view) {

    override fun applyConstraints(constraints: ConstraintSet): ConstraintSet {
        super.applyConstraints(constraints)
        constraints.apply {
            constrainWidth(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, WRAP_CONTENT)
            connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, START, PARENT_ID, START, 0)
            connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, END, PARENT_ID, END, 0)
        }

        return constraints
    }

    override fun applyPreviewConstraints(
        clockPreviewConfig: ClockPreviewConfig,
        constraints: ConstraintSet,
    ): ConstraintSet {
        super.applyPreviewConstraints(clockPreviewConfig, constraints)

        constraints.apply {
            constrainWidth(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, WRAP_CONTENT)
            constrainHeight(
                ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL,
                view.context.resources.getDimensionPixelSize(clocksR.dimen.small_clock_height),
            )
            connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, START, PARENT_ID, START, 0)
            connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, END, PARENT_ID, END, 0)
            connect(
                ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL,
                TOP,
                PARENT_ID,
                TOP,
                clockPreviewConfig.getSmallClockTopPadding(),
            )
        }

        return constraints
    }
}
