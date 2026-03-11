package com.tmix.education.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Pull to Refresh State
 */
class PullRefreshState(
    val isRefreshing: Boolean,
    val progress: Float
)

/**
 * Custom Refresh Indicator
 */
@Composable
fun RefreshIndicator(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    progress: Float
) {
    val rotation by rememberInfiniteTransition(label = "refresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TMixRed,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(progress * 360f),
                tint = if (progress >= 1f) TMixRed else TMixNavy.copy(alpha = progress)
            )
        }
    }
}

/**
 * Animated Card Entrance
 * Use this to animate cards when they appear on screen
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300, delayMillis = delay),
        label = "alpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(300, delayMillis = delay),
        label = "offsetY"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .then(Modifier.run {
                if (alpha < 1f) this.fillMaxWidth() else this
            })
    ) {
        if (alpha > 0f) {
            content()
        }
    }
}

/**
 * Pulsating Dot - for notification indicators
 */
@Composable
fun PulsatingDot(
    modifier: Modifier = Modifier,
    color: Color = TMixRed,
    size: Float = 8f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Canvas(modifier = modifier.size(size.dp)) {
        val radius = size / 2 * scale
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius * 1.5f
        )
        drawCircle(
            color = color,
            radius = size / 2
        )
    }
}

/**
 * Loading Button
 */
@Composable
fun LoadingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    text: String,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = TMixShapes.Button,
        colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}

/**
 * Animated Counter - for stats that change
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier
) {
    var oldValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(1000),
        label = "counter"
    )
    
    LaunchedEffect(targetValue) {
        oldValue = targetValue
    }
    
    Text(
        text = animatedValue.toString(),
        modifier = modifier
    )
}
