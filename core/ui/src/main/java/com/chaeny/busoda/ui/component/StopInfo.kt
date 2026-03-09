package com.chaeny.busoda.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.ui.theme.Gray60

@Composable
fun StopInfo(
    stop: BusStop,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stop.stopName,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(top = 15.dp, bottom = 5.dp)
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(bottom = 15.dp)
        ) {
            Text(
                text = stop.stopId,
                color = Gray60,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.3f)
            )
            Text(
                text = stop.nextStopName,
                color = Gray60,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}
