package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Student
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel
import com.tmix.education.ui.viewmodel.NotificationViewModel
import com.tmix.education.ui.components.BannerCarousel
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
 * Parent Dashboard Screen
 * Overview of children's activities - connected to real backend data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: ParentDashboardViewModel = viewModel(),
    notificationViewModel: NotificationViewModel? = null,
    onNotificationClick: () -> Unit = {},
    onChildClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onCourseClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val children = state.children
    val parentName = state.parent?.name ?: state.user?.name ?: "phụ huynh"
    val unreadCount by (notificationViewModel?.unreadCount ?: MutableStateFlow(0)).collectAsState()
    val showSnackbar by (notificationViewModel?.showSnackbar ?: MutableStateFlow(false)).collectAsState()
    val latestNotification by (notificationViewModel?.latestNotification ?: MutableStateFlow(null)).collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show Snackbar when new notification arrives
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
                        Text("TMIX", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TMixRed)
                        Text("Xin chào, $parentName!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onNotificationClick()
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
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                        Spacer(Modifier.height(16.dp))
                        Text(state.error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = TMixRed)) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Banner carousel
                    item {
                        BannerCarousel(Modifier.fillMaxWidth())
                    }
                    
                    // Quick stats
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                onClick = onChildClick,
                                modifier = Modifier.weight(1f),
                                shape = TMixShapes.Card,
                                colors = CardDefaults.cardColors(containerColor = TMixNavy)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.People, null, Modifier.size(32.dp), tint = Color.White)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("${children.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Con của tôi", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                                    }
                                }
                            }
                            Card(
                                onClick = onPaymentClick,
                                modifier = Modifier.weight(1f),
                                shape = TMixShapes.Card,
                                colors = CardDefaults.cardColors(containerColor = TMixRed)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Payment, null, Modifier.size(32.dp), tint = Color.White)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        val pendingText = if (state.totalPendingAmount > 0) {
                                            val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                                            formatter.maximumFractionDigits = 0
                                            "${formatter.format(state.totalPendingAmount.toLong())}đ"
                                        } else "0đ"
                                        Text(pendingText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Học phí/tháng", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                                    }
                                }
                            }
                        }
                    }

                    // Children overview
                    if (children.isNotEmpty()) {
                        item {
                            Text("Con của tôi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(children) { child ->
                                    val classCount = child.classes?.size ?: 0
                                    ChildOverviewCard(
                                        name = child.name,
                                        grade = "$classCount lớp",
                                        attendance = 0f, // Will be loaded per child
                                        onClick = onChildClick
                                    )
                                }
                            }
                        }
                    }

                    // Pending payments info
                    if (state.pendingPaymentsCount > 0) {
                        item {
                            Card(
                                onClick = onPaymentClick,
                                shape = TMixShapes.Card,
                                colors = CardDefaults.cardColors(containerColor = WarningLight)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, null, Modifier.size(24.dp), tint = Warning)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Có ${state.pendingPaymentsCount} hóa đơn chưa thanh toán",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Warning
                                        )
                                        Text("Nhấn để xem chi tiết", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    // Quick actions
                    item {
                        Text("Thao tác nhanh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onChildClick,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TMixNavy),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = TMixShapes.Button
                            ) {
                                Icon(Icons.Filled.CalendarMonth, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Xem lịch học", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onPaymentClick,
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = TMixShapes.Button
                            ) {
                                Icon(Icons.Filled.QrCode2, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Thanh toán", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    
                    item {
                        Button(
                            onClick = onCourseClick,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TMixNavy),
                            shape = TMixShapes.Button
                        ) {
                            Icon(Icons.Filled.School, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Đăng ký khóa học mới", fontWeight = FontWeight.SemiBold)
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
        modifier = Modifier.width(180.dp),
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(TMixNavy, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(name.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(name.split(" ").lastOrNull() ?: name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(grade, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            if (attendance > 0) {
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Điểm danh", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${(attendance * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Success)
                }

                LinearProgressIndicator(
                    progress = { attendance },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    color = Success,
                    trackColor = SuccessLight
                )
            }
        }
    }
}
