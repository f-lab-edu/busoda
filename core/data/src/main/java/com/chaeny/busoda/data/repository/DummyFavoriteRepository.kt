package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DummyFavoriteRepository @Inject constructor() : FavoriteRepository {

    private val dummyData = mutableListOf(
        BusStop("02218", "남대문경찰서.서울역10번출구", "숭례문"),
        BusStop("03119", "신용산역3번출구", "신용산지하차도"),
        BusStop("19114", "영등포역", "신길역5호선"),
        BusStop("19113", "영등포역.패어필드호텔", "경방타임스퀘어.신세계백화점")
    )

    override fun getFavorites(): Flow<List<BusStop>> {
        return MutableStateFlow(dummyData)
    }

    override suspend fun addFavorite(stop: BusStop) {
        dummyData.add(stop)
    }
}
