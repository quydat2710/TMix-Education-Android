package com.tmix.education.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tmix.education.data.model.Notification
import com.tmix.education.data.repository.NotificationRepository
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {}
) {
    val notificationRepository = remember { NotificationRepository() }
    
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF111827) else Color(0xFFF1F5F9)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    
    fun loadNotifications() {
        scope.launch {
            isLoading = notifications.isEmpty()
            isRefreshing = notifications.isNotEmpty()
            error = null
            
            val result = notificationRepository.getNotifications(page = 1, limit = 50)
            result.onSuccess { response ->
                notifications = response.result
            }.onFailure { e ->
                error = e.message ?: "Không thể tải thông báo"
            }
            
            isLoading = false
            isRefreshing = false
        }
    }
    
    LaunchedEffect(Unit) {
        loadNotifications()
    }
    
    val unreadCount = notifications.count { !it.isRead }
    
    Scaffold(
        containerColor = bgColor,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TMixNavy, Color(0xFF2A4D7A), TMixNavySoft)))
                    .drawBehind {
                        drawCircle(Color.White, radius = size.height * 0.7f, center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f), alpha = 0.05f)
                        drawCircle(Color.White, radius = size.height * 0.5f, center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.9f), alpha = 0.03f)
                    }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp).clickable { onBack() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Thông báo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            if (unreadCount > 0) {
                                Text("$unreadCount chưa đọc", style = MaterialTheme.typography.labelMedium, color = SuccessLight)
                            }
                        }
                    }
                    
                    Row {
                        if (unreadCount > 0) {
                            IconButton(onClick = {
                                scope.launch {
                                    notificationRepository.markAllAsRead()
                                    notifications = notifications.map { it.copy(isRead = true) }
                                }
                            }) {
                                Icon(Icons.Default.DoneAll, "Đã đọc tất cả", tint = SuccessLight)
                            }
                        }
                        IconButton(onClick = { loadNotifications() }) {
                            Icon(Icons.Default.Refresh, "Làm mới", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            error != null && notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, null, Modifier.size(64.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text(error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { loadNotifications() }, colors = ButtonDefaults.buttonColors(containerColor = TMixNavy)) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsOff, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Không có thông báo nào", style = MaterialTheme.typography.titleMedium, color = textColor)
                        Spacer(Modifier.height(8.dp))
                        Text("Mọi thông báo mới nhất sẽ hiện ở đây", style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(0.7f))
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isRefreshing) {
                        item {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape), color = TMixRed)
                        }
                    }
                    
                    val unreadNotifications = notifications.filter { !it.isRead }
                    if (unreadNotifications.isNotEmpty()) {
                        item {
                            Text(
                                "MỚI NHẤT (${unreadNotifications.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = TMixRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                        }
                        
                        itemsIndexed(unreadNotifications, key = { _, it -> it.id }) { index, notification ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(delayMillis = index * 50)) + slideInVertically(tween(delayMillis = index * 50)) { it / 4 }
                            ) {
                                PremiumNotificationCard(
                                    notification = notification,
                                    isDark = isDark,
                                    onRead = {
                                        scope.launch {
                                            notificationRepository.markAsRead(notification.id)
                                            notifications = notifications.map {
                                                if (it.id == notification.id) it.copy(isRead = true) else it
                                            }
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            notificationRepository.deleteNotification(notification.id)
                                            notifications = notifications.filter { it.id != notification.id }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    val readNotifications = notifications.filter { it.isRead }
                    if (readNotifications.isNotEmpty()) {
                        item {
                            Text(
                                "TRƯỚC ĐÓ",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp, top = if (unreadNotifications.isNotEmpty()) 16.dp else 4.dp)
                            )
                        }
                        
                        itemsIndexed(readNotifications, key = { _, it -> it.id }) { index, notification ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(delayMillis = index * 50)) + slideInVertically(tween(delayMillis = index * 50)) { it / 4 }
                            ) {
                                PremiumNotificationCard(
                                    notification = notification,
                                    isDark = isDark,
                                    onRead = null,
                                    onDelete = {
                                        scope.launch {
                                            notificationRepository.deleteNotification(notification.id)
                                            notifications = notifications.filter { it.id != notification.id }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumNotificationCard(
    notification: Notification,
    isDark: Boolean,
    onRead: (() -> Unit)?,
    onDelete: (() -> Unit)? = null
) {
    val (icon, color) = when (notification.type.lowercase()) {
        "payment", "payment_reminder" -> Icons.Default.Payment to Warning
        "attendance" -> Icons.Default.CheckCircle to Info
        "new_registration" -> Icons.Default.PersonAdd to Success
        "announcement", "general" -> Icons.Default.Campaign to TMixNavy
        "score", "test_result" -> Icons.Default.Grade to Success
        "schedule" -> Icons.Default.CalendarMonth to Info
        else -> Icons.Default.Notifications to TextSecondary
    }
    
    val bgBase = if (isDark) Color(0xFF1F2937) else Color.White
    val bgColor by animateColorAsState(
        targetValue = if (!notification.isRead) color.copy(alpha = if (isDark) 0.1f else 0.05f) else bgBase,
        label = "bg"
    )
    
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val accentColor = if (!notification.isRead) color else Color.Transparent

    Card(
        onClick = { onRead?.invoke() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.04f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Accent strip for unread
            Box(Modifier.fillMaxHeight().width(4.dp).background(accentColor))
            
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(color.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, Modifier.size(24.dp), tint = color)
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!notification.isRead) {
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(8.dp).background(TMixRed, CircleShape))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White.copy(0.7f) else TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, Modifier.size(14.dp), tint = TextSecondary.copy(0.7f))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatNotificationTime(notification.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(0.7f)
                        )
                    }
                }
                
                // Delete button
                if (onDelete != null) {
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(32.dp).align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Xóa",
                            Modifier.size(18.dp),
                            tint = TextSecondary.copy(0.4f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatNotificationTime(isoDate: String?): String {
    if (isoDate == null) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoDate)
        val now = System.currentTimeMillis()
        val diff = now - (date?.time ?: now)
        
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)
        
        when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "${minutes} phút trước"
            hours < 24 -> "${hours} giờ trước"
            days < 7 -> "${days} ngày trước"
            else -> {
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
                formatter.format(date!!)
            }
        }
    } catch (e: Exception) {
        ""
    }
}
