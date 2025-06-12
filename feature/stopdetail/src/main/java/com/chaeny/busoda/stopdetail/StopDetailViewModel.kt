package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.model.BusStopDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopDetailViewModel @Inject constructor(
    private val busStopDetailRepository: BusStopDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentCount = 15
    private val _stopDetail = MutableLiveData<BusStopDetail>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _stopId = MutableLiveData<String>(savedStateHandle.get(BUS_STOP_ID))
    private val _refreshEvent = MutableSharedFlow<Unit>()
    private val _timer = MutableStateFlow(currentCount)
    val stopDetail: LiveData<BusStopDetail> = _stopDetail
    val isLoading: LiveData<Boolean> = _isLoading
    val stopId: LiveData<String> = _stopId
    val refreshEvent: SharedFlow<Unit> = _refreshEvent
    val timer: StateFlow<Int> = _timer

    init {
        asyncDataLoad()
        startTimer()
    }

    private fun asyncDataLoad() {
        _isLoading.value = true
        viewModelScope.launch {
            _stopDetail.value = busStopDetailRepository.getBusStopDetail(_stopId.value!!)
            _isLoading.value = false
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                _timer.value = currentCount
                delay(1000)
                currentCount--

                if (currentCount == 0) {
                    refreshData()
                }
            }
        }
    }

    fun refreshData() {
        currentCount = 15
        viewModelScope.launch {
            _refreshEvent.emit(Unit)
        }
        asyncDataLoad()
    }

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}
