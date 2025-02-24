package com.chaeny.busoda.data.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "ServiceResult")
data class StopDetailResponse(
    @Element(name = "msgBody")
    val msgBody: StopDetailBody?
)

@Xml(name = "msgBody")
data class StopDetailBody(
    @Element(name = "itemList")
    val busInfos: List<StopDetailItem>?
)

@Xml(name = "itemList")
data class StopDetailItem(
    @PropertyElement(name = "rtNm")
    val busNumber: String?,

    @PropertyElement(name = "stNm")
    val stopName: String?,

    @PropertyElement(name = "nxtStn")
    val nextStopName: String?,

    @PropertyElement(name = "arrmsg1")
    val firstArrivalInfo: String?,

    @PropertyElement(name = "arrmsg2")
    val secondArrivalInfo: String?,

    @PropertyElement(name = "congestion1")
    val firstBusCongestion: String?,

    @PropertyElement(name = "congestion2")
    val secondBusCongestion: String?
)
