// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import com.android.systemui.customization.clocks.view.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPreviewConfig

/** The constraints of this face on the lock screen and in its preview, from the area it takes. */
internal fun FaceView.createLayout(): ClockFaceLayout =
    if (design.area.spansHostWidth) HostWidthFaceLayout(this) else DefaultClockFaceLayout(this)

/**
 * Spans the face across the screen at the height of its design. SystemUI places the content under
 * the face, such as the notifications under a small clock, below the bottom of the face.
 */
private class HostWidthFaceLayout(private val face: FaceView) : DefaultClockFaceLayout(face) {
    override fun applyConstraints(constraints: ConstraintSet): ConstraintSet =
        super.applyConstraints(constraints).also { it.spanHostWidth() }

    override fun applyPreviewConstraints(
        clockPreviewConfig: ClockPreviewConfig,
        constraints: ConstraintSet,
    ): ConstraintSet =
        super.applyPreviewConstraints(clockPreviewConfig, constraints).also { it.spanHostWidth() }

    private fun ConstraintSet.spanHostWidth() {
        constrainWidth(face.id, WRAP_CONTENT)
        constrainHeight(face.id, WRAP_CONTENT)
        connect(face.id, START, PARENT_ID, START, 0)
        connect(face.id, END, PARENT_ID, END, 0)
    }
}
