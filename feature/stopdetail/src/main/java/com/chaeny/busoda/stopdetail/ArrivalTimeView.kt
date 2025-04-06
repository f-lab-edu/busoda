package com.chaeny.busoda.stopdetail

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class ArrivalTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var currentTime = 0
    private val handler = Handler(Looper.getMainLooper())
    private val decreaseTimeRunnable = object : Runnable {
        override fun run() {
            if (currentTime > 0) {
                currentTime--
                text = formattedArrivalTime()
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun startCountdown(seconds: Int) {
        stopCountdown()
        currentTime = seconds
        text = formattedArrivalTime()
        handler.post(decreaseTimeRunnable)
    }

    private fun stopCountdown() {
        handler.removeCallbacks(decreaseTimeRunnable)
    }

    private fun formattedArrivalTime(): String {
        if (currentTime <= 0) return context.getString(R.string.no_data)

        val minutes = currentTime / 60
        val seconds = currentTime % 60

        return when {
            minutes > 0 && seconds > 0 -> context.getString(R.string.minutes_seconds, minutes, seconds)
            minutes > 0 -> context.getString(R.string.minutes, minutes)
            else -> context.getString(R.string.seconds, seconds)
        }
    }
}
