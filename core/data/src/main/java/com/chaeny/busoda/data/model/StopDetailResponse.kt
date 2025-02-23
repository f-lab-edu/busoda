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
    val busList: List<StopDetailItem>?
)

@Xml(name = "itemList")
data class StopDetailItem(
    @PropertyElement(name = "arsId")
    val arsId: String?,

    @PropertyElement(name = "stNm")
    val stopName: String?,

    @PropertyElement(name = "rtNm")
    val busNumber: String?,

    @PropertyElement(name = "nxtStn")
    val nextStopName: String?,

    @PropertyElement(name = "arrmsg1")
    val arrivalMessage1: String?,

    @PropertyElement(name = "arrmsg2")
    val arrivalMessage2: String?,

    @PropertyElement(name = "congestion1")
    val congestion1: String?,

    @PropertyElement(name = "congestion2")
    val congestion2: String?
)
