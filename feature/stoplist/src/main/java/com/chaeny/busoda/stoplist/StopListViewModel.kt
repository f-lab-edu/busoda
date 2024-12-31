package com.chaeny.busoda.stoplist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class StopListViewModel : ViewModel() {

    val dummyData = MutableLiveData(
        listOf(
            listOf("16206", "화곡역4번출구", "화곡본동시장"),
            listOf("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
            listOf("16143", "한국폴리텍1.서울강서대학교", "우장초등학교"),
            listOf("16142", "우장초등학교", "강서구청.한국건강관리협회"),
            listOf("16139", "강서구청.한국건강관리협회", "강서구청사거리.서울디지털대학교"),
            listOf("16008", "강서구청사거리.서울디지털대학교", "등촌중학교.백석초등학교")
        )
    )

    fun removeLastStop() {
        dummyData.value?.dropLast(1).also {
            dummyData.value = it
        }
    }
}
