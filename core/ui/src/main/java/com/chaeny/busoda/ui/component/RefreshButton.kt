package com.chaeny.busoda.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chaeny.busoda.ui.R
import com.chaeny.busoda.ui.theme.DarkGreen

@Composable
fun RefreshButton(
    rotation: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ),
        label = "refreshButtonRotation"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = DarkGreen,
        shape = CircleShape,
        modifier = modifier
            .padding(25.dp)
            .graphicsLayer {
                rotationZ = animRotation
            }
    ) {
        Icon(
            imageVector = Icons.Filled.Autorenew,
            contentDescription = stringResource(R.string.refresh),
            tint = Color.Black
        )
    }
}
