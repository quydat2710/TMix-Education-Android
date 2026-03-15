package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Payment
import com.tmix.education.data.model.User
import com.tmix.education.data.repository.AuthRepository
import com.tmix.education.data.repository.StudentRepository
import com.tmix.education.data.repository.ParentRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String, // score, payment, attendance, announcement, reminder
    val isRead: Boolean
)

/**
 * Notifications Screen  
 * Generates notifications from real backend data (pending payments, upcoming schedule, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {}
) {
    val authRepository = remember { AuthRepository() }
    val studentRepository = remember { StudentRepository() }
    val parentRepository = remember { ParentRepository() }
    
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Load notifications from real backend data
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val generatedNotifications = mutableListOf<NotificationItem>()
            val userId = authRepository.getCurrentUserId()
            val isStudent = authRepository.isStudent()
            var notifId = 1
            
            if (userId != null) {
                if (isStudent) {
                    // Student notifications: pending payments, schedule
                    val paymentsResult = studentRepository.getPayments(userId)
                    paymentsResult.getOrNull()?.let { payments ->
                        val pendingPayments = payments.filter { !it.isPaid }
                        pendingPayments.forEach { payment ->
                            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            generatedNotifications.add(
                                NotificationItem(
                                    id = (notifId++).toString(),
                                    title = "Nhắc học phí",
                                    message = "Học phí tháng ${payment.month}/${payment.year} chưa thanh toán: ${formatter.format(payment.remainingAmount)}",
                                    time = "Tháng ${payment.month}/${payment.year}",
                                    type = "payment",
                                    isRead = false
                                )
                            )
                        }
                    }
                    
                    // Schedule for today
                    val scheduleResult = studentRepository.getSchedule(userId)
                    scheduleResult.getOrNull()?.let { schedule ->
                        if (schedule.isNotEmpty()) {
                            generatedNotifications.add(
                                NotificationItem(
                                    id = (notifId++).toString(),
                                    title = "Lịch học hôm nay",
                                    message = "Bạn có ${schedule.size} buổi học hôm nay",
                                    time = "Hôm nay",
                                    type = "reminder",
                                    isRead = false
                                )
                            )
                        }
                    }
                    
                    // Attendance stats
                    val statsResult = studentRepository.getAttendanceStats(userId)
                    statsResult.getOrNull()?.let { stats ->
                        if (stats.absent > 0) {
                            generatedNotifications.add(
                                NotificationItem(
                                    id = (notifId++).toString(),
                                    title = "Điểm danh",
                                    message = "Bạn đã vắng ${stats.absent} buổi trong tổng ${stats.total} buổi. Hãy cố gắng đi học đầy đủ nhé!",
                                    time = "Tổng kết",
                                    type = "attendance",
                                    isRead = true
                                )
                            )
                        }
                    }
                } else {
                    // Parent notifications: children payments
                    val paymentsResult = parentRepository.getAllChildrenPayments(userId)
                    paymentsResult.getOrNull()?.let { payments ->
                        val pendingPayments = payments.filter { !it.isPaid }
                        if (pendingPayments.isNotEmpty()) {
                            val totalAmount = pendingPayments.sumOf { it.remainingAmount }
                            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            generatedNotifications.add(
                                NotificationItem(
                                    id = (notifId++).toString(),
                                    title = "Nhắc học phí",
                                    message = "Có ${pendingPayments.size} hóa đơn chưa thanh toán, tổng cộng ${formatter.format(totalAmount)}",
                                    time = "Cần thanh toán",
                                    type = "payment",
                                    isRead = false
                                )
                            )
                        }
                        
                        // Recent paid payments
                        val recentPaid = payments.filter { it.isPaid }.take(3)
                        recentPaid.forEach { payment ->
                            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            generatedNotifications.add(
                                NotificationItem(
                                    id = (notifId++).toString(),
                                    title = "Thanh toán thành công",
                                    message = "Đã thanh toán học phí tháng ${payment.month}/${payment.year}: ${formatter.format(payment.paidAmount)}",
                                    time = "Tháng ${payment.month}/${payment.year}",
                                    type = "payment",
                                    isRead = true
                                )
                            )
                        }
                    }
                    
                    // Children info
                    val childrenResult = parentRepository.getChildren(userId)
                    childrenResult.getOrNull()?.let { children ->
                        if (children.isNotEmpty()) {
                            generatedNotifications.add(
                                NotificationItem(
                                    id = (notifId++).toString(),
                                    title = "Thông báo",
                                    message = "Bạn đang theo dõi ${children.size} học sinh. Nhấn vào \"Con của tôi\" để xem chi tiết.",
                                    time = "Tổng quan",
                                    type = "announcement",
                                    isRead = true
                                )
                            )
                        }
                    }
                }
            }
            
            // If no real notifications, show a welcome message
            if (generatedNotifications.isEmpty()) {
                generatedNotifications.add(
                    NotificationItem(
                        id = "welcome",
                        title = "Chào mừng",
                        message = "Chào mừng bạn đến với TMIX Education! Hiện chưa có thông báo mới.",
                        time = "Hôm nay",
                        type = "announcement",
                        isRead = true
                    )
                )
            }
            
            notifications = generatedNotifications
            isLoading = false
        }
    }
    
    val unreadCount = notifications.count { !it.isRead }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Thông báo")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            notifications = notifications.map { it.copy(isRead = true) }
                        }) {
                            Text("Đánh dấu đã đọc", color = TMixRed)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            notifications.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsOff, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Không có thông báo", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (unreadCount > 0) {
                        item {
                            Text(
                                "Chưa đọc ($unreadCount)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        items(notifications.filter { !it.isRead }) { notification ->
                            NotificationCard(notification) {
                                notifications = notifications.map {
                                    if (it.id == notification.id) it.copy(isRead = true) else it
                                }
                            }
                        }
                        
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                    
                    val readNotifications = notifications.filter { it.isRead }
                    if (readNotifications.isNotEmpty()) {
                        item {
                            Text(
                                "Đã đọc",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        items(readNotifications) { notification ->
                            NotificationCard(notification, onRead = null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: NotificationItem,
    onRead: (() -> Unit)?
) {
    val (icon, color) = when (notification.type) {
        "score" -> Icons.Default.Grade to Success
        "payment" -> Icons.Default.Payment to Warning
        "attendance" -> Icons.Default.CheckCircle to Info
        "announcement" -> Icons.Default.Campaign to TMixNavy
        "reminder" -> Icons.Default.Alarm to TMixRed
        else -> Icons.Default.Notifications to TextSecondary
    }
    
    Card(
        onClick = { onRead?.invoke() },
        shape = TMixShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) color.copy(0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(20.dp), tint = color)
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(8.dp).background(TMixRed, CircleShape))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(notification.time, style = MaterialTheme.typography.labelSmall, color = TextSecondary.copy(0.7f))
            }
        }
    }
}
