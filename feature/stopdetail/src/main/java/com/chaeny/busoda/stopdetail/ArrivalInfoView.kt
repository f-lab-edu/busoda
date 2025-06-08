package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.CongestionLevel
import com.chaeny.busoda.stopdetail.databinding.ArrivalInfoViewBinding
import kotlinx.coroutines.flow.Flow

class ArrivalInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    private var binding = ArrivalInfoViewBinding.inflate(layoutInflater, this)

    internal fun bindArrivalInfo(arrivalInfo: BusArrivalInfo?, position: Int, timerFlow: Flow<Int>) {
        if (arrivalInfo != null) {
            with(binding) {
                composeInfoTitle.setContent {
                    MaterialTheme {
                        ArrivalTitle(context.getString(R.string.nth_bus, position + 1))
                    }
                }
                composeArrivalTime.setContent {
                    MaterialTheme {
                        ArrivalTime("1분 1초 후")
                    }
                }
                composePosition.setContent {
                    MaterialTheme {
                        ArrivalPosition(arrivalInfo.position)
                    }
                }
                composeCongestion.setContent {
                    MaterialTheme {
                        ArrivalCongestion(
                            congestion = arrivalInfo.getCongestionText(),
                            textColor = arrivalInfo.getCongestionColor()
                        )
                    }
                }
            }
        } else {
            bindEmptyInfo(position)
        }
    }

    private fun bindEmptyInfo(position: Int) {
        with(binding) {
            composeInfoTitle.setContent {
                MaterialTheme {
                    ArrivalTitle(context.getString(R.string.nth_bus, position + 1))
                }
            }
            composeArrivalTime.setContent {
                MaterialTheme {
                    ArrivalTime(context.getString(R.string.no_info))
                }
            }
            composePosition.setContent {
                MaterialTheme {
                    ArrivalPosition(context.getString(R.string.no_data))
                }
            }
            composeCongestion.setContent {
                MaterialTheme {
                    ArrivalCongestion(
                        congestion = context.getString(R.string.no_data),
                        textColor = context.getColor(R.color.congestion_unknown)
                    )
                }
            }
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

    @Composable
    fun ArrivalTitle(
        title: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = title,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 5.dp),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    @Composable
    fun ArrivalTime(
        time: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = time,
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall
        )
    }

    @Composable
    fun ArrivalPosition(
        position: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = position,
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    @Composable
    fun ArrivalCongestion(
        congestion: String,
        textColor: Int,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = congestion,
            color = Color(textColor),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
