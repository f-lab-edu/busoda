package com.chaeny.busoda.stopdetail

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ArrivalTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    fun setTextTime(arrivalTime: Int) {
        text = formattedArrivalTime(arrivalTime)
    }

    fun observeCountdownFlow(timerFlow: Flow<Int>, arrivalTime: Int) {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                timerFlow.collect { elapsedTime ->
                    val currentTime = arrivalTime - (15 - elapsedTime)
                    setTextTime(currentTime)
                }
            }
        }
    }

    private fun formattedArrivalTime(arrivalTime: Int): String {
        if (arrivalTime <= 0) return context.getString(R.string.no_data)

        val minutes = arrivalTime / 60
        val seconds = arrivalTime % 60

        return when {
            minutes > 0 && seconds > 0 -> context.getString(R.string.minutes_seconds, minutes, seconds)
            minutes > 0 -> context.getString(R.string.minutes, minutes)
            else -> context.getString(R.string.seconds, seconds)
        }
    }
}
