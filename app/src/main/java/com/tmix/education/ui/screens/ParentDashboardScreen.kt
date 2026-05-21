package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Student
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel
import com.tmix.education.ui.viewmodel.NotificationViewModel
import com.tmix.education.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.NumberFormat
import java.util.Locale


data class ParentNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String,
    val isRead: Boolean
)

/**
 * Parent Dashboard — Premium Redesign
 * With entrance animations, shimmer loading, gradient depth
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: ParentDashboardViewModel = viewModel(),
    notificationViewModel: NotificationViewModel? = null,
    onNotificationClick: () -> Unit = {},
    onChildClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onCourseClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val children = state.children
    val parentName = state.parent?.name ?: state.user?.name ?: "phụ huynh"
    val unreadCount by (notificationViewModel?.unreadCount ?: MutableStateFlow(0)).collectAsState()
    val showSnackbar by (notificationViewModel?.showSnackbar ?: MutableStateFlow(false)).collectAsState()
    val latestNotification by (notificationViewModel?.latestNotification ?: MutableStateFlow(null)).collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) isRefreshing = false
    }

    LaunchedEffect(showSnackbar, latestNotification) {
        if (showSnackbar && latestNotification != null) {
            snackbarHostState.showSnackbar(
                message = "${latestNotification!!.title}: ${latestNotification!!.message}",
                duration = SnackbarDuration.Short
            )
            notificationViewModel?.dismissSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TMIX", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = TMixRed,
                            letterSpacing = 2.sp)
                        Text("Xin chào, $parentName!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                actions = {
                    Box {
                        var showNotificationDropdown by remember { mutableStateOf(false) }
                        val recentNotifications by (notificationViewModel?.recentNotifications ?: MutableStateFlow(emptyList())).collectAsState()

                        IconButton(onClick = {
                            showNotificationDropdown = true
                            notificationViewModel?.loadRecentNotifications()
                            notificationViewModel?.refreshUnreadCount()
                        }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = TMixRed) {
                                            Text(
                                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Notifications, "Thông báo")
                            }
                        }

                        DropdownMenu(
                            expanded = showNotificationDropdown,
                            onDismissRequest = { showNotificationDropdown = false },
                            modifier = Modifier
                                .width(340.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Thông báo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (unreadCount > 0) {
                                    Surface(
                                        color = TMixRed.copy(0.1f),
                                        shape = CircleShape
                                    ) {
                                        Text("$unreadCount mới", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = TMixRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                            // List
                            if (recentNotifications.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.NotificationsOff, null, tint = TextSecondary.copy(0.5f), modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Không có thông báo", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    recentNotifications.forEachIndexed { index, notif ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showNotificationDropdown = false
                                                    onNotificationClick()
                                                }
                                                .background(if (!notif.isRead) MaterialTheme.colorScheme.primary.copy(0.03f) else Color.Transparent)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            // Icon indicator
                                            Box(
                                                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (!notif.isRead) TMixRed.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(if (!notif.isRead) Icons.Default.NotificationsActive else Icons.Default.Notifications, null, tint = if (!notif.isRead) TMixRed else TextSecondary, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            // Text
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    notif.title, 
                                                    fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold, 
                                                    style = MaterialTheme.typography.bodyMedium, 
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    notif.message, 
                                                    style = MaterialTheme.typography.bodySmall, 
                                                    color = TextSecondary, 
                                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                            // Unread Dot
                                            if (!notif.isRead) {
                                                Spacer(Modifier.width(8.dp))
                                                Box(Modifier.size(8.dp).clip(CircleShape).background(TMixRed).align(Alignment.CenterVertically))
                                            }
                                        }
                                        if (index < recentNotifications.size - 1) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                            
                            // Footer Button
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showNotificationDropdown = false
                                        onNotificationClick()
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Xem tất cả", 
                                    color = TMixNavy, 
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                // Shimmer skeleton loading
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { ShimmerBox(width = 300.dp, height = 120.dp, cornerRadius = 20.dp) }
                    item { SkeletonStatsRow() }
                    item { repeat(2) { SkeletonCard(Modifier.padding(bottom = 12.dp)) } }
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Card(
                        shape = TMixShapes.CardLarge,
                        colors = CardDefaults.cardColors(containerColor = ErrorLight),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(56.dp), tint = Error)
                            Spacer(Modifier.height(16.dp))
                            Text(state.error ?: "Có lỗi xảy ra",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                                shape = TMixShapes.Button
                            ) {
                                Text("Thử lại", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            else -> {
              PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refresh()
                    notificationViewModel?.refreshUnreadCount()
                },
                modifier = Modifier.fillMaxSize().padding(padding)
              ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Banner
                    item {
                        SlideInFromBottom(index = 0) {
                            BannerCarousel(Modifier.fillMaxWidth())
                        }
                    }

                    // Quick stats — gradient cards
                    item {
                        SlideInFromBottom(index = 1) {
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Card(
                                    onClick = onChildClick,
                                    modifier = Modifier.weight(1f),
                                    shape = TMixShapes.CardLarge,
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Box(
                                        Modifier.fillMaxWidth()
                                            .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)))
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                Modifier.size(38.dp).clip(CircleShape)
                                                    .background(Color.White.copy(0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.People, null, Modifier.size(20.dp), tint = Color.White)
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text("${children.size}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold, color = Color.White,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("Con của tôi",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(0.8f))
                                            }
                                        }
                                    }
                                }

                                Card(
                                    onClick = onPaymentClick,
                                    modifier = Modifier.weight(1f),
                                    shape = TMixShapes.CardLarge,
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Box(
                                        Modifier.fillMaxWidth()
                                            .background(Brush.linearGradient(listOf(TMixRed, TMixRedSoft)))
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                Modifier.size(38.dp).clip(CircleShape)
                                                    .background(Color.White.copy(0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Payment, null, Modifier.size(20.dp), tint = Color.White)
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                val pendingText = if (state.totalPendingAmount > 0) {
                                                    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                                                    formatter.maximumFractionDigits = 0
                                                    "${formatter.format(state.totalPendingAmount.toLong())}đ"
                                                } else "0đ"
                                                Text(pendingText,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold, color = Color.White,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("Học phí/tháng",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(0.8f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Children section
                    if (children.isNotEmpty()) {
                        item {
                            SlideInFromBottom(index = 2) {
                                SectionHeader(icon = Icons.Filled.FamilyRestroom, title = "Con của tôi")
                            }
                        }
                        item {
                            SlideInFromBottom(index = 3) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    items(children) { child ->
                                        FadeInScale(delay = 100) {
                                            ChildOverviewCard(
                                                name = child.name,
                                                grade = "${child.classes?.size ?: 0} lớp",
                                                attendance = 0f,
                                                onClick = onChildClick
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pending payments
                    if (state.pendingPaymentsCount > 0) {
                        item {
                            SlideInFromBottom(index = 4) {
                                Card(
                                    onClick = onPaymentClick,
                                    shape = TMixShapes.Card,
                                    colors = CardDefaults.cardColors(containerColor = WarningTint),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(36.dp).clip(CircleShape)
                                                .background(Warning.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Warning, null, Modifier.size(18.dp), tint = Warning)
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "Có ${state.pendingPaymentsCount} hóa đơn chưa thanh toán",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Warning
                                            )
                                            Text("Nhấn để xem chi tiết",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), tint = Warning)
                                    }
                                }
                            }
                        }
                    }

                    // Quick actions
                    item {
                        SlideInFromBottom(index = 5) {
                            SectionHeader(icon = Icons.Filled.Bolt, title = "Thao tác nhanh")
                        }
                    }

                    item {
                        SlideInFromBottom(index = 6) {
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                QuickActionCard(Modifier.weight(1f), Icons.Filled.CalendarMonth,
                                    "Lịch học", "Xem chi tiết",
                                    Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)),
                                    onClick = onScheduleClick)
                                QuickActionCard(Modifier.weight(1f), Icons.Filled.QrCode2,
                                    "Thanh toán", "Quét QR",
                                    Brush.linearGradient(listOf(TMixRed, TMixRedSoft)),
                                    onClick = onPaymentClick)
                            }
                        }
                    }

                    item {
                        SlideInFromBottom(index = 7) {
                            Card(
                                onClick = onCourseClick,
                                shape = TMixShapes.CardLarge,
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(
                                    Modifier.fillMaxWidth()
                                        .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)))
                                        .padding(18.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            Modifier.size(36.dp).clip(CircleShape)
                                                .background(Color.White.copy(0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.School, null, Modifier.size(20.dp), tint = Color.White)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text("Đăng ký khóa học mới",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
              }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildOverviewCard(name: String, grade: String, attendance: Float, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = TMixShapes.CardLarge,
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gradient avatar ring
                Box(
                    Modifier.size(46.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(38.dp).clip(CircleShape).background(TMixNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name.split(" ").lastOrNull() ?: name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        color = NavyTint,
                        shape = TMixShapes.Chip
                    ) {
                        Text(grade, Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TMixNavy, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (attendance > 0) {
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Điểm danh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(attendance * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = Success)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { attendance },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(TMixShapes.Badge),
                    color = Success,
                    trackColor = SuccessTint
                )
            }
        }
    }
}
