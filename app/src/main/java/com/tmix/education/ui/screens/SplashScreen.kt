package com.tmix.education.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmix.education.R
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Splash Screen with animated logo
 * Checks auth state: if already logged in, navigates directly to dashboard
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStudentDashboard: () -> Unit,
    onNavigateToParentDashboard: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Scale animation for logo
    var logoVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "logo_alpha"
    )
    
    // Text animation
    var textVisible by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "text_alpha"
    )
    
    // Loading dots animation
    val dots by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )
    
    LaunchedEffect(Unit) {
        logoVisible = true
        delay(500)
        textVisible = true
        delay(1500)
        
        // Check auth state - if logged in, go directly to dashboard
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TMixNavy, TMixNavyDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_tmix),
                contentDescription = "TMIX Education",
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )
            
            Spacer(Modifier.height(24.dp))
            
            // App name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "TMIX",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TMixRed
                )
                Text(
                    text = "EDUCATION",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            // Loading indicator
            Text(
                text = "Đang tải" + ".".repeat(dots.toInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.alpha(textAlpha)
            )
        }
        
        // Footer
        Text(
            text = "© 2026 TMIX Education",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(textAlpha)
        )
    }
}
