package com.tmix.education.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Material
import com.tmix.education.data.model.StudentClassInfo
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel
import kotlinx.coroutines.launch

/**
 * Materials Screen — Student views learning materials by class
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    dashboardViewModel: StudentDashboardViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dashboardState by dashboardViewModel.state.collectAsState()

    var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedClassId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    // In-app viewer states
    var viewingPdfUrl by remember { mutableStateOf<String?>(null) }
    var viewingImageUrl by remember { mutableStateOf<String?>(null) }

    val classes = dashboardState.classes

    // Auto-select first class when available
    LaunchedEffect(classes) {
        if (classes.isNotEmpty() && selectedClassId == null) {
            selectedClassId = classes.first().classInfo.id
        }
    }

    // Load materials when class or category changes
    LaunchedEffect(selectedClassId, selectedCategory) {
        selectedClassId?.let { classId ->
            isLoading = true
            error = null
            try {
                val response = dashboardViewModel.loadMaterials(classId, selectedCategory)
                materials = response
            } catch (e: Exception) {
                error = e.message ?: "Không thể tải tài liệu"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Full-screen Image Viewer overlay
    if (viewingImageUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewingImageUrl = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { viewingImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = viewingImageUrl,
                    contentDescription = "Xem ảnh",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                // Close button
                IconButton(
                    onClick = { viewingImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, "Đóng", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
    
    // PDF Viewer using WebView (Google Docs Viewer)
    if (viewingPdfUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewingPdfUrl = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Xem tài liệu") },
                        navigationIcon = {
                            IconButton(onClick = { viewingPdfUrl = null }) {
                                Icon(Icons.Default.Close, "Đóng")
                            }
                        },
                        actions = {
                            // Open in browser fallback
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewingPdfUrl))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.OpenInNew, "Mở ngoài")
                            }
                        }
                    )
                }
            ) { padding ->
                val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=${java.net.URLEncoder.encode(viewingPdfUrl, "UTF-8")}"
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = {
                        android.webkit.WebView(it).apply {
                            settings.javaScriptEnabled = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            loadUrl(googleDocsUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 Tài liệu học tập", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Class selector tabs
            if (classes.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = classes.indexOfFirst { it.classInfo.id == selectedClassId }.coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 8.dp
                ) {
                    classes.forEach { classItem ->
                        Tab(
                            selected = classItem.classInfo.id == selectedClassId,
                            onClick = {
                                selectedClassId = classItem.classInfo.id
                                selectedCategory = null
                            },
                            text = {
                                Text(
                                    classItem.classInfo.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            // Category filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    null to "Tất cả",
                    "grammar" to "📖 Grammar",
                    "vocabulary" to "📝 Vocabulary",
                    "listening" to "🎧 Listening",
                    "reading" to "📚 Reading",
                    "writing" to "✍️ Writing",
                    "speaking" to "🗣️ Speaking",
                    "other" to "📁 Khác",
                )
                items(categories) { (value, label) ->
                    FilterChip(
                        selected = selectedCategory == value,
                        onClick = { selectedCategory = value },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // Content
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
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Error)
                            Spacer(Modifier.height(16.dp))
                            Text(error ?: "Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    selectedClassId?.let { classId ->
                                        scope.launch {
                                            isLoading = true
                                            error = null
                                            try {
                                                materials = dashboardViewModel.loadMaterials(classId, selectedCategory)
                                            } catch (e: Exception) {
                                                error = e.message
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                selectedClassId == null -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có lớp nào", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    }
                }
                materials.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderOpen, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("Chưa có tài liệu nào", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Giáo viên chưa upload tài liệu cho lớp này", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                "${materials.size} tài liệu",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(materials) { material ->
                            MaterialCard(
                                material = material,
                                onClick = {
                                    when (material.fileType) {
                                        "pdf", "document" -> {
                                            viewingPdfUrl = material.fileUrl
                                        }
                                        "image" -> {
                                            viewingImageUrl = material.fileUrl
                                        }
                                        else -> {
                                            // Fallback: open in external browser
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.fileUrl))
                                            context.startActivity(intent)
                                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialCard(material: Material, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = TMixShapes.Card,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File type icon
            Surface(
                color = getFileTypeColor(material.fileType).copy(alpha = 0.15f),
                shape = TMixShapes.Chip,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = getFileTypeIcon(material.fileType),
                        contentDescription = material.fileType,
                        tint = getFileTypeColor(material.fileType),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(Modifier.weight(1f)) {
                Text(
                    material.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!material.description.isNullOrBlank()) {
                    Text(
                        material.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        getCategoryLabel(material.category),
                        style = MaterialTheme.typography.labelSmall,
                        color = TMixRed
                    )
                    Text(
                        formatFileSize(material.fileSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Open icon
            Icon(
                Icons.Default.OpenInNew,
                "Mở",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getFileTypeIcon(fileType: String) = when (fileType) {
    "pdf" -> Icons.Default.PictureAsPdf
    "image" -> Icons.Default.Image
    "audio" -> Icons.Default.AudioFile
    "video" -> Icons.Default.VideoFile
    "document" -> Icons.Default.Description
    else -> Icons.Default.InsertDriveFile
}

private fun getFileTypeColor(fileType: String) = when (fileType) {
    "pdf" -> Color(0xFFE53935)
    "image" -> Color(0xFF1E88E5)
    "audio" -> Color(0xFF8E24AA)
    "video" -> Color(0xFFFF8F00)
    "document" -> Color(0xFF43A047)
    else -> Color(0xFF757575)
}

private fun getCategoryLabel(category: String) = when (category) {
    "grammar" -> "📖 Grammar"
    "vocabulary" -> "📝 Vocabulary"
    "listening" -> "🎧 Listening"
    "reading" -> "📚 Reading"
    "writing" -> "✍️ Writing"
    "speaking" -> "🗣️ Speaking"
    else -> "📁 Khác"
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${bytes / 1024} KB"
    return "${bytes / (1024 * 1024)} MB"
}
