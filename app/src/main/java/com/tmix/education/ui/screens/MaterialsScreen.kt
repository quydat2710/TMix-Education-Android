package com.tmix.education.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.data.model.Material
import com.tmix.education.data.model.StudentClassInfo
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.StudentDashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Materials Screen — Premium Redesign
 * Student views learning materials by class, with external app viewer
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
    val isDark = isSystemInDarkTheme()

    var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedClassId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var downloadingId by remember { mutableStateOf<String?>(null) }

    // In-app image viewer
    var viewingImageUrl by remember { mutableStateOf<String?>(null) }

    val classes = dashboardState.classes

    // Auto-select first class
    LaunchedEffect(classes) {
        if (classes.isNotEmpty() && selectedClassId == null) {
            selectedClassId = classes.first().classInfo.id
        }
    }

    // Load materials
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

    // Download & open file with external app
    fun openWithExternalApp(material: Material) {
        scope.launch {
            downloadingId = material.id
            try {
                val file = withContext(Dispatchers.IO) {
                    val cacheDir = File(context.cacheDir, "materials")
                    cacheDir.mkdirs()

                    // Determine file extension from original filename or fileType
                    val extension = material.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.takeIf { it.isNotBlank() }
                        ?.let { ".$it" }
                        ?: when (material.fileType) {
                            "pdf" -> ".pdf"
                            "document" -> ".docx"
                            "image" -> ".jpg"
                            "audio" -> ".mp3"
                            "video" -> ".mp4"
                            else -> ""
                        }

                    val fileName = material.title.replace(Regex("[^a-zA-Z0-9._-]"), "_") + extension
                    val targetFile = File(cacheDir, fileName)

                    // Always re-download (delete stale cache from previous failures)
                    if (targetFile.exists()) targetFile.delete()

                    // Build full URL
                    // fileUrl from DB = "/materials/files/classId/filename"
                    // Backend route = GET /api/v1/materials/files/:classId/:filename
                    val fullUrl = if (material.fileUrl.startsWith("http")) {
                        material.fileUrl
                    } else {
                        val apiBase = com.tmix.education.data.api.ApiConfig.BASE_URL.removeSuffix("/")
                        val cleanPath = material.fileUrl.removePrefix("/")
                        "$apiBase/$cleanPath"
                    }

                    android.util.Log.d("MaterialsScreen", "Downloading: $fullUrl")

                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    val request = okhttp3.Request.Builder().url(fullUrl).build()
                    val response = client.newCall(request).execute()

                    android.util.Log.d("MaterialsScreen", "Response: ${response.code} ${response.message}")

                    if (!response.isSuccessful) {
                        throw Exception("HTTP ${response.code}: ${response.message}\nURL: $fullUrl")
                    }

                    val body = response.body ?: throw Exception("Response body rỗng")
                    body.byteStream().use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    android.util.Log.d("MaterialsScreen", "Saved: ${targetFile.absolutePath} (${targetFile.length()} bytes)")

                    if (targetFile.length() == 0L) {
                        targetFile.delete()
                        throw Exception("File tải về rỗng (0 bytes)")
                    }

                    targetFile
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val mimeType = when (material.fileType) {
                    "pdf" -> "application/pdf"
                    "document" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    "image" -> "image/*"
                    "audio" -> "audio/*"
                    "video" -> "video/*"
                    else -> "*/*"
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Always use chooser so user can pick an app
                context.startActivity(Intent.createChooser(intent, "Mở bằng..."))
            } catch (e: Exception) {
                android.util.Log.e("MaterialsScreen", "Open file error", e)
                Toast.makeText(
                    context,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                downloadingId = null
            }
        }
    }

    // Image viewer overlay
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.MenuBook,
                            null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Tài liệu học tập", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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
                    containerColor = Color.Transparent,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    classes.forEach { classItem ->
                        val isSelected = classItem.classInfo.id == selectedClassId
                        Tab(
                            selected = isSelected,
                            onClick = {
                                selectedClassId = classItem.classInfo.id
                                selectedCategory = null
                            },
                            text = {
                                Text(
                                    classItem.classInfo.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Category filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                    val isSelected = selectedCategory == value
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = value },
                        label = {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TMixNavy,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Content
            when {
                isLoading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(4) {
                            ShimmerMaterialCard(isDark)
                        }
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2A1A1A) else ErrorLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, Modifier.size(36.dp), tint = Error)
                            }
                            Spacer(Modifier.height(20.dp))
                            Text("Không thể tải tài liệu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(error ?: "Có lỗi xảy ra", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    selectedClassId?.let { classId ->
                                        scope.launch {
                                            isLoading = true; error = null
                                            try { materials = dashboardViewModel.loadMaterials(classId, selectedCategory) }
                                            catch (e: Exception) { error = e.message }
                                            finally { isLoading = false }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TMixNavy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Thử lại", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                selectedClassId == null -> {
                    EmptyState(
                        icon = Icons.Outlined.Class,
                        title = "Chưa có lớp nào",
                        subtitle = "Bạn cần được thêm vào lớp học trước",
                        isDark = isDark
                    )
                }
                materials.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.FolderOpen,
                        title = "Chưa có tài liệu",
                        subtitle = "Giáo viên chưa upload tài liệu cho lớp này",
                        isDark = isDark
                    )
                }
                else -> {
                    // Results count
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Description, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${materials.size} tài liệu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            materials,
                            key = { _, m -> m.id },
                            contentType = { _, _ -> "material_card" }
                        ) { index, material ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(350)) + slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = tween(350, easing = EaseOutCubic)
                                )
                            ) {
                                PremiumMaterialCard(
                                    material = material,
                                    isDownloading = downloadingId == material.id,
                                    isDark = isDark,
                                    onOpen = {
                                        if (material.fileType == "image") {
                                            viewingImageUrl = if (material.fileUrl.startsWith("http")) {
                                                material.fileUrl
                                            } else {
                                                val apiBase = com.tmix.education.data.api.ApiConfig.BASE_URL
                                                    .removeSuffix("/")
                                                "$apiBase/${material.fileUrl.removePrefix("/")}"
                                            }
                                        } else {
                                            openWithExternalApp(material)
                                        }
                                    },
                                    onDownload = { openWithExternalApp(material) }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

// ================================================================
// Premium Material Card
// ================================================================
@Composable
private fun PremiumMaterialCard(
    material: Material,
    isDownloading: Boolean,
    isDark: Boolean,
    onOpen: () -> Unit,
    onDownload: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1A2030) else Color.White
    val fileColor = getFileTypeColor(material.fileType)

    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3347)) else null
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // File type icon — large, colored
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(fileColor.copy(if (isDark) 0.15f else 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = fileColor,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            getFileTypeIcon(material.fileType),
                            material.fileType,
                            Modifier.size(26.dp),
                            tint = fileColor
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Content
                Column(Modifier.weight(1f)) {
                    Text(
                        material.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isDark) Color.White else Color(0xFF1A1A2E)
                    )

                    if (!material.description.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            material.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color.White.copy(0.7f) else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Meta info row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category badge
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(fileColor.copy(if (isDark) 0.12f else 0.08f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                getCategoryLabel(material.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = fileColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }

                        // File size
                        if (material.fileSize > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Storage, null,
                                    Modifier.size(12.dp),
                                    tint = if (isDark) Color.White.copy(0.5f) else Color(0xFF94A3B8)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    formatFileSize(material.fileSize),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Color.White.copy(0.6f) else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // File type label
                        Text(
                            getFileTypeLabel(material.fileType),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Action button
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0xFF232A3A) else Color(0xFFF1F5F9)
                        )
                        .clickable { onOpen() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (material.fileType == "image") Icons.Outlined.Visibility
                        else Icons.Outlined.OpenInNew,
                        "Mở",
                        Modifier.size(18.dp),
                        tint = if (isDark) Color.White.copy(0.8f) else TMixNavy
                    )
                }
            }
        }
    }
}

// ================================================================
// Empty State
// ================================================================
@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String, isDark: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1A2433) else NavyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(40.dp), tint = TMixNavy.copy(0.5f))
            }
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ================================================================
// Shimmer Loading Card
// ================================================================
@Composable
private fun ShimmerMaterialCard(isDark: Boolean) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    val shimmer = if (isDark) Color(0xFF2A3347) else Color(0xFFE2E8F0)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A2030) else Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(shimmer.copy(alpha)))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmer.copy(alpha)))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmer.copy(alpha * 0.6f)))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(60.dp).height(18.dp).clip(RoundedCornerShape(6.dp)).background(shimmer.copy(alpha * 0.4f)))
                    Box(Modifier.width(40.dp).height(18.dp).clip(RoundedCornerShape(6.dp)).background(shimmer.copy(alpha * 0.4f)))
                }
            }
            Box(Modifier.size(40.dp).clip(CircleShape).background(shimmer.copy(alpha * 0.4f)))
        }
    }
}

// ================================================================
// Helpers
// ================================================================
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

private fun getFileTypeLabel(fileType: String) = when (fileType) {
    "pdf" -> "PDF"
    "image" -> "Ảnh"
    "audio" -> "Âm thanh"
    "video" -> "Video"
    "document" -> "Tài liệu"
    else -> "Tệp"
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
    return "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
}
