package com.chaeny.busoda.stoplist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chaeny.busoda.ui.theme.Gray40

@Composable
fun StopListScreen() {
    val viewModel: StopListViewModel = hiltViewModel()
    SearchBarContent(viewModel)
}

@Composable
private fun SearchBarContent(viewModel: StopListViewModel) {
    var keyword by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    SearchBar(
        keyword,
        onKeywordChange = {
            keyword = it.copy(selection = TextRange(it.text.length))
            viewModel.setKeyWord(it.text)
        }
    )
}

@Composable
private fun SearchBar(
    keyword: TextFieldValue,
    onKeywordChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    TextField(
        value = keyword,
        onValueChange = onKeywordChange,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Gray40
        ),
        placeholder = {
            Text(
                stringResource(R.string.stop_search), color = Color.Gray
            )
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
            .padding(top = 20.dp)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        keyword = TextFieldValue(""),
        onKeywordChange = {}
    )
}
