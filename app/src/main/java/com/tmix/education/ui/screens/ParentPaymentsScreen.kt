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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tmix.education.data.model.Payment
import com.tmix.education.data.model.PaymentStatus
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Parent Payments Screen with VietQR
 * Connected to real backend data
 */
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

    // Calculate totals from real data
    val totalPaid = payments.sumOf { it.paidAmount }.toLong()
    val totalRemaining = payments.filter { !it.isPaid }.sumOf { it.remainingAmount }.toLong()
    val totalDiscount = payments.sumOf { it.discountAmount }.toLong()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán học phí") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TMixNavy,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            payments.isEmpty() -> {
                // Show estimated fee breakdown from class data
                val children = state.children
                val hasClasses = children.any { (it.classes?.size ?: 0) > 0 }
                
                if (!hasClasses) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Receipt, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("Chưa có hóa đơn nào", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Hóa đơn sẽ hiển thị khi có phát sinh học phí", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total estimated monthly fee
                        item {
                            Card(shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = TMixNavy)) {
                                Column(Modifier.padding(20.dp)) {
                                    Text("Học phí ước tính / tháng", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(0.8f))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        formatPaymentCurrency(state.totalPendingAmount.toLong()),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text("Chưa phát sinh hóa đơn", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
                                }
                            }
                        }
                        
                        // Fee per child per class
                        children.forEach { child ->
                            val activeClasses = child.classes?.filter { 
                                it.classInfo?.status == "active" 
                            } ?: emptyList()
                            
                            if (activeClasses.isNotEmpty()) {
                                item {
                                    Text(
                                        child.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                activeClasses.forEach { enrollment ->
                                    val classInfo = enrollment.classInfo!!
                                    val feePerLesson = classInfo.feePerLesson ?: 0.0
                                    val lessonsPerWeek = classInfo.schedule?.daysOfWeek?.size ?: 0
                                    val monthlyFee = feePerLesson * lessonsPerWeek * 4
                                    val discount = enrollment.discountPercent
                                    val finalFee = monthlyFee * (1 - discount / 100.0)
                                    
                                    item {
                                        Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(2.dp)) {
                                            Column(Modifier.padding(16.dp)) {
                                                Row(
                                                    Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(Modifier.weight(1f)) {
                                                        Text(classInfo.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                                        Text(
                                                            classInfo.teacher?.name ?: "Chưa phân công",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextSecondary
                                                        )
                                                    }
                                                    Surface(color = SuccessLight, shape = TMixShapes.Chip) {
                                                        Text("Đang học", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Success)
                                                    }
                                                }
                                                Spacer(Modifier.height(12.dp))
                                                HorizontalDivider()
                                                Spacer(Modifier.height(12.dp))
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Học phí/buổi", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                    Text(formatPaymentCurrency(feePerLesson.toLong()), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Số buổi/tuần", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                    Text("$lessonsPerWeek buổi", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                }
                                                if (discount > 0) {
                                                    Spacer(Modifier.height(4.dp))
                                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text("Giảm giá", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                        Text("-${discount.toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = Success)
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                HorizontalDivider()
                                                Spacer(Modifier.height(8.dp))
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Ước tính/tháng", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        formatPaymentCurrency(finalFee.toLong()),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TMixRed
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard(Modifier.weight(1f), "Đã thanh toán", totalPaid, SuccessLight, Success)
                            SummaryCard(Modifier.weight(1f), "Còn nợ", totalRemaining, ErrorLight, Error)
                            SummaryCard(Modifier.weight(1f), "Giảm giá", totalDiscount, WarningLight, Warning)
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Danh sách hóa đơn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    items(payments) { payment ->
                        PaymentInvoiceCard(payment) {
                            selectedPayment = payment
                            showQRSheet = true
                        }
                    }
                }
            }
        }

        // QR Bottom Sheet
        if (showQRSheet && selectedPayment != null) {
            ModalBottomSheet(
                onDismissRequest = { showQRSheet = false },
                sheetState = sheetState,
                shape = TMixShapes.BottomSheet
            ) {
                PaymentQRSheetContent(selectedPayment!!, viewModel) { showQRSheet = false }
            }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, label: String, amount: Long, bgColor: Color, textColor: Color) {
    Card(modifier = modifier, shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text(formatPaymentCurrency(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun PaymentInvoiceCard(payment: Payment, onPayClick: () -> Unit) {
    val studentName = payment.student?.name ?: "Học sinh"
    val className = payment.classInfo?.name ?: "Lớp học"
    val monthYear = "Tháng ${payment.month}/${payment.year}"
    val remainingAmount = payment.remainingAmount.toLong()

    Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("$className - $monthYear", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                PaymentStatusChip(payment.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Còn lại", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        formatPaymentCurrency(remainingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!payment.isPaid) Error else Success
                    )
                }
                if (!payment.isPaid) {
                    Button(onClick = onPayClick, shape = TMixShapes.Button, colors = ButtonDefaults.buttonColors(containerColor = TMixRed)) {
                        Icon(Icons.Filled.QrCode2, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Thanh toán")
                    }
                }
            }

            // Show payment history if any
            if (!payment.histories.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Đã thanh toán ${payment.histories.size} lần — ${formatPaymentCurrency(payment.paidAmount.toLong())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success
                )
            }
        }
    }
}

@Composable
fun PaymentStatusChip(status: String) {
    val (bg, text, label) = when (status) {
        PaymentStatus.PAID -> Triple(SuccessLight, Success, "Đã thanh toán")
        PaymentStatus.PARTIAL -> Triple(WarningLight, Warning, "Một phần")
        PaymentStatus.OVERDUE -> Triple(ErrorLight, Error, "Quá hạn")
        else -> Triple(InfoLight, Info, "Chờ thanh toán")
    }
    Surface(color = bg, shape = TMixShapes.Chip) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = text)
    }
}

@Composable
fun PaymentQRSheetContent(payment: Payment, viewModel: ParentDashboardViewModel, onClose: () -> Unit) {
    val amount = payment.remainingAmount.toLong()
    var qrUrl by remember { mutableStateOf<String?>(null) }
    var qrDescription by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var paymentConfirmed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load QR code from backend
    LaunchedEffect(payment.id) {
        isLoading = true
        error = null
        try {
            val result = viewModel.getPaymentQRCode(payment.id, amount.toDouble())
            result.onSuccess { qrResponse ->
                qrUrl = qrResponse.qrDataUrl
                qrDescription = qrResponse.description
                isLoading = false
            }.onFailure { e ->
                // Fallback to direct SePay URL
                qrUrl = "https://qr.sepay.vn/img?bank=MB&acc=0346857241&amount=$amount&des=TMIX ${payment.id}&template=compact"
                isLoading = false
            }
        } catch (e: Exception) {
            qrUrl = "https://qr.sepay.vn/img?bank=MB&acc=0346857241&amount=$amount&des=TMIX ${payment.id}&template=compact"
            isLoading = false
        }
    }

    // Auto-poll payment status every 5 seconds
    LaunchedEffect(payment.id) {
        while (!paymentConfirmed) {
            kotlinx.coroutines.delay(5000)
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
            kotlinx.coroutines.delay(2500)
            viewModel.refresh()
            onClose()
        }
    }

    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Quét mã QR để thanh toán", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Đóng") }
        }

        Spacer(Modifier.height(16.dp))

        // Payment info
        Card(shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = SurfaceVariant)) {
            Column(Modifier.padding(12.dp)) {
                Text("Học sinh: ${payment.student?.name ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Lớp: ${payment.classInfo?.name ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Tháng ${payment.month}/${payment.year}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (paymentConfirmed) {
            // Success state
            Card(shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = SuccessLight)) {
                Column(
                    Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp), tint = Success)
                    Spacer(Modifier.height(12.dp))
                    Text("Thanh toán thành công!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                    Spacer(Modifier.height(4.dp))
                    Text("Hóa đơn đã được xác nhận tự động", style = MaterialTheme.typography.bodySmall, color = Success.copy(0.8f))
                }
            }
        } else if (isLoading) {
            Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TMixRed)
            }
        } else if (qrUrl != null) {
            Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(8.dp)) {
                AsyncImage(
                    model = qrUrl,
                    contentDescription = "VietQR",
                    modifier = Modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(formatPaymentCurrency(amount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TMixRed)

        Spacer(Modifier.height(8.dp))

        if (!paymentConfirmed) {
            Text("Mở app ngân hàng và quét mã QR để thanh toán", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            // Waiting indicator
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Warning)
                Text("Đang chờ thanh toán...", style = MaterialTheme.typography.labelSmall, color = Warning)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

private fun formatPaymentCurrency(amount: Long): String {
    val formatter = java.text.NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.maximumFractionDigits = 0
    return "${formatter.format(amount)}đ"
}

