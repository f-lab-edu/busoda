package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop
import javax.inject.Inject

class DummyBusStopRepository @Inject constructor() : BusStopRepository {
    override suspend fun getBusStops(stopName: String) = listOf(
        BusStop("16206", "화곡역4번출구"),
        BusStop("16146", "화곡본동시장"),
        BusStop("16143", "한국폴리텍1.서울강서대학교"),
        BusStop("16142", "우장초등학교"),
        BusStop("16139", "강서구청.한국건강관리협회"),
        BusStop("16008", "강서구청사거리.서울디지털대학교")
    )
}
