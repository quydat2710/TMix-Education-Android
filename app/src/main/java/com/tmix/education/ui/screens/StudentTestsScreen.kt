package com.tmix.education.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Test
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.TestListState
import com.tmix.education.ui.viewmodel.TestViewModel

/**
 * Student Tests Screen — Full dark mode support
 * Loads real data from Backend API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTestsScreen(
    onStartTest: (String) -> Unit = {},
    onTestResults: () -> Unit = {},
    testViewModel: TestViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Chưa làm", "Đã làm")
    val isDark = isSystemInDarkTheme()
    
    val testsState by testViewModel.testsState.collectAsState()
    
    // Load tests on first composition
    LaunchedEffect(Unit) {
        testViewModel.loadAvailableTests()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bài kiểm tra", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onTestResults) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Kết quả học tập",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            
            when (val state = testsState) {
                is TestListState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TMixRed)
                    }
                }
                is TestListState.Error -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                            Spacer(Modifier.height(16.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { testViewModel.loadAvailableTests() },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is TestListState.Success -> {
                    val allTests = state.tests
                    val notAttempted = allTests.filter { !it.hasAttempted }
                    val attempted = allTests.filter { it.hasAttempted }
                    
                    // Stats card — Glassmorphic Premium
                    Card(
                        Modifier.fillMaxWidth().padding(16.dp),
                        shape = TMixShapes.CardLarge,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isDark) listOf(Color(0xFF0D1B2E), Color(0xFF1A2B45), Color(0xFF0F2035))
                                        else listOf(TMixNavy, Color(0xFF2A4A7A), TMixNavySoft)
                                    )
                                )
                        ) {
                            // Decorative floating circles 
                            Canvas(Modifier.matchParentSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.04f),
                                    radius = 80.dp.toPx(),
                                    center = Offset(size.width * 0.9f, size.height * -0.2f)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.03f),
                                    radius = 60.dp.toPx(),
                                    center = Offset(size.width * 0.1f, size.height * 1.3f)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.05f),
                                    radius = 30.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.2f)
                                )
                            }

                            Row(
                                Modifier.fillMaxWidth().padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Total
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${allTests.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Tổng đề",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(0.7f)
                                    )
                                }

                                // Divider
                                Box(
                                    Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(Color.White.copy(0.12f))
                                )

                                // Not attempted
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${notAttempted.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Success
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Chưa làm",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(0.7f)
                                    )
                                }

                                // Divider
                                Box(
                                    Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(Color.White.copy(0.12f))
                                )

                                // Attempted
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${attempted.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TMixRedLight
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Đã làm",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(0.7f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        color = if (selectedTab == index) 
                                            MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                    
                    val filteredTests = if (selectedTab == 0) notAttempted else attempted
                    
                    // Test list
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (filteredTests.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Không có bài kiểm tra", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        
                        items(filteredTests) { test ->
                            RealTestCard(test, isDark = isDark, onStartTest = { onStartTest(test.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTestCard(test: Test, isDark: Boolean = false, onStartTest: () -> Unit = {}) {
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = { if (!test.hasAttempted) onStartTest() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 2.dp,
            pressedElevation = if (isDark) 2.dp else 6.dp
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(test.className ?: "Chưa gán lớp", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                }
                
                if (test.hasAttempted && test.lastAttempt != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (test.lastAttempt.passed)
                                (if (isDark) Success.copy(0.15f) else SuccessLight)
                            else
                                (if (isDark) Error.copy(0.15f) else ErrorLight),
                            shape = TMixShapes.Chip
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (test.lastAttempt.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    null, Modifier.size(14.dp),
                                    tint = if (test.lastAttempt.passed) Success else Error
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${test.lastAttempt.percentage.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (test.lastAttempt.passed) Success else Error
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Kết quả gần nhất",
                            style = MaterialTheme.typography.labelSmall,
                            color = textSecondary
                        )
                    }
                } else {
                    Surface(
                        color = if (isDark) Info.copy(0.15f) else InfoLight,
                        shape = TMixShapes.Chip
                    ) {
                        Text("Chưa làm", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, color = Info)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Skill type badge
                val skillLabel = when (test.skillType) {
                    "listening" -> "🎧 Nghe"
                    "writing" -> "✍️ Viết"
                    "speaking" -> "🎤 Nói"
                    else -> "📖 Đọc"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(skillLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                        color = if (isDark) TMixNavyLight else TMixNavy)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, Modifier.size(14.dp), tint = textSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("${test.duration} phút", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                }
                if (test.skillType != "writing" && test.skillType != "speaking") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Quiz, null, Modifier.size(14.dp), tint = textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text("${test.questionCount} câu", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }
                if (test.teacherName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(test.teacherName, style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }
            }
            
            if (!test.hasAttempted) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStartTest,
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Bắt đầu làm bài")
                }
            }
        }
    }
}
