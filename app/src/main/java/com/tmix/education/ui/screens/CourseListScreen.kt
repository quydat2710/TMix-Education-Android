package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tmix.education.data.model.ClassInfo
import com.tmix.education.data.repository.ClassRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Course List Screen
 * Browse public courses available for registration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    onBack: () -> Unit = {},
    onRegister: (String) -> Unit = {}
) {
    val classRepository = remember { ClassRepository() }
    
    var classes by remember { mutableStateOf<List<ClassInfo>>(emptyList()) }
    var filteredClasses by remember { mutableStateOf<List<ClassInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Load public classes
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val result = classRepository.getPublicClasses(page = 1, limit = 100)
            result.onSuccess { data ->
                classes = data
                filteredClasses = data
            }.onFailure { e ->
                error = e.message ?: "Không thể tải danh sách khóa học"
            }
            isLoading = false
        }
    }
    
    // Filter classes by search query
    LaunchedEffect(searchQuery, classes) {
        filteredClasses = if (searchQuery.isBlank()) {
            classes
        } else {
            classes.filter { classInfo ->
                classInfo.name.contains(searchQuery, ignoreCase = true) ||
                classInfo.teacher?.name?.contains(searchQuery, ignoreCase = true) == true ||
                classInfo.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khóa học") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm kiếm khóa học...") },
                leadingIcon = { Icon(Icons.Default.Search, "Tìm kiếm") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Results count
            Text(
                "${filteredClasses.size} khóa học",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            when {
                isLoading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TMixRed)
                    }
                }
                error != null -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudOff, null, Modifier.size(64.dp), tint = TextSecondary.copy(0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text(error ?: "Có lỗi xảy ra", color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        error = null
                                        val result = classRepository.getPublicClasses(page = 1, limit = 100)
                                        result.onSuccess { data ->
                                            classes = data
                                            filteredClasses = data
                                        }.onFailure { e ->
                                            error = e.message
                                        }
                                        isLoading = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                filteredClasses.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.School, null, Modifier.size(64.dp), tint = TextSecondary.copy(0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isBlank()) "Chưa có khóa học nào"
                                else "Không tìm thấy khóa học phù hợp",
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredClasses, key = { it.id }) { classInfo ->
                            CourseCard(
                                classInfo = classInfo,
                                onRegister = { onRegister(classInfo.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseCard(
    classInfo: ClassInfo,
    onRegister: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(TMixNavy, Color(0xFF2C5282)))
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Lớp ${classInfo.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            classInfo.grade?.let { grade ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Khối $grade", style = MaterialTheme.typography.labelSmall) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = Color.White.copy(0.2f),
                                        labelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                            StatusChip(classInfo.status)
                        }
                    }
                }
            }
            
            // Course info
            Column(Modifier.padding(16.dp)) {
                // Schedule info
                classInfo.schedule?.let { schedule ->
                    val daysMap = mapOf(
                        "0" to "CN", "1" to "T2", "2" to "T3", "3" to "T4",
                        "4" to "T5", "5" to "T6", "6" to "T7"
                    )
                    val daysText = schedule.daysOfWeek?.joinToString(", ") { daysMap[it] ?: it } ?: "Liên hệ"
                    val timeText = schedule.timeSlots?.let { "${it.startTime} - ${it.endTime}" } ?: "Liên hệ"
                    
                    CourseInfoRow(Icons.Default.CalendarMonth, "Lịch học", daysText)
                    CourseInfoRow(Icons.Default.AccessTime, "Giờ học", timeText)
                }
                
                classInfo.room?.let { room ->
                    CourseInfoRow(Icons.Default.MeetingRoom, "Phòng", room)
                }
                
                CourseInfoRow(
                    Icons.Default.Groups, "Sĩ số",
                    "${classInfo.maxStudent ?: "N/A"} học sinh"
                )
                
                CourseInfoRow(
                    Icons.Default.AttachMoney, "Học phí",
                    classInfo.feePerLesson?.let { formatCurrencyVND(it) + "/buổi" } ?: "Liên hệ"
                )
                
                classInfo.teacher?.let { teacher ->
                    CourseInfoRow(Icons.Default.Person, "Giáo viên", teacher.name)
                }
                
                // Description
                classInfo.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TMixNavy.copy(0.04f))
                                .padding(12.dp)
                        )
                    }
                }
                
                // Register button
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AppRegistration, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng ký ngay", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CourseInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(TMixNavy.copy(0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = TMixNavy)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF444444)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, bgColor) = when (status.lowercase()) {
        "active", "ongoing" -> "Đang học" to Color(0xFF4CAF50)
        "upcoming" -> "Sắp khai giảng" to Color(0xFFFF9800)
        "closed" -> "Đã kết thúc" to Color.White.copy(0.3f)
        else -> status to Color.White.copy(0.2f)
    }
    
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = bgColor,
            labelColor = Color.White
        ),
        border = null
    )
}

private fun formatCurrencyVND(amount: Double): String {
    return java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")).format(amount)
}
