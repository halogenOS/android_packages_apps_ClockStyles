/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package custom.clockstyles.symmetra

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout

/**
 * The view of one clock face: a column with the time and, centered under it, the date row. A host
 * places and moves the view as a whole, so the row is under the middle of the time wherever the
 * host shows the face.
 */
internal open class SymmetraFaceView(context: Context, init: SymmetraFaceView.() -> Unit) :
    LinearLayout(context) {
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        init()
    }
}

/**
 * A face view with as much room above its first view as the views after it take below it. A host
 * centers the view of a large clock, and the first view, the time, is then in that middle.
 */
internal class TimeCenteredFaceView(context: Context, init: SymmetraFaceView.() -> Unit) :
    SymmetraFaceView(context, init) {
    init {
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val time = getChildAt(0) ?: return
        val belowTime = (measuredHeight - time.measuredHeight).coerceAtLeast(0)
        setMeasuredDimension(
            measuredWidthAndState,
            resolveSize(measuredHeight + belowTime, heightMeasureSpec),
        )
    }
}
