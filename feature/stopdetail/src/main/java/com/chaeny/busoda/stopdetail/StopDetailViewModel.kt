package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.ui.event.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopDetailViewModel @Inject constructor(
    private val busStopDetailRepository: BusStopDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _stopDetail = MutableLiveData<BusStopDetail>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _stopId = MutableLiveData<String>(savedStateHandle.get(BUS_STOP_ID))
    private val _refreshEvent = MutableLiveData<Event<Boolean>>()
    val stopDetail: LiveData<BusStopDetail> = _stopDetail
    val isLoading: LiveData<Boolean> = _isLoading
    val stopId: LiveData<String> = _stopId
    val refreshEvent: LiveData<Event<Boolean>> = _refreshEvent

    private var currentCount = 15

    val timer = flow {
        while (true) {
            emit(currentCount)
            delay(1000)
            currentCount--

            if (currentCount == 0) {
                refreshData()
            }
        }
    }

    init {
        asyncDataLoad()
    }

    private fun asyncDataLoad() {
        _isLoading.value = true
        viewModelScope.launch {
            _stopDetail.value = busStopDetailRepository.getBusStopDetail(_stopId.value!!)
            _isLoading.value = false
        }
    }

    fun refreshData() {
        currentCount = 15
        _refreshEvent.value = Event(true)
        asyncDataLoad()
    }

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}
