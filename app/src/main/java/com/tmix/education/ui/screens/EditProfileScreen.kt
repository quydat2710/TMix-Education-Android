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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.theme.*
import com.tmix.education.data.model.User
import com.tmix.education.ui.viewmodel.ProfileViewModel
import com.tmix.education.ui.viewmodel.ProfileUiState

/**
 * Edit Profile Screen
 * Connected to ProfileViewModel for real API calls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User? = null,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val currentUser = user ?: profileViewModel.getCurrentUser()
    
    var fullName by remember { mutableStateOf(currentUser?.name ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var dob by remember { mutableStateOf(currentUser?.dayOfBirth ?: "") }
    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    
    val updateState by profileViewModel.updateState.collectAsState()
    val isLoading = updateState is ProfileUiState.Loading
    val error = (updateState as? ProfileUiState.Error)?.message
    val successMessage = (updateState as? ProfileUiState.Success)?.message
    
    // Handle success
    LaunchedEffect(updateState) {
        if (updateState is ProfileUiState.Success) {
            kotlinx.coroutines.delay(1000) // Show success briefly
            profileViewModel.resetUpdateState()
            onSave()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin cá nhân") },
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
            
            // Form fields
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true,
                enabled = !isLoading
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
                singleLine = true,
                enabled = !isLoading
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
                singleLine = true,
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                maxLines = 2,
                enabled = !isLoading
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = {
                    profileViewModel.updateProfile(
                        name = fullName,
                        phone = phone,
                        dayOfBirth = dob,
                        address = address
                    )
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
