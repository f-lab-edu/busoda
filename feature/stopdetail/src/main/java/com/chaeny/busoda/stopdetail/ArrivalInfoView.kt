package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.stopdetail.databinding.ArrivalInfoViewBinding

class ArrivalInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    private var binding = ArrivalInfoViewBinding.inflate(layoutInflater, this)

    internal fun bindArrivalInfo(arrivalInfo: BusArrivalInfo?, position: Int) {
        if (arrivalInfo != null) {
            with(binding) {
                textInfoTitle.text = context.getString(R.string.nth_bus, position + 1)
                textArrivalTime.text = arrivalInfo.arrivalTime
                textPosition.text = arrivalInfo.position

                val congestionText = getCongestionText(arrivalInfo.congestion)
                textCongestion.text = congestionText
                textCongestion.setTextColor(getCongestionColor(congestionText))
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

    private fun getCongestionText(congestion: String?): String {
        return when (congestion) {
            "6" -> context.getString(R.string.congestion_very_high)
            "5" -> context.getString(R.string.congestion_high)
            "4" -> context.getString(R.string.congestion_medium)
            "3" -> context.getString(R.string.congestion_low)
            else -> context.getString(R.string.no_data)
        }
    }

    private fun getCongestionColor(congestion: String?): Int {
        return when (congestion) {
            context.getString(R.string.congestion_very_high) -> context.getColor(R.color.congestion_very_high)
            context.getString(R.string.congestion_high) -> context.getColor(R.color.congestion_high)
            context.getString(R.string.congestion_medium) -> context.getColor(R.color.congestion_medium)
            context.getString(R.string.congestion_low) -> context.getColor(R.color.congestion_low)
            else -> context.getColor(R.color.congestion_unknown)
        }
    }
}
