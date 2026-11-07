/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
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
    private var savedPaddingEnd = 0
    private var savedGravity = Gravity.START

    // SystemUI attaches the date row to the start of the screen with a margin, after the clock has
    // set its constraints. Padding as wide as that margin at the end centres the date on screen.
    private val balanceStartMargin =
        View.OnLayoutChangeListener { sliceView, _, _, _, _, _, _, _, _ ->
            sliceView.padEndLikeStartMargin()
        }

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
                        savedPaddingEnd = sliceView.paddingEnd
                        sliceView.addOnLayoutChangeListener(balanceStartMargin)
                    }
                    sliceView.setPaddingRelative(
                        0, sliceView.paddingTop, sliceView.paddingEnd, sliceView.paddingBottom
                    )
                    sliceView.padEndLikeStartMargin()
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
        savedSliceView?.run {
            removeOnLayoutChangeListener(balanceStartMargin)
            setPaddingRelative(savedPaddingStart, paddingTop, savedPaddingEnd, paddingBottom)
        }
        savedSliceView = null
        savedRow?.gravity = savedGravity
        savedRow = null
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

private fun View.padEndLikeStartMargin() {
    val startMargin = (layoutParams as? MarginLayoutParams)?.marginStart ?: 0
    if (paddingEnd != startMargin) {
        setPaddingRelative(paddingStart, paddingTop, startMargin, paddingBottom)
    }
}
