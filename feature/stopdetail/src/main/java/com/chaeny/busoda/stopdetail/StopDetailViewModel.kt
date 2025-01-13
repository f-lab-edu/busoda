package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class StopDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val dummyData = MutableLiveData<List<Bus>>()
    private val stopNameData = MutableLiveData<String>(savedStateHandle.get(BUS_STOP_NAME))
    private var isLoading = false
    val busInfos: LiveData<List<Bus>> = dummyData
    val stopName: LiveData<String> = stopNameData

    init {
        asyncDataLoad()
    }

    private fun asyncDataLoad() {
        isLoading = true
        viewModelScope.launch {
            delay(3000)
            dummyData.postValue(getDummyData())
            isLoading = false
        }
    }

    private fun getDummyData() = listOf(
        Bus(
            "604", "화곡본동시장",
            listOf(
                BusArrivalInfo("2분 38초", "2번째 전", "보통"),
                BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
            )
        ),
        Bus(
            "5712", "화곡본동시장",
            listOf(
                BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
            )
        ),
        Bus(
            "652", "화곡역1번출구",
            listOf(
                BusArrivalInfo("4분 58초", "4번째 전", "보통"),
                BusArrivalInfo("18분 20초", "11번째 전", "매우혼잡")
            )
        )
    )

    fun isLoading() = isLoading

    companion object {
        private const val BUS_STOP_NAME = "stopName"
    }
}
