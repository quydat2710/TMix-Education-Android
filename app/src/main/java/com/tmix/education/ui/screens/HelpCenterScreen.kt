package com.tmix.education.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tmix.education.ui.theme.*

data class HelpTopic(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String
)

data class FAQ(
    val question: String,
    val answer: String
)

/**
 * Help Center Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onBack: () -> Unit = {}
) {
    val topics = listOf(
        HelpTopic("1", Icons.Default.Person, "Tài khoản", "Quản lý thông tin cá nhân, mật khẩu"),
        HelpTopic("2", Icons.Default.Payment, "Thanh toán", "Học phí, hoá đơn, phương thức thanh toán"),
        HelpTopic("3", Icons.Default.MenuBook, "Lớp học", "Đăng ký, lịch học, điểm danh"),
        HelpTopic("4", Icons.Default.Quiz, "Kiểm tra", "Bài thi, điểm số, kết quả"),
        HelpTopic("5", Icons.Default.Notifications, "Thông báo", "Cài đặt, thông báo đẩy")
    )
    
    val faqs = listOf(
        FAQ(
            "Làm sao để xem điểm của con tôi?",
            "Vào mục 'Con của tôi' > Chọn con > Xem điểm số. Bạn cũng có thể xem điểm từng môn và lịch sử điểm."
        ),
        FAQ(
            "Tôi quên mật khẩu, làm sao để lấy lại?",
            "Tại màn hình đăng nhập, nhấn 'Quên mật khẩu'. Hệ thống sẽ gửi link đặt lại mật khẩu qua email của bạn."
        ),
        FAQ(
            "Làm sao để thanh toán học phí?",
            "Vào mục 'Học phí' > Chọn hoá đơn cần thanh toán > Nhấn 'Thanh toán' > Quét mã QR bằng app ngân hàng."
        ),
        FAQ(
            "Tôi muốn đổi lịch học, phải làm sao?",
            "Vui lòng liên hệ trực tiếp trung tâm qua hotline hoặc đến văn phòng để được hỗ trợ đổi lịch."
        )
    )
    
    var expandedFaq by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trung tâm trợ giúp") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contact card
            item {
                Card(
                    shape = TMixShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = TMixNavy)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Cần hỗ trợ?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                        Text("Liên hệ với chúng tôi", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.White.copy(0.8f))
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {},
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Hotline")
                            }
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = TMixRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Chat, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Chat")
                            }
                        }
                    }
                }
            }
            
            // Topics
            item {
                Text("Chủ đề hỗ trợ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            
            item {
                Card(shape = TMixShapes.Card) {
                    Column {
                        topics.forEachIndexed { index, topic ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(topic.icon, null, Modifier.size(24.dp), tint = TMixNavy)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(topic.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(topic.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSecondary)
                            }
                            if (index < topics.size - 1) {
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
            
            // FAQs
            item {
                Text("Câu hỏi thường gặp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            
            items(faqs) { faq ->
                Card(
                    onClick = { 
                        expandedFaq = if (expandedFaq == faq.question) null else faq.question 
                    },
                    shape = TMixShapes.Card
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                faq.question,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (expandedFaq == faq.question) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = TextSecondary
                            )
                        }
                        
                        if (expandedFaq == faq.question) {
                            Spacer(Modifier.height(12.dp))
                            Text(faq.answer, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
