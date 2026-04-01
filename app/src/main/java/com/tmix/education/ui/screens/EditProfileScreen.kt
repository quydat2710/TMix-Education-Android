package com.tmix.education.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tmix.education.ui.theme.*
import com.tmix.education.data.model.User
import com.tmix.education.ui.viewmodel.ProfileViewModel
import com.tmix.education.ui.viewmodel.ProfileUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Edit Profile Screen
 * Connected to ProfileViewModel for real API calls
 * Features: Avatar picker, form fields, Material 3 DatePicker
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
    
    // Format DOB from ISO to dd/MM/yyyy if needed
    var dob by remember { 
        mutableStateOf(
            currentUser?.dayOfBirth?.let { dateStr ->
                try {
                    if (dateStr.contains("T")) {
                        val instant = Instant.parse(dateStr)
                        val configDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                        configDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } else {
                        dateStr
                    }
                } catch (e: Exception) {
                    dateStr
                }
            } ?: ""
        ) 
    }
    
    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    
    // Avatar state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val avatarUrl = currentUser?.avatar
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Image picker launcher — triggers Cloudinary upload on selection
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Trigger full upload flow: pick → POST /files → PATCH /user/avatar
            profileViewModel.uploadAvatar(context.contentResolver, it)
        }
    }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            val parts = dob.split("/", "-")
            if (parts.size == 3) {
                LocalDate.of(
                    parts[2].toInt(),
                    parts[1].toInt(),
                    parts[0].toInt()
                ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else null
        } catch (_: Exception) { null }
    )
    
    val updateState by profileViewModel.updateState.collectAsState()
    val avatarState by profileViewModel.avatarState.collectAsState()
    val isLoading = updateState is ProfileUiState.Loading || avatarState is ProfileUiState.Loading
    val error = (updateState as? ProfileUiState.Error)?.message ?: (avatarState as? ProfileUiState.Error)?.message
    val successMessage = (updateState as? ProfileUiState.Success)?.message ?: (avatarState as? ProfileUiState.Success)?.message
    
    // Handle success
    LaunchedEffect(updateState) {
        if (updateState is ProfileUiState.Success) {
            kotlinx.coroutines.delay(1000)
            profileViewModel.resetUpdateState()
            onSave()
        }
    }
    
    // Handle avatar upload success
    LaunchedEffect(avatarState) {
        if (avatarState is ProfileUiState.Success) {
            kotlinx.coroutines.delay(2000)
            profileViewModel.resetAvatarState()
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        dob = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
            // Avatar section — clickable with real image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    // Sử dụng màu nền xám nhạt phẳng giống như trong mockup thay vì Card có đổ bóng
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(100.dp)) {
                        // Ảnh đại diện
                        when {
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Ảnh đã chọn",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            !avatarUrl.isNullOrBlank() -> {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Ảnh đại diện",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Box(
                                    Modifier.fillMaxSize().clip(CircleShape).background(TMixNavy.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        (currentUser?.name ?: "?").split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TMixNavy
                                    )
                                }
                            }
                        }
                        
                        // Nút hình máy ảnh đỏ góc dưới phải (Red Camera Badge)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface) // viền trắng tạo khoảng cách
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(TMixRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                contentDescription = "Đổi ảnh",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Nhấn để thay đổi ảnh đại diện", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            
            // Date of Birth with DatePicker
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Ngày sinh") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                trailingIcon = { 
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, null, tint = TMixRed)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = TMixShapes.TextField,
                singleLine = true,
                enabled = !isLoading,
                readOnly = true
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
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Lưu thay đổi", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
