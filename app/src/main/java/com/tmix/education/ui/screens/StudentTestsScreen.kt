package com.tmix.education.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tmix.education.data.model.Test
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.TestListState
import com.tmix.education.ui.viewmodel.TestViewModel

/**
 * Student Tests Screen - loads real data from Backend API
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
                            tint = TMixNavy
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
                            Text(state.message, color = TextSecondary)
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
                    
                    // Stats card
                    Card(
                        Modifier.fillMaxWidth().padding(16.dp),
                        shape = TMixShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = TMixNavy)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${allTests.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Tổng đề", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${notAttempted.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Success)
                                Text("Chưa làm", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${attempted.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TMixRed)
                                Text("Đã làm", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
                            }
                        }
                    }
                    
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
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
                                    Text("Không có bài kiểm tra", color = TextSecondary)
                                }
                            }
                        }
                        
                        items(filteredTests) { test ->
                            RealTestCard(test, onStartTest = { onStartTest(test.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTestCard(test: Test, onStartTest: () -> Unit = {}) {
    Card(
        onClick = { if (!test.hasAttempted) onStartTest() },
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(test.className ?: "Chưa gán lớp", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                
                if (test.hasAttempted && test.lastAttempt != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (test.lastAttempt.passed) SuccessLight else ErrorLight,
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
                            color = TextSecondary
                        )
                    }
                } else {
                    Surface(color = InfoLight, shape = TMixShapes.Chip) {
                        Text("Chưa làm", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Info)
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
                    Text(skillLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TMixNavy)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("${test.duration} phút", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                if (test.skillType != "writing" && test.skillType != "speaking") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Quiz, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text("${test.questionCount} câu", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                if (test.teacherName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(test.teacherName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
