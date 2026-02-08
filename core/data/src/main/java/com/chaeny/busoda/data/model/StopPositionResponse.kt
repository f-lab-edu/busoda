package com.chaeny.busoda.data.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "ServiceResult")
data class StopPositionResponse(
    @Element(name = "msgBody")
    val msgBody: StopPositionBody?
)

@Xml(name = "msgBody")
data class StopPositionBody(
    @Element(name = "itemList")
    val busStops: List<StopPositionItem>?
)

@Xml(name = "itemList")
data class StopPositionItem(
    @PropertyElement(name = "arsId")
    val stopId: String?,

    @PropertyElement(name = "stationNm")
    val stopName: String?,

    @PropertyElement(name = "gpsX")
    val gpsX: Double?,

    @PropertyElement(name = "gpsY")
    val gpsY: Double?
)
