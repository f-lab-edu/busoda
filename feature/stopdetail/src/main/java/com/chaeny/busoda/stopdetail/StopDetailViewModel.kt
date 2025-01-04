package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class StopDetailViewModel : ViewModel() {

    private val dummyData = MutableLiveData(
        listOf(
            Bus(
                "604", "화곡본동시장", "2분 38초", "2번째 전", "보통",
                "16분 18초", "9번째 전", "혼잡"
            ),
            Bus(
                "5712", "화곡본동시장", "3분 38초", "3번째 전", "보통",
                "17분 18초", "10번째 전", "혼잡"
            ),
            Bus(
                "652", "화곡역1번출구", "4분 38초", "4번째 전", "보통",
                "18분 18초", "11번째 전", "혼잡"
            ),
            Bus(
                "강서01", "우체국", "3분 39초", "3번째 전", "여유",
                "15분 14초", "11번째 전", "매우혼잡"
            )
        )
    )

    val busInfos: LiveData<List<Bus>> = dummyData
}
