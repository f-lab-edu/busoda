package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class StopDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val dummyData = MutableLiveData<BusStopDetail>()
    private val isLoadingData = MutableLiveData<Boolean>()
    private val stopIdData = MutableLiveData<String>(savedStateHandle.get(BUS_STOP_ID))
    val stopDetail: LiveData<BusStopDetail> = dummyData
    val isLoading: LiveData<Boolean> = isLoadingData
    val stopId: LiveData<String> = stopIdData

    private val dummyDataMapCreators: Map<String, BusStopDetail> = mapOf(
        "16206" to BusStopDetail(
            "화곡역4번출구", listOf(
                BusInfo(
                    "604", "화곡본동시장", listOf(
                        BusArrivalInfo("2분 38초", "2번째 전", "보통"),
                        BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "5712", "화곡본동시장", listOf(
                        BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                        BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "652", "화곡역1번출구", listOf(
                        BusArrivalInfo("4분 58초", "4번째 전", "보통"),
                        BusArrivalInfo("18분 20초", "11번째 전", "매우 혼잡")
                    )
                )
            )
        ),
        "16146" to BusStopDetail(
            "화곡본동시장", listOf(
                BusInfo(
                    "604", "한국폴리텍1.서울강서대학교", listOf(
                        BusArrivalInfo("4분 38초", "2번째 전", "보통"),
                        BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "5712", "한국폴리텍1.서울강서대학교", listOf(
                        BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                        BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
                    )
                )
            )
        ),
        "16143" to BusStopDetail(
            "한국폴리텍1.서울강서대학교", listOf(
                BusInfo(
                    "604", "우장초등학교", listOf(
                        BusArrivalInfo("2분 38초", "2번째 전", "보통"),
                        BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "5712", "우장초등학교", listOf(
                        BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                        BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
                    )
                )
            )
        ),
        "16142" to BusStopDetail(
            "우장초등학교", listOf(
                BusInfo(
                    "604", "강서구청.한국건강관리협회", listOf(
                        BusArrivalInfo("2분 38초", "2번째 전", "보통"),
                        BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "5712", "강서구청.한국건강관리협회", listOf(
                        BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                        BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
                    )
                )
            )
        ),
        "16139" to BusStopDetail(
            "강서구청.한국건강관리협회", listOf(
                BusInfo(
                    "604", "강서구청사거리.서울디지털대학교", listOf(
                        BusArrivalInfo("2분 38초", "2번째 전", "보통"),
                        BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "5712", "강서구청사거리.서울디지털대학교", listOf(
                        BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                        BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
                    )
                )
            )
        ),
        "16008" to BusStopDetail(
            "강서구청사거리.서울디지털대학교", listOf(
                BusInfo(
                    "604", "등촌중학교.백석초등학교", listOf(
                        BusArrivalInfo("2분 38초", "2번째 전", "보통"),
                        BusArrivalInfo("16분 18초", "9번째 전", "혼잡")
                    )
                ),
                BusInfo(
                    "5712", "등촌중학교.백석초등학교", listOf(
                        BusArrivalInfo("3분 48초", "3번째 전", "여유"),
                        BusArrivalInfo("17분 19초", "10번째 전", "혼잡")
                    )
                )
            )
        )
    )

    init {
        asyncDataLoad()
    }

    private fun asyncDataLoad() {
        isLoadingData.value = true
        viewModelScope.launch {
            delay(3000)
            dummyData.value = getDummyData()
            isLoadingData.value = false
        }
    }

    private fun getDummyData() =
        dummyDataMapCreators.get(stopIdData.value) ?: BusStopDetail("", emptyList())

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}
