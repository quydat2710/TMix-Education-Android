package com.tmix.education.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.tmix.education.R
import com.tmix.education.ui.theme.*
import com.tmix.education.data.model.User
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tmix.education.ui.viewmodel.ProfileViewModel

/**
 * Profile Screen (shared between Student and Parent)
 * TopCV Redesign: Features a fixed top banner, floating white profile card, and sticky header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isStudent: Boolean = true,
    user: User? = null,
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onHelpCenter: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onChangeAvatar: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    val languageManager = LocalLanguageManager.current
    val currentLangCode by languageManager.currentLanguage.collectAsState()
    
    // Derived state for the displayed language
    val currentLanguageStr = if (currentLangCode == "vi") stringResource(R.string.language_vietnamese) else stringResource(R.string.language_english)

    val roleStudent = stringResource(R.string.role_student)
    val roleParent = stringResource(R.string.role_parent)
    val userName = user?.name ?: if (isStudent) roleStudent else roleParent
    val userRole = if (isStudent) roleStudent else roleParent
    val userEmail = user?.email ?: ""
    val avatarUrl = user?.avatar

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileViewModel.uploadAvatar(context.contentResolver, it)
        }
    }

    val listState = rememberLazyListState()

    // Lắng nghe vị trí cuộn để bật/tắt Sticky Header
    val showTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 220
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // --- 1. Main Scrollable Content ---
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().zIndex(1f),
            contentPadding = PaddingValues(bottom = 100.dp) // Card nằm chìm ngập tự do trên cùng
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Background Banner (Scrolls naturally with the list with parallax)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .graphicsLayer {
                                // Parallax hiệu ứng "bị hút": nền dồn co lại lên mép trên
                                val scroll = if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset.toFloat() else 180f
                                val fraction = (scroll / 180f).coerceIn(0f, 1f)
                                alpha = 1f - fraction
                                scaleY = 1f - fraction
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f) 
                                translationY = scroll * 0.5f // đẩy xuống lại một chút để cảm giác bị dồn ép
                            }
                            .background(Brush.linearGradient(listOf(TMixNavy, TMixNavySoft)))
                            .drawBehind {
                                // 1. Large soft decorative circles (Tech/Glassmorphism feel)
                                drawCircle(
                                    color = Color.White,
                                    radius = size.height * 0.8f,
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, -size.height * 0.2f),
                                    alpha = 0.06f
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = size.height * 0.6f,
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 1.2f),
                                    alpha = 0.03f
                                )

                                
                                // 2.style Dotted Chevron Pattern (Ma trận hạt tạo hình mũi tên >)
                                val dotRadius = 1.5.dp.toPx()
                                val spacing = 12.dp.toPx()
                                val cols = (size.width / spacing).toInt()
                                val rows = (size.height / spacing).toInt()
                                
                                val tipX = size.width * 0.9f
                                val tipY = size.height * 0.5f
                                val thickness = 60.dp.toPx() 
                                
                                for (i in 0..cols) {
                                    for (j in 0..rows) {
                                        val x = i * spacing
                                        val y = j * spacing
                                        
                                        // Phương trình mũi tên chĩa sang phải: >
                                        val expectedX = tipX - kotlin.math.abs(y - tipY) * 1.2f // * 1.2f để bẹt ra một chút
                                        val distanceToChevron = kotlin.math.abs(x - expectedX)
                                        
                                        if (distanceToChevron < thickness) {
                                            val alphaBase = 1f - (distanceToChevron / thickness)
                                            val alphaFadeLeft = (x / size.width).coerceIn(0f, 1f)
                                            val finalAlpha = (alphaBase * alphaFadeLeft * 0.25f).coerceIn(0f, 1f)
                                            
                                            if (finalAlpha > 0.02f) {
                                                drawCircle(
                                                    color = Color.White,
                                                    radius = dotRadius,
                                                    center = androidx.compose.ui.geometry.Offset(x, y),
                                                    alpha = finalAlpha
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                    )
                    
                    // Profile Card positioned overlapping the banner
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(100.dp)) // Đẩy Card xuống ngang nửa dưới của Banner
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .graphicsLayer {
                                    val scroll = if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset.toFloat() else 250f
                                    val fraction = (scroll / 250f).coerceIn(0f, 1f)
                                    // Thể hiện sự "teo/thu nhỏ" và hút lên
                                    scaleX = 1f - (fraction * 0.15f) 
                                    scaleY = 1f - (fraction * 0.15f)
                                    alpha = 1f - fraction           
                                    translationY = -(scroll * 0.3f) 
                                }
                        ) {
                            ExpandedProfileCard(
                                userName = userName,
                                userEmail = userEmail,
                                userRole = userRole,
                                avatarUrl = selectedImageUri ?: avatarUrl,
                                onChangeAvatar = { imagePickerLauncher.launch("image/*") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Account Section
            item { SectionTitle(stringResource(R.string.profile_section_account)) }
            item {
                Card(modifier = Modifier.padding(horizontal = 20.dp), shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column {
                        ProfileMenuItem(Icons.Default.Person, stringResource(R.string.profile_item_edit_info), onClick = onEditProfile)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Lock, stringResource(R.string.profile_item_change_pwd), onClick = onChangePassword)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Fingerprint, stringResource(R.string.profile_item_biometric))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Settings Section
            item { SectionTitle(stringResource(R.string.profile_section_settings)) }
            item {
                Card(modifier = Modifier.padding(horizontal = 20.dp), shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column {
                        ProfileMenuItem(Icons.Default.Notifications, stringResource(R.string.profile_item_notifications), onClick = onNotificationsClick)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Language, stringResource(R.string.profile_item_language), trailing = currentLanguageStr, onClick = { showLanguageSheet = true })
                        HorizontalDivider()
                        // Dark mode toggle
                        Row(
                            Modifier.fillMaxWidth().clickable { themeManager.setDarkMode(!isDarkMode) }.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DarkMode, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Text(stringResource(R.string.profile_item_dark_mode), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { themeManager.setDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = TMixNavy,
                                    uncheckedThumbColor = TMixNavy,
                                    uncheckedTrackColor = SurfaceVariant
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            
            // Support Section
            item { SectionTitle(stringResource(R.string.profile_section_support)) }
            item {
                Card(modifier = Modifier.padding(horizontal = 20.dp), shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column {
                        ProfileMenuItem(Icons.Default.Help, stringResource(R.string.profile_item_help_center), onClick = onHelpCenter)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Info, stringResource(R.string.profile_item_about), trailing = "v1.0.0")
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Star, stringResource(R.string.profile_item_rate))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Logout
            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = TMixShapes.Button,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_logout))
                }
                Spacer(Modifier.height(24.dp))
            }
            
            item {
                Text(
                    "© 2026 TMIX Education",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 2. Header thu gọn (Sticky) đè lên trên cùng 
        // Hiệu ứng Fade mượt mà (chỉ hiện khi cuộn đủ cao), bỏ slideIn gây lấp/cắt ngang thẻ
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(200)),
            modifier = Modifier.zIndex(2f)
        ) {
            CollapsedTopBar(
                userName = userName,
                userEmail = userEmail,
                avatarUrl = selectedImageUri ?: avatarUrl
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Error.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.action_logout),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.dialog_logout_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.action_logout),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.Medium)
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
        
        // Language Selection Bottom Sheet
        if (showLanguageSheet) {
            val languages = listOf("vi" to stringResource(R.string.language_vietnamese), "en" to stringResource(R.string.language_english))
            
            val toastMessage = stringResource(R.string.toast_language_changed)
            val contextForToast = androidx.compose.ui.platform.LocalContext.current

            ModalBottomSheet(
                onDismissRequest = { showLanguageSheet = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(stringResource(R.string.language_select_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(16.dp))
                    languages.forEach { (code, langName) ->
                        val isSelected = currentLangCode == code
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    languageManager.setLanguage(code)
                                    showLanguageSheet = false 
                                    android.widget.Toast.makeText(contextForToast, toastMessage, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(langName, style = MaterialTheme.typography.bodyLarge, color = if (isSelected) TMixNavy else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Default.Check, "Selected", tint = TMixNavy)
                            }
                        }
                        if (code != languages.last().first) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    } // Box
} // ProfileScreen

// ---------------------------------------------------------
// REUSABLE COMPONENTS
// ---------------------------------------------------------

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title, 
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleMedium, 
        fontWeight = FontWeight.Bold, 
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ExpandedProfileCard(
    userName: String,
    userEmail: String,
    userRole: String,
    avatarUrl: Any?,
    onChangeAvatar: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = TMixShapes.CardLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar With TopCV style Border & Camera Overlay
            Box(
                Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circle Avatar
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onChangeAvatar() },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null && avatarUrl.toString().isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Ảnh đại diện",
                            modifier = Modifier.fillMaxSize().padding(3.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            userName.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TMixNavy
                        )
                    }
                }
                
                // Camera Icon overlay on bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface) // white gap
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface) // Dark camera circle
                        .clickable { onChangeAvatar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Đổi ảnh",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    text = userName, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (userEmail.isNotBlank()) {
                    Text(
                        text = userEmail, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                // TopCV style badge: Gray pill, dark text, up icon
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowCircleUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = userRole, 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollapsedTopBar(
    userName: String,
    userEmail: String,
    avatarUrl: Any?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp), // Standard TopAppBar height
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp).fillMaxHeight(),
            verticalAlignment = Alignment.Bottom 
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null && avatarUrl.toString().isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Ảnh đại diện thu nhỏ",
                        modifier = Modifier.fillMaxSize().padding(1.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        userName.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TMixNavy
                    )
                }
            }

            Spacer(Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (userEmail.isNotBlank()) {
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, trailing: String? = null, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
