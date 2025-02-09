package com.chaeny.busoda.stoplist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chaeny.busoda.data.repository.BusStopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(private val repository: BusStopRepository) : ViewModel() {

    private val dummyData = MutableLiveData(getDummyData())
    private val _removeCompleted = MutableLiveData<Event<RemoveResult>>()
    private val _busStopClicked = MutableLiveData<Event<String>>()
    val busStops: LiveData<List<BusStop>> = dummyData
    val removeCompleted: LiveData<Event<RemoveResult>> = _removeCompleted
    val busStopClicked: LiveData<Event<String>> = _busStopClicked

    fun removeLastStop() {
        val currentStops = dummyData.value
        if (currentStops.isNullOrEmpty()) {
            return
        }
        dummyData.value = currentStops.dropLast(1)
        _removeCompleted.value = Event(RemoveResult.SUCCESS)
    }

    fun handleBusStopClick(stopId: String) {
        _busStopClicked.value = Event(stopId)
    }

    private fun getDummyData() = listOf(
        BusStop("16206", "화곡역4번출구", "화곡본동시장"),
        BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
        BusStop("16143", "한국폴리텍1.서울강서대학교", "우장초등학교"),
        BusStop("16142", "우장초등학교", "강서구청.한국건강관리협회"),
        BusStop("16139", "강서구청.한국건강관리협회", "강서구청사거리.서울디지털대학교"),
        BusStop("16008", "강서구청사거리.서울디지털대학교", "등촌중학교.백석초등학교")
    )
}
