package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class StopDetailViewModel : ViewModel() {

    private val dummyData = MutableLiveData<List<Bus>>()
    val busInfos: LiveData<List<Bus>> = dummyData

    init {
        asyncDataLoad()
    }

    private fun asyncDataLoad() {
        viewModelScope.launch {
            delay(3000)
            dummyData.postValue(getDummyData())
        }
    }

    private fun getDummyData(): List<Bus> {
        return listOf(
            Bus(
                "604", "화곡본동시장", "2분 38초", "2번째 전", "보통",
                "16분 18초", "9번째 전", "혼잡"
            ),
            Bus(
                "5712", "화곡본동시장", "3분 48초", "3번째 전", "여유",
                "17분 19초", "10번째 전", "혼잡"
            ),
            Bus(
                "652", "화곡역1번출구", "4분 58초", "4번째 전", "보통",
                "18분 20초", "11번째 전", "매우혼잡"
            )
        )
    }
}
