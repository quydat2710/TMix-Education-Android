package com.tmix.education.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tmix.education.data.model.Payment
import com.tmix.education.data.model.PaymentStatus
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPaymentsScreen(
    viewModel: ParentDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val payments = state.payments
    var showQRSheet by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf<Payment?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF111827) else Color(0xFFF1F5F9)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)

    // Calculate totals
    val totalPaid = payments.sumOf { it.paidAmount }.toLong()
    val totalRemaining = payments.filter { !it.isPaid }.sumOf { it.remainingAmount }.toLong()
    val totalDiscount = payments.sumOf { it.discountAmount }.toLong()

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TMixNavy, Color(0xFF2A4D7A), TMixNavySoft)))
                    .drawBehind {
                        drawCircle(Color.White, radius = size.height * 0.7f, center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.2f), alpha = 0.05f)
                        drawCircle(Color.White, radius = size.height * 0.5f, center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.9f), alpha = 0.03f)
                    }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(vertical = 24.dp, horizontal = 20.dp)
            ) {
                Text("Thanh toán học phí", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            payments.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có hóa đơn nào", style = MaterialTheme.typography.titleMedium, color = textColor)
                        Text("Hóa đơn sẽ hiển thị khi có phát sinh học phí", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PremiumSummaryCard(Modifier.weight(1f), "Đã thanh toán", totalPaid, Success, Icons.Default.CheckCircle, isDark)
                            PremiumSummaryCard(Modifier.weight(1f), "Còn nợ", totalRemaining, Error, Icons.Default.Warning, isDark)
                            PremiumSummaryCard(Modifier.weight(1f), "Giảm giá", totalDiscount, Warning, Icons.Default.Loyalty, isDark)
                        }
                    }

                    item {
                        Text(
                            "Danh sách hóa đơn",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    itemsIndexed(payments) { _, payment ->
                        PremiumPaymentInvoiceCard(payment, isDark) {
                            selectedPayment = payment
                            showQRSheet = true
                        }
                    }
                }
            }
        }

        if (showQRSheet && selectedPayment != null) {
            ModalBottomSheet(
                onDismissRequest = { showQRSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = if (isDark) Color(0xFF1E293B) else Color.White
            ) {
                PaymentQRSheetContentPremium(selectedPayment!!, viewModel, isDark) { showQRSheet = false }
            }
        }
    }
}

@Composable
fun PremiumSummaryCard(modifier: Modifier, label: String, amount: Long, color: Color, icon: ImageVector, isDark: Boolean) {
    val bgColor = if (isDark) Color(0xFF1F2937) else Color.White
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = color)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
                color = if (isDark) Color.White.copy(0.7f) else TextSecondary, 
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatPaymentCurrency(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PremiumPaymentInvoiceCard(payment: Payment, isDark: Boolean, onPayClick: () -> Unit) {
    val studentName = payment.student?.name ?: "Học sinh"
    val className = payment.classInfo?.name ?: "Lớp học"
    val monthYear = "Tháng ${payment.month}/${payment.year}"
    val remainingAmount = payment.remainingAmount.toLong()
    
    val bgColor = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val statusColor = if (!payment.isPaid) TMixRed else Success

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 4.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Accent strip
            Box(Modifier.fillMaxHeight().width(6.dp).background(statusColor))
            
            Column(Modifier.padding(16.dp).weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                        Spacer(Modifier.height(2.dp))
                        Text("$className - $monthYear", style = MaterialTheme.typography.bodySmall, color = if (isDark) Color.White.copy(0.6f) else TextSecondary)
                    }
                    PremiumPaymentStatusChip(payment.status)
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Số tiền cần đóng", style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(0.6f) else TextSecondary)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            formatPaymentCurrency(remainingAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    if (!payment.isPaid) {
                        Button(
                            onClick = onPayClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TMixRed)
                        ) {
                            Icon(Icons.Filled.QrCode2, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Thanh toán")
                        }
                    }
                }

                if (!payment.histories.isNullOrEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Đã thanh toán ${payment.histories.size} lần — ${formatPaymentCurrency(payment.paidAmount.toLong())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumPaymentStatusChip(status: String) {
    val (bg, text, label) = when (status) {
        PaymentStatus.PAID -> Triple(SuccessLight, Success, "Đã thanh toán")
        PaymentStatus.PARTIAL -> Triple(WarningLight, Warning, "Một phần")
        PaymentStatus.OVERDUE -> Triple(ErrorLight, Error, "Quá hạn")
        else -> Triple(InfoLight, Info, "Chờ thanh toán")
    }
    Surface(color = bg, shape = CircleShape) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(text))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentQRSheetContentPremium(payment: Payment, viewModel: ParentDashboardViewModel, isDark: Boolean, onClose: () -> Unit) {
    val amount = payment.remainingAmount.toLong()
    var qrUrl by remember { mutableStateOf<String?>(null) }
    var qrDescription by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var paymentConfirmed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    
    val fallbackDes = "TMIX ${payment.id}"

    // Load QR code from backend
    LaunchedEffect(payment.id) {
        isLoading = true
        try {
            val result = viewModel.getPaymentQRCode(payment.id, amount.toDouble())
            result.onSuccess { qrResponse ->
                qrUrl = qrResponse.qrDataUrl
                qrDescription = qrResponse.description
                isLoading = false
            }.onFailure { 
                qrUrl = "https://qr.sepay.vn/img?bank=MB&acc=0346857241&amount=$amount&des=$fallbackDes&template=compact"
                qrDescription = fallbackDes
                isLoading = false
            }
        } catch (e: Exception) {
            qrUrl = "https://qr.sepay.vn/img?bank=MB&acc=0346857241&amount=$amount&des=$fallbackDes&template=compact"
            qrDescription = fallbackDes
            isLoading = false
        }
    }

    // Auto-poll payment status every 5 seconds
    LaunchedEffect(payment.id) {
        while (!paymentConfirmed) {
            delay(5000)
            try {
                val result = viewModel.getChildPayments(payment.student?.id ?: "")
                result.onSuccess { payments ->
                    val matched = payments.find { it.id == payment.id }
                    if (matched != null && matched.isPaid) {
                        paymentConfirmed = true
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // Auto-close after confirmed
    LaunchedEffect(paymentConfirmed) {
        if (paymentConfirmed) {
            delay(2500)
            viewModel.refresh()
            onClose()
        }
    }

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
        Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.LightGray))
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Quét mã QR để thanh toán", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Đóng", tint = if (isDark) Color.White else Color.Black) }
        }

        Spacer(Modifier.height(16.dp))

        if (paymentConfirmed) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SuccessLight)) {
                Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp), tint = Success)
                    Spacer(Modifier.height(12.dp))
                    Text("Thanh toán thành công!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                    Spacer(Modifier.height(4.dp))
                    Text("Hóa đơn đã được xác nhận tự động", style = MaterialTheme.typography.bodySmall, color = Success.copy(0.8f))
                }
            }
        } else if (isLoading) {
            Box(Modifier.height(250.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TMixRed)
            }
        } else if (qrUrl != null) {
            Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                AsyncImage(
                    model = qrUrl,
                    contentDescription = "VietQR",
                    modifier = Modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            Button(
                onClick = {
                    try {
                        val request = DownloadManager.Request(Uri.parse(qrUrl))
                        request.setTitle("TMIX_QR_${payment.id}.png")
                        request.setDescription("Mã QR Thanh toán Học phí")
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "TMIX/QR_${payment.id}.png")
                        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        manager.enqueue(request)
                        Toast.makeText(context, "Đang lưu ảnh QR vào thư viện...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Không thể tải QR: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TMixNavy)
            ) {
                Icon(Icons.Default.Download, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tải mã QR về máy", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Manual Transfer Info Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1F2937) else Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.05f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Hoặc chuyển khoản thủ công:", fontWeight = FontWeight.SemiBold, color = textColor)
                    Spacer(Modifier.height(12.dp))
                    
                    CopyableRowAction("Ngân hàng", "Quân Đội (MB)", clipboard, context, isDark)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = if(isDark) Color.White.copy(0.1f) else Color.LightGray)
                    CopyableRowAction("Số tài khoản", "0346857241", clipboard, context, isDark)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = if(isDark) Color.White.copy(0.1f) else Color.LightGray)
                    CopyableRowAction("Số tiền", formatPaymentCurrency(amount), clipboard, context, isDark, amount.toString())
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = if(isDark) Color.White.copy(0.1f) else Color.LightGray)
                    
                    // NỘI DUNG is critical, make it standout
                    Row(
                        Modifier.fillMaxWidth().clickable { 
                            clipboard.setText(AnnotatedString(qrDescription ?: fallbackDes))
                            Toast.makeText(context, "Đã sao chép Nội dung", Toast.LENGTH_SHORT).show()
                        },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Nội dung", style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(0.6f) else TextSecondary)
                            Text(
                                qrDescription ?: fallbackDes,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TMixRed
                            )
                            Text("Bắt buộc sao chép chính xác nội dung này", style = MaterialTheme.typography.labelSmall, color = TMixRed.copy(0.8f))
                        }
                        Icon(Icons.Default.ContentCopy, "Sao chép", tint = TMixRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun CopyableRowAction(label: String, value: String, clipboard: androidx.compose.ui.platform.ClipboardManager, context: Context, isDark: Boolean, copyValue: String = value) {
    Row(
        Modifier.fillMaxWidth().clickable { 
            clipboard.setText(AnnotatedString(copyValue.replace(".", "").replace("đ", "").trim())) // Make sure numbers are unformatted if needed, but copyValue handles that usually. If value is pure string, it's fine.
            Toast.makeText(context, "Đã sao chép $label", Toast.LENGTH_SHORT).show()
        },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(0.6f) else TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1E293B))
        }
        Icon(Icons.Default.ContentCopy, "Sao chép", tint = TMixNavy, modifier = Modifier.size(20.dp))
    }
}

private fun formatPaymentCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.maximumFractionDigits = 0
    return "${formatter.format(amount)}đ"
}
