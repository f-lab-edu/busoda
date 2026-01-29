package com.chaeny.busoda.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.chaeny.busoda.ui.R

@Composable
fun MainSearchBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = TextFieldValue(""),
        onValueChange = {},
        enabled = false,
        colors = TextFieldDefaults.colors(
            disabledContainerColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = stringResource(R.string.stop_search),
                color = Color.Gray
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
            .padding(top = 10.dp)
            .clickable { onSearchClick() }
    )
}
