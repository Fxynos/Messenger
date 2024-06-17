package com.vl.messenger.ui.view

import android.content.Context
import android.text.StaticLayout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.ceil

/**
 * Custom TextView implementation.
 * [AppCompatTextView] with `android:layout_width="wrap_content"` behaves like with `match_parent`
 * when there are more than one lines.
 *
 * This implementation fixes this bug, it sets width equal to max line width.
 */
class ThinTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val text = text
        val paint = paint

        val (width, height) = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            paint,
            MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        ).build().run {
            ceil( // use max line width
                (0 until layout.lineCount).maxOf { getLineWidth(it) }
            ).toInt() to height
        }

        setMeasuredDimension(
            width + paddingLeft + paddingRight,
            height + paddingTop + paddingBottom
        )
    }
}