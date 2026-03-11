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
import com.tmix.education.ui.theme.*

/**
 * Change Password Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val isValid = currentPassword.isNotEmpty() && 
                  newPassword.length >= 8 && 
                  newPassword == confirmPassword
    
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
            
            Spacer(Modifier.height(8.dp))
            
            // Current password
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; error = null },
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
                singleLine = true
            )
            
            // New password
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; error = null },
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
                } else null
            )
            
            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; error = null },
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
                } else null
            )
            
            // Error message
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
                    isLoading = true
                    // Simulate API call
                    onSuccess()
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
