package com.example.imagebrowser

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * 正方形 FrameLayout，高度等于宽度，用于网格图片列表
 */
class SquareFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 强制高度等于宽度，保持正方形
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
