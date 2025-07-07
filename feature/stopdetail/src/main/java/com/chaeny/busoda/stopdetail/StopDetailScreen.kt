package com.chaeny.busoda.stopdetail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StopDetailScreen(stopId: String) {
    val viewModel: StopDetailViewModel = hiltViewModel()
    StopId(stopId)
}

@Composable
private fun StopId(
    stopId: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stopId,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Preview(showBackground = true)
@Composable
private fun StopIdPreview() {
    MaterialTheme { StopId("16206") }
}
