package com.tmix.education.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmix.education.R
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- Lightweight Particle Data ---
data class FloatingParticle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val color: Color
)

/**
 * Premium Splash Screen — Optimized
 * - 12 particles (was 30), 2 rings (was 3)
 * - 2000ms delay (was 3500ms)
 * - Smooth tween logo (was spring bounce)
 * - Dark mode + gradient background
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStudentDashboard: () -> Unit,
    onNavigateToParentDashboard: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val isDark = isSystemInDarkTheme()

    // ==========================================
    // 1. ANIMATION STATES (Reduced)
    // ==========================================
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // 2 rings only (removed 3rd outer ring — barely visible, wasted frames)
    val ringRotation1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "ring1"
    )
    val ringRotation2 by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)), label = "ring2"
    )

    // Particle time driver
    val continuousTime by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "time"
    )

    // Entrance Animations — smooth tween instead of spring bounce
    var componentsVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (componentsVisible) 1f else 0.5f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "logo_s"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (componentsVisible) 1f else 0f,
        animationSpec = tween(600, easing = LinearOutSlowInEasing),
        label = "logo_a"
    )
    val backgroundGlowScale by animateFloatAsState(
        targetValue = if (componentsVisible) 1.2f else 0.5f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bg_glow"
    )

    // Shimmer for text
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "shimmer"
    )

    // 12 particles instead of 30
    val particles = remember {
        List(12) {
            FloatingParticle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                color = if (Random.nextBoolean()) TMixRed.copy(alpha = 0.3f) else TMixNavy.copy(alpha = 0.3f)
            )
        }
    }

    // ==========================================
    // 2. LIFECYCLE — Faster (2000ms)
    // ==========================================
    LaunchedEffect(Unit) {
        delay(200)
        componentsVisible = true

        delay(2000) // Was 3500ms — much snappier

        if (authRepository.isLoggedIn()) {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                // Verify token is still valid by making a lightweight API call
                val isTokenValid = try {
                    val response = com.tmix.education.data.api.ApiConfig.getApiService().getDashboard()
                    response.isSuccessful
                } catch (e: Exception) {
                    false
                }
                
                if (isTokenValid) {
                    if (authRepository.isStudent()) {
                        onNavigateToStudentDashboard()
                    } else {
                        onNavigateToParentDashboard()
                    }
                } else {
                    // Token expired — interceptor already cleared data
                    onNavigateToLogin()
                }
            } else {
                onNavigateToLogin()
            }
        } else {
            onNavigateToLogin()
        }
    }

    // ==========================================
    // 3. UI — Premium Gradient Background
    // ==========================================
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0A1628), Color(0xFF0F1E33), Color(0xFF0A1628))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFF0F4FA), Color(0xFFF8FAFD), Color(0xFFEDF1F8))
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {

        // Canvas for rings + particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // A. Center Glow
            val glowColor = if (isDark) Color(0xFF1A2B45) else Color.White
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = canvasWidth * backgroundGlowScale * 0.9f
                ),
                radius = canvasWidth * backgroundGlowScale * 0.9f
            )

            // B. 2 Orbiting Rings
            val baseRadius = (canvasWidth * 0.35f) * logoScale
            val ringAlpha = if (isDark) 0.12f else 0.08f

            // Inner ring (dashed)
            rotate(ringRotation1, center) {
                drawCircle(
                    color = TMixNavy.copy(alpha = ringAlpha),
                    radius = baseRadius + 30f,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 30f))
                    )
                )
            }
            // Outer ring (solid thin)
            rotate(ringRotation2, center) {
                drawCircle(
                    color = TMixRed.copy(alpha = ringAlpha * 1.5f),
                    radius = baseRadius + 60f,
                    style = Stroke(width = 2f)
                )
            }

            // C. 12 Floating Particles
            particles.forEach { p ->
                val totalDistance = p.speed * continuousTime * 15f
                val movingY = (p.initialY * canvasHeight - totalDistance) % canvasHeight
                val finalY = if (movingY < 0) canvasHeight + movingY else movingY
                val finalX = p.initialX * canvasWidth

                val edgeFade = when {
                    finalY < 200f -> finalY / 200f
                    finalY > canvasHeight - 200f -> (canvasHeight - finalY) / 200f
                    else -> 1f
                }

                val orbColor = if (p.color == TMixRed.copy(alpha = 0.3f)) TMixRed else TMixNavy
                val particleAlpha = if (isDark) 0.2f else 0.12f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbColor.copy(alpha = particleAlpha * edgeFade), Color.Transparent),
                        center = Offset(finalX, finalY),
                        radius = p.radius * 6f
                    ),
                    radius = p.radius * 6f,
                    center = Offset(finalX, finalY)
                )
            }
        }

        // ============================
        // FOREGROUND — Logo + Text
        // ============================
        // Logo
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(150.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
                .background(
                    if (isDark) Color(0xFF1A2B45) else Color.White,
                    CircleShape
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_tmix),
                contentDescription = "TMIX Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Brand Text with shimmer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 140.dp)
                .alpha(logoAlpha)
        ) {
            Text(
                text = "TMIX",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else TMixNavy,
                letterSpacing = 4.sp
            )
            Text(
                text = "EDUCATION",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TMixRed.copy(alpha = 0.9f),
                letterSpacing = 10.sp
            )

            Spacer(Modifier.height(40.dp))

            // Dot pulse loader
            PremiumDotLoader(alpha = logoAlpha, isDark = isDark)
        }

        // Footer
        Text(
            text = "INNOVATION IN EDUCATION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = if (isDark) Color.White.copy(0.2f) else TMixNavy.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(logoAlpha)
        )
    }
}

/**
 * Premium Dot Pulse Loader — lightweight 3-dot animation
 */
@Composable
fun PremiumDotLoader(alpha: Float, isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.alpha(alpha)
    ) {
        (0..2).forEach { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = index * 150, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = index * 150, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
                label = "dotS_$index"
            )

            val color = when (index) {
                0 -> if (isDark) TMixNavyLight else TMixNavy
                1 -> TMixRed
                else -> if (isDark) TMixNavyLight else TMixNavy
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .background(color, CircleShape)
            )
        }
    }
}
