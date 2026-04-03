package com.tmix.education.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Student
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel
import kotlinx.coroutines.delay

/**
 * Parent Children Screen — Premium Redesign
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildrenScreen(
    viewModel: ParentDashboardViewModel = viewModel(),
    onChildClick: (String) -> Unit = {},
    onChildSchedule: () -> Unit = {},
    onChildPayment: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val children = state.children
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(TMixNavy, TMixNavySoft))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FamilyRestroom, null,
                                Modifier.size(18.dp), tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Con của tôi",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "${children.size} học sinh",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixNavy)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2A1520) else ErrorLight),
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
                                fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Thử lại", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            children.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF252D3F) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, null, Modifier.size(40.dp),
                                tint = if (isDark) Color.White.copy(0.4f) else Color(0xFF94A3B8))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có thông tin con",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Liên hệ trung tâm để liên kết tài khoản",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        children,
                        key = { _, c -> c.id },
                        contentType = { _, _ -> "child_card" }
                    ) { index, child ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 80L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(400)) + slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(400, easing = EaseOutCubic)
                            )
                        ) {
                            PremiumChildCard(
                                child = child,
                                isDark = isDark,
                                onClick = { onChildClick(child.id) },
                                onSchedule = onChildSchedule,
                                onPayment = onChildPayment
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Premium Child Card
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumChildCard(
    child: Student,
    isDark: Boolean,
    onClick: () -> Unit = {},
    onSchedule: () -> Unit = {},
    onPayment: () -> Unit = {}
) {
    val classCount = child.classes?.size ?: 0
    val cardBg = if (isDark) Color(0xFF1A2030) else Color.White
    val tileBg = if (isDark) Color(0xFF232A3A) else Color(0xFFF8FAFC)
    val initials = child.name.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?"

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null
    ) {
        Column {
            // ── Header with gradient + avatar ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(TMixNavy, Color(0xFF2A4D7A), TMixNavySoft)
                        )
                    )
                    .drawBehind {
                        // Decorative circles
                        drawCircle(
                            color = Color.White,
                            radius = size.height * 0.6f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, -size.height * 0.1f),
                            alpha = 0.05f
                        )
                        drawCircle(
                            color = Color.White,
                            radius = size.height * 0.4f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 1.1f),
                            alpha = 0.03f
                        )
                    }
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar with ring
                    Box(
                        Modifier.size(56.dp).clip(CircleShape)
                            .border(2.dp, Color.White.copy(0.3f), CircleShape)
                            .background(TMixRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initials,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            child.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (child.email != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                child.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(0.15f)
                    ) {
                        Text(
                            "$classCount lớp",
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Info grid ──
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ChildInfoTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MenuBook,
                        label = "Số lớp",
                        value = "$classCount",
                        accentColor = TMixNavy,
                        isDark = isDark,
                        tileBg = tileBg
                    )
                    ChildInfoTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Person,
                        label = "Giới tính",
                        value = when (child.gender) {
                            "male" -> "Nam"
                            "female" -> "Nữ"
                            else -> "—"
                        },
                        accentColor = Info,
                        isDark = isDark,
                        tileBg = tileBg
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ChildInfoTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Phone,
                        label = "SĐT",
                        value = child.phone ?: "—",
                        accentColor = Warning,
                        isDark = isDark,
                        tileBg = tileBg
                    )
                    ChildInfoTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocationOn,
                        label = "Địa chỉ",
                        value = child.address ?: "—",
                        accentColor = Success,
                        isDark = isDark,
                        tileBg = tileBg
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Action buttons ──
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onSchedule,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF3A4560) else TMixNavy.copy(0.3f)
                        )
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp),
                            tint = if (isDark) Color.White.copy(0.8f) else TMixNavy)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Lịch học",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(0.8f) else TMixNavy,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onPayment,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(TMixRed, Color(0xFFE84357))),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payment, null, Modifier.size(16.dp), tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Học phí",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Info Tile
// ================================================================
@Composable
fun ChildInfoTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    isDark: Boolean,
    tileBg: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tileBg)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accentColor.copy(if (isDark) 0.15f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(14.dp), tint = accentColor)
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(0.6f) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White else Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
