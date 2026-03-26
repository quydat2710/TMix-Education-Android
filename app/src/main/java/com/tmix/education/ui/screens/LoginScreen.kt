package com.tmix.education.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.LoginViewModel
import com.tmix.education.ui.viewmodel.LoginUiState

/**
 * Login Screen with Email/Password
 * Premium design with TMIX branding
 * Now integrated with LoginViewModel for real API calls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (isStudent: Boolean) -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val loginState by viewModel.loginState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Subtle entrance animation
    var visible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        if (visible) 1f else 0.85f,
        spring(dampingRatio = 0.6f, stiffness = 200f), label = "logo_s"
    )
    val logoAlpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(500), label = "logo_a"
    )
    val cardAlpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(400, delayMillis = 200), label = "card_a"
    )
    val cardOffsetY by animateFloatAsState(
        if (visible) 0f else 30f, tween(400, delayMillis = 200, easing = FastOutSlowInEasing), label = "card_y"
    )

    // Shake on error
    var triggerShake by remember { mutableStateOf(false) }
    val shakeX by animateFloatAsState(
        if (triggerShake) 1f else 0f,
        if (triggerShake) spring(Spring.DampingRatioHighBouncy, Spring.StiffnessHigh) else tween(0),
        label = "shake",
        finishedListener = { triggerShake = false }
    )
    val shakeTranslation = if (triggerShake) kotlin.math.sin(shakeX.toDouble() * Math.PI * 6).toFloat() * 10f else 0f

    LaunchedEffect(Unit) { visible = true }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginUiState.Success -> onLoginSuccess(viewModel.isStudent())
            is LoginUiState.Error -> triggerShake = true
            else -> {}
        }
    }
    
    val isLoading = loginState is LoginUiState.Loading
    val error = (loginState as? LoginUiState.Error)?.message
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TMixNavy, TMixNavyDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // Logo Text
            Text(
                text = "TMIX",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TMixRed,
                modifier = Modifier.scale(logoScale).alpha(logoAlpha)
            )
            Text(
                text = "EDUCATION",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                letterSpacing = 4.sp,
                modifier = Modifier.scale(logoScale).alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth().graphicsLayer {
                    alpha = cardAlpha
                    translationY = cardOffsetY
                    translationX = shakeTranslation
                },
                shape = TMixShapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Dành cho Học sinh & Phụ huynh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            if (loginState is LoginUiState.Error) {
                                viewModel.resetState()
                            }
                        },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TMixShapes.TextField,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (loginState is LoginUiState.Error) {
                                viewModel.resetState()
                            }
                        },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TMixShapes.TextField,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(email, password)
                            }
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    // Error
                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it, 
                            color = Error, 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Login Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = TMixShapes.Button,
                        colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), 
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Đăng nhập", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Forgot password link
                    TextButton(
                        onClick = onForgotPassword,
                        enabled = !isLoading
                    ) {
                        Text(
                            "Quên mật khẩu?",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Biometric (placeholder - can be implemented later)
                    OutlinedButton(
                        onClick = { /* TODO: Implement biometric auth */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = TMixShapes.Button,
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Fingerprint, null, Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Đăng nhập bằng vân tay")
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "© 2026 TMIX Education",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
