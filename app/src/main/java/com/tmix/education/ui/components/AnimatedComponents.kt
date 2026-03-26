package com.tmix.education.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
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
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Box(modifier = modifier.size(40.dp).padding(8.dp), contentAlignment = Alignment.Center) {
        if (isRefreshing) {
            CircularProgressIndicator(Modifier.size(24.dp), color = TMixRed, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.Refresh, null,
                Modifier.size(24.dp).rotate(progress * 360f),
                tint = if (progress >= 1f) TMixRed else TMixNavy.copy(alpha = progress))
        }
    }
}

/**
 * Animated Card Entrance
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(300, delayMillis = delay), label = "alpha"
    )
    val offsetY by animateFloatAsState(
        if (visible) 0f else 20f, tween(300, delayMillis = delay), label = "offsetY"
    )
    LaunchedEffect(Unit) { visible = true }
    Box(modifier = modifier.offset(y = offsetY.dp)) {
        if (alpha > 0f) content()
    }
}

/**
 * Pulsating Dot - for notification indicators
 */
@Composable
fun PulsatingDot(modifier: Modifier = Modifier, color: Color = TMixRed, size: Float = 8f) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        1f, 1.3f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "pulse_scale"
    )
    Canvas(modifier = modifier.size(size.dp)) {
        drawCircle(color.copy(0.3f), radius = size / 2 * scale * 1.5f)
        drawCircle(color, radius = size / 2)
    }
}

/**
 * Loading Button
 */
@Composable
fun LoadingButton(
    onClick: () -> Unit, modifier: Modifier = Modifier,
    isLoading: Boolean = false, text: String, enabled: Boolean = true
) {
    Button(onClick, modifier.height(52.dp), shape = TMixShapes.Button,
        colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), Color.White, 2.dp)
        else Text(text)
    }
}

/**
 * Animated Counter
 */
@Composable
fun AnimatedCounter(targetValue: Int, modifier: Modifier = Modifier) {
    var oldValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(targetValue, tween(1000), label = "counter")
    LaunchedEffect(targetValue) { oldValue = targetValue }
    Text(animatedValue.toString(), modifier = modifier)
}

// ======================================================
// ENHANCED ANIMATION HELPERS (duplicates removed)
// ======================================================

/**
 * Staggered slide-in from bottom.
 * Use `index` to create cascading entrance delays.
 */
@Composable
fun SlideInFromBottom(
    index: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val delayMs = index * 60

    val alpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(350, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "slide_alpha_$index"
    )
    val offsetY by animateFloatAsState(
        if (visible) 0f else 24f,
        tween(350, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "slide_offset_$index"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.graphicsLayer { this.alpha = alpha; translationY = offsetY }) {
        content()
    }
}

/**
 * Fade in with slight scale bounce.
 */
@Composable
fun FadeInScale(
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(300, delayMillis = delay), label = "fade_alpha"
    )
    val scale by animateFloatAsState(
        if (visible) 1f else 0.92f,
        spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "fade_scale"
    )

    LaunchedEffect(Unit) { delay(delay.toLong()); visible = true }

    Box(Modifier.graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }) {
        content()
    }
}

/**
 * Shimmer loading placeholder box.
 */
@Composable
fun ShimmerBox(
    width: Dp = 100.dp,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offsetX by transition.animateFloat(
        -300f, 300f,
        infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmer_x"
    )

    Box(
        Modifier
            .width(width).height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX + 200f, 0f)
                )
            )
    )
}

/**
 * Animated progress ring (for scores/attendance)
 */
@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 5.dp,
    color: Color = TMixRed,
    trackColor: Color = SurfaceVariant,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        progress.coerceIn(0f, 1f), tween(1000, easing = FastOutSlowInEasing), label = "ring"
    )

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(trackColor, 0f, 360f, false, style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round))
            drawArc(color, -90f, 360f * animatedProgress, false,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round))
        }
        content()
    }
}

/**
 * Gradient border ring for avatars
 */
@Composable
fun GradientBorderRing(
    size: Dp = 72.dp,
    borderWidth: Dp = 3.dp,
    gradientColors: List<Color> = listOf(TMixNavy, TMixRed, TMixNavy),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_ring")
    val angle by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "ring_angle"
    )

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.sweepGradient(gradientColors),
                radius = (size / 2).toPx(),
                style = Stroke(borderWidth.toPx())
            )
        }
        Box(
            Modifier.size(size - borderWidth * 2).clip(androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
