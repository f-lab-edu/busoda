package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.CongestionLevel
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
                textPosition.text = arrivalInfo.position

                textCongestion.text = arrivalInfo.getCongestionText()
                textCongestion.setTextColor(arrivalInfo.getCongestionColor())

                val arrivalTime = arrivalInfo.arrivalTime.toIntOrNull() ?: 0
                textArrivalTime.setInitialTime(arrivalTime)
            }
        } else {
            bindEmptyInfo(position)
        }
    }

    internal fun decreaseArrivalTime() {
        binding.textArrivalTime.decreaseOneSec()
    }

    private fun bindEmptyInfo(position: Int) {
        with(binding) {
            textInfoTitle.text = context.getString(R.string.nth_bus, position + 1)
            textArrivalTime.text = context.getString(R.string.no_info)
            textPosition.text = context.getString(R.string.no_data)
            textCongestion.text = context.getString(R.string.no_data)
        }
    }

    private fun BusArrivalInfo.getCongestionText(): String {
        return when (this.congestion) {
            CongestionLevel.VERY_HIGH -> context.getString(R.string.congestion_very_high)
            CongestionLevel.HIGH -> context.getString(R.string.congestion_high)
            CongestionLevel.MEDIUM -> context.getString(R.string.congestion_medium)
            CongestionLevel.LOW -> context.getString(R.string.congestion_low)
            else -> context.getString(R.string.no_data)
        }
    }

    private fun BusArrivalInfo.getCongestionColor(): Int {
        return when (this.getCongestionText()) {
            context.getString(R.string.congestion_very_high) -> context.getColor(R.color.congestion_very_high)
            context.getString(R.string.congestion_high) -> context.getColor(R.color.congestion_high)
            context.getString(R.string.congestion_medium) -> context.getColor(R.color.congestion_medium)
            context.getString(R.string.congestion_low) -> context.getColor(R.color.congestion_low)
            else -> context.getColor(R.color.congestion_unknown)
        }
    }
}
