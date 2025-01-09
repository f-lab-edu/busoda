package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class ArrivalInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflate(context, R.layout.arrival_info_view, this)
    }
}
