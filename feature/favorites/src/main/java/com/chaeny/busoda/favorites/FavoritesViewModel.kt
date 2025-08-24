package com.chaeny.busoda.favorites

import androidx.lifecycle.ViewModel
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor() : ViewModel() {

    private val _favorites = MutableStateFlow(
        listOf(
            BusStop("02218", "남대문경찰서.서울역10번출구", "숭례문"),
            BusStop("03119", "신용산역3번출구", "신용산지하차도"),
            BusStop("19114", "영등포역", "신길역5호선"),
            BusStop("19113", "영등포역.패어필드호텔", "경방타임스퀘어.신세계백화점")
        )
    )

    val favorites: StateFlow<List<BusStop>> = _favorites
}
