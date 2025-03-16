package com.chaeny.busoda.data.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "ServiceResult")
data class StopListResponse(
    @Element(name = "msgBody")
    val msgBody: StopListBody?
)

@Xml(name = "msgBody")
data class StopListBody(
    @Element(name = "itemList")
    val busStops: List<StopListItem>?
)

@Xml(name = "itemList")
data class StopListItem(
    @PropertyElement(name = "arsId")
    val stopId: String?,

    @PropertyElement(name = "stNm")
    val stopName: String?
)
