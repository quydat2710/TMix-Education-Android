package com.tmix.education.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ProfileViewModel
import com.tmix.education.ui.viewmodel.ProfileUiState

/**
 * Change Password Screen
 * Connected to ProfileViewModel for real API call (PATCH /auth/change-password)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    val changePasswordState by profileViewModel.changePasswordState.collectAsState()
    val isLoading = changePasswordState is ProfileUiState.Loading
    val error = (changePasswordState as? ProfileUiState.Error)?.message
    val successMessage = (changePasswordState as? ProfileUiState.Success)?.message
    
    val isValid = currentPassword.isNotEmpty() && 
                  newPassword.length >= 8 && 
                  newPassword == confirmPassword
    
    // Handle success
    LaunchedEffect(changePasswordState) {
        if (changePasswordState is ProfileUiState.Success) {
            kotlinx.coroutines.delay(1500)
            profileViewModel.resetChangePasswordState()
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                shape = TMixShapes.Card,
                colors = CardDefaults.cardColors(containerColor = InfoLight)
            ) {
                Row(Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, null, tint = Info)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Info
                    )
                }
            }
            
            // Success message
            successMessage?.let {
                Card(
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = SuccessLight)
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Success)
                        Spacer(Modifier.width(12.dp))
                        Text(it, color = Success)
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Current password
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { 
                    currentPassword = it
                    if (changePasswordState is ProfileUiState.Error) {
                        profileViewModel.resetChangePasswordState()
                    }
                },
                label = { Text("Mật khẩu hiện tại") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true,
                enabled = !isLoading
            )
            
            // New password
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    if (changePasswordState is ProfileUiState.Error) {
                        profileViewModel.resetChangePasswordState()
                    }
                },
                label = { Text("Mật khẩu mới") },
                leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true,
                isError = newPassword.isNotEmpty() && newPassword.length < 8,
                supportingText = if (newPassword.isNotEmpty() && newPassword.length < 8) {
                    { Text("Mật khẩu phải có ít nhất 8 ký tự", color = Error) }
                } else null,
                enabled = !isLoading
            )
            
            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    if (changePasswordState is ProfileUiState.Error) {
                        profileViewModel.resetChangePasswordState()
                    }
                },
                label = { Text("Xác nhận mật khẩu mới") },
                leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                supportingText = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                    { Text("Mật khẩu không khớp", color = Error) }
                } else null,
                enabled = !isLoading
            )
            
            // Error message from API
            error?.let {
                Card(
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = ErrorLight)
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(Modifier.width(12.dp))
                        Text(it, color = Error)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = {
                    profileViewModel.changePassword(
                        oldPassword = currentPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = TMixShapes.Button,
                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                enabled = isValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text("Đổi mật khẩu")
                }
            }
        }
    }
}
