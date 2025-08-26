package com.chaeny.busoda.stopdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
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
    private val favoriteRepository: FavoriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentCount = 15
    private val _stopDetail = MutableStateFlow<BusStopDetail>(BusStopDetail("", emptyList()))
    private val _isLoading = MutableStateFlow(false)
    private val _stopId: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get(BUS_STOP_ID) ?: "")
    private val _timer = MutableStateFlow(currentCount)
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    private val _refreshEvent = MutableSharedFlow<Unit>()
    val stopDetail: StateFlow<BusStopDetail> = _stopDetail
    val isLoading: StateFlow<Boolean> = _isLoading
    val stopId: StateFlow<String> = _stopId
    val timer: StateFlow<Int> = _timer
    val currentTime: StateFlow<Long> = _currentTime
    val refreshEvent: SharedFlow<Unit> = _refreshEvent

    init {
        asyncDataLoad()
        startTimer()
    }

    private fun asyncDataLoad() {
        _isLoading.value = true
        viewModelScope.launch {
            _stopDetail.value = busStopDetailRepository.getBusStopDetail(_stopId.value)
            _isLoading.value = false
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                _timer.value = currentCount
                _currentTime.value = System.currentTimeMillis() / 1000
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

    fun addToFavorites() {
        viewModelScope.launch {
            favoriteRepository.addFavorite(
                BusStop(
                    _stopId.value,
                    _stopDetail.value.stopName,
                    _stopDetail.value.busInfos.firstOrNull()?.nextStopName ?: ""
                )
            )
        }
    }

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}
