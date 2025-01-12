package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.chaeny.busoda.stopdetail.databinding.ArrivalInfoViewBinding

class ArrivalInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    private var binding = ArrivalInfoViewBinding.inflate(layoutInflater, this, true)

    internal fun bindArrivalInfo(arrivalInfo: BusArrivalInfo?, position: Int) {
        if (arrivalInfo != null) {
            with(binding) {
                textInfoTitle.text = context.getString(R.string.nth_bus, position + 1)
                textArrivalTime.text = arrivalInfo.arrivalTime
                textPosition.text = arrivalInfo.position
                textCongestion.text = arrivalInfo.congestion
            }
        } else {
            bindEmptyInfo(position)
        }
    }

    private fun bindEmptyInfo(position: Int) {
        with(binding) {
            textInfoTitle.text = context.getString(R.string.nth_bus, position + 1)
            textArrivalTime.text = context.getString(R.string.no_info)
            textPosition.text = context.getString(R.string.no_data)
            textCongestion.text = context.getString(R.string.no_data)
        }
    }
}
