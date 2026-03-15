package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Forgot Password Screen
 * Step 1: Enter email to receive reset code
 * Step 2: Enter code + new password to reset
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val apiService = remember { ApiConfig.getApiService() }
    val scope = rememberCoroutineScope()
    
    var currentStep by remember { mutableIntStateOf(1) } // 1 = enter email, 2 = enter code + password
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quên mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(step = 1, currentStep = currentStep, label = "Nhập email")
                Box(
                    Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(if (currentStep >= 2) TMixRed else TextSecondary.copy(0.3f))
                )
                StepIndicator(step = 2, currentStep = currentStep, label = "Đặt lại")
            }
            
            Spacer(Modifier.height(32.dp))
            
            if (currentStep == 1) {
                // Step 1: Enter email
                Icon(
                    Icons.Default.MarkEmailRead, null,
                    Modifier.size(80.dp),
                    tint = TMixNavy
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Nhập email đã đăng ký",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TMixNavy
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Chúng tôi sẽ gửi mã xác nhận đến email của bạn để đặt lại mật khẩu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = TMixShapes.TextField,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    enabled = !isLoading
                )
                
                // Error / Success
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Error, style = MaterialTheme.typography.bodySmall)
                }
                successMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Success, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            error = "Vui lòng nhập email"
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                val response = apiService.sendPasswordResetRequest(mapOf("email" to email))
                                if (response.isSuccessful) {
                                    successMessage = "Đã gửi mã xác nhận đến email!"
                                    currentStep = 2
                                } else {
                                    error = "Email không tồn tại trong hệ thống"
                                }
                            } catch (e: Exception) {
                                error = "Lỗi kết nối: ${e.message}"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = TMixShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                    enabled = email.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Gửi mã xác nhận", fontWeight = FontWeight.SemiBold)
                    }
                }
                
            } else {
                // Step 2: Enter OTP + new password
                Icon(
                    Icons.Default.LockReset, null,
                    Modifier.size(80.dp),
                    tint = TMixNavy
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Đặt lại mật khẩu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TMixNavy
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Nhập mã xác nhận đã gửi đến $email và mật khẩu mới.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it; error = null },
                    label = { Text("Mã xác nhận") },
                    leadingIcon = { Icon(Icons.Default.Pin, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = TMixShapes.TextField,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("Mật khẩu mới") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = TMixShapes.TextField,
                    singleLine = true,
                    isError = newPassword.isNotEmpty() && newPassword.length < 8,
                    supportingText = if (newPassword.isNotEmpty() && newPassword.length < 8) {
                        { Text("Mật khẩu phải có ít nhất 8 ký tự") }
                    } else null,
                    enabled = !isLoading
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Xác nhận mật khẩu") },
                    leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = TMixShapes.TextField,
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                    supportingText = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                        { Text("Mật khẩu không khớp") }
                    } else null,
                    enabled = !isLoading
                )
                
                // Error / Success
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Error, style = MaterialTheme.typography.bodySmall)
                }
                successMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Success, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(Modifier.height(24.dp))
                
                val canSubmit = otp.isNotBlank() && newPassword.length >= 8 && newPassword == confirmPassword
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            successMessage = null
                            try {
                                val response = apiService.resetPassword(mapOf(
                                    "email" to email,
                                    "otp" to otp,
                                    "password" to newPassword,
                                    "confirmPassword" to confirmPassword
                                ))
                                if (response.isSuccessful) {
                                    successMessage = "Đặt lại mật khẩu thành công!"
                                    kotlinx.coroutines.delay(1500)
                                    onSuccess()
                                } else {
                                    error = "Mã xác nhận không đúng hoặc đã hết hạn"
                                }
                            } catch (e: Exception) {
                                error = "Lỗi kết nối: ${e.message}"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = TMixShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                    enabled = canSubmit && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Đặt lại mật khẩu", fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                TextButton(onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            apiService.sendPasswordResetRequest(mapOf("email" to email))
                            successMessage = "Đã gửi lại mã xác nhận!"
                        } catch (_: Exception) {}
                        isLoading = false
                    }
                }, enabled = !isLoading) {
                    Text("Gửi lại mã xác nhận", color = TMixNavy)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, currentStep: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = if (currentStep >= step) TMixRed else TextSecondary.copy(0.3f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (currentStep > step) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("$step", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (currentStep >= step) TMixNavy else TextSecondary)
    }
}
