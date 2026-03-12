package com.chaeny.busoda.stopdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
internal fun Dp.toSp(): TextUnit = with(LocalDensity.current) { toSp() }
