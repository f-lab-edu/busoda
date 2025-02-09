package com.chaeny.busoda.stopdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.model.BusStopDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class StopDetailViewModel @Inject constructor(
    private val busStopDetailRepository: BusStopDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dummyData = MutableLiveData<BusStopDetail>()
    private val isLoadingData = MutableLiveData<Boolean>()
    private val stopIdData = MutableLiveData<String>(savedStateHandle.get(BUS_STOP_ID))
    val stopDetail: LiveData<BusStopDetail> = dummyData
    val isLoading: LiveData<Boolean> = isLoadingData
    val stopId: LiveData<String> = stopIdData

    init {
        asyncDataLoad()
    }

    private fun asyncDataLoad() {
        isLoadingData.value = true
        viewModelScope.launch {
            delay(3000)
            dummyData.value = busStopDetailRepository.getBusStopDetail(stopIdData.value!!)
            isLoadingData.value = false
        }
    }

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}
