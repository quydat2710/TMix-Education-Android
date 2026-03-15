package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Student
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel

/**
 * Parent Children Screen
 * View and manage children info - connected to real backend data
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Con của tôi", fontWeight = FontWeight.Bold) }
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
            children.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.People, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có thông tin con", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text("Liên hệ trung tâm để liên kết tài khoản", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(children) { child ->
                        ChildCard(
                            child, 
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCard(child: Student, onClick: () -> Unit = {}, onSchedule: () -> Unit = {}, onPayment: () -> Unit = {}) {
    val classCount = child.classes?.size ?: 0

    Card(
        onClick = onClick,
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Header with avatar
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(TMixNavy)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TMixRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        child.name.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        child.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (child.email != null) {
                        Text(
                            child.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.8f)
                        )
                    }
                }
            }

            // Stats grid
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ChildStat("Số lớp", "$classCount", Icons.Default.MenuBook, TMixNavy)
                    ChildStat(
                        "Giới tính",
                        when (child.gender) {
                            "male" -> "Nam"
                            "female" -> "Nữ"
                            else -> "—"
                        },
                        Icons.Default.Person,
                        Info
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ChildStat("SĐT", child.phone ?: "—", Icons.Default.Phone, Warning)
                    ChildStat("Địa chỉ", if (child.address != null) "Có" else "—", Icons.Default.LocationOn, Success)
                }

                Spacer(Modifier.height(16.dp))

                // Quick actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSchedule,
                        Modifier.weight(1f),
                        shape = TMixShapes.Button
                    ) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lịch học")
                    }

                    Button(
                        onClick = onPayment,
                        Modifier.weight(1f),
                        shape = TMixShapes.Button,
                        colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                    ) {
                        Icon(Icons.Default.Payment, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Học phí")
                    }
                }
            }
        }
    }
}

@Composable
fun ChildStat(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp), tint = color)
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
