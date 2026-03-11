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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tmix.education.ui.theme.*

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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {}
) {
    var notifications by remember {
        mutableStateOf(listOf(
            NotificationItem("1", "Điểm mới", "Bạn vừa đạt band 6.5 bài thi IELTS Mock Test", "5 phút trước", "score", false),
            NotificationItem("2", "Nhắc học phí", "Học phí tháng 01/2025 sắp đến hạn thanh toán", "1 giờ trước", "payment", false),
            NotificationItem("3", "Điểm danh", "Bạn đã điểm danh thành công lớp Tiếng Anh Giao Tiếp Nhóm", "2 giờ trước", "attendance", true),
            NotificationItem("4", "Thông báo", "CLB Nói Tiếng Anh (English Club) tuần này chuyển sang sáng CN", "1 ngày trước", "announcement", true),
            NotificationItem("5", "Nhắc nhở", "Bạn có bài kiểm tra IELTS Listening vào ngày mai lúc 8:00", "1 ngày trước", "reminder", true),
            NotificationItem("6", "Điểm mới", "Bạn vừa nhận 850 điểm bài thi thử TOEIC", "2 ngày trước", "score", true),
            NotificationItem("7", "Thông báo", "Lớp Ngữ pháp căn bản đổi phòng sang 305", "3 ngày trước", "announcement", true)
        ))
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
        if (notifications.isEmpty()) {
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
        } else {
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
                
                item {
                    Text(
                        "Đã đọc",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                items(notifications.filter { it.isRead }) { notification ->
                    NotificationCard(notification, onRead = null)
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
