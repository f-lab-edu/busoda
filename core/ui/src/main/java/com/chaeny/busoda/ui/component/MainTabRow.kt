package com.chaeny.busoda.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chaeny.busoda.ui.R

@Composable
fun MainTabRow(
    selectedTab: MainTab,
    onHomeClick: () -> Unit,
    onNearbyStopsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab.index,
        modifier = modifier
            .padding(top = 12.dp)
            .padding(horizontal = 36.dp),
        divider = { }
    ) {
        Tab(
            selected = selectedTab == MainTab.HOME,
            onClick = onHomeClick,
            text = { Text(text = stringResource(R.string.tab_home)) }
        )
        Tab(
            selected = selectedTab == MainTab.NEARBY_STOPS,
            onClick = onNearbyStopsClick,
            text = { Text(text = stringResource(R.string.tab_nearby_stops)) }
        )
    }
}
