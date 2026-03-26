package com.tmix.education.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

// --- Simple Particle Data Class ---
data class FloatingParticle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val color: Color
)

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStudentDashboard: () -> Unit,
    onNavigateToParentDashboard: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    // ==========================================
    // 1. ANIMATION STATES
    // ==========================================
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    // Continuous Rotation for Tech Rings
    val ringRotation1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "ring1"
    )
    val ringRotation2 by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)), label = "ring2"
    )
    val ringRotation3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(35000, easing = LinearEasing)), label = "ring3"
    )

    // Continuous Particle Time driver
    val continuousTime by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "time"
    )

    // Entrance Animations
    var componentsVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (componentsVisible) 1f else 0.2f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "logo_s"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (componentsVisible) 1f else 0f,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing),
        label = "logo_a"
    )
    val backgroundGlowScale by animateFloatAsState(
        targetValue = if (componentsVisible) 1.2f else 0.5f,
        animationSpec = tween(2500, easing = FastOutSlowInEasing),
        label = "bg_glow"
    )

    // Generate Particles Once
    val particles = remember {
        List(30) {
            FloatingParticle(
                initialX = Random.nextFloat(), // 0.0 to 1.0 (relative to canvas)
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                color = if (Random.nextBoolean()) TMixRed.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f)
            )
        }
    }

    // ==========================================
    // 2. LIFECYCLE & LOGIC
    // ==========================================
    LaunchedEffect(Unit) {
        delay(300)
        componentsVisible = true
        
        delay(3500) 
        
        if (authRepository.isLoggedIn()) {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                if (authRepository.isStudent()) {
                    onNavigateToStudentDashboard()
                } else {
                    onNavigateToParentDashboard()
                }
            } else {
                onNavigateToLogin()
            }
        } else {
            onNavigateToLogin()
        }
    }

    // ==========================================
    // 3. UI RENDERING
    // ==========================================
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF071221))) { // Ultra deep navy

        // Canvas for Advanced Draw Effects (Particles & Rings)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // A. Draw Massive Center Glow Behind Logo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TMixNavy.copy(alpha = 0.9f), Color.Transparent),
                    center = center,
                    radius = canvasWidth * backgroundGlowScale
                ),
                radius = canvasWidth * backgroundGlowScale
            )

            // B. Draw Futuristic Orbiting Rings
            val baseRadius = (canvasWidth * 0.35f) * logoScale
            
            // Inner Red ring (dashed)
            rotate(ringRotation1, center) {
                drawCircle(
                    color = TMixRed.copy(alpha = 0.5f),
                    radius = baseRadius + 30f,
                    style = Stroke(
                        width = 4f, 
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 30f))
                    )
                )
            }
            // Middle White ring (solid thin)
            rotate(ringRotation2, center) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = baseRadius + 55f,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 20f))
                    )
                )
            }
            // Outer Navy ring (glowy dashed)
            rotate(ringRotation3, center) {
                drawCircle(
                    color = TMixNavyLight.copy(alpha = 0.4f),
                    radius = baseRadius + 90f,
                    style = Stroke(
                        width = 8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(100f, 80f))
                    )
                )
            }

            // C. Draw Floating Particles
            particles.forEach { p ->
                // Calculate moving Y relative to time, wrapping around screen
                val totalDistance = p.speed * continuousTime * 20f
                val movingY = (p.initialY * canvasHeight - totalDistance) % canvasHeight
                val finalY = if (movingY < 0) canvasHeight + movingY else movingY
                val finalX = p.initialX * canvasWidth
                
                // Opacity fades near edges
                val edgeFade = if (finalY < 200f) finalY / 200f else if (finalY > canvasHeight - 200f) (canvasHeight - finalY) / 200f else 1f
                
                drawCircle(
                    color = p.color.copy(alpha = p.color.alpha * edgeFade),
                    radius = p.radius,
                    center = Offset(finalX, finalY)
                )
            }
        }

        // ============================
        // CENTRALLY FIXED FOREGROUND
        // ============================
        // 1. Logo perfectly aligned with Canvas Center
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp) // Premium size
                .scale(logoScale)
                .alpha(logoAlpha)
                // Multi-layer container to hide white square
                .background(Color.White.copy(0.15f), CircleShape) // Subtle outer ring
                .padding(4.dp) // Inner gap
                .background(Color.White, CircleShape) // PURE WHITE inner to blend logo flawlessly!
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_tmix),
                contentDescription = "TMIX Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 2. Text offset downwards from Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                // Pushes text below logo (80dp logo half + 50dp spacing)
                .offset(y = 150.dp + ((1f - logoAlpha) * 40f).dp) 
                .alpha(logoAlpha)
        ) {
            Text(
                text = "TMIX",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TMixRed,
                letterSpacing = 4.sp
            )
            Text(
                text = "EDUCATION",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 12.sp // Premium cinematic wide tracking
            )
            
            Spacer(Modifier.height(50.dp))
            
            PremiumTripleLoader(alpha = logoAlpha)
        }
        
        // Premium Footer
        Text(
            text = "INNOVATION IN EDUCATION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(logoAlpha)
        )
    }
}

/**
 * Ultra-slick continuous geometric loader.
 */
@Composable
fun PremiumTripleLoader(alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    
    val phase1 by infiniteTransition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(800, easing = LinearOutSlowInEasing), RepeatMode.Reverse), label = "l1"
    )
    val phase2 by infiniteTransition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(800, easing = LinearOutSlowInEasing, delayMillis = 200), RepeatMode.Reverse), label = "l2"
    )
    val phase3 by infiniteTransition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(800, easing = LinearOutSlowInEasing, delayMillis = 400), RepeatMode.Reverse), label = "l3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.alpha(alpha)
    ) {
        val phases = listOf(phase1, phase2, phase3)
        phases.forEachIndexed { index, phase ->
            val color = if (index == 1) TMixRed else TMixNavyLight
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .scale(scaleX = 0.5f + (0.5f * phase), scaleY = 1f)
                    .background(color.copy(alpha = 0.3f + (0.7f * phase)), CircleShape)
            )
        }
    }
}
