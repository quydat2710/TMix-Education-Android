package com.tmix.education.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tmix.education.ui.theme.*
import com.tmix.education.data.model.User

/**
 * Profile Screen (shared between Student and Parent)
 * Now displays real avatar from user.avatar URL using Coil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isStudent: Boolean = true,
    user: User? = null,
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onHelpCenter: () -> Unit = {}
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    val userName = user?.name ?: if (isStudent) "Học sinh" else "Phụ huynh"
    val userRole = if (isStudent) "Học sinh" else "Phụ huynh"
    val userEmail = user?.email ?: ""
    val avatarUrl = user?.avatar
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header
            item {
                Card(
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = TMixNavy)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar — real image or letter fallback
                        Box(
                            Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(TMixRed),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Ảnh đại diện",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    userName.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column {
                            Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                            Spacer(Modifier.height(4.dp))
                            Surface(color = TMixRed, shape = TMixShapes.Chip) {
                                Text(userRole, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }
            
            // Account section
            item {
                Text("Tài khoản", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            }
            
            item {
                Card(shape = TMixShapes.Card) {
                    Column {
                        ProfileMenuItem(Icons.Default.Person, "Thông tin cá nhân", onClick = onEditProfile)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Lock, "Đổi mật khẩu", onClick = onChangePassword)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Fingerprint, "Đăng nhập sinh trắc học")
                    }
                }
            }
            
            // Settings section
            item {
                Text("Cài đặt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            }
            
            item {
                Card(shape = TMixShapes.Card) {
                    Column {
                        ProfileMenuItem(Icons.Default.Notifications, "Thông báo")
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Language, "Ngôn ngữ", trailing = "Tiếng Việt")
                        HorizontalDivider()
                        // Dark Mode Toggle - hoạt động thật
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DarkMode, null, Modifier.size(24.dp), tint = TMixNavy)
                            Spacer(Modifier.width(16.dp))
                            Text("Chế độ tối", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
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
            }
            
            // Support section
            item {
                Text("Hỗ trợ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            }
            
            item {
                Card(shape = TMixShapes.Card) {
                    Column {
                        ProfileMenuItem(Icons.Default.Help, "Trung tâm trợ giúp", onClick = onHelpCenter)
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Info, "Về ứng dụng", trailing = "v1.0.0")
                        HorizontalDivider()
                        ProfileMenuItem(Icons.Default.Star, "Đánh giá ứng dụng")
                    }
                }
            }
            
            // Logout
            item {
                OutlinedButton(
                    onClick = onLogout,
                    Modifier.fillMaxWidth(),
                    shape = TMixShapes.Button,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất")
                }
            }
            
            item {
                Text(
                    "© 2026 TMIX Education",
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = TMixNavy)
        Spacer(Modifier.width(16.dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSecondary)
    }
}
