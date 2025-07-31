package com.chaeny.busoda.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesScreen(
    onSearchBarClick: () -> Unit
) {
    Column {
        SearchBar(onClick = onSearchBarClick)
        FavoritesGuide()
    }
}

@Composable
private fun SearchBar(
    onClick: () -> Unit,
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
            .padding(top = 20.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun FavoritesGuide(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.guide),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    SearchBar(onClick = {})
}

@Preview(showBackground = true)
@Composable
private fun FavoritesGuidePreview() {
    FavoritesGuide()
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    FavoritesScreen(onSearchBarClick = {})
}
