package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tmix.education.data.model.ClassInfo
import com.tmix.education.data.model.CreateRegistrationRequest
import com.tmix.education.data.repository.ClassRepository
import com.tmix.education.data.repository.RegistrationRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Course Registration Screen
 * Form for registering to a specific course/class
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseRegistrationScreen(
    classId: String,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val classRepository = remember { ClassRepository() }
    val registrationRepository = remember { RegistrationRepository() }
    
    var classInfo by remember { mutableStateOf<ClassInfo?>(null) }
    var isLoadingClass by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Form fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    // Validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Load class info
    LaunchedEffect(classId) {
        isLoadingClass = true
        val result = classRepository.getClass(classId)
        result.onSuccess { data ->
            classInfo = data
        }
        isLoadingClass = false
    }
    
    // Validation function
    fun validate(): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "Vui lòng nhập họ tên"
            isValid = false
        } else {
            nameError = null
        }
        
        if (email.isBlank()) {
            emailError = "Vui lòng nhập email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email không hợp lệ"
            isValid = false
        } else {
            emailError = null
        }
        
        if (phone.isBlank()) {
            phoneError = "Vui lòng nhập số điện thoại"
            isValid = false
        } else if (phone.length < 9) {
            phoneError = "Số điện thoại không hợp lệ"
            isValid = false
        } else {
            phoneError = null
        }
        
        return isValid
    }
    
    // Submit registration
    fun submit() {
        if (!validate()) return
        
        scope.launch {
            isSubmitting = true
            errorMessage = null
            
            val request = CreateRegistrationRequest(
                email = email.trim(),
                name = name.trim(),
                phone = phone.trim(),
                gender = gender,
                address = address.trim(),
                note = note.trim(),
                classId = classId
            )
            
            val result = registrationRepository.submitRegistration(request)
            result.onSuccess {
                showSuccessDialog = true
            }.onFailure { e ->
                errorMessage = e.message ?: "Đăng ký thất bại. Vui lòng thử lại."
            }
            
            isSubmitting = false
        }
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    Modifier.size(48.dp),
                    tint = Success
                )
            },
            title = { Text("Đăng ký thành công!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Chúng tôi đã nhận được đăng ký của bạn cho lớp ${classInfo?.name ?: ""}. " +
                    "Trung tâm sẽ liên hệ với bạn sớm nhất để xác nhận.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Hoàn tất")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đăng ký khóa học") },
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
        ) {
            // Class info header
            if (isLoadingClass) {
                Box(
                    Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            } else {
                classInfo?.let { info ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                "Lớp ${info.name}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            info.teacher?.let { teacher ->
                                Text(
                                    "GV: ${teacher.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(0.85f)
                                )
                            }
                            info.schedule?.let { schedule ->
                                val daysMap = mapOf(
                                    "0" to "CN", "1" to "T2", "2" to "T3", "3" to "T4",
                                    "4" to "T5", "5" to "T6", "6" to "T7"
                                )
                                val days = schedule.daysOfWeek?.joinToString(", ") { daysMap[it] ?: it } ?: ""
                                val time = schedule.timeSlots?.let { "${it.startTime} - ${it.endTime}" } ?: ""
                                if (days.isNotBlank() || time.isNotBlank()) {
                                    Text(
                                        "📅 $days  ⏰ $time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(0.8f)
                                    )
                                }
                            }
                            info.feePerLesson?.let { fee ->
                                Text(
                                    "💰 ${java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")).format(fee)}/buổi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Registration Form
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Thông tin đăng ký",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Error message
                errorMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(msg, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Họ và tên *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text("Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = null },
                    label = { Text("Số điện thoại *") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Gender selection
                Text(
                    "Giới tính",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == "male",
                            onClick = { gender = "male" },
                            colors = RadioButtonDefaults.colors(selectedColor = TMixNavy)
                        )
                        Text("Nam")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == "female",
                            onClick = { gender = "female" },
                            colors = RadioButtonDefaults.colors(selectedColor = TMixNavy)
                        )
                        Text("Nữ")
                    }
                }
                
                // Address field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Note field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Submit button
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isSubmitting) "Đang gửi..." else "Gửi đăng ký",
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Privacy note
                Text(
                    "Bằng cách gửi đăng ký, bạn đồng ý cho trung tâm liên hệ qua email và số điện thoại đã cung cấp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
