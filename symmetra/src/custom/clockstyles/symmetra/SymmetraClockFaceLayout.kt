/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
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

class SymmetraClockFaceLayout(
    view: View,
    private val hostCtx: Context,
) : DefaultClockFaceLayout(view) {

    private val sliceViewId: Int =
        hostCtx.resources.getIdentifier("keyguard_slice_view", "id", hostCtx.packageName)
    private val rowId: Int =
        hostCtx.resources.getIdentifier("row", "id", hostCtx.packageName)

    private var savedSliceView: View? = null
    private var savedRow: LinearLayout? = null
    private var savedPaddingStart = 0
    private var savedGravity = Gravity.START

    init {
        // When our clock view is detached (clock switch), restore original date alignment
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                resetDateView()
            }
        })
    }

    override fun applyConstraints(constraints: ConstraintSet): ConstraintSet {
        super.applyConstraints(constraints)
        constraints.apply {
            constrainWidth(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, WRAP_CONTENT)
            connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, START, PARENT_ID, START, 0)
            connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL, END, PARENT_ID, END, 0)
        }

        // Center the date row — use rootView to search the entire window hierarchy
        view.rootView?.let { root ->
            if (sliceViewId != 0) {
                root.findViewById<View>(sliceViewId)?.let { sliceView ->
                    if (savedSliceView == null) {
                        savedSliceView = sliceView
                        savedPaddingStart = sliceView.paddingStart
                    }
                    sliceView.setPaddingRelative(
                        0, sliceView.paddingTop, 0, sliceView.paddingBottom
                    )
                    if (rowId != 0) {
                        sliceView.findViewById<LinearLayout>(rowId)?.let { row ->
                            if (savedRow == null) {
                                savedRow = row
                                savedGravity = row.gravity
                            }
                            row.gravity = Gravity.CENTER
                        }
                    }
                }
            }
        }

        return constraints
    }

    private fun resetDateView() {
        savedSliceView?.setPaddingRelative(
            savedPaddingStart,
            savedSliceView?.paddingTop ?: 0,
            0,
            savedSliceView?.paddingBottom ?: 0,
        )
        savedRow?.gravity = savedGravity
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
