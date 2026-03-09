package com.chaeny.busoda.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaeny.busoda.model.BusInfo

@Composable
fun BusArrivalInfoList(
    busInfo: BusInfo,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(8.dp))

    ArrivalInfo(
        arrivalInfo = busInfo.arrivalInfos.getOrNull(0),
        position = 0,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    ArrivalInfo(
        arrivalInfo = busInfo.arrivalInfos.getOrNull(1),
        position = 1,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 15.dp)
    )
}
