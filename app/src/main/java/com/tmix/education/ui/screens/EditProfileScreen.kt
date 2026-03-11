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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tmix.education.ui.theme.*
import com.tmix.education.data.model.User

/**
 * Edit Profile Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User? = null,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var dob by remember { mutableStateOf(user?.dayOfBirth ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isLoading = true
                            // Simulate save
                            onSave()
                        },
                        enabled = !isLoading
                    ) {
                        Text("Lưu", color = TMixRed)
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
            // Avatar section
            Card(shape = TMixShapes.Card) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(64.dp), tint = TMixNavy)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Ảnh đại diện", style = MaterialTheme.typography.titleSmall)
                            Text("Nhấn để thay đổi", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CameraAlt, null, tint = TMixRed)
                    }
                }
            }
            
            // Form fields
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true,
                enabled = false // Email usually can't be changed
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Số điện thoại") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true
            )
            
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Ngày sinh") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                trailingIcon = { 
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.DateRange, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                maxLines = 2
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    onSave()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = TMixShapes.Button,
                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text("Lưu thay đổi", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
