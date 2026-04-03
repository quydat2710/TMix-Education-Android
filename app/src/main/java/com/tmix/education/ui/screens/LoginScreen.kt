package com.tmix.education.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.tmix.education.R
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
        if (visible) 0f else 30f,
        tween(400, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "card_y"
    )

    // Shake on error
    var triggerShake by remember { mutableStateOf(false) }
    val shakeX by animateFloatAsState(
        if (triggerShake) 1f else 0f,
        if (triggerShake) spring(Spring.DampingRatioHighBouncy, Spring.StiffnessHigh) else tween(0),
        label = "shake",
        finishedListener = { triggerShake = false }
    )
    val shakeTranslation =
        if (triggerShake) kotlin.math.sin(shakeX.toDouble() * Math.PI * 6).toFloat() * 10f else 0f

    LaunchedEffect(Unit) { visible = true }

    // Success state
    var showSuccess by remember { mutableStateOf(false) }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginUiState.Success -> {
                showSuccess = true
                kotlinx.coroutines.delay(1000)
                onLoginSuccess(viewModel.isStudent())
            }

            is LoginUiState.Error -> triggerShake = true
            else -> {}
        }
    }

    val isLoading = loginState is LoginUiState.Loading
    val error = (loginState as? LoginUiState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TMixNavyDark) // Base background applies everywhere
    ) {
        // Premium Aurora / Glowing Background
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Base Deep Navy Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(TMixNavy, TMixNavyDark)
                )
            )

            // 2. Abstract Glowing Orbs (Aurora Effect)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.15f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.1f),
                    radius = w * 0.6f
                ),
                center = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.1f),
                radius = w * 0.6f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TMixRed.copy(alpha = 0.12f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.35f),
                    radius = w * 0.6f
                ),
                center = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.35f),
                radius = w * 0.6f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TMixNavyLight.copy(alpha = 0.4f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.6f),
                    radius = w * 0.6f
                ),
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.6f),
                radius = w * 0.6f
            )

            // 3. Elegant Subtle Curves (Replaces dot matrix)
            val path1 = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h * 0.15f)
                cubicTo(w * 0.4f, h * 0.25f, w * 0.6f, h * 0.05f, w, h * 0.2f)
            }
            drawPath(
                path = path1,
                color = Color.White.copy(alpha = 0.04f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            val path2 = androidx.compose.ui.graphics.Path().apply {
                moveTo(-w * 0.2f, h * 0.3f)
                cubicTo(w * 0.3f, h * 0.4f, w * 0.8f, h * 0.15f, w * 1.2f, h * 0.35f)
            }
            drawPath(
                path = path2,
                color = Color.White.copy(alpha = 0.03f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
            )
        }

        // Main Content Layer (Form & Branding)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // Adjust for keyboard automatically
        ) {
            // Nửa trên (Branding)
            Box(
                modifier = Modifier
                    .weight(1f) // Takes remaining space above card
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Logo & Text Branding
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .padding(vertical = 24.dp)
                ) {
                    // Elevated Logo Container
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        shadowElevation = 12.dp // Premium depth, keeping pure white
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_tmix),
                                contentDescription = "TMIX Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "T M I X   E D U C A T I O N",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )
                }
            }

            // Nửa dưới (Login Form) - Bottom Surface
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = cardAlpha
                        translationY = cardOffsetY
                        translationX = shakeTranslation
                    },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 0.dp // Flat modern design without harsh shadows
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 24.dp, start = 32.dp, end = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TMixNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Vui lòng đăng nhập để tiếp tục",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray // Gray text
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Form Container
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Email Field (Outlined Style — like Forgot Password)
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (loginState is LoginUiState.Error) {
                                    viewModel.resetState()
                                }
                            },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email", tint = Color(0xFF9CA3AF)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TMixNavy,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = TMixNavy,
                                unfocusedLabelColor = Color(0xFF9CA3AF),
                                cursorColor = TMixNavy,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            ),
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

                        // Password Field (Outlined Style)
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (loginState is LoginUiState.Error) {
                                    viewModel.resetState()
                                }
                            },
                            label = { Text("Mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, "Lock", tint = Color(0xFF9CA3AF)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        "Toggle Password",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TMixNavy,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = TMixNavy,
                                unfocusedLabelColor = Color(0xFF9CA3AF),
                                cursorColor = TMixNavy,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            ),
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
                    }

                    // Forgot password link right aligned below the field
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = onForgotPassword,
                            enabled = !isLoading,
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 8.dp,
                                start = 8.dp,
                                end = 0.dp
                            )
                        ) {
                            Text(
                                "Quên mật khẩu?",
                                color = TMixNavy,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Animated Error Card
                    AnimatedVisibility(
                        visible = error != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    error ?: "",
                                    color = Error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Animated Success Card
                    AnimatedVisibility(
                        visible = showSuccess,
                        enter = slideInVertically() + fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = Success,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Đăng nhập thành công!",
                                    color = Success,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button — Gradient Premium
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        enabled = !isLoading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(TMixRed, Color(0xFFE8475F))
                                    ),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Đăng nhập",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White.copy(0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // -- SOCIAL LOGIN DIVIDER --
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE8ECF0))
                        Text(
                            "  hoặc  ",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE8ECF0))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Social Buttons — Wide Pill Style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Facebook Pill
                        OutlinedButton(
                            onClick = { /* TODO: Facebook */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8F9FB)
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_facebook),
                                contentDescription = "Facebook",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Facebook",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                        }

                        // Google Pill
                        OutlinedButton(
                            onClick = { /* TODO: Google */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8F9FB)
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Google",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}